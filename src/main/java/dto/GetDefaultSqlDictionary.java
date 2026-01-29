package dto;

import com.hankcs.hanlp.dictionary.DynamicCustomDictionary;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.seg.common.Term;
import filterdocid.DocumentSimpleInfo;

import java.sql.*;
import java.util.*;

public class GetDefaultSqlDictionary {

//    private static volatile Keymapper cachedKeymapper = null;
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
    public static List<Term> extractMetricTerms(String text) {
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
        Set<String> FileNamesSet = new HashSet<>(cachedKeymapper.getFileNames());

        List<Term> termList = segment.seg(text);
        List<Term> matchedTerms = new ArrayList<>();

        for (Term term : termList) {
            if (FileNamesSet.contains(term.word)) {
                matchedTerms.add(term);
            }
        }

        return matchedTerms;
    }

    /**
     * 从数据库加载自定义词典
     */
    private static Keymapper loadCustomWordsFromDatabase() {
        DynamicCustomDictionary customDict = new DynamicCustomDictionary();
        List<String> FileNames = new ArrayList<>();

        // 从数据库中读取Tag标签中的文件名称
        List<DocumentSimpleInfo> docList = readDocIdAndFileNameFromDB();
//        for (DocumentSimpleInfo item : docList) {
//            System.out.println("doc_id = " + item.getDocId()
//                    + ", file_name = " + item.getFileName());
//        }
        for (DocumentSimpleInfo item : docList) {
            String fileName = item.getFileName();
            if (fileName != null && !fileName.trim().isEmpty()) {
                fileName = fileName.trim();
                FileNames.add(fileName);
                customDict.add(fileName, "nz");
            }
        }

        ViterbiSegment viterbi = new ViterbiSegment();
        viterbi.enableCustomDictionary(customDict);
        viterbi.enableCustomDictionaryForcing(true);
        return new Keymapper(viterbi, FileNames);

    }

    // 将输入字符串转换为小写

    public static String convertToLowerCase(String text) {
        return text != null ? text.toLowerCase() : null;
    }
    // 保留 main 用于测试
    public static void main(String[] args) {
        String text = "231aw产品手册的电源要求是什么？";
        String lowerText = convertToLowerCase(text);
        List<Term> matchedTerms = extractMetricTerms(lowerText);

        System.out.println("\n=== 在数据库指标词典中成功命中的词 ===");
        if (matchedTerms.isEmpty()) {
            System.out.println("（无匹配项）");
        } else {
            matchedTerms.forEach(term ->
                    System.out.println(term.word + "  (offset=" + term.offset + ")")
            );
        }
        System.out.println("--------------------------------");
    }
}