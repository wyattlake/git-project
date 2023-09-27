
import static org.junit.Assert.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TreeTester {
    @BeforeAll
    static void setupBeforeClass() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Git git = new Git();
        git.init();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
    }

    @Test
    @DisplayName("Verify tree add method throws exceptions correctly")
    void testTreeAdd() throws Exception {
        Tree tree = new Tree();

        // Confirming Tree throws an exception when adding an improperly formatted
        // string
        assertThrows(Exception.class, () -> {
            tree.add("hello");
        });

        // Confirming Tree throws an exception when adding a blob without a filename
        assertThrows(Exception.class, () -> {
            tree.add("blob : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        });

        // Confirming Tree throws an exception when adding a tree without a filename
        assertThrows(Exception.class, () -> {
            tree.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b");
        });

        tree.add("blob : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b : file.txt");

        // Confirming Tree throws an exception when adding a duplicate file
        assertThrows(Exception.class, () -> {
            tree.add("blob : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b : file.txt");
        });

        tree.add("blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file2.txt");

        tree.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b : tree.txt");

        // Confirming Tree throws an exception when adding a duplicate tree
        assertThrows(Exception.class, () -> {
            tree.add("tree : bd1ccec139dead5ee0d8c3a0499b42a7d43ac44b : tree.txt");
        });

        tree.add("tree : a1f239cbcd40f722555acfc7d23be06dee9d815e : tree2.txt");
    }

    @Test
    @DisplayName("Verify tree remove method works")
    void testTreeRemove() throws Exception {
        Tree tree = new Tree();
        tree.add("tree : a1f239cbcd40f722555acfc7d23be06dee9d815e : tree.txt");
        tree.add("blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file.txt");

        // Confirming tree can remove a tree which has been added
        assertTrue(tree.remove("tree.txt"));

        // Confirming tree cannot remove a tree which has already been removed
        assertFalse(tree.remove("tree.txt"));

        // Confirming tree cannot remove a tree which has not been added
        assertFalse(tree.remove("tree2.txt"));

        // Confirming tree can remove a blob which has already been added
        assertTrue(tree.remove("file.txt"));
    }

    @Test
    @DisplayName("Verify tree saving works")
    void testTreeSaveToFile() throws Exception {
        Tree tree = new Tree();
        tree.add("tree : a1f239cbcd40f722555acfc7d23be06dee9d815e : tree.txt");
        tree.add("tree : 039c501ac8dfcac91c6f05601cee876e1cc07e17 : tree2.txt");
        tree.add("blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file1.txt");
        tree.add("blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file2.txt");
        tree.add("blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file3.txt");

        tree.writeToObjects();

        // Confirming the tree file has been saved correctly
        assertTrue(Utils.exists(".gitproject/objects/6a15ea192692596193c9e87bfb08d1b1425557a9"));

        // Confirming the tree file's contents are correct (Order of blobs and trees is
        // random due to hashmap)
        assertEquals(Utils.unzipFile(".gitproject/objects/6a15ea192692596193c9e87bfb08d1b1425557a9"),
                "blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file1.txt\n" + //
                        "blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file3.txt\n" + //
                        "blob : a64e2a4adcc4ae20e6e35babd2a181619cb8e224 : file2.txt\n" + //
                        "tree : 039c501ac8dfcac91c6f05601cee876e1cc07e17 : tree2.txt\n" + //
                        "tree : a1f239cbcd40f722555acfc7d23be06dee9d815e : tree.txt");
    }

    @Test
    @DisplayName("Verify adding a directory works in a simple case")
    void testAddDirectorySimple() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteDirectory("dir");

        Utils.writeFile("dir/file1.txt", "file1");
        Utils.writeFile("dir/file2.txt", "file2");
        Utils.writeFile("dir/file3.txt", "file3");

        Tree tree = new Tree();
        tree.addDirectory("dir");

        // Confirming the tree file has been saved to the correct place
        assertTrue(Utils.exists(".gitproject/objects/06226da7a066932629c0ef261765a1ba4ef674ad"));

        // Confirming the tree file has the correct contents
        assertEquals("blob : 4a0a81eb2fc662e554e9bc711c44e3caa424fca8 : file1.txt\n" + //
                "blob : 02ee912b44bc11cc17890b7e6f889471ca89a671 : file3.txt\n" + //
                "blob : cf38160f02777e57fa8436d860f94caa4c7587d3 : file2.txt",
                Utils.unzipFile(".gitproject/objects/06226da7a066932629c0ef261765a1ba4ef674ad"));
    }

    @Test
    @DisplayName("Verify adding a directory works with nested folders")
    void testAddDirectoryNested() throws Exception {
        Utils.deleteDirectory(".gitproject/objects");
        Utils.deleteDirectory("dir");

        Utils.writeFile("dir/file1.txt", "file1");
        Utils.writeFile("dir/file2.txt", "file2");
        Utils.writeFile("dir/file3.txt", "file3");

        Utils.createFolder("dir/subdir1");
        Utils.writeFile("dir/subdir2/file4.txt", "file4");

        Tree tree = new Tree();
        tree.addDirectory("dir");

        // Confirming the tree file has been saved to the correct place
        assertTrue(Utils.exists(".gitproject/objects/b9b17e90c93e4ed5933633d2859e9938459c30af"));

        // Confirming the tree file has the correct contents
        assertEquals("blob : 4a0a81eb2fc662e554e9bc711c44e3caa424fca8 : file1.txt\n" + //
                "blob : 02ee912b44bc11cc17890b7e6f889471ca89a671 : file3.txt\n" + //
                "blob : cf38160f02777e57fa8436d860f94caa4c7587d3 : file2.txt\n" + //
                "tree : 93f7e1782458d7015a24f8d8944d70198fd7414e : subdir2\n" + //
                "tree : 70246bde7d6bb9bdadc1a69206354b0e54afc709 : subdir1",
                Utils.unzipFile(".gitproject/objects/b9b17e90c93e4ed5933633d2859e9938459c30af"));

        // Confirming the first subtree file has been saved to the correct place
        assertTrue(Utils.exists(".gitproject/objects/b9b17e90c93e4ed5933633d2859e9938459c30af"));
    }
}
