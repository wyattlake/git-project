public class App {
    public static void main(String[] args) throws Exception {
        Git git = new Git("test-project");
        git.init();
        git.add("Main.java");
        git.add("test.txt");
        git.remove("Main.java");
        git.add("folder/folder.txt");
        // index.removeBlob("test-project/Main.java");
    }
}