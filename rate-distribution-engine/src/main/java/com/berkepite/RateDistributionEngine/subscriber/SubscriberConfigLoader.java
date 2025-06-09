package com.berkepite.RateDistributionEngine.subscriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Helper class for loading subscriber configuration files in YAML format.
 */
public class SubscriberConfigLoader {

    /**
     * Loads the given YAML configuration file and returns it as an instance of the specified class.
     *
     * @param configName the name of the configuration file to load (e.g., "rest-config.yaml").
     * @param clazz      the class type to map the YAML content to.
     * @return an instance of {@link ISubscriberConfig} loaded from the YAML file.
     * @throws Exception if the file cannot be found or read.
     */
    public static ISubscriberConfig load(String configName, Class<?> clazz) throws Exception {
        Yaml yaml = new Yaml(new Constructor(clazz, new LoaderOptions()));

        String jarDir = "subscribers/";

        // Construct the absolute path to the YAML file
        File configFile = new File(jarDir, configName);

        InputStream inputStream = new FileInputStream(configFile);
        return yaml.load(inputStream);
    }
}
