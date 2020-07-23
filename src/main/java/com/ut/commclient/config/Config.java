package com.ut.commclient.config;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {
    public final static Map configMap = initConfig();
    public final static String recPath = initRecPath();
    public final static String starterPath = initStarterPath();
    public final static String taskPath = initTaskPath();

    private static Map initConfig() {
        try {
            YamlReader reader = new YamlReader(new FileReader("src\\main\\resources\\application.yml"));
            List<Map> configList = new ArrayList<>();
            while (true) {
                Map config = (Map) reader.read();
                if (config == null) break;
                configList.add(config);
            }

            String active = (String) ((Map) ((Map) configList.get(0).get("spring")).get("profiles")).get("active");
            int mapNum = 0;
            switch (active) {
                case "dev":
                    mapNum = 1;
                    break;
                case "pro":
                    mapNum = 2;
                    break;
                case "test":
                    mapNum = 3;
                default:
                    break;
            }
            return configList.get(mapNum);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String initRecPath() {
        return (String) configMap.get("recPath");
    }

    private static String initStarterPath() {
        return (String) configMap.get("starterPath");
    }

    private static String initTaskPath() {
        return (String) configMap.get("taskPath");
    }


}
