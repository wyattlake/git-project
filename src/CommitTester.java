import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

public class CommitTester {
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        Files.deleteIfExists(Paths.get("./objects/"));
    }

    @Test
    @DisplayName("Test the sha generation")
    public void testSha() throws Exception{
        Tree t = new Tree();
        Commit com = new Commit(Optional.empty(),"","");
        String toHash = "\n"+com.getDate()+"\n\n"+t.getSHA1()+"\n";
        String testString = com.hashString(toHash.getBytes());
        assertEquals(testString, com.getSHA());
    }
    
    @Test
    @DisplayName("Tests the write to file")
    public void testWrite() throws Exception{
        Commit com = new Commit(Optional.empty(),"David","This is a commit");
        com.writeFile();
        File file = new File("./objects/"+com.getSHA());
        assertTrue(file.exists());
        Tree t = new Tree();
        String str = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        while(br.ready()){
            str+=br.readLine()+"\n";
        }
        br.close();
        String check = t.getSHA1()+"\n"+"\n\n"+"David\n"+com.getDate()+"\n"+"This is a commit\n";
        assertEquals(str,check);
    }

}
