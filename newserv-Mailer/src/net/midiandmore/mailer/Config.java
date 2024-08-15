/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.midiandmore.mailer;

import jakarta.json.Json;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the Mailer config
 * 
 * @author Andreas Pschorn
 */
public class Config {
    
    private NewservMailer mi;
    private Properties configFile;
    private Properties mailFile;
    private Properties templateFile;
    
    /**
     * Initiales the class
     * 
     * @param mi The NewservMailer class
     */
    protected Config(NewservMailer mi, String configFile) {
        setMi(mi);
        loadConfig(configFile);
        System.out.printf("Loaded config file:\n");
        loadMail("mail.json");
        System.out.printf("Loaded config mail:\n");
        loadTemplate("template.json");
        System.out.printf("Loaded templates:\n");
    }    

    /**
     * Loads the config file in the Properties
     */
    private void loadConfig(String configFile) {
        setConfigFile(loadDataFromJSONasProperties(configFile, "name", "value"));
    }

   /**
     * Loads the mail file in the Properties
     */
    private void loadTemplate(String configFile) {
        setTemplateFile(loadDataFromJSONasProperties(configFile, "id", "subject", "body"));
    }
    
   /**
     * Loads the mail file in the Properties
     */
    private void loadMail(String configFile) {
        setMailFile(loadDataFromJSONasProperties(configFile, "name", "value"));
    }
    
    /**
     * Loads the config data from a JSON file
     *
     * @param file The file
     * @param obj First element
     * @param obj2 Second element
     * @return The properties
     */
    protected Properties loadDataFromJSONasProperties(String file, String obj, String obj2) {
        var ar = new Properties();
        try {
            InputStream is = new FileInputStream(file);
            var rdr = Json.createReader(is);
            var results = rdr.readArray();
            var i = 0;
            for (var jsonValue : results) {
                var jobj = results.getJsonObject(i);
                ar.put(jobj.getString(obj), jobj.getString(obj2));
                i++;
            }
        } catch (Exception fne) {
            fne.printStackTrace();
        }
        return ar;
    }

    /**
     * Loads the config data from a JSON file
     *
     * @param file The file
     * @param obj First element
     * @param obj2 Second element
     * @param obj3 Third element
     * @return The properties
     */
    protected Properties loadDataFromJSONasProperties(String file, String obj, String obj2, String obj3) {
        var ar = new Properties();
        try {
            InputStream is = new FileInputStream(file);
            var rdr = Json.createReader(is);
            var results = rdr.readArray();
            var i = 0;
            for (var jsonValue : results) {
                var jobj = results.getJsonObject(i);
                var obj1 = new String[2];
                obj1[0] = jobj.getString(obj2);
                obj1[1] = jobj.getString(obj3);
                ar.put(jobj.getString(obj), obj1);
                i++;
            }
        } catch (Exception fne) {
            fne.printStackTrace();
        }
        return ar;
    }
    
    /**
     * @return the mi
     */
    public NewservMailer getMi() {
        return mi;
    }

    /**
     * @param mi the mi to set
     */
    public void setMi(NewservMailer mi) {
        this.mi = mi;
    }

    /**
     * @return the configFile
     */
    public Properties getConfigFile() {
        return configFile;
    }

    /**
     * @param configFile the configFile to set
     */
    public void setConfigFile(Properties configFile) {
        this.configFile = configFile;
    }

    /**
     * @return the mailFile
     */
    public Properties getMailFile() {
        return mailFile;
    }

    /**
     * @param mailFile the mailFile to set
     */
    public void setMailFile(Properties mailFile) {
        this.mailFile = mailFile;
    }

    /**
     * @return the templateFile
     */
    public Properties getTemplateFile() {
        return templateFile;
    }

    /**
     * @param templateFile the templateFile to set
     */
    public void setTemplateFile(Properties templateFile) {
        this.templateFile = templateFile;
    }
}