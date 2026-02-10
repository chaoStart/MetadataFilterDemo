package com.springsciyon.business.rag.filterdocid;

import java.util.List;

public class Tag {
    private String docId;
    private String fileName;
    private String author;
    private String dateTime;
    private List<String> metadataList; // 对应 metadata_list 字段

    public Tag(String fileName, String author, String dateTime, String docId) {
        this.docId = docId;
        this.fileName = fileName;
        this.author = author;
        this.dateTime = dateTime;
    }
    // 新增构造函数（元数据列表）
    public Tag(String author, String fileName, String dateTime, String docId, List<String> metadataList) {
        this( fileName,author, dateTime, docId);
        this.metadataList = metadataList;
    }

    public String getDocId() {
        return docId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAuthor() {
        return author;
    }

    public String getDateTime() {
        return dateTime;
    }
    public List<String> getMetadataList() { return metadataList; }

    public void setMetadataList(List<String> metadataList) { this.metadataList = metadataList; }
    @Override
    public String toString() {
        return "Tag{" +
                "fileName='" + fileName + '\'' +
                ", author='" + author + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", docId='" + docId + '\'' +
                ", metadataList=" + metadataList +
                '}';
    }
}
