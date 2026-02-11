package com.springsciyon.business.rag.controller;

import com.springsciyon.business.rag.dto.ApiResponse;
import com.springsciyon.business.rag.dto.GetDocIdInfoByCustomDictionary;
import com.springsciyon.business.rag.dto.TagRequest;
import com.springsciyon.business.rag.dto.TagResponse;
import com.springsciyon.business.rag.filterdocid.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.springsciyon.business.rag.service.TagService;

import java.util.List;

/**
 * 标签管理 Controller
 * 提供标签的增删改查 RESTful API
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*") // 允许跨域访问
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private GetDocIdInfoByCustomDictionary getDocIdInfoByCustomDictionary;


    /**
     * 添加或更新标签
     * POST /api/tags/add
     *
     * 请求体示例：
     * {
     *   "doc_id": "756063966790811660",
     *   "metadataList": ["sc235aw", "235aw产品手册", "NT6000文档"],
     *   "filename": "235aw.pdf",
     *   "author": "研发管理部门",
     *   "dateTime": "2025-01-10 10:30:00"
     * }
     *
     * @param request 标签请求对象
     * @return 统一响应格式
     */
    @PostMapping("/saveOrUpdateTags")
    public ApiResponse<String> saveOrUpdateTags(@RequestBody TagRequest request) {
        try {
            boolean success = tagService.saveOrUpdateTags(request);
            if (success) {
                return ApiResponse.success("标签添加成功");
            } else {
                return ApiResponse.error("标签添加失败");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }
    /**
     * 删除单个标签
     * DELETE /api/tags/delete/{docId}
     *
     * 示例：DELETE /api/tags/delete/756063966790811660
     *
     * @param docId 文档ID
     * @return 统一响应格式
     */
    @DeleteMapping("/delete/{docId}")
    public ApiResponse<String> deleteTag(@PathVariable String docId) {
        try {
            boolean success = tagService.deleteTag(docId);
            if (success) {
                return ApiResponse.success("标签删除成功");
            } else {
                return ApiResponse.error("标签删除失败");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 通过请求体删除单个标签（备用方法）
     * POST /api/tags/delete
     *
     * 请求体示例：
     * {
     *   "doc_id": "756063966790811660"
     * }
     *
     * @param request 标签请求对象
     * @return 统一响应格式
     */
    @PostMapping("/delete")
    public ApiResponse<String> deleteTagByBody(@RequestBody TagRequest request) {
        try {
            boolean success = tagService.deleteTag(request.getDocId());
            if (success) {
                return ApiResponse.success("标签删除成功");
            } else {
                return ApiResponse.error("标签删除失败");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 批量删除标签
     * POST /api/tags/batch-delete
     *
     * 请求体示例：
     * {
     *   "docIds": ["756063966790811660", "756063966790811661"]
     * }
     *
     * @param docIds 文档ID列表（包装在对象中）
     * @return 统一响应格式
     */
    @PostMapping("/batch-delete")
    public ApiResponse<String> deleteTags(@RequestBody List<String> docIds) {
        try {
            boolean success = tagService.deleteTags(docIds);
            if (success) {
                return ApiResponse.success("批量删除标签成功，共 " + docIds.size() + " 条");
            } else {
                return ApiResponse.error("批量删除标签失败");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 根据文档ID查询标签
     * GET /api/tags/get/{docId}
     *
     * 示例：GET /api/tags/get/756063966790811660
     *
     * @param docId 文档ID
     * @return 标签响应对象
     */
    @GetMapping("/get/{docId}")
    public ApiResponse<TagResponse> getTagByDocId(@PathVariable String docId) {
        try {
            Tag tag = tagService.getTagByDocId(docId);
            if (tag != null) {
                TagResponse response = TagResponse.fromTag(tag);
                return ApiResponse.success("查询成功", response);
            } else {
                return ApiResponse.error(404, "未找到该文档的标签信息");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 通过请求体查询标签（备用方法）
     * POST /api/tags/get
     *
     * 请求体示例：
     * {
     *   "doc_id": "756063966790811660"
     * }
     *
     * @param request 标签请求对象
     * @return 标签响应对象
     */
    @PostMapping("/get")
    public ApiResponse<TagResponse> getTagByBody(@RequestBody TagRequest request) {
        try {
            Tag tag = tagService.getTagByDocId(request.getDocId());
            if (tag != null) {
                TagResponse response = TagResponse.fromTag(tag);
                return ApiResponse.success("查询成功", response);
            } else {
                return ApiResponse.error(404, "未找到该文档的标签信息");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "服务器错误: " + e.getMessage());
        }
    }
    /**
     * 健康检查接口
     * GET /api/tags/health
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Tag API 服务正常运行");
    }

    @GetMapping("/test")
    public void test(String query) throws Exception {
        query = query.toLowerCase().trim();
        System.out.println(getDocIdInfoByCustomDictionary.extractMetricTerms(query));;
    }
}
