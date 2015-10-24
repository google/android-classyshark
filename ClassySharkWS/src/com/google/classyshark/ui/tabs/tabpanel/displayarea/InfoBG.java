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

package com.google.classyshark.ui.tabs.tabpanel.displayarea;

/**
 *  the info page
 */
public class InfoBG {

    public static final String INFO =
            "\n\n\t\t\tClassyShark ver. 2.8"
                    + "\n\t            ========================"
                    + "\n\n\t\t\tMOUSE USAGE"
                    + "\n\t            ==============="
                    + "\t\t\t\n1. Open open archive (dex, jar, apk, class)"
                    + "\t\t\t\n2. ==> opens top most file"
                    + "\t\t\t\n3. <== goes back to all files in archive"
                    + "\t\t\t\n4. Double click on class name opens class"
                    + "\t\t\t\n5. Double click on import opens class, \nif the class is part of the "
                    + "loaded archive"
                    + "\t\t\t\n6. Mouse selection for command line removes the selected text"
                    + "\t\t\t\n7. Mouse selection for text shows pop up to copy/paste"
                    + "\n\n\t\t\tKEYBOARD USAGE"
                    + "\n\t            =================="
                    + "\t\t\t\n1. Can pass archives as command line arguments"
                    + "\t\t\t\n2. Press TAB twice to gain focus on the command line "
                    + "(starts to blink)"
                    + "\t\t\t\n3. Press any key to open an archive (dex, apk, jar, class)"
                    + "\t\t\t\n4. Type any interesting class name in the command line"
                    + "\t\t\t\n5. Press -> (right arrow) to see the top most class"
                    + "\t\t\t\n6. Press <- (left arrow) to open another archive"
                    + "\t\t\t\n7. Type CAPs for camel search (AM == ActivityManager)"
                    + "\t\t\t\n8. Press CTRL + s to save java file"
                    + "\t\t\t\n9. (Optional) Press CTRL + 1..4 to move between the tabs";
}
