package dto;

import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.seg.common.Term;
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
    // text是传入的用户问题
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
        ViterbiSegment segment = cachedKeymapper.getSegment();
        List<Term> termList = segment.seg(text);
        Map<String, DocumentSimpleInfo> fileNameMap = cachedKeymapper.getFileNameMap();
        List<DocumentSimpleInfo>  latestDocLists = new ArrayList<>();
        // 匹配文件名称filenames和文档doc_id信息
        for (Term term : termList) {
            String word = term.word.toLowerCase();
            // O(1) 查 Map
            DocumentSimpleInfo info = fileNameMap.get(word);
            if (info != null) {
                latestDocLists.add(info);
            }
        }
        return latestDocLists;
    }

    /**
     * 从数据库加载自定义词典
     */
/*    private static Keymapper loadCustomWordsFromDatabase() {
        DynamicCustomDictionary dynamicCustomDictionary = new DynamicCustomDictionary();
        List<String> FileNames = new ArrayList<>();

        // 从数据库中读取Tag标签中的文件名称
        List<DocumentSimpleInfo> docList = readDocIdAndFileNameFromDB();
        for (DocumentSimpleInfo item : docList) {
            String fileName = item.getFileName();
            if (fileName != null && !fileName.trim().isEmpty()) {
                String filenamelower = convertToLowerCase(fileName);
                filenamelower = filenamelower.trim();
                FileNames.add(filenamelower);
                dynamicCustomDictionary.add(filenamelower,"nz 1024");
            }
        }

        ViterbiSegment viterbi = new ViterbiSegment();
        viterbi.customDictionary = dynamicCustomDictionary;
        viterbi.enableCustomDictionary(true);
        viterbi.enableCustomDictionaryForcing(true);
        return new Keymapper(viterbi, FileNames, docList);
    }*/
    private static Keymapper loadCustomWordsFromDatabase() {

        try {
            String mysqlVersion = getMysqlVersion();
            String redisVersion = RedisUtil.get("documentTag:version");

            List<DocumentSimpleInfo> docList;
            List<String> fileNames;

            // Step1 + Step2：判断是否变化
            if (redisVersion != null && redisVersion.equals(mysqlVersion)) {
                System.out.println("【Redis】使用缓存数据");
                docList = RedisUtil.getList("documentTag:docList", DocumentSimpleInfo.class);
                fileNames = RedisUtil.getList("documentTag:fileNames", String.class);
            } else {
                System.out.println("【MySQL】检测到数据变化，重新加载");

                docList = readDocIdAndFileNameFromDB();
                fileNames = new ArrayList<>();

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

            // 构建 HanLP 词典
            DynamicCustomDictionary dynamicCustomDictionary = new DynamicCustomDictionary();
            for (String name : fileNames) {
                dynamicCustomDictionary.add(name, "nz 1024");
            }

            ViterbiSegment viterbi = new ViterbiSegment();
            viterbi.customDictionary = dynamicCustomDictionary;
            viterbi.enableCustomDictionary(true);
            viterbi.enableCustomDictionaryForcing(true);

            return new Keymapper(viterbi, fileNames, docList);

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
        String text = "sc235Bw和sc232aw产品手册的电源要求是什么？";
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