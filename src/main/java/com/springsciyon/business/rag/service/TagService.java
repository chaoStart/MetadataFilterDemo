package com.springsciyon.business.rag.service;

import com.springsciyon.business.rag.dto.TagRequest;
import com.springsciyon.business.rag.filterdocid.Tag;
import com.springsciyon.business.rag.filterdocid.WriteDocumentsTag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签服务层
 * 处理标签相关的业务逻辑
 */
@Service
public class TagService {

    private final WriteDocumentsTag writeDocumentsTag;

    public TagService() {
        this.writeDocumentsTag = new WriteDocumentsTag();
    }

    /**
     * 添加标签
     * @param request 标签请求对象
     * @return 是否成功
     */
    public boolean addTag(TagRequest request) {
        try {
            // 验证必填字段
            if (request.getDocId() == null || request.getDocId().trim().isEmpty()) {
                throw new IllegalArgumentException("doc_id 不能为空");
            }

            // 创建 Tag 对象
            Tag tag = new Tag(
                    request.getAuthor() != null ? request.getAuthor() : "",
                    request.getFilename() != null ? request.getFilename() : "",
                    request.getDateTime() != null ? request.getDateTime() : "",
                    request.getDocId(),
                    request.getMetadataList()
            );

            // 调用 WriteDocumentsTag 的 addTag 方法
            List<Tag> tags = new ArrayList<>();
            tags.add(tag);
            writeDocumentsTag.addTag(tags);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("添加标签失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加标签
     * @param requests 标签请求列表
     * @return 是否成功
     */
    public boolean addTags(List<TagRequest> requests) {
        try {
            if (requests == null || requests.isEmpty()) {
                throw new IllegalArgumentException("标签列表不能为空");
            }

            List<Tag> tags = new ArrayList<>();
            for (TagRequest request : requests) {
                if (request.getDocId() == null || request.getDocId().trim().isEmpty()) {
                    throw new IllegalArgumentException("doc_id 不能为空");
                }

                Tag tag = new Tag(
                        request.getAuthor() != null ? request.getAuthor() : "",
                        request.getFilename() != null ? request.getFilename() : "",
                        request.getDateTime() != null ? request.getDateTime() : "",
                        request.getDocId(),
                        request.getMetadataList()
                );
                tags.add(tag);
            }

            writeDocumentsTag.addTag(tags);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("批量添加标签失败: " + e.getMessage());
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
            Tag tag = new Tag("", "", "", docId, null);
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

    /**
     * 更新标签
     * @param request 标签请求对象
     * @return 是否成功
     */
    public boolean updateTag(TagRequest request) {
        try {
            if (request.getDocId() == null || request.getDocId().trim().isEmpty()) {
                throw new IllegalArgumentException("doc_id 不能为空");
            }

            Tag tag = new Tag(
                    request.getAuthor() != null ? request.getAuthor() : "",
                    request.getFilename() != null ? request.getFilename() : "",
                    request.getDateTime() != null ? request.getDateTime() : "",
                    request.getDocId(),
                    request.getMetadataList()
            );

            List<Tag> tags = new ArrayList<>();
            tags.add(tag);
            writeDocumentsTag.updateTag(tags);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新标签失败: " + e.getMessage());
        }
    }
}
