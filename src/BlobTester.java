
import static org.junit.Assert.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BlobTester {
    @BeforeAll
    static void setupBeforeClass() throws Exception {
        Utils.deleteDirectory("objects");

        Git git = new Git();
        git.init();

        Utils.writeFile("testerFile.txt", "hello world");
        Blob blob = new Blob("testerFile.txt");
        blob.writeToObjects();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Utils.deleteFile("testerFile.txt");
        Utils.deleteDirectory("objects");
    }

    @Test
    @DisplayName("Verify Blob file has been created")
    void testBlobCreated() throws Exception {
        // Confirm the Blob creates a file in the correct location with the correct hash
        assertTrue(Utils.exists("objects/d816fef546b01829364d78c90a6d49f3ea9eb356"));
    }

    @Test
    @DisplayName("Verify Blob has correct contents")
    void testBlobContents() throws Exception {
        String originalFileContent = Utils.readFile("testerFile.txt");
        String blobFileContent = Utils.unzipFile("objects/d816fef546b01829364d78c90a6d49f3ea9eb356");

        // Confirm the Blob's contents match the original file's contents
        assertEquals(originalFileContent, blobFileContent);
    }
}
