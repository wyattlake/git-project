import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Git {
    protected String projectDirectory, gitDirectory, objectsPath, indexPath;

    protected HashMap<String, String> fileHashMap;

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = System.getProperty("user.dir") + "/" + projectDirectory;
        this.gitDirectory = this.projectDirectory + ".gitproject/";
        this.objectsPath = gitDirectory + "objects/";
        this.indexPath = gitDirectory + "index";

        this.fileHashMap = new HashMap<>();
    }

    /**
     * Creates objects folder and index file within .gitproject directory
     * 
     * @throws Exception
     */
    public void init() throws Exception {
        Path gitprojectDirectories = Paths.get(objectsPath);
        if (!Files.exists(gitprojectDirectories))
            Files.createDirectories(gitprojectDirectories);

        Path indexFile = Paths.get(indexPath);
        if (!Files.exists(indexFile))
            Files.createFile(indexFile);
    }

    /**
     * Adds a new Blob to the index
     * 
     * @param path
     * @throws Exception
     */
    public void add(String path) throws Exception {
        Blob blob = new Blob(path);
        blob.writeToObjects();

        fileHashMap.putIfAbsent(path, blob.getHash());

        // This code is currently set to update the index file every time you add a new
        // Blob. However, a more optimized version of this code could wait to update the
        // index file until multiple blobs have been added but you would have to call
        // updateIndexFile manually.
        updateIndexFile();
    }

    /**
     * Removes a blob from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void remove(String path) throws Exception {
        fileHashMap.remove(path);

        // Same situation as add().
        updateIndexFile();
    }

    /**
     * Updates the index file based on the current file HashMap
     * 
     * @throws Exception
     */
    protected void updateIndexFile() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (HashMap.Entry<String, String> file : fileHashMap.entrySet()) {
            builder.append(file.getKey() + " : " + file.getValue() + "\n");
        }

        FileWriter writer = new FileWriter(indexPath, false);

        // Deletes extra \n char
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        writer.write(builder.toString());
        writer.close();
    }
}
