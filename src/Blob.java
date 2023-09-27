import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class Blob {
    protected String hash, projectDirectory, hashPath, content;

    protected MessageDigest md;

    /**
     * Creates a new blob
     * 
     * @param path Path to source file
     * @throws Exception
     */
    public Blob(String path, String projectDirectory) throws Exception {
        this.projectDirectory = projectDirectory;

        // Initialize the MessageDigest for hashing
        md = MessageDigest.getInstance("SHA-1");

        content = Files.readString(Paths.get(projectDirectory + path));
    }

    /**
     * Creates a new blob
     * 
     * @param path Path to source file
     * @throws Exception
     */
    public Blob(String path) throws Exception {
        this("", path);
    }

    public void writeToObjects() throws Exception {
        if (content == null || content.length() == 0) {
            return;
        }

        this.hash = Utils.hashAndWriteFile(".gitproject/objects/", content, Consts.COMPRESS_FILES);
        this.hashPath = this.projectDirectory + ".gitproject/objects/" + hash;
    }

    public String getHash() {
        return hash;
    }

    /**
     * Deletes a file. Should not be used in the index code.
     * 
     * @throws Exception
     */
    protected void deleteFile() throws Exception {
        Files.deleteIfExists(Paths.get(hashPath));
    }
}
