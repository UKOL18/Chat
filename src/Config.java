import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Карен on 18.04.2017.
 */
public class Config {
    private static final String PROPERTIES_FILE = "./chat.properties";

    public static int PORT;
    public static String ADDRESS;


    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;

        try {
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);

            PORT = Integer.parseInt(properties.getProperty("PORT"));
            ADDRESS = properties.getProperty("ADDRESS");
        } catch (FileNotFoundException ex) {
            System.err.println("Properties not found" + ex);
        } catch (IOException ex) {
            System.err.println("Error while reading file");
        } finally {
            try {
                propertiesFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
