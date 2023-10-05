import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Commit {
    protected String author, summary, parent, treeSha, date, child, hash, parentTree;
    protected static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd");
    protected Path objectsPath, headPath, indexPath, projectDirectory;

    public Commit(String author, String summary, String projectDirectory) throws Exception {
        this.projectDirectory = Paths.get(projectDirectory);
        this.objectsPath = this.projectDirectory.resolve(".gitproject/objects/");
        this.headPath = this.projectDirectory.resolve(".gitproject/HEAD");
        this.indexPath = this.projectDirectory.resolve(".gitproject/index");

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
            String[] previousCommitLines = Utils.unzipFile(parent).split("\n");

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
            String parentPath = objectsPath.resolve(parentTree).toString();

            String[] previousCommitLines = Utils.unzipFile(parentPath).split("\n");

            // Updates parent commit's child
            previousCommitLines[2] = unzippedHash;

            StringBuilder lastCommitBuilder = new StringBuilder();

            for (int i = 0; i < previousCommitLines.length; i++) {
                lastCommitBuilder.append(previousCommitLines[i] + "\n");
            }

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

        while (nextLine != null) {
            tree.add(nextLine);
            nextLine = fileReader.readLine();
        }

        fileReader.close();

        // Clears the index file
        Utils.writeFile(indexPath.toString(), "");

        if (parentTree != "") {
            tree.add("tree : " + parentTree);
        }

        return tree.writeToObjects();
    }

    public String getHash() {
        return hash;
    }

    public String getTree() {
        return treeSha;
    }
}
