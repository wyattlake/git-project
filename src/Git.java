import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Git {
    protected Path projectDirectory, gitDirectory, objectsPath, indexPath;

    protected HashMap<String, String> blobMap;
    protected HashMap<String, String> treeMap;

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = Paths.get(projectDirectory);
        this.gitDirectory = this.projectDirectory.resolve(".gitproject/");
        this.objectsPath = this.gitDirectory.resolve("objects/");
        this.indexPath = this.gitDirectory.resolve("index");

        this.blobMap = new HashMap<>();
        this.treeMap = new HashMap<>();
    }

    /**
     * Creates objects folder and index file within .gitproject directory
     * 
     * @throws Exception
     */
    public void init() throws Exception {
        if (!Files.exists(objectsPath))
            Files.createDirectories(objectsPath);

        if (!Files.exists(indexPath))
            Files.createFile(indexPath);
    }

    /**
     * Adds all files and folders from the project directory to the index
     * 
     * @throws Exception
     */
    public void add() throws Exception {
        File projectDirectoryObject = projectDirectory.toFile();

        for (File file : projectDirectoryObject.listFiles()) {
            if (file.list() == null) {
                addFileToMap(file.getName());
            } else {
                // Prevents the .gitproject folder from being added to the index
                if (!file.getName().equals(".gitproject")) {
                    addDirectoryToMap(file.getName());
                }
            }
        }

        updateIndexFile();
    }

    /**
     * Adds a new file to the index
     * 
     * @param path
     * @throws Exception
     */
    public void addFile(String path) throws Exception {
        addFileToMap(path);

        updateIndexFile();
    }

    /**
     * Adds a new file to the index map
     * 
     * @param path
     * @throws Exception
     */
    public void addFileToMap(String path) throws Exception {
        Blob blob = new Blob(path, projectDirectory.toString());
        blob.writeToObjects();

        blobMap.putIfAbsent(path, blob.getHash());
    }

    /**
     * Adds a new directory to the index
     * 
     * @param path
     * @throws Exception
     */
    public void addDirectory(String path) throws Exception {
        addDirectoryToMap(path);

        updateIndexFile();
    }

    /**
     * Adds a new directory to the index map
     * 
     * @param path
     * @throws Exception
     */
    public void addDirectoryToMap(String path) throws Exception {
        // Create a new tree from the project directory
        Tree tree = new Tree(projectDirectory.toString());
        String hash = tree.addDirectory(path);

        treeMap.put(path, hash);
    }

    /**
     * Removes a file from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void removeFile(String path) throws Exception {
        blobMap.remove(path);

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
            builder.append("blob : " + file.getValue() + " : " + file.getKey() + "\n");
        }

        for (HashMap.Entry<String, String> file : treeMap.entrySet()) {
            builder.append("tree : " + file.getValue() + " : " + file.getKey() + "\n");
        }

        FileWriter writer = new FileWriter(indexPath.toString(), false);

        // Deletes extra \n char
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        writer.write(builder.toString());
        writer.close();
    }
}
