package Client.StorageManager;

public class StorageFile {
    private String originalName;
    private int ID;

    public StorageFile(String fileName, int ID){
        originalName = fileName;
        this.ID = ID;
    }

    public StorageFile(String fileName){
        originalName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
