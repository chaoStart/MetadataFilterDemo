package filterdocid;

public class DocumentSimpleInfo {

    private String docId;
    private String fileName;

    // ✅ Jackson 反序列化必须要
    public DocumentSimpleInfo() {
    }

    public DocumentSimpleInfo(String docId, String fileName) {
        this.docId = docId;
        this.fileName = fileName;
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

    @Override
    public String toString() {
        return "DocumentSimpleInfo{" +
                "docId='" + docId + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
