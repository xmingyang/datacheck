package org.datacheck;



import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;


public class PropHelper {

    private static Configuration config;

    static {
        try {
            config = new PropertiesConfiguration("conf/config.properties");
            
     //       ((AbstractFileConfiguration) config).setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValue(String key){
        return config.getString(key);
    }

    public static String[] getStringsValue(String key){
        return config.getStringArray(key);
    }

    public static int getIntegerValue(String key){
        return config.getInt(key);
    }




    public static void main(String[] args) {

        System.out.println(PropHelper.getStringValue("cmd"));


    }
}
