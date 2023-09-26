import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
    public static String hashString(String input) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        byte[] messageDigest = crypt.digest(input.getBytes());
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

    public static void writeFile(String path, String content) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            createFile(path);
        }

        FileWriter writer = new FileWriter(path, false);
        writer.write(content);
        writer.close();
    }

    public static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

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

    public static void deleteFile(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    public static void createFile(String path) throws IOException {
        Path pathObject = Paths.get(path);

        if (pathObject.getParent() != null) {
            Files.createDirectories(pathObject.getParent());
        }

        Files.createFile(pathObject);
    }
}
