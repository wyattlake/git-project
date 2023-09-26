import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
        assertTrue(Util.exists("objects/da39a3ee5e6b4b0d3255bfef95601890afd80709"));
    }

    @Test
    @DisplayName("Tests the write to file")
    public void testWrite() throws Exception {
        Commit commit = new Commit("2b98fbd4f414b26b612fa50b17879f62733254e6", "Did incredible things.",
                "Buddy the Wolverine");

        commit.writeToFile();

        // Confirm sure the hash of the file created is correct
        assertTrue(Util.exists("objects/b0ced1739869a9ef72749ea17f0beaa5c75a128e"));

        // Confirm the object file contents match what is expected
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n" +
                "\n" +
                "Buddy the Wolverine\n" +
                "2023/09/21\n" +
                "Did incredible things.", Util.readFile("objects/b0ced1739869a9ef72749ea17f0beaa5c75a128e"));
    }

}
