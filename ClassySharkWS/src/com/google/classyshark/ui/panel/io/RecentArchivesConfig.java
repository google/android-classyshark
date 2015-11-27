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

package com.google.classyshark.ui.panel.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Class that manages recent archives config
 */
public enum RecentArchivesConfig {
    INSTANCE;

    private static final String CLASSYSHARK_RECENTS = "classyshark_recents.properties";
    private static final String UPDATE_ARCHIVE = "CURRENT_FOLDER";

    public void addArchive(String name, File currentDirectory) {
        try {
            File configFile = new File(CLASSYSHARK_RECENTS);

            Properties props = new Properties();

            if (!configFile.exists()) {
                configFile.createNewFile();
            } else {
                configFile = new File(CLASSYSHARK_RECENTS);
                FileReader reader = new FileReader(configFile);
                props.load(reader);
            }

            props.setProperty(name, currentDirectory.getAbsolutePath());
            FileWriter writer = new FileWriter(configFile);
            props.store(writer, UPDATE_ARCHIVE + "wrote");
            writer.close();

        } catch (Exception ex) {
        }
    }

    public void clear() {
        try {
            File configFile = new File(CLASSYSHARK_RECENTS);

            Properties props = new Properties();

            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            FileWriter writer = new FileWriter(configFile);
            props.store(writer, UPDATE_ARCHIVE + "wrote");
            writer.close();

        } catch (Exception ex) {
        }
    }

    public List<String> getRecentArchiveNames() {
        List<String> result = new ArrayList<>();

        try {
            File configFile = new File(CLASSYSHARK_RECENTS);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            Enumeration e = props.keys();

            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                result.add(key);
            }

            Collections.sort(result);
            reader.close();

        } catch (Exception ex) {
        }

        return result;
    }

    public String getFilePath(String name) {
        String result;

        try {
            File configFile = new File(CLASSYSHARK_RECENTS);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);

            result = props.getProperty(name);

            reader.close();

        } catch (Exception ex) {
            result = "";
        }

        return result;
    }
}

