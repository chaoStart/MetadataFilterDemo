package filterdocid;

public class DocumentSimpleInfo {

    private String docId;
    private String fileName;

    public DocumentSimpleInfo(String docId, String fileName) {
        this.docId = docId;
        this.fileName = fileName;
    }

    public String getDocId() {
        return docId;
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
