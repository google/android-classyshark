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

        // IntentService
        for(String serviceName : amptr.getServices()) {
            Translator translator =
                    TranslatorFactory.createTranslator(serviceName, apkFile);
            translator.apply();
            String strRep = translator.toString();

            if(strRep.contains("IntentService")) {
                result.add("* " + serviceName +
                        " is a background service (IntentService) \n");
            }
        }

        return result;
    }
}
