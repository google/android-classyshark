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

import com.google.classyshark.silverghost.SilverGhostFacade;
import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ManifestInspector {

    private final File apkFile;

    public ManifestInspector(File apkFile) {
        this.apkFile = apkFile;
    }

    public List<String> getInspections() {

        String manifestStr = SilverGhostFacade.getManifest(apkFile);

        AndroidManifestPlainTextReader amptr =
                new AndroidManifestPlainTextReader(manifestStr);

        List<String> result = new LinkedList<>();

        // receivers with system actions
        Map<String, String> actions = amptr.getActionsWithReceivers();
        ReceiverActionsBL rabl = new ReceiverActionsBL(actions);
        result.addAll(rabl.getBGActionsList());

        for (String serviceName : amptr.getServices()) {
            Translator translator =
                    TranslatorFactory.createTranslator(serviceName, apkFile);
            translator.apply();
            String strRep = translator.toString();
            inspectService(result, serviceName, strRep);
        }

        return result;
    }

    private void inspectService(List<String> serviceInspections, String serviceName, String serviceCode) {
        // base services that should be ok
        if (serviceCode.contains("JobService") ||
                serviceCode.contains("GcmTaskService") ||
                serviceCode.contains("FirebaseInstanceIdService") ||
                serviceCode.contains("FirebaseMessagingService")) {
            return;
        }

        // IntentService - verify are not running when the app is down
        if (serviceCode.contains("IntentService")) {
            serviceInspections.add("* " + serviceName +
                    " extends IntentService, make sure it doesn't run " +
                    "when the app is down\n");

            return;
        }

        // all services need to be reviewed
        if (serviceCode.contains("Service")) {
            serviceInspections.add("* please check that " + serviceName +
                    " is not a background service \n");
        }
    }
}
