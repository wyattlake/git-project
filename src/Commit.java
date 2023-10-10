import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class Commit {
    protected String author, summary, parent, treeSha, date, child, hash, parentTree;
    protected static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd");
    protected Path objectsPath, headPath, indexPath, projectDirectory;

    public Commit(String author, String summary, String projectDirectory) throws Exception {
        this.projectDirectory = Paths.get(projectDirectory);
        this.objectsPath = this.projectDirectory.resolve("objects/");
        this.headPath = this.projectDirectory.resolve("HEAD");
        this.indexPath = this.projectDirectory.resolve("index");

        if (!Utils.exists(headPath.toString())) {
            Utils.createFile(headPath.toString());
        }

        if (!Utils.exists(indexPath.toString())) {
            Utils.createFile(indexPath.toString());
        }

        this.author = author;
        this.summary = summary;
        this.parent = Utils.readFile(headPath.toString());
        this.parentTree = "";

        child = "";

        if (parent != "") {
            String[] previousCommitLines = Utils.unzipFile(objectsPath.resolve(parent).toString()).split("\n");

            // Gets tree location from first line
            this.parentTree = previousCommitLines[0];

            Utils.zipFile(objectsPath.resolve(parentTree).toString(), "");
        }

        treeSha = createTree();
        date = getDate();

        hash = writeToObjects();

        Utils.writeFile(headPath.toString(), hash);
    }

    public Commit(String author, String summary) throws Exception {
        this(author, summary, "");
    }

    private String writeToObjects() throws Exception {
        StringBuilder builder = new StringBuilder(
                treeSha + "\n" + parent + "\n" + author + "\n" + date + "\n" + summary);

        // No reason to zip here because this hash is not of the entire file's contents.
        String unzippedHash = Utils.hashString(builder.toString());

        // Inserting the nextCommitHash after the second newline
        builder.insert(builder.indexOf("\n", builder.indexOf("\n") + 1), child + "\n");

        // Zipping to the location determined by the unzippedHash
        Utils.zipFile(this.objectsPath.resolve(unzippedHash).toString(), builder.toString());

        // Modifying previous commit to point to this one
        if (parent != "") {
            String parentPath = objectsPath.resolve(parent).toString();

            String[] previousCommitLines = Utils.unzipFile(parentPath).split("\n");

            // Updates parent commit's child
            previousCommitLines[2] = unzippedHash;

            StringBuilder lastCommitBuilder = new StringBuilder();

            for (int i = 0; i < previousCommitLines.length; i++) {
                lastCommitBuilder.append(previousCommitLines[i] + "\n");
            }

            if (lastCommitBuilder.length() > 0) {
                lastCommitBuilder.deleteCharAt(lastCommitBuilder.length() - 1);
            }

            Utils.deleteFile(parentPath);
            Utils.zipFile(parentPath, lastCommitBuilder.toString());
        }

        return unzippedHash;
    }

    public static String getCommitTree(String commitHash) throws Exception {
        return getCommitTree(commitHash, "");
    }

    public static String getCommitTree(String commitHash, String projectDirectory) throws Exception {
        return Utils.unzipFile(Paths.get(projectDirectory).resolve("objects/" + commitHash).toString()).split("\n")[0];
    }

    public static String getDate() {
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }

    public String createTree() throws Exception {
        Tree tree = new Tree(projectDirectory.toString());

        BufferedReader fileReader = new BufferedReader(new FileReader(indexPath.toString()));
        String nextLine = fileReader.readLine();

        ArrayList<String> deletedFileHashes = new ArrayList<String>();
        ArrayList<String> editedFileHashes = new ArrayList<String>();

        // Reading the index file
        while (nextLine != null) {
            if (nextLine.substring(0, 9).equals("*deleted*")) {
                deletedFileHashes.add(nextLine.substring(10));
            } else if (nextLine.substring(0, 8).equals("*edited*")) {
                editedFileHashes.add(nextLine.substring(9));
            } else {
                tree.add(nextLine);
            }
            nextLine = fileReader.readLine();
        }

        // Checking if any files were deleted or edited
        if (deletedFileHashes.size() + editedFileHashes.size() > 0) {
            // Start looking through previous trees for deleted/edited files
            String previousTreeHash = this.parentTree;

            while (previousTreeHash != "") {
                // Unzip and parse the previous tree
                String previousTreeString = Utils.unzipFile(objectsPath.resolve(previousTreeHash).toString());
                Tree previousTree = Tree.parseTreeFile(previousTreeString);

                HashMap<String, String> fileMap = previousTree.getFileMap();

                // Iterate through all the files in the previous tree
                for (HashMap.Entry<String, String> file : fileMap.entrySet()) {
                    if (deletedFileHashes.contains(file.getKey())) {
                        // If the file is one of the ones we are deleting, remove it from our list of
                        // files to delete
                        deletedFileHashes.remove(file.getKey());
                    } else if (editedFileHashes.contains(file.getKey())) {
                        // If the file is one of the ones we are editing, remove it from our list of
                        // files to edit and compute the hash of the file's new contents before adding
                        // it to our tree
                        editedFileHashes.remove(file.getKey());

                        Blob blob = new Blob(file.getKey(), projectDirectory.toString());
                        blob.writeToObjects();

                        tree.add("blob : " + blob.getHash() + " : " + file.getKey());
                    } else {
                        // Otherwise, directly link to the file in our tree (because this previous tree
                        // is no longer being linked to)
                        tree.add("blob : " + file.getValue() + " : " + file.getKey());
                    }
                }

                // If the earliest file we removed was removed in this tree, then we can safely
                // add its parent tree (which we call the ancestor) to our commit
                if (deletedFileHashes.size() == 0) {
                    String ancestorTreeHash = previousTree.getPreviousTreeHash();
                    if (ancestorTreeHash != "") {
                        tree.add("tree : " + ancestorTreeHash);
                    }

                    // We can safely break from the loop now
                    break;
                } else {
                    // Otherwise, continue looking back into the commit trees for files to delete
                    previousTreeHash = previousTree.getPreviousTreeHash();
                }
            }
        } else {
            if (parentTree != "") {
                tree.add("tree : " + parentTree);
            }
        }

        fileReader.close();

        // Clears the index file
        Utils.writeFile(indexPath.toString(), "");

        return tree.writeToObjects();
    }

    public String getHash() {
        return hash;
    }

    public String getTree() {
        return treeSha;
    }
}
