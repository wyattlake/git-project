public class App {
    public static void main(String[] args) throws Exception {
        Git git = new Git("test-project");
        git.init();
        git.add("test.txt");
        // index.removeBlob("test-project/Main.java");
    }
}