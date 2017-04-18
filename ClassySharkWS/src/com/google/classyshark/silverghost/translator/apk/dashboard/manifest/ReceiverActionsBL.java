/*
 * Copyright 2017 Google, Inc.
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

package com.google.classyshark.silverghost.translator.apk.dashboard.manifest;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReceiverActionsBL {


    // https://developer.android.com/preview/features/background-broadcasts.html

    static List<String> approvedActions = Arrays.asList(
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.USER_INITIALIZE",
            "android.intent.action.USER_ADDED",
            "android.intent.action.USER_REMOVED",
            "android.intent.action.TIMEZONE_CHANGED",
            "android.intent.action.TIME_SET",
            "android.intent.action.LOCALE_CHANGED",
            "android.hardware.usb.action.USB_ACCESSORY_ATTACHED",
            "android.hardware.usb.action.USB_ACCESSORY_DETACHED",
            "android.hardware.usb.action.USB_DEVICE_ATTACHED",
            "android.hardware.usb.action.USB_DEVICE_DETACHED",
            "android.accounts.LOGIN_ACCOUNTS_CHANGED",
            "android.intent.action.DEVICE_STORAGE_LOW",
            "android.intent.action.DEVICE_STORAGE_OK",
            "android.intent.action.PACKAGE_DATA_CLEARED",
            "android.intent.action.PACKAGE_FULLY_REMOVED",
            "android.intent.action.NEW_OUTGOING_CALL",
            "android.intent.action.HEADSET_PLUG",
            "android.intent.action.EVENT_REMINDER",
            "android.hardware.usb.action.USB_ACCESSORY_ATTACHED",
            "android.hardware.usb.action.USB_ACCESSORY_DETACHED",
            "android.hardware.usb.action.USB_DEVICE_ATTACHED",
            "android.hardware.usb.action.USB_DEVICE_DETACHED",
            "android.app.action.DEVICE_OWNER_CHANGED",
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED",
            "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED"
    );

    private final Map<String, String> bgActionsToReceivers;


    public ReceiverActionsBL(Map<String, String> actionsToReceivers) {
        this.bgActionsToReceivers = filterBGActions(actionsToReceivers);
    }

    public List<String> getBGActionsList() {
        List<String> result = new LinkedList<>();

        for (Map.Entry<String, String> entry : bgActionsToReceivers.entrySet()) {
            result.add(entry.getKey() + " ==> " + entry.getValue());
        }
        return result;
    }

    private Map<String, String> filterBGActions(Map<String, String> actions) {
        TreeMap<String, String> result = new TreeMap<>();

        for (Map.Entry<String, String> entry : actions.entrySet()) {
            if (!approvedActions.contains(entry.getKey())) {
                if (entry.getKey().startsWith("com.google.") ||
                        entry.getKey().startsWith("android.")) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
}