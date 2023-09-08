import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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

        compressAndWriteString(fileContent);
    }

    public void compressAndWriteString(String input) throws IOException {
        if (input == null || input.length() == 0) {
            return;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(input.getBytes("UTF-8"));
                gzip.close();
            }
            byte[] zippedByteArray = outputStream.toByteArray();

            this.hash = hashString(zippedByteArray);
            this.hashPath = this.projectDirectory + "/.gitproject/objects/" + hash;

            FileOutputStream fileOutput = new FileOutputStream(hashPath);
            fileOutput.write(zippedByteArray, 0, zippedByteArray.length);
            fileOutput.close();
        } catch (Exception exception) {
            throw exception;
        }
    }

    public String getHash() {
        return hash;
    }

    protected void deleteFile() throws Exception {
        Files.deleteIfExists(Paths.get(hashPath));
    }

    protected String hashString(byte[] byteArray) {
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
