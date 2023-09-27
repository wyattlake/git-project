import static org.junit.Assert.*;

import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class CommitTester {
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
        assertEquals(treeHash, "da39a3ee5e6b4b0d3255bfef95601890afd80709");

        // Confirm the tree object file was created
        assertTrue(Utils.exists("objects/da39a3ee5e6b4b0d3255bfef95601890afd80709"));
    }

    @Test
    @DisplayName("Tests the write to file")
    public void testWrite() throws Exception {
        Commit commit = new Commit("2b98fbd4f414b26b612fa50b17879f62733254e6", "Buddy the Wolverine",
                "Did incredible things.");

        commit.writeToFile();

        // Confirm the hash of the file created is correct
        assertTrue(Utils.exists("objects/0dfeffda8f5fe22798f4e5270b539e10756c2442"));

        // Confirm the object file contents match what is expected
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n" +
                "\n" +
                "Buddy the Wolverine\n",
                Utils.readFile("objects/0dfeffda8f5fe22798f4e5270b539e10756c2442").substring(0, 103));

        // Confirm the commit message matches what is expected
        assertEquals("Did incredible things.",
                Utils.readFile("objects/0dfeffda8f5fe22798f4e5270b539e10756c2442").substring(114));
    }

}
