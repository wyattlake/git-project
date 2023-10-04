import static org.junit.Assert.*;

import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class CommitTester {
    @BeforeAll
    static void setupBeforeClass() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Git git = new Git();
        git.init();
    }

    @BeforeEach
    static void deleteFolders() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteDirectory(".gitproject/index");
        Utils.deleteDirectory(".gitproject/HEAD");
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
        assertTrue(Utils.exists(".gitproject/objects/70246bde7d6bb9bdadc1a69206354b0e54afc709"));
    }

    @Test
    @DisplayName("Tests the write to file method")
    public void testWrite() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");

        new Commit("2b98fbd4f414b26b612fa50b17879f62733254e6", "Buddy the Wolverine",
                "Did incredible things.", "");

        String commitString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n" +
                "Buddy the Wolverine\n" +
                Commit.getDate() + "\n" +
                "Did incredible things.";
        String commitStringHash = Utils.hashString(commitString);
        String finalString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n\n" +
                "Buddy the Wolverine\n" +
                Commit.getDate() + "\n" +
                "Did incredible things.";

        // Confirm the hash of the file created is correct
        assertTrue(Utils.exists(".gitproject/objects/" + commitStringHash));

        // Confirm the object file contents match what is expected
        assertEquals(finalString,
                Utils.unzipFile(".gitproject/objects/" + commitStringHash));
    }

    @Test
    @DisplayName("Tests updating the HEAD file with commits")
    public void testHead() throws Exception {
        Utils.deleteFile(".gitproject/HEAD");

        assertFalse(Utils.exists(".gitproject/HEAD"));

        new Commit("Wyatt Lake", "c1");
        String commitString = "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "\n" +
                "Wyatt Lake\n" +
                Commit.getDate() + "\n" +
                "c1";
        String commitStringHash = Utils.hashString(commitString);

        assertEquals(Utils.readFile(".gitproject/HEAD"), commitStringHash);
    }
}
