public class App {
    public static void main(String[] args) throws Exception {
        Git git = new Git("test-project");
        git.init();
        git.add("test.txt");
        git.add("test2.txt");
        git.remove("test.txt");
        git.remove("test2.txt");
    }
}