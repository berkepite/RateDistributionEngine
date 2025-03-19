package com.berkepite.RateDistributionEngine.BloombergRestSubscriber;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class BloombergRestConfigLoader {
    public BloombergRestConfig load() {
        Yaml yaml = new Yaml(new Constructor(BloombergRestConfig.class, new LoaderOptions()));

        try {
            // Get the directory of the current JAR file
            String jarDir = getJarDirectory();

            // Construct the absolute path to the YAML file
            File configFile = new File(jarDir, "bloomberg_rest-config.yaml");

            try (InputStream inputStream = new FileInputStream(configFile)) {
                return yaml.load(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getJarDirectory() throws Exception {
        // Get the location of the JAR that contains this class
        String jarPath = new File(BloombergRestConfigLoader.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent();
        return jarPath; // Return the parent directory where the JAR is located
    }
}
