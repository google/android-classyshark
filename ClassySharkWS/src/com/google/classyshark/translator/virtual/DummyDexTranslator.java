package com.google.classyshark.translator.virtual;

import com.google.classyshark.translator.Translator;
import java.util.LinkedList;
import java.util.List;

public class DummyDexTranslator implements Translator{
    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public void apply() {
    }

    @Override
    public List<ELEMENT> getElementsList() {

        // TODO add here dex data

        return new LinkedList<>();
    }

    @Override
    public List<String> getDependencies() {
        return new LinkedList<>();
    }
}
