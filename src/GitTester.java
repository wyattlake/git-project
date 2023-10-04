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
                git.addFile("testFile.txt");

                // Confirm blob file has been created in objects with the correct hash
                assertTrue(Utils.exists(".gitproject/objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

                // Confirm index has been updated
                assertEquals(Utils.readFile(".gitproject/index"),
                                "blob : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51 : testFile.txt");

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

                git.addFile("testFile.txt");
                git.addFile("testFile2.txt");

                // Confirm index has both files
                assertEquals(Utils.readFile(".gitproject/index"),
                                "blob : 7e3c2056ff8f7039e116baac1fc70505cb654578 : testFile2.txt\nblob : 5d6dceb0452f8a0eb37d5bc089984fc05e49fa51 : testFile.txt");

                git.removeFile("testFile.txt");

                // Confirm testFile.txt has been removed from index
                assertEquals(Utils.readFile(".gitproject/index"),
                                "blob : 7e3c2056ff8f7039e116baac1fc70505cb654578 : testFile2.txt");

                // Confirm the blob file still exists
                assertTrue(Utils.exists(".gitproject/objects/5d6dceb0452f8a0eb37d5bc089984fc05e49fa51"));

                git.removeFile("testFile2.txt");

                // Confirm testFile2.txt has been removed from index
                assertEquals(Utils.readFile(".gitproject/index"), "");

                // Confirm the blob file still exists
                assertTrue(Utils.exists(".gitproject/objects/7e3c2056ff8f7039e116baac1fc70505cb654578"));

                Utils.deleteDirectory(".gitproject/objects");
                Utils.deleteFile(".gitproject/index");
        }

        @Test
        @DisplayName("Verify add works correctly with files and folders")
        void testAdd() throws Exception {
                // Creating a project folder so JUnit files aren't added to the index
                Git git = new Git("project");

                Utils.writeFile("project/file1.txt", "file1");
                Utils.writeFile("project/folder1/folder2/file2.txt", "file2");

                git.init();
                git.add();

                assertEquals(Utils.readFile("project/.gitproject/index"),
                                "blob : 4a0a81eb2fc662e554e9bc711c44e3caa424fca8 : file1.txt\n" + //
                                                "tree : fb5272f8669faa5a21818f72323ff4433184d45e : folder1");

                assertEquals("tree : faa3516b4644211485496cd5a9b3d99f7bf64025 : folder2",
                                Utils.unzipFile("project/.gitproject/objects/fb5272f8669faa5a21818f72323ff4433184d45e"));

                assertEquals("blob : cf38160f02777e57fa8436d860f94caa4c7587d3 : file2.txt",
                                Utils.unzipFile("project/.gitproject/objects/faa3516b4644211485496cd5a9b3d99f7bf64025"));

                Utils.deleteDirectory("project/.gitproject");
        }
}
