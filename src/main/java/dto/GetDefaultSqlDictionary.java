package dto;

import com.hankcs.hanlp.dictionary.CustomDictionary;
import filterdocid.DocumentSimpleInfo;
import java.sql.*;
import java.util.*;

import static dto.SqlConnect.getMysqlVersion;


public class GetDefaultSqlDictionary {

    private static Keymapper cachedKeymapper;

    /**
     * 从 DocumentTag 表中读取 doc_id 和 file_name
     */
    public static List<DocumentSimpleInfo> readDocIdAndFileNameFromDB() {
        List<DocumentSimpleInfo> resultList = new ArrayList<>();
        String sql = "SELECT doc_id, file_name FROM DocumentTag";
        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String docId = rs.getString("doc_id");
                String fileName = rs.getString("file_name");

                resultList.add(new DocumentSimpleInfo(docId, fileName));
            }
        }  catch (SQLException e) {
            System.err.println("数据库读取失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return resultList;
    }

    /**
     * 对外提供的核心方法：输入文本，返回其中命中的数据库指标词（Term 列表）
     */
    // text是用户的问题
    public static List<DocumentSimpleInfo>  extractMetricTerms(String text) {
        if (text == null || text.trim().isEmpty()) {
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
        Set<String> dict = cachedKeymapper.getFileNames();
        Map<String, DocumentSimpleInfo> fileNameMap = cachedKeymapper.getFileNameMap();
        List<DocumentSimpleInfo>  latestDocLists = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 匹配文件名称filenames和文档doc_id信息
        CustomDictionary.parseText(text, (begin, end, value) -> {
            String word = text.substring(begin, end);   //匹配到Tag标签名称
            if (dict.contains(word) && seen.add(word)) {
                DocumentSimpleInfo docInfo = fileNameMap.get(word);
                if (docInfo != null) {
                    latestDocLists.add(docInfo);
                }
            }
        });

        return latestDocLists;
    }

    /**
     * 从数据库加载自定义词典
     */
    private static Keymapper loadCustomWordsFromDatabase() {
        try {
            String mysqlVersion = getMysqlVersion();
            String redisVersion = RedisUtil.get("documentTag:version");

            List<DocumentSimpleInfo> docList;
            Set<String> fileNames;

            // Step1 + Step2：判断是否变化
            if (redisVersion != null && redisVersion.equals(mysqlVersion)) {
                System.out.println("【Redis】使用缓存数据");
                docList = RedisUtil.getList("documentTag:docList", DocumentSimpleInfo.class);
                fileNames = RedisUtil.getSet("documentTag:fileNames", String.class);
            } else {
                System.out.println("【MySQL】检测到数据变化，重新加载");

                docList = readDocIdAndFileNameFromDB();
                fileNames = new HashSet<>();

                assert docList != null;
                for (DocumentSimpleInfo item : docList) {
                    String fn = convertToLowerCase(item.getFileName());
                    if (fn != null && !fn.trim().isEmpty()) {
                        fileNames.add(fn.trim());
                    }
                }

                // Step3：更新 Redis
                RedisUtil.set("documentTag:version", mysqlVersion);
                RedisUtil.setObject("documentTag:docList", docList);
                RedisUtil.setObject("documentTag:fileNames", fileNames);
            }

            // 加载自定义 HanLP 词典
            CustomDictionary  myCustomDictionary = new CustomDictionary();
            assert fileNames != null;
            for (String name : fileNames) {
                CustomDictionary.add(name, "nz 1024");
            }

            assert docList != null;
            return new Keymapper(myCustomDictionary, fileNames, docList);

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
    public static void main(String[] args) {
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