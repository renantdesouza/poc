import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DocumentAccess {

    private static final String PATH = "/home/renan.souza/doc-" + Quickstart.MODE;

    public synchronized static List<String> scannedValues() {
        Scanner scan = getScan();
        if (scan == null) {
            return null;
        }

        List<String> strs = new ArrayList<>();
        int i = 0;
        for (; scan.hasNext(); i++) {
            strs.add(scan.nextLine());
            scan.reset();
        }
        return i == 0 ? null : strs;
    }

    private static Scanner getScan() {
        try {
            return new Scanner(new File(PATH));
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return null;
        }
    }

}