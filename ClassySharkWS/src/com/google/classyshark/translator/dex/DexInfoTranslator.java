package com.google.classyshark.translator.dex;

import com.google.classyshark.reducer.ArchiveReader;
import com.google.classyshark.translator.Translator;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

public class DexInfoTranslator implements Translator {
    private final File archiveFile;
    private String className;
    private List<ELEMENT> elements = new ArrayList<>();


    public DexInfoTranslator(String className, File archiveFile) {
        this.className = className;
        this.archiveFile = archiveFile;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void apply() {
        try {
            DexFile dxFile = ArchiveReader.get(new File(className));

            DexBackedDexFile dataPack = (DexBackedDexFile) dxFile;

            ELEMENT element = new ELEMENT("\nclasses: " + dataPack.getClassCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nstrings: " + dataPack.getStringCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\ntypes: " + dataPack.getTypeCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nprotos: " + dataPack.getProtoCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nfields: " + dataPack.getFieldCount(), TAG.ANNOTATION);
            elements.add(element);
            element = new ELEMENT("\nmethods: " + dataPack.getMethodCount(), TAG.ANNOTATION);
            elements.add(element);

        } catch (Exception e) {

        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return elements;
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }
}
