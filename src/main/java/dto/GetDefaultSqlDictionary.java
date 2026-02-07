package dto;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import filterdocid.DocumentSimpleInfo;
import java.sql.*;
import java.util.*;

import static dto.SqlConnect.getMysqlVersion;


public class GetDefaultSqlDictionary {

    private static Keymapper cachedKeymapper;

    // Redis Hash key 用于存储 filename -> DocumentSimpleInfo 映射
    private static final String FILENAME_HASH_KEY = "documentTag:metadataListHash";

    /**
     * 从 DocumentTag 表中读取 doc_id 、 file_name和 metadata_list
     */
    public static List<DocumentSimpleInfo> readDocIdAndFileNameFromDB() {
        List<DocumentSimpleInfo> resultList = new ArrayList<>();
        String sql = "SELECT doc_id, file_name, metadata_list FROM DocumentTag";

        // 推荐：复用 ObjectMapper（可设为静态字段）
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String docId = rs.getString("doc_id");
                String fileName = rs.getString("file_name");
                String metadataJson = rs.getString("metadata_list"); // ← JSON 字符串

                // 解析 JSON 字符串为 List<String>
                List<String> metadataList = null;
                if (metadataJson != null && !metadataJson.trim().isEmpty()) {
                    try {
                        metadataList = objectMapper.readValue(metadataJson, new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        System.err.println("解析 metadata_list 失败，doc_id=" + docId + ", json=" + metadataJson);
                        metadataList = Collections.emptyList(); // 或保留 null，根据业务需求
                    }
                    resultList.add(new DocumentSimpleInfo(docId, fileName, metadataList));
                } else {
//                    metadataList = Collections.emptyList(); // 或 null
                    continue;
                }

            }
        } catch (SQLException e) {
            System.err.println("数据库读取失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return resultList;
    }

    /**
     * 对外提供的核心方法：输入文本，返回其中命中的数据库指标词（Term 列表）
     * 改进版：使用 Redis HMGET 直接查询匹配到的 metadata_list，而不是取出整个 Map
     */
    // 根据用户问题question进行hanlp提取关键词并匹配到DocumentSimpleInfo
    public static List<DocumentSimpleInfo> extractMetricTerms(String question) throws Exception {
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

        // 使用 Redis HMGET 批量查询匹配到的 Metadata_list 对应的 DocumentSimpleInfo
        // 这里直接使用 Redis 查询语句，而不是取出整个 Map 再筛选
        List<DocumentSimpleInfo> latestDocLists = RedisUtil.hmget(
            FILENAME_HASH_KEY, matchedMetadataListNames,
            DocumentSimpleInfo.class
        );

        return latestDocLists;
    }

    /**
     * 从数据库加载自定义词典
     * 改进版：使用 Redis Hash 存储 metadataList -> DocumentSimpleInfo 映射
     */
    private static Keymapper loadCustomWordsFromDatabase() {
        try {
            String mysqlVersion = getMysqlVersion();
            String redisVersion = RedisUtil.get("documentTag:version");
            List<DocumentSimpleInfo> docInfoList;
            Set<String> customWords = new HashSet<>(); // ← 用于收集所有去重的自定义词（ metadata 标签）
            // Step1 + Step2：判断是否变化
            if (redisVersion != null && redisVersion.equals(mysqlVersion)) {
                System.out.println("【Redis】使用缓存数据");
                docInfoList = RedisUtil.getList("documentTag:docInfoList", DocumentSimpleInfo.class);
            } else {
                System.out.println("【MySQL】检测到数据变化，重新加载");

                docInfoList = readDocIdAndFileNameFromDB();
                if (docInfoList != null) {
                    for (DocumentSimpleInfo item : docInfoList) {
                        // 添加 metadataList 中的每个标签（小写 + 去重）
                        List<String> metadataList = item.getMetadataList();
                        if (metadataList != null) {
                            for (String tag : metadataList) {
                                if (tag != null && !tag.trim().isEmpty()) {
                                    customWords.add(tag.toLowerCase().trim());
                                }
                            }
                        }
                    }
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
    public static void main(String[] args) throws Exception {
        String text = "sc231ad和sc231aw产品手册的电源要求是什么？";
        String lowerText = convertToLowerCase(text);
        List<DocumentSimpleInfo> matchedDocIds = extractMetricTerms(lowerText);

        System.out.println("\n=== 在数据库指标词典中成功命中的文档id和文档名称 ===");
        if (matchedDocIds.isEmpty()) {
            System.out.println("（无匹配项）");
        } else {
            matchedDocIds.forEach(term ->
                    System.out.println( "  (docIds=" + term.getDocId() + ", fileName=" + term.getFileName() + ")")
            );
        }
        System.out.println("--------------------------------");
    }
}