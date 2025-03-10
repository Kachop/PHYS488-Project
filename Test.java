import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        double[] position = {0, 0, 0};
        //Scan scan = new Scan(position, "/home/robertbatty3009/Development/Java/PHYS488/Project_Advanced_V2/test/");
        //Scan scan = new Scan(position, "C:\\Users\\sgrbatty\\Google Drive\\Year 3\\PHYS488\\Project_Advanced_V2\\test");
        Scan scan = new Scan(position, "C:\\Users\\sgrbatty\\Google Drive\\Year 3\\PHYS488\\Project_Advanced_V2\\brain_test");
        //scan.findTumour();
        //scan.writeToDisk("cancer_test.csv");
        System.out.println("Done");
    }
}
