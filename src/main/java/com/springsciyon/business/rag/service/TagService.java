package com.springsciyon.business.rag.service;

import com.springsciyon.business.rag.dto.TagRequest;
import com.springsciyon.business.rag.entity.DocumentTagEntity;
import com.springsciyon.business.rag.filterdocid.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签服务层
 * 处理标签相关的业务逻辑
 */
@Service
public class TagService {
    @Autowired
    private  WriteDocumentsTag writeDocumentsTag;

    /**
     * 添加标签
     * @param request 标签请求对象
     * @return 是否成功
     */
    public boolean saveOrUpdateTags(TagRequest request) {
        try {
            // 验证必填字段
            if (request.getDocId() == null || request.getDocId().trim().isEmpty()) {
                throw new IllegalArgumentException("doc_id 不能为空");
            }

            // 1️⃣ 先构造 Tag（如果你业务里还需要 Tag）
            Tag tag = new Tag(
                    request.getDocId(),
                    request.getFilename() != null ? request.getFilename() : "",
                    request.getAuthor() != null ? request.getAuthor() : "",
                    request.getDateTime() != null ? request.getDateTime() : "",
                    request.getUpdateTime() != null ? request.getUpdateTime() : "",
                    request.getMetadataList()
            );

            // 2️⃣ 把 Tag 转成 DocumentTagEntity（关键）
            DocumentTagEntity entity = new DocumentTagEntity();
            entity.setDocId(tag.getDocId());
            entity.setAuthor(tag.getAuthor());
            entity.setFileName(tag.getFileName());

            // String -> LocalDateTime
            if (tag.getDateTime() != null && !tag.getDateTime().isEmpty()) {
                entity.setDateTime(
                        LocalDateTime.parse(tag.getDateTime(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            }

            entity.setMetadataList(tag.getMetadataList());

            // 3️⃣ 构造 List<DocumentTagEntity>
            List<DocumentTagEntity> entities = new ArrayList<>();
            entities.add(entity);

            // 4️⃣ 现在类型匹配了 ✅
            writeDocumentsTag.saveOrUpdateTags(entities);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 删除标��
     * @param docId 文档ID
     * @return 是否成功
     */
    public boolean deleteTag(String docId) {
        try {
            if (docId == null || docId.trim().isEmpty()) {
                throw new IllegalArgumentException("doc_id 不能为空");
            }

            // 创建一个包含 docId 的 Tag 对象用于删除
            Tag tag = new Tag(docId, "", "",null , null);
            List<Tag> tags = new ArrayList<>();
            tags.add(tag);

            writeDocumentsTag.deleteTag(tags);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除标签失败: " + e.getMessage());
        }
    }
    /**
     * 批量删除标签
     * @param docIds 文档ID列表
     * @return 是否成功
     */
    public boolean deleteTags(List<String> docIds) {
        try {
            if (docIds == null || docIds.isEmpty()) {
                throw new IllegalArgumentException("doc_id 列表不能为空");
            }

            List<Tag> tags = new ArrayList<>();
            for (String docId : docIds) {
                if (docId != null && !docId.trim().isEmpty()) {
                    Tag tag = new Tag("", "", "", docId, null);
                    tags.add(tag);
                }
            }

            if (tags.isEmpty()) {
                throw new IllegalArgumentException("没有有效的 doc_id");
            }

            writeDocumentsTag.deleteTag(tags);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("批量删除标签失败: " + e.getMessage());
        }
    }

    /**
     * 根据文档ID获取标签
     * @param docId 文档ID
     * @return Tag 对象
     */
    public Tag getTagByDocId(String docId) {
        try {
            if (docId == null || docId.trim().isEmpty()) {
                throw new IllegalArgumentException("doc_id 不能为空");
            }

            return writeDocumentsTag.getTagByDocId(docId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询标签失败: " + e.getMessage());
        }
    }
}
