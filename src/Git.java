import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Git {
    protected Path projectDirectory, gitDirectory, objectsPath, indexPath;

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = Paths.get(projectDirectory);
        this.gitDirectory = this.projectDirectory.resolve(".gitproject/");
        this.objectsPath = this.gitDirectory.resolve("objects/");
        this.indexPath = this.gitDirectory.resolve("index");
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
            Utils.writeFile(indexPath.toString(), "");
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
                addFile(file.getName());
            } else {
                // Prevents the .gitproject folder from being added to the index
                if (!file.getName().equals(".gitproject")) {
                    addDirectory(file.getName());
                }
            }
        }
    }

    /**
     * Adds a new file to the index map
     * 
     * @param path
     * @throws Exception
     */
    public void addFile(String path) throws Exception {
        Blob blob = new Blob(path, projectDirectory.toString());
        blob.writeToObjects();

        String prefix = "\n";
        if (Utils.readFile(indexPath.toString()).equals("")) {
            prefix = "";
        }
        Utils.appendToFile(indexPath.toString(), prefix + "blob : " + blob.getHash() + " : " + path);
    }

    /**
     * Adds a new directory to the index map
     * 
     * @param path
     * @throws Exception
     */
    public void addDirectory(String path) throws Exception {
        // Create a new tree from the project directory
        Tree tree = new Tree(projectDirectory.toString());
        String hash = tree.addDirectory(path);

        String prefix = "\n";
        if (Utils.readFile(indexPath.toString()).equals("")) {
            prefix = "";
        }
        Utils.appendToFile(indexPath.toString(), prefix + "tree : " + hash + " : " + path);
    }

    /**
     * Removes a file from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void removeFile(String path) throws Exception {
        String[] lines = Utils.readFile(indexPath.toString()).split("\n");

        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            String[] splits = line.split(" : ");
            // Classic De Morgan's Law moment
            if (!(splits[0].equals("blob") && splits[2].equals(path))) {
                builder.append(line);
            }
        }

        Utils.writeFile(indexPath.toString(), builder.toString());
    }

    /**
     * Removes a folder from the index.
     * 
     * @param path
     * @throws Exception
     */
    public void removeFolder(String path) throws Exception {
        String[] lines = Utils.readFile(indexPath.toString()).split("\n");

        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            String[] splits = line.split(" : ");
            // Classic De Morgan's Law moment
            if (!(splits[0].equals("tree") && splits[2].equals(path))) {
                builder.append(line);
            }
        }

        Utils.writeFile(indexPath.toString(), builder.toString());
    }
}
