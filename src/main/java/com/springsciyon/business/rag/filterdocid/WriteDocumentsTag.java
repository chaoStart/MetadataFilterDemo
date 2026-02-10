package com.springsciyon.business.rag.filterdocid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsciyon.business.rag.dto.SqlConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
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

    // 在类中定义 ObjectMapper（推荐 static final）
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 将 List<String> 转换为 JSON 数组字符串
     */
    private String toJsonArray(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert list to JSON string", e);
        }
    }

    private List<String> fromJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to list", e);
        }
    }

    /** 初始化数据 */
    private void initData() {
        Map<String, Tag> kb1Docs = new HashMap<>();

        // 为第一个文档添加 metadata_list 示例数据
        List<String> meta1 = Arrays.asList("sc231aw", "231aw产品手册", "PLC文档");
        kb1Docs.put("756063966790811650",
                new Tag("大数据分析部", "sc231aw产品手册.pdf", "2025-01-10 10:30:00", "756063966790811650", meta1));

        List<String> meta2 = Arrays.asList("sc231ad", "231ad产品手册", "PLC文档");
        kb1Docs.put("756063966790811651",
                new Tag("大数据分析部", "sc231ad产品手册.pdf", "2025-01-11 14:20:00", "756063966790811651", meta2));

        kbDocTagMap.put("571776832787972098", kb1Docs);

        Map<String, Tag> kb2Docs = new HashMap<>();

        List<String> meta3 = Arrays.asList("sc231ew", "231ew产品手册", "NT6000文档");
        kb2Docs.put("756063966790811652",
                new Tag("研发管理部门", "sc231ew产品手册.pdf", "2025-01-12 09:00:00", "756063966790811657", meta3));

        kbDocTagMap.put("571776832787972097", kb2Docs);
    }

    // 新增标签元数据metadata_list
    public void addTag(List<Tag> tags) {
        String sql = "INSERT INTO DocumentTag (doc_id, file_name, author, date_time, metadata_list) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "file_name = VALUES(file_name), " +
                "author = VALUES(author), " +
                "date_time = VALUES(date_time), " +
                "metadata_list = VALUES(metadata_list)";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Tag tag : tags) {
                ps.setString(1, tag.getDocId());
                ps.setString(2, tag.getFileName());
                ps.setString(3, tag.getAuthor());
                ps.setTimestamp(4, Timestamp.valueOf(tag.getDateTime()));
                // 转换 metadataList 为 JSON 字符串
                String jsonStr = toJsonArray(tag.getMetadataList());
                ps.setString(5, jsonStr); // MySQL JSON 类型接受合法 JSON 字符串
                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println(String.format("数据已成功写入 %d条数据到 DocumentTag 表", tags.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 删除标签元数据metadata_list
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
    // 修改标签元数据metadata_list
    public void updateTag(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            System.out.println("没有需要更新的标签");
            return;
        }

        // ✅ 使用 UPDATE 语句，并包含 metadata_list
        String sql = "UPDATE DocumentTag " +
                "SET file_name = ?, author = ?, date_time = ?, metadata_list = ? " +
                "WHERE doc_id = ?";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Tag tag : tags) {
                // SET 子句参数顺序：file_name, author, date_time, metadata_list
                ps.setString(1, tag.getFileName());
                ps.setString(2, tag.getAuthor());

                // 处理 date_time 为 null 的情况
                if (tag.getDateTime() != null && !tag.getDateTime().trim().isEmpty()) {
                    ps.setTimestamp(3, Timestamp.valueOf(tag.getDateTime()));
                } else {
                    ps.setNull(3, java.sql.Types.TIMESTAMP);
                }

                // 转换 metadataList 为 JSON 字符串
                String jsonStr = toJsonArray(tag.getMetadataList());
                ps.setString(4, jsonStr);

                // WHERE 条件：doc_id
                ps.setString(5, tag.getDocId());

                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            int totalUpdated = Arrays.stream(results).sum();
            System.out.println(String.format("成功更新 %d 条数据到 DocumentTag 表", totalUpdated));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 查询标签元数据（含 metadata_list）
    public Tag getTagByDocId(String docId) {
        String sql = "SELECT doc_id, file_name, author, date_time, metadata_list FROM DocumentTag WHERE doc_id = ?";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, docId);
            ResultSet rs = ps.executeQuery(); // ResultSet 来自 java.sql

            if (rs.next()) {
                String docIdVal = rs.getString("doc_id");
                String author = rs.getString("author");
                String fileName = rs.getString("file_name");
                Timestamp dateTimeTs = rs.getTimestamp("date_time");
                String metadataJson = rs.getString("metadata_list");

                // 处理日期时间：可能为 null
                String dateTimeStr = null;
                if (dateTimeTs != null) {
                    dateTimeStr = dateTimeTs.toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }

                // 解析 metadata_list JSON 字符串为 List<String>
                List<String> metadataList = fromJsonArray(metadataJson);

                // 使用包含 metadataList 的构造函数
                return new Tag(author, fileName, dateTimeStr, docIdVal, metadataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        // 修改 SQL：加入 metadata_list 字段
        String sql = "INSERT INTO DocumentTag (doc_id, file_name, author, date_time, metadata_list) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "file_name = VALUES(file_name), " +
                "author = VALUES(author), " +
                "date_time = VALUES(date_time), " +
                "metadata_list = VALUES(metadata_list)";

        try (Connection conn = SqlConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Tag tag : tags) {
                ps.setString(1, tag.getDocId());
                ps.setString(2, tag.getFileName());
                ps.setString(3, tag.getAuthor());

                // 处理 date_time：允许为 null
                if (tag.getDateTime() != null && !tag.getDateTime().trim().isEmpty()) {
                    ps.setTimestamp(4, Timestamp.valueOf(tag.getDateTime()));
                } else {
                    ps.setNull(4, java.sql.Types.TIMESTAMP);
                }

                // 处理 metadata_list：转换为 JSON 字符串
                String metadataJson = toJsonArray(tag.getMetadataList());
                ps.setString(5, metadataJson); // MySQL JSON 类型接受合法 JSON 字符串

                ps.addBatch();
            }

            ps.executeBatch();
            System.out.println(String.format("数据已成功写入 %d 条数据到 DocumentTag 表", tags.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        WriteDocumentsTag info = new WriteDocumentsTag();
//
//        List<String> querykbIds = Arrays.asList("571776832787972098","571776832787972097");
//        List<Tag> tags = info.getAllTagsByKbIds(querykbIds);
//        System.out.println("kb_ids = " + querykbIds + " 下所有 doc_id 的 Tag 信息：");
//        for (Tag tag : tags) {
//            System.out.println("file_name = " + tag.getFileName()
//                    + ", author = " + tag.getAuthor()
//                    + ", date_time = " + tag.getDateTime()
//                    + ", doc_id = " + tag.getDocId()
//                    + ", metadata_list = " + tag.getMetadataList()
//            );
//        }
//        info.saveTagsToDatabase(tags);
//
//        // 插入带 metadata_list 的数据
//        ArrayList<Tag> insertTags = new ArrayList<>();
//        List<String> meta1 = Arrays.asList("sc235aw", "235aw产品手册", "NT6000文档");
//        insertTags.add(new Tag("研发管理部门", "235aw.pdf", "2025-01-10 10:30:00", "756063966790811660", meta1));
//
//        info.addTag(insertTags);
//
//        // 更新 metadata_list
//        List<String> meta2 = Arrays.asList("sc235Aw", "231AW产品手册","NT6000文档");
//        ArrayList<Tag> updateTags = new ArrayList<>();
//        updateTags.add(new Tag("研发管理部门", "sc235AW产品手册.pdf", "2025-02-01 00:00:00", "756063966790811660", meta2));
//        info.updateTag(updateTags);
//    }
}
