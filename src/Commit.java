import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class Commit {
    String author;
    String summary;
    String parent;
    String next;
    String treeSha;
    public Commit(Optional<String> parent, Optional<String> next, String author, String summary){
        this.author = author;
        this.summary = summary;
        if(parent.isPresent())this.parent = parent.get();else{this.parent = "";}
        if(next.isPresent())this.next = next.get();else{this.next = "";}
        treeSha= makeTree();
    }

    public void writeFile() throws NoSuchAlgorithmException, IOException{
        FileWriter fw = new FileWriter("./objects/"+getSHA());
        fw.write(treeSha+"\n");
        fw.write(parent+"\n");
        fw.write(next+"\n");
        fw.write(author+"\n");
        fw.write(getDate()+"\n");
        fw.write(summary);
        fw.close();
    }

    public String getSHA() throws NoSuchAlgorithmException{
        StringBuilder bob = new StringBuilder();
        bob.append(summary+"\n");
        bob.append(getDate()+"\n");
        bob.append(author+"\n");
        bob.append(treeSha+"\n");
        bob.append(parent);
        return hashString(bob.toString().getBytes());
    }

    public String getDate(){
        return ""+java.time.LocalDate.now();
    }

    private String makeTree(){
        Tree tree = new Tree();
        new File("./objects/"+tree.getSHA1());
        return tree.getSHA1();
    }

    private String hashString(byte[] byteArray) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = md.digest(byteArray);
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashString = no.toString(16);

        // Add preceding 0s to make it 32 bit
        while (hashString.length() < 32) {
            hashString = "0" + hashString;
        }

        // return the HashText
        return hashString;
    }
}
