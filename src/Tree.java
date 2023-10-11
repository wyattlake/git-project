import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Tree {
    protected Path projectDirectory, objectsPath;

    private HashMap<String, String> treeMap;
    private HashMap<String, String> blobMap;

    public Tree(String projectDirectory) {
        treeMap = new HashMap<String, String>();
        blobMap = new HashMap<String, String>();

        this.projectDirectory = Paths.get(projectDirectory);
        objectsPath = this.projectDirectory.resolve("objects/");
    }

    public Tree() {
        this("");
    }

    public void add(String input) throws Exception {
        String[] splits = input.split(" : ");

        if (splits[0].equals("tree")) {
            if (splits.length == 2) {
                treeMap.put("", splits[1]);
            } else {
                if (treeMap.containsKey(splits[2])) {
                    throw new Exception("Cannot add a duplicate tree");
                }
                treeMap.put(splits[2], splits[1]);
            }
            // Adding a tree
            return;

        } else if (splits[0].equals("blob")) {
            // Adding a blob
            if (blobMap.containsKey(splits[2])) {
                throw new Exception("Cannot add a blob with a duplicate filename");
            }
            blobMap.put(splits[2], splits[1]);
            return;
        }

        throw new Exception("Invalid add format");
    }

    public boolean remove(String key) {
        if (blobMap.containsKey(key)) {
            blobMap.remove(key);
            return true;
        } else if (treeMap.containsKey(key)) {
            treeMap.remove(key);
            return true;
        }
        return false;
    }

    public String addDirectory(String path) throws Exception {
        addDirectoryHelper(projectDirectory.resolve(path).toString());
        return writeToObjects();
    }

    private void addDirectoryHelper(String path) throws Exception {
        File parent = new File(path);

        for (File child : parent.listFiles()) {
            if (child.list() == null) {
                // If the folder contains a file, add the file to the tree
                Blob blob = new Blob(child.getAbsolutePath(), projectDirectory.toString());
                blob.writeToObjects();

                add("blob : " + blob.getHash() + " : " + child.getName());
            } else {
                // If the folder contains a subfolder, create a tree from the subfolder and add
                // the subtree to the original tree. Note that the subTree inherits the parent
                // tree's project directory
                Tree subTree = new Tree(projectDirectory.toString());

                add("tree : " + subTree.addDirectory(child.getAbsolutePath()) + " : " + child.getName());
            }
        }
    }

    public String writeToObjects() throws Exception {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : blobMap.entrySet()) {
            builder.append("blob : " + entry.getValue() + " : " + entry.getKey() + "\n");
        }

        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            if (entry.getKey() == "") {
                builder.append("tree : " + entry.getValue() + "\n");
            } else {
                builder.append("tree : " + entry.getValue() + " : " + entry.getKey() + "\n");
            }
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        String zippedTreeHash = Utils.hashAndWriteFile(objectsPath.toString(), builder.toString(),
                Consts.COMPRESS_FILES);

        // Returns the hash of the tree so it can be accessed after writing
        return zippedTreeHash;
    }

    // Parses a tree file and returns a Tree object
    public static Tree parseTreeFile(String contents) throws Exception {
        Tree result = new Tree();
        String[] lines = contents.split("\n");

        for (String line : lines) {
            result.add(line);
        }

        return result;
    }

    public boolean containsFile(String filename) {
        return blobMap.containsKey(filename);
    }

    public String getFileHash(String file) {
        return blobMap.get(file);
    }

    public String getTreeHash(String tree) {
        return treeMap.get(tree);
    }

    public HashMap<String, String> getFileMap() {
        return blobMap;
    }

    public HashMap<String, String> getTreeMap() {
        return treeMap;
    }

    public String getPreviousTreeHash() {
        return this.treeMap.getOrDefault("", "");
    }
}
