import static org.junit.Assert.*;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class CommitTester {
    @BeforeAll
    static void setupBeforeClass() throws Exception {
        Utils.deleteDirectory("objects");
        Git git = new Git();
        git.init();
    }

    @BeforeEach
    void deleteFolders() throws Exception {
        Utils.deleteDirectory("objects");
        Utils.deleteFile("index");
        Utils.deleteFile("HEAD");
        Utils.deleteDirectory("project");
    }

    @Test
    @DisplayName("Verify getDate creates a valid date")
    public void testGetDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd");

        // Will throw an error if the date format is not valid
        dtf.parse(Commit.getDate());
    }

    @Test
    @DisplayName("Verify createTree creates a Tree in the correct location")
    public void testCreateTree() throws Exception {
        Commit commit = new Commit("Alex Lee", "Code");
        String treeHash = commit.createTree();

        // Confirm the empty tree has the correct hash
        assertEquals(treeHash, "70246bde7d6bb9bdadc1a69206354b0e54afc709");

        // Confirm the tree object file was created
        assertTrue(Utils.exists("objects/70246bde7d6bb9bdadc1a69206354b0e54afc709"));
    }

    @Test
    @DisplayName("Tests the write to file method")
    public void testWrite() throws Exception {
        new Commit("Buddy the Wolverine",
                "Did incredible things.", "");

        String commitString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "\n" +
                "Buddy the Wolverine\n" +
                Commit.getDate() + "\n" +
                "Did incredible things.";
        String commitStringHash = Utils.hashString(commitString);
        String finalString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "\n\n" +
                "Buddy the Wolverine\n" +
                Commit.getDate() + "\n" +
                "Did incredible things.";

        // Confirm the hash of the file created is correct
        assertTrue(Utils.exists("objects/" + commitStringHash));

        // Confirm the object file contents match what is expected
        assertEquals(finalString,
                Utils.unzipFile("objects/" + commitStringHash));
    }

    @Test
    @DisplayName("Tests updating the HEAD file with commits")
    public void testHead() throws Exception {
        assertFalse(Utils.exists("HEAD"));

        new Commit("Wyatt Lake", "c1");
        String commitString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "\n" +
                "Wyatt Lake\n" +
                Commit.getDate() + "\n" +
                "c1";
        String commitStringHash = Utils.hashString(commitString);

        assertEquals(Utils.readFile("HEAD"), commitStringHash);
    }

    @Test
    public void test1Commit() throws Exception {
        Utils.deleteDirectory("project");

        Utils.writeFile("project/file1", "file1");
        Utils.writeFile("project/file2", "file2");

        Git git = new Git("project");
        git.init();
        git.addFile("file1");
        git.addFile("file2");

        Commit commit = new Commit("Wyatt", "commit1", "project");

        // Checking that commit has the correct previous and next SHAs
        assertTrue(validCommit(commit.getTree(), "", "", "Wyatt", "commit1"));

        String treeContents = Utils.unzipFile("project/objects/" + commit.getTree());

        // Checking that the tree file contans both files in it
        assertTrue(treeContents.contains("file1") && treeContents.contains("file2"));
    }

    @Test
    public void test2Commits() throws Exception {
        Utils.writeFile("project/file1", "file1");
        Utils.writeFile("project/file2", "file2");

        Git git = new Git("project");
        git.init();
        git.addFile("file1");
        git.addFile("file2");

        Commit c1 = new Commit("Wyatt", "commit1", "project");

        Utils.writeFile("project/file3", "file3");
        Utils.writeFile("project/folder1/file4", "file4");

        git.addFile("file3");
        git.addDirectory("folder1");

        Commit c2 = new Commit("Wyatt", "c2", "project");

        // Checking that commit has the correct previous and next SHAs
        assertTrue(validCommit(c2.getTree(), c1.getHash(), "", "Wyatt", "c2"));

        // Checking that the previous commit has been updated to point to the current
        // commit
        assertTrue(Utils.unzipFile("project/objects/" + c1.getHash())
                .contains(c2.getHash()));

        String c2TreeContents = Utils.unzipFile("project/objects/" + c2.getTree());

        // Checking that the tree file has the correct contents
        assertTrue(c2TreeContents.contains("file3") && c2TreeContents.contains("folder1")
                && c2TreeContents.contains("tree : 96aead6804e98650f524020af7eb6dda7b5e37e5"));
    }

    @Test
    public void test4Commits() throws Exception {
        Utils.writeFile("project/file1", "file1");
        Utils.writeFile("project/file2", "file2");

        Git git = new Git("project");
        git.init();
        git.addFile("file1");
        git.addFile("file2");

        new Commit("Wyatt", "commit1", "project");

        Utils.writeFile("project/file3", "file3");
        Utils.writeFile("project/folder1/file4", "file4");

        git.addFile("file3");
        git.addDirectory("folder1");

        Commit c2 = new Commit("Wyatt", "c2", "project");

        Utils.writeFile("project/folder2/file5", "file5");
        Utils.writeFile("project/folder2/file6", "file6");

        git.addDirectory("folder2");

        Commit c3 = new Commit("Wyatt", "c3", "project");

        // Checking that commit has the correct previous and next SHAs
        assertTrue(validCommit(c3.getTree(), c2.getHash(), "", "Wyatt", "c3"));

        // Checking that the previous commit has been updated to point to the current
        // commit
        assertTrue(Utils.unzipFile("project/objects/" + c2.getHash())
                .contains(c3.getHash()));

        String c3TreeContents = Utils.unzipFile("project/objects/" + c3.getTree());

        // Checking that the tree file has the correct contents
        assertTrue(c3TreeContents.contains("folder2")
                && c3TreeContents.contains("tree : 333d75d4368ad760435262cb9800327415d7ac97"));

        Utils.writeFile("project/folder3/folder4/file7", "file7");
        Utils.writeFile("project/folder3/file8", "file8");

        git.addDirectory("folder3");

        Commit c4 = new Commit("Wyatt", "c4", "project");

        // Checking that commit has the correct previous and next SHAs
        assertTrue(validCommit(c4.getTree(), c3.getHash(), "", "Wyatt", "c4"));

        // Checking that the previous commit has been updated to point to the current
        // commit
        assertTrue(Utils.unzipFile("project/objects/" + c3.getHash()).contains(c4.getHash()));

        String c4TreeContents = Utils.unzipFile("project/objects/" + c4.getTree());

        // Checking that the tree file has the correct contents
        assertTrue(c4TreeContents.contains("folder3")
                && c4TreeContents.contains("tree : " + c3.getTree()));
    }

    @Test
    public void testEditAndDelete() throws Exception {
        Utils.writeFile("f1", "f1");
        Utils.writeFile("f2", "f2");

        Git git = new Git();
        git.init();
        git.addFile("f1");
        git.addFile("f2");

        Commit c1 = new Commit("Wyatt", "c1");

        Tree c1Tree = Tree.parseTreeFile(Utils.unzipFile("objects/" + c1.getTree()));

        // Making sure the commit tree both new files
        assertTrue(c1Tree.containsFile("f1") && c1Tree.containsFile("f2"));

        // Making sure the commit tree files are hashed in the objects folder
        assertTrue(Utils.exists("objects/" + Utils.getHashedZip("f1"))
                && Utils.exists("objects/" + Utils.getHashedZip("f2")));

        Utils.writeFile("f1", "f1_edited");
        Utils.writeFile("f3", "f3");

        git.editFile("f1");
        git.addFile("f3");

        Commit c2 = new Commit("Wyatt", "c2");

        Tree c2Tree = Tree.parseTreeFile(Utils.unzipFile("objects/" + c2.getTree()));

        // Making sure the commit tree contains all files
        assertTrue(c2Tree.containsFile("f1") && c2Tree.containsFile("f2") && c2Tree.containsFile("f3"));

        // Making sure the commit tree points to the correct object files
        assertEquals(Utils.unzipFile("objects/" + c2Tree.getFileHash("f1")), "f1_edited");
        assertEquals(Utils.unzipFile("objects/" + c2Tree.getFileHash("f2")), "f2");
        assertEquals(Utils.unzipFile("objects/" + c2Tree.getFileHash("f3")), "f3");

        Utils.writeFile("f4", "f4");
        Utils.writeFile("f5", "f5");

        git.addFile("f4");
        git.addFile("f5");

        Commit c3 = new Commit("Wyatt", "c3");

        Tree c3Tree = Tree.parseTreeFile(Utils.unzipFile("objects/" + c3.getTree()));

        // Making sure the commit tree contains new files and has its parent set to c2's
        // tree
        assertTrue(c3Tree.getPreviousTreeHash().equals(c2.getTree())
                && c3Tree.containsFile("f4") && c3Tree.containsFile("f5"));

        git.deleteFile("f4");

        Commit c4 = new Commit("Wyatt", "c4");
        Tree c4Tree = Tree.parseTreeFile(Utils.unzipFile("objects/" + c4.getTree()));

        // Ensuring c4's tree has ancestor c2 and also contains f3, f5 but not f4
        assertTrue(c4Tree.getPreviousTreeHash().equals(c2.getTree())
                && c4Tree.containsFile("f5") && !c4Tree.containsFile("f4"));

        Utils.writeFile("f2", "f2_edited");

        git.editFile("f2");
        git.deleteFile("f1");

        Commit c5 = new Commit("Wyatt", "c5");
        Tree c5Tree = Tree.parseTreeFile(Utils.unzipFile("objects/" + c5.getTree()));

        // Ensuring c5's tree has no ancestor and contains f2, f3, f5 but not f1, f4
        assertTrue(c5Tree.getPreviousTreeHash().equals("")
                && c5Tree.containsFile("f2") && c5Tree.containsFile("f3") && c5Tree.containsFile("f5")
                && !c5Tree.containsFile("f1")
                && !c5Tree.containsFile("f4"));

        // Ensuring f2 has been edited
        assertEquals(Utils.unzipFile("objects/" + c5Tree.getFileHash("f2")), "f2_edited");
        assertEquals(Utils.unzipFile("objects/" + c5Tree.getFileHash("f3")), "f3");
        assertEquals(Utils.unzipFile("objects/" + c5Tree.getFileHash("f5")), "f5");
    }

    // Checks to see if a commit formatted correctly
    private boolean validCommit(String tree, String previousCommit, String nextCommit, String author,
            String summary) throws Exception {
        String date = Commit.getDate();
        String commitPartialString = tree + "\n" + previousCommit + "\n" + author + "\n" + date
                + "\n" + summary;
        String commitPartialHash = Utils.hashString(commitPartialString);
        String commitFullString = tree + "\n" + previousCommit + "\n" + nextCommit + "\n" + author + "\n" + date
                + "\n"
                + summary;

        return (Utils.unzipFile("project/objects/" + commitPartialHash).equals(commitFullString));
    }
}
