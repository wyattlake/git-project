import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.GZIPOutputStream;

public class Blob {
    protected String hash, projectDirectory, hashPath;

    protected MessageDigest md;

    /**
     * Creates a new blob
     * 
     * @param path Path to source file
     * @throws Exception
     */
    public Blob(String path, String projectDirectory) throws Exception {
        this.projectDirectory = projectDirectory;

        // Initialize the MessageDigest for hashing
        md = MessageDigest.getInstance("SHA-1");

        String fileContent = Files.readString(Paths.get(projectDirectory + path));

        this.hash = hashString(fileContent);
        this.hashPath = this.projectDirectory + "/.gitproject/objects/" + hash;

        compressAndWriteString(fileContent, hashPath);
    }

    public static void compressAndWriteString(String input, String hashPath) throws IOException {
        if (input == null || input.length() == 0) {
            return;
        }

        BufferedWriter writer = null;

        try {
            File file = new File(hashPath);
            GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(file));

            writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));

            writer.append(input);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public String getHash() {
        return hash;
    }

    protected void deleteFile() throws Exception {
        Files.deleteIfExists(Paths.get(hashPath));
    }

    protected String hashString(String input) {
        byte[] messageDigest = md.digest(input.getBytes());
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
