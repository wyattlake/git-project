import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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

    public void compressAndWriteString(String input) throws Exception {
        if (input == null || input.length() == 0) {
            return;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(input.getBytes("UTF-8"));
        }
        byte[] zippedByteArray = outputStream.toByteArray();

        this.hash = Utils.hashString(outputStream.toString());
        this.hashPath = this.projectDirectory + ".gitproject/objects/" + hash;

        FileOutputStream fileOutput = new FileOutputStream(hashPath);
        fileOutput.write(zippedByteArray, 0, zippedByteArray.length);
        fileOutput.close();
    }

    public String getHash() {
        return hash;
    }

    /**
     * Deletes a file. Should not be used in the index code.
     * 
     * @throws Exception
     */
    protected void deleteFile() throws Exception {
        Files.deleteIfExists(Paths.get(hashPath));
    }
}
