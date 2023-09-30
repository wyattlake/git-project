import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Git {
    protected String projectDirectory, gitDirectory, objectsPath, indexPath;

    protected HashMap<String, String> blobMap;
    protected HashMap<String, String> treeMap;

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = System.getProperty("user.dir") + "/" + projectDirectory;
        this.gitDirectory = this.projectDirectory + ".gitproject/";
        this.objectsPath = gitDirectory + "objects/";
        this.indexPath = gitDirectory + "index";

        this.blobMap = new HashMap<>();
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
     * Adds a new file to the index
     * 
     * @param path
     * @throws Exception
     */
    public void addFile(String path) throws Exception {
        Blob blob = new Blob(path);
        blob.writeToObjects();

        blobMap.putIfAbsent(path, blob.getHash());

        // This code is currently set to update the index file every time you add a new
        // Blob. However, a more optimized version of this code could wait to update the
        // index file until multiple blobs have been added but you would have to call
        // updateIndexFile manually.
        updateIndexFile();
    }

    /**
     * Adds a new directory to the index
     * 
     * @param path
     * @throws Exception
     */
    public void addDirectory(String path) throws Exception {
        Tree tree = new Tree();
        String hash = tree.addDirectory(path);

        treeMap.put(path, hash);

        // Same situation as addFile()
        updateIndexFile();
    }

    /**
     * Removes a file from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void removeFile(String path) throws Exception {
        blobMap.remove(path);

        // Same situation as addFile().
        updateIndexFile();
    }

    /**
     * Removes a folder from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void removeFolder(String path) throws Exception {
        treeMap.remove(path);

        // Same situation as addFile().
        updateIndexFile();
    }

    /**
     * Updates the index file based on the current file HashMap
     * 
     * @throws Exception
     */
    protected void updateIndexFile() throws Exception {
        StringBuilder builder = new StringBuilder();

        for (HashMap.Entry<String, String> file : blobMap.entrySet()) {
            builder.append("blob : " + file.getKey() + " : " + file.getValue() + "\n");
        }

        for (HashMap.Entry<String, String> file : treeMap.entrySet()) {
            builder.append("tree : " + file.getKey() + " : " + file.getValue() + "\n");
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
