package com.springsciyon.business.rag.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsciyon.business.rag.dao.DocumentTagMapper;
import com.springsciyon.business.rag.dto.SqlConnect;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import com.springsciyon.business.rag.filterdocid.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WriteDocumentsTag {
    /** kb_id -> (doc_id -> Tag)*/
    private Map<String, Map<String, Tag>> kbDocTagMap = new HashMap<>();

    /**存储某个 kb_id 下所有 doc_id 的 Tag 信息*/
    private List<Tag> kb_ids_all_tag = new ArrayList<>();
    /**初始化模拟文档信息*/
    public WriteDocumentsTag() {
    }
   @Autowired
    private ObjectMapper objectMapper;
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

    @Autowired
    private DocumentTagMapper documentTagMapper;
    /**
     * 保存或更新标签元数据metadata_list
     */
    public void saveOrUpdateTags(List<DocumentTagEntity> tags) {
        for (DocumentTagEntity tag : tags) {

            // 1️⃣ 先根据 doc_id 查询是否已存在
            DocumentTagEntity exist = documentTagMapper.selectOne(
                    Wrappers.lambdaQuery(DocumentTagEntity.class)
                            .eq(DocumentTagEntity::getDocId, tag.getDocId())
            );

            if (exist == null) {
                // 2️⃣ 不存在 → insert
                documentTagMapper.insert(tag);
                System.out.println("成功保存 " + tag.getDocId() + " 的数据");
            } else {
                // 3️⃣ 已存在 → update by doc_id
                tag.setUpdateTime(LocalDateTime.now());
                documentTagMapper.update(
                        tag,
                        Wrappers.lambdaQuery(DocumentTagEntity.class)
                                .eq(DocumentTagEntity::getDocId, tag.getDocId())
                );
                System.out.println("成功更新 文档id信息为" + tag.getDocId() + " 的数据");
            }
        }
    }

    /**
     * 删除标签元数据metadata_list
     */
    public void deleteTag(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            System.out.println("没有需要删除的标签");
            return;
        }
        // 提取 doc_id 列表
        List<String> docIds = tags.stream()
                .map(Tag::getDocId)
                .collect(Collectors.toList());
        // 批量删除：DELETE FROM DocumentTag WHERE doc_id IN (...)
        documentTagMapper.delete(
                Wrappers.lambdaQuery(DocumentTagEntity.class)
                        .in(DocumentTagEntity::getDocId, docIds)
        );
        System.out.println("成功删除 " + docIds.size() + " 条数据");
    }

    /**
     * 查询标签元数据（含 metadata_list）
     */
    public Tag getTagByDocId(String docId) {
        if (docId == null || docId.trim().isEmpty()) {
            return null;
        }
        // 1️⃣ 用 MyBatis-Plus 查询 Entity
        DocumentTagEntity entity = documentTagMapper.selectOne(
                Wrappers.lambdaQuery(DocumentTagEntity.class)
                        .eq(DocumentTagEntity::getDocId, docId)
        );

        if (entity == null) {
            return null;
        }

        // 2️⃣ Entity -> Tag
        String dateTimeStr = null;
        if (entity.getDateTime() != null) {
            dateTimeStr = entity.getDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String updateTimeStr = dateTimeStr;
        return new Tag(
                entity.getDocId(),
                entity.getFileName(),
                entity.getAuthor(),
                dateTimeStr,
                updateTimeStr,
                entity.getMetadataList()
        );
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
}
