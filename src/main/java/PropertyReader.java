import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertyReader {

    private Properties props = new Properties();

    public PropertyReader(String fileName) {
        try {
            props.load(new FileInputStream(new File(fileName)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object prop(Object obj) {
        return props.get(obj);
    }

}
