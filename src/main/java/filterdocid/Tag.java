package filterdocid;

public class Tag {

    private String fileName;
    private String author;
    private String dateTime;
    private String docId;

    public Tag(String fileName, String author, String dateTime, String docId) {
        this.fileName = fileName;
        this.author = author;
        this.dateTime = dateTime;
        this.docId = docId;
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

    @Override
    public String toString() {
        return "Tag{" +
                "fileName='" + fileName + '\'' +
                ", author='" + author + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", docId='" + docId + '\'' +
                '}';
    }
}
