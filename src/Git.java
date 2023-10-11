import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Git {
    protected Path projectDirectory, gitDirectory, objectsPath, indexPath;
    static final String[] IGNORED_PATHS = { "objects", "HEAD", "index" };

    public Git() {
        this("");
    }

    public Git(String projectDirectory) {
        this.projectDirectory = Paths.get(projectDirectory);
        this.gitDirectory = this.projectDirectory.resolve("");
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

    /**
     * Adds a delete file entry to the index file.
     * 
     * @param path
     * @throws Exception
     */
    public void deleteFile(String path) throws Exception {
        String prefix = "\n";
        if (Utils.readFile(indexPath.toString()).equals("")) {
            prefix = "";
        }

        Utils.appendToFile(indexPath.toString(), prefix + "*deleted* " + path);
    }

    public void checkout(String commitHash) throws Exception {
        Utils.clearDirectory(projectDirectory.toString(), IGNORED_PATHS);
        String commitTreeHash = Commit.getCommitTree(commitHash, projectDirectory.toString());
        Tree commitTree = Tree.parseTreeFile(Utils.unzipFile(objectsPath.resolve(commitTreeHash).toString()));
        checkoutHelper(commitTree, projectDirectory.toString() + ((projectDirectory.toString().equals("")) ? "" : "/"));
    }

    private void checkoutHelper(Tree tree, String directory) throws Exception {
        // Create all files in tree
        for (HashMap.Entry<String, String> fileEntry : tree.getFileMap().entrySet()) {
            // Writes the unzipped blob contents to the current directory
            Utils.writeFile(directory + fileEntry.getKey(),
                    Utils.unzipFile(objectsPath.resolve(fileEntry.getValue()).toString()));
        }

        // Recursively create all folders in tree
        for (HashMap.Entry<String, String> treeEntry : tree.getTreeMap().entrySet()) {
            // Loads the child tree from the hash
            Tree childTree = Tree.parseTreeFile(Utils.unzipFile(objectsPath.resolve(treeEntry.getValue()).toString()));

            // Runs checkoutHelper with the tree's folder added to the base directory. If
            // the tree is a previous commit tree and not a folder, nothing is added to the
            // directory path
            String childDirectory = (treeEntry.getKey().equals("")) ? "" : treeEntry.getKey() + "/";
            checkoutHelper(childTree, directory + childDirectory);
        }
    }

    /**
     * Adds an edit file entry to the index file.
     * 
     * @param path
     * @throws Exception
     */
    public void editFile(String path) throws Exception {
        String prefix = "\n";
        if (Utils.readFile(indexPath.toString()).equals("")) {
            prefix = "";
        }

        Utils.appendToFile(indexPath.toString(), prefix + "*edited* " + path);
    }
}
