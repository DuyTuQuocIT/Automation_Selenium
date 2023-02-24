package com.selenium.helper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class GeneralHelper {
    static String filePath = "./src/test/conf/retry_info.properties";

    public static void writeDataIntoPropertyFile(HashMap<String,String> data) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
        File file = new File(filePath);
        file.createNewFile();
        Properties prop = new Properties();
        try(InputStream in = new FileInputStream(file)) {
            prop.load(in);
            for (HashMap.Entry<String, String> d : data.entrySet()) {
                prop.setProperty(d.getKey(), d.getValue());
            }
            OutputStream out = new FileOutputStream(file);
            prop.store(out, "Written by: Duy Tu - Number of TCs failed before/after retry");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        printProperties(prop);
    }

    public static void printProperties(Properties prop)
    {
        prop.stringPropertyNames().stream()
                .map(key -> key + ":" + prop.getProperty(key))
                .forEach(System.out::println);
    }
}
