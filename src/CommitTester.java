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
        test1Commit();

        Utils.writeFile("project/file3", "file3");
        Utils.writeFile("project/folder1/file4", "file4");

        Git git = new Git("project");

        git.addFile("file3");
        git.addDirectory("folder1");

        Commit c2 = new Commit("Wyatt", "c2", "project");

        // Checking that commit has the correct previous and next SHAs
        assertTrue(validCommit(c2.getTree(), "cb38718019af6a7b2356d16f3f4efd088d0f4111", "", "Wyatt", "c2"));

        String c2TreeContents = Utils.unzipFile("project/objects/" + c2.getTree());

        // Checking that the tree file has the correct contents
        assertTrue(c2TreeContents.contains("file3") && c2TreeContents.contains("folder1")
                && c2TreeContents.contains("tree : 96aead6804e98650f524020af7eb6dda7b5e37e5"));
    }

    private boolean validCommit(String tree, String previousCommit, String nextCommit, String author,
            String summary) throws Exception {
        String date = Commit.getDate();
        String commitPartialString = tree + "\n" + previousCommit + "\n" + author + "\n" + date
                + "\n" + summary;
        String commitPartialHash = Utils.hashString(commitPartialString);
        String commitFullString = tree + "\n" + previousCommit + "\n" + nextCommit + "\n" + author + "\n" + date + "\n"
                + summary;

        return (Utils.unzipFile("project/objects/" + commitPartialHash).equals(commitFullString));
    }
}
