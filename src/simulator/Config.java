package simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
 
public class Config {
	
    private static final String FILE_CONFIG = ""
    		+ "/home/hieudv10/eclipse-workspace/simulator/src/resources/config.properties";
    private static Config instance = null;
    private Properties properties = new Properties();
 
    /**
     * Use singleton pattern to create ReadConfig object one time and use
     * anywhere
     *
     * @return instance of ReadConfig object
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            instance.readConfig();
        }
        return instance;
    }
 
    // Get property
    public int getAsInteger(String key) {
		String trimmer = this.getTrimmed(key);
		return Integer.parseInt(trimmer);
	}

	public long getAsLong(String key) {
		String trimmer = this.getTrimmed(key);
		return Long.parseLong(trimmer);
	}

	public double getAsDouble(String key) {
		String trimmer = this.getTrimmed(key);
		return Double.parseDouble(trimmer);
	}

	public float getAsFloat(String key) {
		String trimmer = this.getTrimmed(key);
		return Float.parseFloat(trimmer);
	}

	public boolean getAsBoolean(String key) {
		String valueString = this.getTrimmed(key);
		valueString = valueString.toLowerCase();
		if ("true".equals(valueString)) {
			return true;
		} else {
			return false;
		}
	}

	public String getAsString(String key) {
		return this.getTrimmed(key);
	}

	private String getTrimmed(String name) {
		String value = this.properties.getProperty(name);
		return value.trim();
	}
 
    // Read file properties
    private void readConfig() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(FILE_CONFIG);
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close objects
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
