
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
}
