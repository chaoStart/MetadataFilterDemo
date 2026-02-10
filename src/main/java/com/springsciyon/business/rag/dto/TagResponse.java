package com.springsciyon.business.rag.dto;

import com.springsciyon.business.rag.filterdocid.Tag;
import java.util.List;

/**
 * 标签响应 DTO
 * 用于返回标签数据给前端
 */
public class TagResponse {
    private String docId;
    private String fileName;
    private String author;
    private String dateTime;
    private List<String> metadataList;

    public TagResponse() {
    }

    public TagResponse(String docId, String fileName, String author, String dateTime, List<String> metadataList) {
        this.docId = docId;
        this.fileName = fileName;
        this.author = author;
        this.dateTime = dateTime;
        this.metadataList = metadataList;
    }

    /**
     * 从 Tag 对象转换为 TagResponse
     */
    public static TagResponse fromTag(Tag tag) {
        if (tag == null) {
            return null;
        }
        return new TagResponse(
                tag.getDocId(),
                tag.getFileName(),
                tag.getAuthor(),
                tag.getDateTime(),
                tag.getMetadataList()
        );
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public List<String> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<String> metadataList) {
        this.metadataList = metadataList;
    }

    @Override
    public String toString() {
        return "TagResponse{" +
                "docId='" + docId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", author='" + author + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", metadataList=" + metadataList +
                '}';
    }
}
