package orhestra.createVmTab.util;

import orhestra.createVmTab.config.Configuration;
import orhestra.createVmTab.config.General;
import orhestra.createVmTab.config.Image;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class IniLoader {
    private static final String GENERAL = "GENERAL";
    private static final String IMAGE_DEPLOY = "IMAGE_DEPLOY";

    private final Ini ini;

    public IniLoader(String fileName) throws IOException {
        ini = new Ini(new File(fileName));
    }

    public Configuration getConfiguration() {
        Configuration config = new Configuration();
        if (ini.containsKey(GENERAL)) {
            config.setGeneralConf(new General(ini.get(GENERAL)));
        }
        if (ini.containsKey(IMAGE_DEPLOY)) {
            config.setImageConf(new Image(ini.get(IMAGE_DEPLOY)));
        }

        return config;
    }
}

