package gorare.sys.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {
    private static Settings _this;
    private Settings(){}
    private Hashtable<String,String> lstSettings = new Hashtable<String, String>();
    public static Settings getInstance()
    {
        if (_this == null)
        {
            _this = new Settings();
        }
        return _this;
    }
    public String getSetting(String key)
    {
        if (lstSettings.size() == 0)
        {
            try {
                Properties p = load("settings.cfg");
                Enumeration en = p.keys();
                String k = null;
                while (en.hasMoreElements())
                {
                    k = (String) en.nextElement();
                    lstSettings.put(k, p.getProperty(k));
                }
                /*
                lstSettings.put("password", "beer");
                lstSettings.put("logdir", "/home/administrator/Desktop/go/goserver/globalops/logs/");*/
            } catch (Exception ex) {
                Logger.getLogger("cvg").severe("Unable to load settings.cfg");
                Logger.getLogger("cvg").severe("Terminating...");
                System.exit(2);
            }
                    /*
            lstSettings.put("password", "beer");
            lstSettings.put("logdir", "/home/administrator/Desktop/go/goserver/globalops/logs/");*/
        }
        return lstSettings.get(key);
    }
 /**
     * Load a properties file from the classpath
     * @param propsName
     * @return Properties
     * @throws Exception
     */
    private Properties load(String propsName) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(propsName));
        return props;
    }

    /**
     * Load a Properties File
     * @param propsFile
     * @return Properties
     * @throws IOException
     */
    private Properties load(File propsFile) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propsFile);
        props.load(fis);
        fis.close();
        return props;
    }

}