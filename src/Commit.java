import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Commit {
    protected String author, summary, parent, treeSha, date, child, hash;
    protected static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd");

    public Commit(String parent, String author, String summary) throws Exception {
        this.author = author;
        this.summary = summary;
        this.parent = parent;

        child = "";

        treeSha = createTree();
        date = getDate();

        hash = writeToFile();

        Utils.writeFile(".gitproject/HEAD", hash);
    }

    public Commit(String author, String summary) throws Exception {
        this("", author, summary);
    }

    private String writeToFile() throws Exception {
        StringBuilder builder = new StringBuilder(
                treeSha + "\n" + parent + "\n" + author + "\n" + date + "\n" + summary);

        // No reason to zip here because this hash is not of the entire file's contents.
        String unzippedHash = Utils.hashString(builder.toString());

        // Inserting the nextCommitHash after the second newline
        builder.insert(builder.indexOf("\n", builder.indexOf("\n") + 1), child + "\n");

        // Zipping to the location determined by the unzippedHash
        Utils.zipFile(".gitproject/objects/" + unzippedHash, builder.toString());

        return unzippedHash;
    }

    public static String getDate() {
        LocalDate localDate = LocalDate.now();
        return dtf.format(localDate);
    }

    public static String createTree() throws Exception {
        Tree tree = new Tree();
        return tree.writeToObjects();
    }

    public String hashString(byte[] byteArray) throws NoSuchAlgorithmException {
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
