import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Git {
    protected String projectDirectory, gitDirectory, objectsPath, indexPath;

    protected HashMap<String, Blob> files;

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = System.getProperty("user.dir") + "/" + projectDirectory + "/";
        this.gitDirectory = this.projectDirectory + ".gitproject/";
        this.objectsPath = gitDirectory + "objects/";
        this.indexPath = gitDirectory + "index";

        this.files = new HashMap<>();
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

    public void add(String path) throws Exception {
        Blob blob = new Blob(path, projectDirectory);
        files.put(path, blob);
        updateIndexFile();
    }

    public void remove(String path) throws Exception {
        Blob removed = files.remove(path);
        removed.deleteFile();
        updateIndexFile();
    }

    /**
     * Updates the index file based on the current file HashMap
     * 
     * @throws Exception
     */
    protected void updateIndexFile() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (HashMap.Entry<String, Blob> file : files.entrySet()) {
            builder.append(file.getKey() + " : " + file.getValue().getHash() + "\n");
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