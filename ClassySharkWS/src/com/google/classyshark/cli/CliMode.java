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

package com.google.classyshark.cli;

import com.google.classyshark.updater.UpdateManager;

import java.io.File;
import java.util.List;

import static com.google.classyshark.silverghost.SilverGhostFacade.exportArchive;
import static com.google.classyshark.silverghost.SilverGhostFacade.exportClassFromApk;
import static com.google.classyshark.silverghost.SilverGhostFacade.inspectApk;
import static com.google.classyshark.silverghost.SilverGhostFacade.inspectPackages;

/**
 * Command line mode
 */
public class CliMode {

    private static final String ERROR_MESSAGE = "Usage: java -jar ClassyShark.jar [-options] <archive> [args...]\n" +
            "           (to execute a ClassyShark on binary archive jar/apk/dex/class)\n" +
            "where options include:\n" +
            "    -open\t  open an archive with GUI \n" +
            "    -export\t  export to file \n" +
            "    -methodcounts\t  packages with method counts \n" +
            "    -inspect  experimental prints apk analysis\n" +
            "    -update\tupdates ClassyShark" +
            "\nwhere args is an optional classname\n";

    private CliMode() {
    }

    public static void with(List<String> args) {
        if (args.size() < 2) {
            System.err.println("missing command line arguments " + "\n\n\n" + ERROR_MESSAGE);
            return;
        }

        File archiveFile = new File(args.get(1));
        if (!archiveFile.exists()) {
            System.err.println("File doesn't exist ==> " + archiveFile + "\n\n\n" + ERROR_MESSAGE);
            return;
        }

        final String operand = args.get(0).toLowerCase();
        switch (operand) {
            case "-export":
                if (args.size() == 2) {
                    exportArchive(args);
                } else {
                    exportClassFromApk(args);
                }
                break;
            case "-inspect":
                inspectApk(args);
                break;
            case "-methodcounts":
                inspectPackages(args);
                break;
            case "-update":
                UpdateManager.getInstance().checkVersionConsole();
                break;
            default:
                System.err.println("wrong operand ==> " + operand + "\n\n\n" + ERROR_MESSAGE);
        }
    }
}