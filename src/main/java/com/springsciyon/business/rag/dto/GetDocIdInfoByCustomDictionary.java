package com.springsciyon.business.rag.dto;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsciyon.business.rag.dao.DocumentTagMapper;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import com.springsciyon.business.rag.filterdocid.DocumentSimpleInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import com.springsciyon.business.rag.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class GetDocIdInfoByCustomDictionary {

    private  Keymapper cachedKeymapper;

    @Autowired
    private  TagService tagService;

    // Redis Hash key 用于存储 MetaData_List_Hash -> DocumentSimpleInfo 映射
    private static final String MetaData_List_Hash = "documentTag:metadataListHash";

    /**
     * 从 DocumentTag 表中读取 doc_id 、 file_name和 metadata_list
     */
    @Autowired
    private  DocumentTagMapper documentTagMapper;

    public   List<DocumentSimpleInfo> readDocIdAndFileNameFromDB() {
        List<DocumentSimpleInfo> resultList = new ArrayList<>();

        // 直接用 MyBatis-Plus 查
        List<DocumentTagEntity> entityList = documentTagMapper.selectSimpleInfo();

        for (DocumentTagEntity entity : entityList) {
            // metadata_list 已经自动反序列化成 List<String>
            List<String> metadataList = entity.getMetadataList();
            if (metadataList == null || metadataList.isEmpty()) {
                continue; // 和你原逻辑保持一致
            }
            resultList.add(
                    new DocumentSimpleInfo(
                            entity.getDocId(),
                            entity.getFileName(),
                            metadataList
                    )
            );
        }
        return resultList;
    }

    /**
     * 对外提供的核心方法：输入文本，返回其中命中的数据库指标词对应的所有文档
     * 改进版：支持一对多映射，使用 Redis HMGET 查询并按 docId 去重
     */
    // 根据用户问题question进行hanlp提取关键词并匹配到DocumentSimpleInfo
    public  List<DocumentSimpleInfo> extractMetricTerms(String question) throws Exception {
        if (question == null || question.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 懒加载 + 简单单例（非严格线程安全，生产环境可加锁或使用更健壮的初始化方式）
        if (cachedKeymapper == null) {
            cachedKeymapper = loadCustomWordsFromDatabase();
            if (cachedKeymapper == null) {
                System.err.println("警告：自定义词典加载失败，将使用空词典进行匹配");
                return Collections.emptyList();
            }
        }

        // 使用 HanLP 分词，收集匹配到的 metadata_list中所有元数据信息
        List<String> matchedMetadataListNames = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        CustomDictionary.parseText(question, (begin, end, value) -> {
            String word = question.substring(begin, end);   // 匹配到的 Tag 标签名称
            if (seen.add(word)) {  // 去重
                matchedMetadataListNames.add(word);
            }
        });

        if (matchedMetadataListNames.isEmpty()) {
            return Collections.emptyList();
        }

        // ========== 使用新的 hmgetList 方法获取一对多映射 ==========
        Map<String, List<DocumentSimpleInfo>> termToDocMap = RedisUtil.hmgetList(
            MetaData_List_Hash,
            matchedMetadataListNames,
            DocumentSimpleInfo.class
        );

        // ========== 合并所有文档列表，并按 docId 去重 ==========
        Map<String, DocumentSimpleInfo> docIdToInfoMap = new LinkedHashMap<>(); // 保持插入顺序

        for (String term : matchedMetadataListNames) {
            List<DocumentSimpleInfo> docList = termToDocMap.get(term);
            if (docList != null) {
                for (DocumentSimpleInfo doc : docList) {
                    // 按 docId 去重，保留第一个出现的
                    docIdToInfoMap.putIfAbsent(doc.getDocId(), doc);
                }
            }
        }
        return new ArrayList<>(docIdToInfoMap.values());
    }

    /**
     * 从数据库加载自定义词典
     * 改进版：构建 术语 -> List<DocumentSimpleInfo> 的映射并存储到 Redis Hash
     */
    private  Keymapper loadCustomWordsFromDatabase() {
        try {
            TagService tagService = new TagService(); // 创建 TagService 实例,用于main测试
            String mysqlVersion = tagService.getMysqlVersion();
            String redisVersion = RedisUtil.get("documentTag:version");
            List<DocumentSimpleInfo> docInfoList;
            Set<String> customWords = new HashSet<>(); // ← 用于收集所有去重的自定义词（ metadata 标签）
            // Step1 + Step2：判断是否变化
            if (redisVersion != null && redisVersion.equals(mysqlVersion)) {
                System.out.println("【Redis】使用缓存数据");
                docInfoList = RedisUtil.getList("documentTag:docInfoList", DocumentSimpleInfo.class);
                // 从缓存的 docInfoList 中恢复 customWords（用于 HanLP 词典）
                if (docInfoList != null) {
                    for (DocumentSimpleInfo item : docInfoList) {
                        List<String> metadataList = item.getMetadataList();
                        if (metadataList != null && !metadataList.isEmpty()) {
                            for (String tag : metadataList) {
                                if (tag != null && !tag.trim().isEmpty()) {
                                    customWords.add(tag.toLowerCase().trim());
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("【MySQL】检测到数据变化，重新加载");
                docInfoList = readDocIdAndFileNameFromDB();
                if (docInfoList != null && !docInfoList.isEmpty()) {
                    // ========== 构建 术语 -> List<DocumentSimpleInfo> 映射 ==========
                    Map<String, List<DocumentSimpleInfo>> termToDocListMap = new HashMap<>();
                    for (DocumentSimpleInfo docInfo : docInfoList) {
                        // 添加 metadataList 中的每个标签（小写 + 去重）
                        List<String> metadataList = docInfo.getMetadataList();
                        if (metadataList != null) {
                            for (String tag : metadataList) {
                                if (tag != null && !tag.trim().isEmpty()) {
                                    String normalizedTag = tag.toLowerCase().trim();
                                    customWords.add(normalizedTag);
                                    // 构建一对多映射：术语 -> List<DocumentSimpleInfo>
                                    termToDocListMap
                                        .computeIfAbsent(normalizedTag, k -> new ArrayList<>())
                                        .add(docInfo);
                                }
                            }
                        }
                    }
                    // ========== 存储映射到 Redis Hash ==========
                    RedisUtil.delHash(MetaData_List_Hash);
                    RedisUtil.hmsetList(MetaData_List_Hash, termToDocListMap);
                    System.out.printf("已构建 %d 个术语的一对多映射并存储到 Redis Hash%n", termToDocListMap.size());
                }
                // Step3：更新 Redis
                RedisUtil.set("documentTag:version", mysqlVersion);
                RedisUtil.setObject("documentTag:docInfoList", docInfoList);
            }

            // === 批量加入 HanLP 自定义词典 ===
            CustomDictionary mycustomDictionary = new CustomDictionary();
            for (String word : customWords) {
                CustomDictionary.add(word, "nz 1024"); // nz: 专有名词，1024: 频次（可调）
            }

            System.out.printf("已加载 %d 个去重后的自定义词到 HanLP%n", customWords.size());

            return new Keymapper(mycustomDictionary, docInfoList);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 将输入字符串转换为小写
    public static String convertToLowerCase(String text) {
        return text != null ? text.toLowerCase() : null;
    }
    // 保留 main 用于测试
    public static  void main(String[] args) throws Exception {
        String text = "PLC文档和sc235aw产品手册的电源要求是什么？";
        String lowerText = convertToLowerCase(text);
        GetDocIdInfoByCustomDictionary loadCustomDictionary = new GetDocIdInfoByCustomDictionary();
        List<DocumentSimpleInfo> matchedDocIds =loadCustomDictionary.extractMetricTerms(lowerText);

        System.out.println("\n=== 在数据库指标词典中成功命中的文档id和文档名称 ===");
        if (matchedDocIds.isEmpty()) {
            System.out.println("（无匹配项）");
        } else {
            matchedDocIds.forEach(term ->
                    System.out.println( "  (docIds=" + term.getDocId() + ", fileName=" + term.getFileName() +
                            ", metadata=" + term.getMetadataList() + ")")
            );
        }
        System.out.println("--------------------------------");
    }
}