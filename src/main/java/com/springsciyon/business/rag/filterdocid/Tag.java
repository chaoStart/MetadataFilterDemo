package com.springsciyon.business.rag.filterdocid;

import java.util.List;

public class Tag {
    private String docId;
    private String fileName;
    private String author;
    private String dateTime;
    private String updateTime;
    private List<String> metadataList; // 对应 metadata_list 字段

    public Tag(String docId,String fileName, String author, String dateTime,String updateTime) {
        this.docId = docId;
        this.fileName = fileName;
        this.author = author;
        this.dateTime = dateTime;
        this.updateTime = updateTime;
    }
    // 新增构造函数（元数据列表）
    public Tag(String docId,String fileName, String author, String dateTime,String updateTime, List<String> metadataList) {
        this(docId,fileName,author, dateTime,updateTime);
        this.metadataList = metadataList;
    }

    public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
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

    public String getUpdateTime() {
        return updateTime;
    }
    public List<String> getMetadataList() { return metadataList; }

    public void setMetadataList(List<String> metadataList) { this.metadataList = metadataList; }
    @Override
    public String toString() {
        return "Tag{" +
                "fileName='" + fileName + '\'' +
                ", author='" + author + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", docId='" + docId + '\'' +
                ", metadataList=" + metadataList +
                '}';
    }
}
