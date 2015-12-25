package com.google.classyshark.contentreader.dex;

import java.io.File;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.iface.DexFile;

// TODO inline class, looks redundant
public class DexlibLoader {
    public static DexFile loadDexFile(File binaryArchiveFile) throws Exception {
        // TODO optimize
        DexFile newDexFile = DexFileFactory.loadDexFile(binaryArchiveFile,
                19 /*api level*/, true);

        return newDexFile;
    }

}
