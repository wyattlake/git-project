import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class Blob {
    protected String hash, content;
    protected Path objectsPath, hashPath;

    protected MessageDigest md;

    /**
     * Creates a new blob
     * 
     * @param path Path to source file
     * @throws Exception
     */
    public Blob(String path, String projectDirectory) throws Exception {
        this.objectsPath = Paths.get(projectDirectory).resolve(".gitproject/objects/");

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
        this(path, "");
    }

    public void writeToObjects() throws Exception {
        if (content == null || content.length() == 0) {
            return;
        }

        this.hash = Utils.hashAndWriteFile(objectsPath.toString(), content, Consts.COMPRESS_FILES);
        this.hashPath = this.objectsPath.resolve(hash);
    }

    public String getHash() {
        return hash;
    }
}
