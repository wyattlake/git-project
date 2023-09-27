import static org.junit.Assert.*;

import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

public class CommitTester {
    @BeforeAll
    static void setupBeforeClass() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Git git = new Git();
        git.init();
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
        String treeHash = Commit.createTree();

        // Confirm the empty tree has the correct hash
        assertEquals(treeHash, "70246bde7d6bb9bdadc1a69206354b0e54afc709");

        // Confirm the tree object file was created
        assertTrue(Utils.exists(".gitproject/objects/70246bde7d6bb9bdadc1a69206354b0e54afc709"));
    }

    @Test
    @DisplayName("Tests the write to file")
    public void testWrite() throws Exception {
        Commit commit = new Commit("2b98fbd4f414b26b612fa50b17879f62733254e6", "Buddy the Wolverine",
                "Did incredible things.");

        commit.writeToFile();

        // Confirm the hash of the file created is correct
        assertTrue(Utils.exists(".gitproject/objects/2af6278cad5862c4ceb0d5aab9a18d1f7a0349ff"));

        // Confirm the object file contents match what is expected
        assertEquals("70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n" +
                "\n" +
                "Buddy the Wolverine\n",
                Utils.unzipFile(".gitproject/objects/2af6278cad5862c4ceb0d5aab9a18d1f7a0349ff").substring(0, 103));

        // Confirm the commit message matches what is expected
        assertEquals("Did incredible things.",
                Utils.unzipFile(".gitproject/objects/2af6278cad5862c4ceb0d5aab9a18d1f7a0349ff").substring(114));
    }

}
