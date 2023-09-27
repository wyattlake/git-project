import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    /**
     * Computes the SHA1 Hash of a string.
     * 
     * @param input
     * @return Hashed string.
     * @throws NoSuchAlgorithmException
     */
    public static String hashString(String input) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = crypt.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashString = no.toString(16);

        // Add preceding 0s to make it 32 bit
        while (hashString.length() < 40) {
            hashString = "0" + hashString;
        }

        // return the HashText
        return hashString;
    }

    /**
     * Writes some content to a file.
     * 
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeFile(String path, String content) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            createFile(path);
        }

        FileWriter writer = new FileWriter(path, false);
        writer.write(content);
        writer.close();
    }

    /**
     * Reads a file to a string.
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    /**
     * Returns true if a path exists.
     * 
     * @param path
     * @return
     */
    public static boolean exists(String path) {
        return new File(path).exists();
    }

    /**
     * Deletes a directory and its contents.
     * 
     * @param path
     * @return
     * @throws IOException
     */
    public static boolean deleteDirectory(String path) throws IOException {
        File directory = new File(path);
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (File file : directoryFiles) {
                deleteDirectory(file.getPath());
            }
        }
        return directory.delete();
    }

    /**
     * Deletes a file at a given path
     * 
     * @param path
     * @throws IOException
     */
    public static void deleteFile(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    /**
     * Creates a new file.
     * 
     * @param path
     * @throws IOException
     */
    public static void createFile(String path) throws IOException {
        Path pathObject = Paths.get(path);

        if (pathObject.getParent() != null) {
            Files.createDirectories(pathObject.getParent());
        }

        Files.createFile(pathObject);
    }

    /**
     * Zips and hashes file contents before saving to a file named after the hash of
     * the file contents.
     * 
     * @param contents
     * @param path
     * @return Hash of file contents.
     * @throws Exception
     */
    public static String zipAndHashFile(String path, String content) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(content.getBytes("UTF-8"));
        }
        byte[] zippedByteArray = outputStream.toByteArray();

        String hash = Utils.hashString(outputStream.toString());
        String hashPath = path + hash;

        FileOutputStream fileOutput = new FileOutputStream(hashPath);
        fileOutput.write(zippedByteArray, 0, zippedByteArray.length);
        fileOutput.close();

        return hash;
    }

    /**
     * Unzips a file to a string.
     * 
     * @param path
     * @return Unzipped file as a string.
     * @throws Exception
     */
    public static String unzipFile(String path) throws Exception {
        try (GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(new File(path)));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            return outputStream.toString();
        } catch (Exception exception) {
            throw exception;
        }
    }
}
