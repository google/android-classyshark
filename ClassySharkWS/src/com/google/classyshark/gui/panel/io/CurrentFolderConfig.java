/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.gui.panel.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 * Class that manages ClassyShark config
 */
public enum CurrentFolderConfig {
    INSTANCE;

    private static final String CLASSYSHARK_PROPERTIES = "classyshark.properties";
    private static final String CURRENT_FOLDER = "CURRENT_FOLDER";

    public void setCurrentDirectory(File path) {
        String curDir = path.getAbsolutePath();
        try {
            File configFile = new File(CLASSYSHARK_PROPERTIES);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            Properties props = new Properties();
            props.setProperty(CURRENT_FOLDER, curDir);
            FileWriter writer = new FileWriter(configFile);
            props.store(writer, CURRENT_FOLDER + "wrote");
            writer.close();
        } catch (Exception ex) {
        }
    }

    public File getCurrentDirectory() {
        try {
            File configFile = new File(CLASSYSHARK_PROPERTIES);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            String result = props.getProperty(CURRENT_FOLDER);
            reader.close();
            return new File(result);

        } catch (Exception ex) {
        }
        return new File(System.getProperty("user.home"));
    }
}
