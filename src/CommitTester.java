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
    @DisplayName("Tests the write to file method")
    public void testWrite() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");

        new Commit("2b98fbd4f414b26b612fa50b17879f62733254e6", "Buddy the Wolverine",
                "Did incredible things.");

        // Confirm the hash of the file created is correct
        assertTrue(Utils.exists(".gitproject/objects/b728c31cbdaece6869d6ba741da3685345713e38"));

        // Confirm the object file contents match what is expected
        assertEquals("70246bde7d6bb9bdadc1a69206354b0e54afc709\n" +
                "2b98fbd4f414b26b612fa50b17879f62733254e6\n" +
                "\n" +
                "Buddy the Wolverine\n",
                Utils.unzipFile(".gitproject/objects/b728c31cbdaece6869d6ba741da3685345713e38").substring(0, 103));

        // Confirm the commit message matches what is expected
        assertEquals("Did incredible things.",
                Utils.unzipFile(".gitproject/objects/b728c31cbdaece6869d6ba741da3685345713e38").substring(114));
    }

    @Test
    @DisplayName("Tests updating the HEAD file with commits")
    public void testHead() throws Exception {
        Utils.deleteFile(".gitproject/HEAD");

        assertFalse(Utils.exists(".gitproject/HEAD"));
        new Commit("Wyatt Lake", "c1");
        assertEquals(Utils.readFile(".gitproject/HEAD"), "58b20040746244459378a94429a9b56a35c71249");
        new Commit("58b20040746244459378a94429a9b56a35c71249", "Wyatt Lake", "c2");
        assertEquals(Utils.readFile(".gitproject/HEAD"), "42dfa51836e02076badfb7b1932dd37180b2628b");
        new Commit("42dfa51836e02076badfb7b1932dd37180b2628b", "Wyatt Lake", "c3");
        assertEquals(Utils.readFile(".gitproject/HEAD"), "51ac9ec4311ad7b699ea326f12eceb37883da019");
        assertEquals(Utils.unzipFile(".gitproject/objects/51ac9ec4311ad7b699ea326f12eceb37883da019"),
                "70246bde7d6bb9bdadc1a69206354b0e54afc709\n" + //
                        "42dfa51836e02076badfb7b1932dd37180b2628b\n" + //
                        "\n" + //
                        "Wyatt Lake\n" + //
                        "2023/09/27\n" + //
                        "c3");
    }
}
