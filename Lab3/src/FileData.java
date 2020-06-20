public class FileData {
    private long fileSize;
    private String fileName;

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileData(String fileName, long fileSize) {
        setFileName(fileName);
        setFileSize(fileSize);
    }
}
