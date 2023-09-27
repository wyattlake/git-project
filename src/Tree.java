import java.util.HashMap;
import java.util.Map;

public class Tree {
    protected String projectDirectory, objectsPath;

    private HashMap<String, String> treeMap;
    private HashMap<String, String> blobMap;

    public Tree(String projectDirectory) {
        treeMap = new HashMap<String, String>();
        blobMap = new HashMap<String, String>();

        this.projectDirectory = projectDirectory;
        objectsPath = projectDirectory + ".gitproject/objects/";
    }

    public Tree() {
        this("");
    }

    public void add(String input) throws Exception {
        String[] splits = input.split(" : ");

        if (splits.length != 3) {
            throw new Exception("Invalid add format");
        }

        if (splits[0].equals("tree")) {
            // Adding a tree
            if (treeMap.containsKey(splits[2])) {
                throw new Exception("Cannot add a duplicate tree");
            }
            treeMap.put(splits[2], splits[1]);
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

    public String writeToObjects() throws Exception {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : blobMap.entrySet()) {
            builder.append("blob : " + entry.getValue() + " : " + entry.getKey() + "\n");
        }

        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            builder.append("tree : " + entry.getValue() + " : " + entry.getKey() + "\n");
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        String zippedTreeHash = Utils.hashAndWriteFile(objectsPath, builder.toString(), Consts.COMPRESS_FILES);

        // Returns the hash of the tree so it can be accessed after writing
        return zippedTreeHash;
    }

}
