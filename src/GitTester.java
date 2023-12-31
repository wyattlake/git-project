import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GitTester {
    @BeforeEach
    void deleteEverything() throws IOException {
        Utils.deleteDirectory(".gitproject");
        Utils.deleteDirectory("project");
    }

    @Test
    @DisplayName("Verify init creates index file and objects folder")
    void testInit() throws Exception {
        Git git = new Git();
        git.init();

        assertTrue(Utils.exists("objects"));
        assertTrue(Utils.exists("index"));

        Utils.deleteDirectory("objects");
        Utils.deleteFile("index");
    }

    @Test
    @DisplayName("Verify blob creates blobs and adds them to index")
    void testBlob() throws Exception {
        Git git = new Git();

        // Because blob is designed to create the blob and update the index, this test
        // tests both of those functionalities

        git.init();

        Utils.writeFile("testFile.txt", "This is a test file.");
        git.addFile("testFile.txt");

        // Confirm blob file has been created in objects with the correct hash
        assertTrue(Utils.exists("objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

        // Confirm index has been updated
        assertEquals(Utils.readFile("index"),
                "blob : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51 : testFile.txt");

        Utils.deleteDirectory("objects");
        Utils.deleteFile("index");
    }

    @Test
    @DisplayName("Verify remove correctly updates index")
    void testRemove() throws Exception {
        Git git = new Git();
        git.init();

        Utils.writeFile("testFile.txt", "This is a test file.");
        Utils.writeFile("testFile2.txt", "This is another test file.");

        git.addFile("testFile.txt");
        git.addFile("testFile2.txt");

        // Confirm index has both files
        assertEquals(Utils.readFile("index"),
                "blob : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51 : testFile.txt\nblob : 7e3c2056ff8f7039e116baac1fc70505cb654578 : testFile2.txt");

        git.removeFile("testFile.txt");

        // Confirm testFile.txt has been removed from index
        assertEquals(Utils.readFile("index"),
                "blob : 7e3c2056ff8f7039e116baac1fc70505cb654578 : testFile2.txt");

        // Confirm the blob file still exists
        assertTrue(Utils.exists("objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

        git.removeFile("testFile2.txt");

        // Confirm testFile2.txt has been removed from index
        assertEquals(Utils.readFile("index"), "");

        // Confirm the blob file still exists
        assertTrue(Utils.exists("objects/7e3c2056ff8f7039e116baac1fc70505cb654578"));

        Utils.deleteDirectory("objects");
        Utils.deleteFile("index");
    }

    @Test
    @DisplayName("Verify add works correctly with files and folders")
    void testAdd() throws Exception {
        // Creating a project folder so JUnit files aren't added to the index
        Git git = new Git("project");

        Utils.writeFile("project/file1.txt", "file1");
        Utils.writeFile("project/folder1/folder2/file2.txt", "file2");

        git.init();
        git.addFile("file1.txt");
        git.addDirectory("folder1");

        assertEquals(Utils.readFile("project/index"),
                "blob : 4a0a81eb2fc662e554e9bc711c44e3caa424fca8 : file1.txt\n" + //
                        "tree : fb5272f8669faa5a21818f72323ff4433184d45e : folder1");

        assertEquals("tree : faa3516b4644211485496cd5a9b3d99f7bf64025 : folder2",
                Utils.unzipFile("project/objects/fb5272f8669faa5a21818f72323ff4433184d45e"));

        assertEquals("blob : cf38160f02777e57fa8436d860f94caa4c7587d3 : file2.txt",
                Utils.unzipFile("project/objects/faa3516b4644211485496cd5a9b3d99f7bf64025"));

        Utils.deleteDirectory("project/.gitproject");
    }

    @Test
    void testCheckout() throws Exception {
        Utils.deleteDirectory("objects");
        Utils.deleteDirectory("index");
        Utils.deleteDirectory("HEAD");

        Utils.writeFile("project/file1.txt", "this is some text");
        Utils.writeFile("project/file2.txt", "this is some more text");

        Git git = new Git("project");
        git.init();

        git.addFile("file1.txt");
        git.addFile("file2.txt");

        Commit c1 = new Commit("Wyatt", "c1", "project");

        Utils.writeFile("project/folder1/file3.txt", "f3");
        Utils.writeFile("project/folder1/file4.txt", "f4");
        Utils.writeFile("project/file1.txt", "f1");

        git.addDirectory("folder1");
        git.editFile("file1.txt");
        git.deleteFile("file2.txt");

        Commit c2 = new Commit("Wyatt", "c2", "project");

        Utils.writeFile("project/folder1/file5.txt", "f5");

        git.addFile("folder1/file5.txt");

        Commit c3 = new Commit("Wyatt", "c3", "project");

        git.checkout(c1.getHash());

        // Confirming only files from first commit exist and have correct contents
        assertEquals(Utils.readFile("project/file1.txt"), "this is some text");
        assertEquals(Utils.readFile("project/file2.txt"), "this is some more text");
        assertFalse(Utils.exists("project/folder1"));

        git.checkout(c2.getHash());

        // Confirming file tree matches second commit
        assertEquals(Utils.readFile("project/file1.txt"), "f1");
        assertEquals(Utils.readFile("project/folder1/file3.txt"), "f3");
        assertEquals(Utils.readFile("project/folder1/file4.txt"), "f4");
        assertFalse(Utils.exists("project/file2.txt"));

        git.checkout(c3.getHash());

        // Confirming file tree matches third commit (this commit's tree should link
        // back to the second commit's tree)
        assertEquals(Utils.readFile("project/file1.txt"), "f1");
        assertEquals(Utils.readFile("project/folder1/file3.txt"), "f3");
        assertEquals(Utils.readFile("project/folder1/file4.txt"), "f4");
        assertEquals(Utils.readFile("project/folder1/file5.txt"), "f5");
        assertFalse(Utils.exists("project/file2.txt"));
    }
}
