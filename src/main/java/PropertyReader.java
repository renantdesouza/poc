import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

public class PropertyReader {

    private Properties props = new Properties();

    public PropertyReader(String fileName) {
        try {
            URL url = PropertyReader.class.getResource(fileName);
            if (url == null) {
                return;
            }
            props.load(new FileInputStream(new File(url.getPath())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object prop(Object obj) {
        return props.get(obj);
    }

    public String stringProp(String name) {
        try {
            return (String) prop(name);
        } catch (ClassCastException e) {
            return null;
        }
    }

}
