import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GitTester {
    @Test
    @DisplayName("Verify init creates index file and objects folder")
    void testInit() throws Exception {
        Git git = new Git();
        git.init();

        assertTrue(Utils.exists(".gitproject/objects"));
        assertTrue(Utils.exists(".gitproject/index"));

        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteFile(".gitproject/index");
    }

    @Test
    @DisplayName("Verify blob creates blobs and adds them to index")
    void testBlob() throws Exception {
        Git git = new Git();

        // Because blob is designed to create the blob and update the index, this test
        // tests both of those functionalities

        git.init();

        Utils.writeFile("testFile.txt", "This is a test file.");
        git.addBlob("testFile.txt");

        // Confirm blob file has been created in objects with the correct hash
        assertTrue(Utils.exists(".gitproject/objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

        // Confirm index has been updated
        assertEquals(Utils.readFile(".gitproject/index"), "testFile.txt : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51");

        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteFile(".gitproject/index");
    }

    @Test
    @DisplayName("Verify remove correctly updates index")
    void testRemove() throws Exception {
        Git git = new Git();
        git.init();

        Utils.writeFile("testFile.txt", "This is a test file.");
        Utils.writeFile("testFile2.txt", "This is another test file.");

        git.add("testFile.txt");
        git.add("testFile2.txt");

        // Confirm index has both files
        assertEquals(Utils.readFile(".gitproject/index"),
                "testFile2.txt : 7e3c2056ff8f7039e116baac1fc70505cb654578\ntestFile.txt : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51");

        git.remove("testFile.txt");

        // Confirm testFile.txt has been removed from index
        assertEquals(Utils.readFile(".gitproject/index"), "testFile2.txt : 7e3c2056ff8f7039e116baac1fc70505cb654578");

        // Confirm the blob file still exists
        assertTrue(Utils.exists(".gitproject/objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

        git.remove("testFile2.txt");

        // Confirm testFile2.txt has been removed from index
        assertEquals(Utils.readFile(".gitproject/index"), "");

        // Confirm the blob file still exists
        assertTrue(Utils.exists(".gitproject/objects/7e3c2056ff8f7039e116baac1fc70505cb654578"));

        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteFile(".gitproject/index");
    }
}
