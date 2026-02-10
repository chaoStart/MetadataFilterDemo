package com.springsciyon.business.rag.dto;

import java.util.List;

/**
 * 标签请求 DTO
 * 用于接收前端传递的标签数据
 */
public class TagRequest {
    private String docId;           // 文档ID
    private List<String> metadataList;  // 元数据列表
    private String id;              // ID（可选）
    private String filename;        // 文件名（可选）
    private String author;          // 作者（可选）
    private String dateTime;        // 日期时间（可选）

    public TagRequest() {
    }

    public TagRequest(String docId, List<String> metadataList, String id, String filename, String author, String dateTime) {
        this.docId = docId;
        this.metadataList = metadataList;
        this.id = id;
        this.filename = filename;
        this.author = author;
        this.dateTime = dateTime;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public List<String> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<String> metadataList) {
        this.metadataList = metadataList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "TagRequest{" +
                "docId='" + docId + '\'' +
                ", metadataList=" + metadataList +
                ", id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", author='" + author + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
