package com.berkepite.RateDistributionEngine.subscriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.berkepite.RateDistributionEngine.common.subscriber.ISubscriberConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class SubscriberConfigLoader {
    public static ISubscriberConfig load(String configName, Class<?> clazz) throws Exception {
        Yaml yaml = new Yaml(new Constructor(clazz, new LoaderOptions()));

        String jarDir = "subscribers/";

        // Construct the absolute path to the YAML file
        File configFile = new File(jarDir, configName);

        InputStream inputStream = new FileInputStream(configFile);
        return yaml.load(inputStream);
    }
}
