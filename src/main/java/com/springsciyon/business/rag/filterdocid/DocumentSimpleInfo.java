package com.springsciyon.business.rag.filterdocid;

import java.util.List;
import java.util.Objects;

public class DocumentSimpleInfo {

    private String docId;
    private String fileName;
    private List<String> metadataList;

    // ✅ Jackson 反序列化必须要
    public DocumentSimpleInfo() {
    }

    public DocumentSimpleInfo(String docId, String fileName, List<String> metadataList) {
        this.docId = docId;
        this.fileName = fileName;
        this.metadataList = metadataList;
    }
    public void setDocId(String docId) {
//        System.out.println("setDocId 被调用：" + docId);
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public void setFileName(String fileName) {
//        System.out.println("set fileName 被调用：" + fileName);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setMetadataList(List<String> metadataList) {
//        System.out.println("set metadataList 被调用：" + metadataList);
        this.metadataList = metadataList;
    }

    public List<String> getMetadataList() {
        return metadataList;
    }

    @Override
    public String toString() {
        return "DocumentSimpleInfo{" +
                "docId='" + docId + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentSimpleInfo that = (DocumentSimpleInfo) o;
        return Objects.equals(docId, that.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId);
    }
}
