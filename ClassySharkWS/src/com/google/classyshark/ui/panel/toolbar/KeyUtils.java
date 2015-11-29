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

package com.google.classyshark.ui.panel.toolbar;

import java.awt.event.KeyEvent;

/**
 * Utility methods for handling input keys
 */
public class KeyUtils {

    private KeyUtils() {
    }

    public static String processKeyPressWithTypedText(KeyEvent e, String text) {
        int code = e.getKeyCode();
        String result = text;

        // delete
        if (code == 8) {
            if (!text.isEmpty()) {
                result = text.substring(0, text.length() - 1);
            }
        } else {
            // TODO handle only letters
            result += e.getKeyChar();
        }
        return result;
    }

    public static boolean isLeftArrowPressed(KeyEvent e) {
        return (e.getKeyCode() == 37);
    }

    public static boolean isRightArrowPressed(KeyEvent e) {
        return (e.getKeyCode() == 39);
    }

}
