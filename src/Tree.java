import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tree {

    public Tree(){
        if(!Files.exists(Paths.get("./objects/"))){
            new File("./objects/").mkdirs();
        }
    }
    public String getSHA1() {
        return "9a6747fc6259aa374ab4e1bb03074b6ec672cf99";
    }
}
