package com.google.classyshark.silverghost.contentreader.dex;

import java.io.File;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.DexFile;

public class DexlibLoader {
    public static DexFile loadDexFile(File binaryArchiveFile) throws Exception {
        DexBackedDexFile newDexFile = DexFileFactory.loadDexFile(binaryArchiveFile,
                Opcodes.forApi(19));

        return newDexFile;
    }
}
