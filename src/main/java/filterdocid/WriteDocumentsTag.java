package filterdocid;

import dto.SqlConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

public class WriteDocumentsTag {

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

    public WriteDocumentsTag() {
        initData();
    }

    /** 初始化数据 */
    private void initData() {
        Map<String, Tag> kb1Docs = new HashMap<>();
        kb1Docs.put("756063966790811650",
                new Tag("235aw", "PLC中文文档", "2025-01-10 10:30:00","756063966790811650"));
        kb1Docs.put("756063966790811651",
                new Tag("235BD", "PLC中文文档", "2025-01-11 14:20:00","756063966790811651"));

        kbDocTagMap.put("571776832787972098", kb1Docs);

        Map<String, Tag> kb2Docs = new HashMap<>();
        kb2Docs.put("756063966790811652",
                new Tag("235BW", "PLC中文文档", "2025-01-12 09:00:00","756063966790811657"));

        kbDocTagMap.put("571776832787972097", kb2Docs);
    }

    // 插入标签元数据Tag
    public void addTag(List<Tag> tags) {
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
            System.out.println(String.format("数据已成功写入 %d条数据到 DocumentTag 表", tags.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 删除标签元数据Tag
    public void deleteTag(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            System.out.println("没有需要删除的标签");
            return;
        }

        String sql = "DELETE FROM DocumentTag WHERE doc_id = ?";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Tag tag : tags) {
                ps.setString(1, tag.getDocId());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            System.out.println(String.format("成功删除 %d 条数据", results.length));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改标签元数据（仅更新已存在的 doc_id 记录）
     */
    public void updateTag(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            System.out.println("没有需要更新的标签");
            return;
        }

        String sql = "UPDATE DocumentTag SET file_name = ?, author = ?, date_time = ? WHERE doc_id = ?";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Tag tag : tags) {
                ps.setString(1, tag.getFileName());
                ps.setString(2, tag.getAuthor());
                ps.setTimestamp(3, Timestamp.valueOf(tag.getDateTime()));
                ps.setString(4, tag.getDocId());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            int totalUpdated = Arrays.stream(results).sum(); // executeBatch 返回每条影响的行数
            System.out.println(String.format("成功更新 %d 条数据到 DocumentTag 表", totalUpdated));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            System.out.println(String.format("数据已成功写入 %d条数据到 DocumentTag 表", tags.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        WriteDocumentsTag info = new WriteDocumentsTag();

//        List<String> querykbIds = Arrays.asList("571776832787972098","571776832787972097");
//        List<Tag> tags = info.getAllTagsByKbIds(querykbIds);
//        System.out.println("kb_ids = " + querykbIds + " 下所有 doc_id 的 Tag 信息：");
//        for (Tag tag : tags) {
//            System.out.println("file_name = " + tag.getFileName()
//                    + ", author = " + tag.getAuthor()
//                    + ", date_time = " + tag.getDateTime()
//                    + ", doc_id = " + tag.getDocId());
//        }
//        info.saveTagsToDatabase(tags);

//        //创建插入标签元数据
        ArrayList<Tag> insertTag = new ArrayList<Tag>();
//        insertTag.add(new Tag("232AD", "PLC中文文档", "2025-01-10 10:30:00", "756063966790811653"));
        insertTag.add(new Tag("232Aw", "PLC中文文档", "2025-01-10 10:30:00", "756063966790811650"));
        info.addTag(insertTag);
//        // 删除标签元数据
//        ArrayList<Tag> deleteTags = new ArrayList<>();
//        deleteTags.add(new Tag(null, null, null, "756063966790811653")); // 只需 doc_id，其他字段可为 null
//        info.deleteTag(deleteTags);
        // 修改标签元数据
        ArrayList<Tag> updateTags = new ArrayList<>();
        updateTags.add(new Tag("232AD", "PLC中文文档", "2025-01-10 10:30:00", "756063966790811653"));
        info.updateTag(updateTags);
    }
}
