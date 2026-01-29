package filterdocid;

import dto.SqlConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

public class CreateDocumentsInfo {

    /** kb_id 列表 */
    private List<String> kbIds = Arrays.asList(
            "571776832787972098",
            "571776832787972097"
    );

    /** doc_id 列表 */
    private List<String> docIds = Arrays.asList(
            "756063966790811656",
            "756063966790811655"
    );

    /**
     * kb_id -> (doc_id -> Tag)
     */
    private Map<String, Map<String, Tag>> kbDocTagMap = new HashMap<>();

    /**
     * 存储某个 kb_id 下所有 doc_id 的 Tag 信息
     */
    private List<Tag> kb_ids_all_tag = new ArrayList<>();

    public CreateDocumentsInfo() {
        initData();
    }

    /** 初始化数据 */
    private void initData() {
        Map<String, Tag> kb1Docs = new HashMap<>();
        kb1Docs.put("756063966790811656",
                new Tag("sc231aw", "PLC中文文档", "2025-01-10 10:30:00","756063966790811656"));
        kb1Docs.put("756063966790811655",
                new Tag("sc231ad", "PLC中文文档", "2025-01-11 14:20:00","756063966790811655"));

        kbDocTagMap.put("571776832787972098", kb1Docs);

        Map<String, Tag> kb2Docs = new HashMap<>();
        kb2Docs.put("756063966790811657",
                new Tag("sc231ew", "PLC中文文档", "2025-01-12 09:00:00","756063966790811657"));

        kbDocTagMap.put("571776832787972097", kb2Docs);
    }

    /**
     * 根据多个 kb_id 获取这些 kb 下所有 doc_id 的 Tag，
     * 并存入 kb_ids_all_tag 集合
     */
    public List<Tag> getAllTagsByKbIds(List<String> kbIds) {
        kb_ids_all_tag.clear(); // 清空旧数据

        if (kbIds == null || kbIds.isEmpty()) {
            return kb_ids_all_tag;
        }

        for (String kbId : kbIds) {
            Map<String, Tag> docMap = kbDocTagMap.get(kbId);
            if (docMap == null) {
                continue;
            }

            for (Map.Entry<String, Tag> entry : docMap.entrySet()) {
                kb_ids_all_tag.add(entry.getValue());
            }
        }

        return kb_ids_all_tag;
    }


    /**
     * 写入数据库
     */
    public void saveTagsToDatabase(List<Tag> tags) {
        String sql = "INSERT INTO DocumentTag (doc_id, file_name, author, date_time) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "file_name = VALUES(file_name), " +
                "author = VALUES(author), " +
                "date_time = VALUES(date_time)";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Tag tag : tags) {
                ps.setString(1, tag.getDocId());
                ps.setString(2, tag.getFileName());
                ps.setString(3, tag.getAuthor());
                ps.setTimestamp(4, Timestamp.valueOf(tag.getDateTime()));
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println("数据已成功写入 DocumentTag 表");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        CreateDocumentsInfo info = new CreateDocumentsInfo();

        List<String> querykbIds = Arrays.asList("571776832787972098","571776832787972097");

        List<Tag> tags = info.getAllTagsByKbIds(querykbIds);

        System.out.println("kb_ids = " + querykbIds + " 下所有 doc_id 的 Tag 信息：");
        for (Tag tag : tags) {
            System.out.println("file_name = " + tag.getFileName()
                    + ", author = " + tag.getAuthor()
                    + ", date_time = " + tag.getDateTime()
                    + ", doc_id = " + tag.getDocId());
        }
        info.saveTagsToDatabase(tags);
    }
}
