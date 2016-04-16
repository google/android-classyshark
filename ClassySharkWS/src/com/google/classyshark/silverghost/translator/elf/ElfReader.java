/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.silverghost.translator.elf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A poor man's implementation of the readelf command. This program is
 * designed to parse ELF (Executable and Linkable Format) files.
 */
public class ElfReader {
    /**
     * The magic values for the ELF identification.
     */
    private static final byte[] ELF_IDENT = {
            (byte) 0x7F, (byte) 'E', (byte) 'L', (byte) 'F',
    };
    /**
     * Size of the e_ident[] structure in the ELF header.
     */
    private static final int EI_NIDENT = 16;
    /**
     * Offset from end of ident structure in half-word sizes.
     */
    private static final int OFFSET_TYPE = 0;
    /**
     * Machine type.
     */
    private static final int OFFSET_MACHINE = 1;
    /**
     * ELF version.
     */
    private static final int OFFSET_VERSION = 2;
    /**
     * The offset to which the system transfers control. e.g., the first thing
     * executed.
     */
    private static final int OFFSET_ENTRY = 4;
    /**
     * Program header offset in bytes.
     */
    private static final int OFFSET_PHOFF = 6;
    /**
     * Segment header offset in bytes.
     */
    private static final int OFFSET_SHOFF = 8;
    /**
     * Processor-specific flags for binary.
     */
    private static final int OFFSET_FLAGS = 10;
    /**
     * ELF header size in bytes.
     */
    private static final int OFFSET_EHSIZE = 12;
    /**
     * All program headers entry size in bytes.
     */
    private static final int OFFSET_PHENTSIZE = 13;
    /**
     * Number of program headers in ELF.
     */
    private static final int OFFSET_PHNUM = 14;
    /**
     * All segment headers entry size in bytes.
     */
    private static final int OFFSET_SHENTSIZE = 15;
    /**
     * Number of segment headers in ELF.
     */
    private static final int OFFSET_SHNUM = 16;
    /**
     * The section header index that refers to string table.
     */
    private static final int OFFSET_SHSTRNDX = 17;
    /**
     * Program header offset for type of this program header.
     */
    private static final int PHOFF_TYPE = 0;
    /**
     * Program header offset for absolute offset in file.
     */
    private static final int PHOFF_OFFSET = 2;
    /**
     * Program header offset for virtual address.
     */
    private static final int PHOFF_VADDR = 4;
    /**
     * Program header offset for physical address.
     */
    private static final int PHOFF_PADDR = 6;
    /**
     * Program header offset for file size in bytes.
     */
    private static final int PHOFF_FILESZ = 8;
    /**
     * Program header offset for memory size in bytes.
     */
    private static final int PHOFF_MEMSZ = 10;
    /**
     * Program header offset for flags.
     */
    private static final int PHOFF_FLAGS = 12;
    /**
     * Program header offset for required alignment. 0 or 1 means no alignment
     * necessary.
     */
    private static final int PHOFF_ALIGN = 14;
    /**
     * Index into string pool for segment name.
     */
    private static final long SHOFF_NAME = 0;
    /**
     * Segment header offset for type (half-words)
     */
    private static final long SHOFF_TYPE = 2;
    /**
     * Segment header offset for offset (meta!) (half-words)
     */
    private static final long SHOFF_OFFSET = 8;
    /**
     * Segment header offset for size (half-words)
     */
    private static final long SHOFF_SIZE = 10;
    /**
     * Data is presented in LSB format.
     */
    private static final int ELFDATA2LSB = 1;
    /**
     * Date is presented in MSB format.
     */
    private static final int ELFDATA2MSB = 2;
    private static final int ELFCLASS32 = 1;
    private static final int ELFCLASS64 = 2;
    private static final long PT_LOAD = 1;
    /**
     * Section Type: Symbol Table
     */
    private static final int SHT_SYMTAB = 2;
    /**
     * Section Type: String Table
     */
    private static final int SHT_STRTAB = 3;
    /**
     * Section Type: Dynamic
     **/
    private static final int SHT_DYNAMIC = 6;
    /**
     * Section Type: Dynamic Symbol Table
     */
    private static final int SHT_DYNSYM = 11;
    /**
     * Symbol Table Entry: Name offset
     */
    private static final int SYMTAB_NAME = 0;
    /**
     * Symbol Table Entry: SymTab Info
     */
    private static final int SYMTAB_ST_INFO = 6;
    /**
     * Symbol Table Entry size (half-words)
     */
    private static final int SYMTAB_ENTRY_HALFWORD_SIZE = 7;
    /**
     * Symbol Table Entry size (extra in bytes) to cover "st_info" and
     * "st_other"
     */
    private static final int SYMTAB_ENTRY_BYTE_EXTRA_SIZE = 2;

    public static class Symbol {
        public static final int STB_LOCAL = 0;
        public static final int STB_GLOBAL = 1;
        public static final int STB_WEAK = 2;
        public static final int STB_LOPROC = 13;
        public static final int STB_HIPROC = 15;
        public final String name;
        public final int bind;
        public final int type;

        Symbol(String name, int st_info) {
            this.name = name;
            this.bind = (st_info >> 4) & 0x0F;
            this.type = st_info & 0x0F;
        }
    }

    ;
    private final RandomAccessFile mFile;
    private final byte[] mBuffer = new byte[512];
    private int mClass;
    private int mEndian;
    private boolean mIsDynamic;
    private boolean mIsPIE;
    private int mType;
    private int mWordSize;
    private int mHalfWordSize;
    /**
     * Symbol Table offset
     */
    private long mSymTabOffset;
    /**
     * Symbol Table size
     */
    private long mSymTabSize;
    /**
     * Dynamic Symbol Table offset
     */
    private long mDynSymOffset;
    /**
     * Dynamic Symbol Table size
     */
    private long mDynSymSize;
    /**
     * Section Header String Table offset
     */
    private long mShStrTabOffset;
    /**
     * Section Header String Table size
     */
    private long mShStrTabSize;
    /**
     * String Table offset
     */
    private long mStrTabOffset;
    /**
     * String Table size
     */
    private long mStrTabSize;
    /**
     * Dynamic String Table offset
     */
    private long mDynStrOffset;
    /**
     * Dynamic String Table size
     */
    private long mDynStrSize;
    /**
     * Symbol Table symbol names
     */
    public Map<String, Symbol> mSymbols;
    /**
     * Dynamic Symbol Table symbol names
     */
    private Map<String, Symbol> mDynamicSymbols;

    static ElfReader read(File file) throws IOException {
        return new ElfReader(file);
    }

    boolean isDynamic() {
        return mIsDynamic;
    }

    int getType() {
        return mType;
    }

    boolean isPIE() {
        return mIsPIE;
    }

    private ElfReader(File file) throws IOException {
        mFile = new RandomAccessFile(file, "r");
        readIdent();
        readHeader();
    }

    protected void finalize() throws Throwable {
        try {
            mFile.close();
        } catch (IOException e) {
            // nothing
        } finally {
            super.finalize();
        }
    }

    private void readHeader() throws IOException {
        mType = readHalf(getHeaderOffset(OFFSET_TYPE));
        final long shOffset = readWord(getHeaderOffset(OFFSET_SHOFF));
        final int shNumber = readHalf(getHeaderOffset(OFFSET_SHNUM));
        final int shSize = readHalf(getHeaderOffset(OFFSET_SHENTSIZE));
        final int shStrIndex = readHalf(getHeaderOffset(OFFSET_SHSTRNDX));
        readSectionHeaders(shOffset, shNumber, shSize, shStrIndex);
        final long phOffset = readWord(getHeaderOffset(OFFSET_PHOFF));
        final int phNumber = readHalf(getHeaderOffset(OFFSET_PHNUM));
        final int phSize = readHalf(getHeaderOffset(OFFSET_PHENTSIZE));
        readProgramHeaders(phOffset, phNumber, phSize);
    }

    private void readSectionHeaders(long tableOffset, int shNumber, int shSize, int shStrIndex)
            throws IOException {
        // Read the Section Header String Table offset first.
        {
            final long shStrTabShOffset = tableOffset + shStrIndex * shSize;
            final long type = readWord(shStrTabShOffset + mHalfWordSize * SHOFF_TYPE);
            if (type == SHT_STRTAB) {
                mShStrTabOffset = readWord(shStrTabShOffset + mHalfWordSize * SHOFF_OFFSET);
                mShStrTabSize = readWord(shStrTabShOffset + mHalfWordSize * SHOFF_SIZE);
            }
        }
        for (int i = 0; i < shNumber; i++) {
            // Don't bother to re-read the Section Header StrTab.
            if (i == shStrIndex) {
                continue;
            }
            final long shOffset = tableOffset + i * shSize;
            final long type = readWord(shOffset + mHalfWordSize * SHOFF_TYPE);
            if ((type == SHT_SYMTAB) || (type == SHT_DYNSYM)) {
                final long nameOffset = readWord(shOffset + mHalfWordSize * SHOFF_NAME);
                final long offset = readWord(shOffset + mHalfWordSize * SHOFF_OFFSET);
                final long size = readWord(shOffset + mHalfWordSize * SHOFF_SIZE);
                final String symTabName = readShStrTabEntry(nameOffset);
                if (".symtab".equals(symTabName)) {
                    mSymTabOffset = offset;
                    mSymTabSize = size;
                } else if (".dynsym".equals(symTabName)) {
                    mDynSymOffset = offset;
                    mDynSymSize = size;
                }
            } else if (type == SHT_STRTAB) {
                final long nameOffset = readWord(shOffset + mHalfWordSize * SHOFF_NAME);
                final long offset = readWord(shOffset + mHalfWordSize * SHOFF_OFFSET);
                final long size = readWord(shOffset + mHalfWordSize * SHOFF_SIZE);
                final String strTabName = readShStrTabEntry(nameOffset);
                if (".strtab".equals(strTabName)) {
                    mStrTabOffset = offset;
                    mStrTabSize = size;
                } else if (".dynstr".equals(strTabName)) {
                    mDynStrOffset = offset;
                    mDynStrSize = size;
                }
            } else if (type == SHT_DYNAMIC) {
                mIsDynamic = true;
            }
        }
    }

    private void readProgramHeaders(long phOffset, int phNumber, int phSize) throws IOException {
        for (int i = 0; i < phNumber; i++) {
            final long baseOffset = phOffset + i * phSize;
            final long type = readWord(baseOffset);
            if (type == PT_LOAD) {
                final long virtAddress = readWord(baseOffset + mHalfWordSize * PHOFF_VADDR);
                if (virtAddress == 0) {
                    mIsPIE = true;
                }
            }
        }
    }

    private void readSymbolTable(Map<String, Symbol> symbolMap, long symStrOffset, long symStrSize,
                                 long symOffset, long symSize) throws IOException {
        final long symEnd = symOffset + symSize;
        for (long off = symOffset; off < symEnd; off += SYMTAB_ENTRY_HALFWORD_SIZE * mHalfWordSize
                + SYMTAB_ENTRY_BYTE_EXTRA_SIZE) {
            long strOffset = readWord(off + SYMTAB_NAME);
            if (strOffset == 0) {
                continue;
            }
            final String symName = readStrTabEntry(symStrOffset, symStrSize, strOffset);
            if (symName != null) {
                final int st_info = readByte(off + SYMTAB_ST_INFO);
                symbolMap.put(symName, new Symbol(symName, st_info));
            }
        }
    }

    private String readShStrTabEntry(long strOffset) throws IOException {
        if ((mShStrTabOffset == 0) || (strOffset < 0) || (strOffset >= mShStrTabSize)) {
            return null;
        }
        return readString(mShStrTabOffset + strOffset);
    }

    private String readStrTabEntry(long tableOffset, long tableSize, long strOffset)
            throws IOException {
        if ((tableOffset == 0) || (strOffset < 0) || (strOffset >= tableSize)) {
            return null;
        }
        return readString(tableOffset + strOffset);
    }

    private int getHeaderOffset(int halfWorldOffset) {
        return EI_NIDENT + halfWorldOffset * mHalfWordSize;
    }

    private int readByte(long offset) throws IOException {
        mFile.seek(offset);
        mFile.readFully(mBuffer, 0, 1);
        return mBuffer[0];
    }

    private int readHalf(long offset) throws IOException {
        mFile.seek(offset);
        mFile.readFully(mBuffer, 0, mWordSize);
        final int answer;
        if (mEndian == ELFDATA2LSB) {
            answer = mBuffer[1] << 8 | mBuffer[0];
        } else {
            answer = mBuffer[0] << 8 | mBuffer[1];
        }
        return answer;
    }

    private long readWord(long offset) throws IOException {
        mFile.seek(offset);
        mFile.readFully(mBuffer, 0, mWordSize);
        int answer = 0;
        if (mEndian == ELFDATA2LSB) {
            for (int i = mWordSize - 1; i >= 0; i--) {
                answer = (answer << 8) | (mBuffer[i] & 0xFF);
            }
        } else {
            final int N = mWordSize - 1;
            for (int i = 0; i <= N; i++) {
                answer = (answer << 8) | mBuffer[i];
            }
        }
        return answer;
    }

    private String readString(long offset) throws IOException {
        mFile.seek(offset);
        mFile.readFully(mBuffer, 0, (int) Math.min(mBuffer.length, mFile.length() - offset));
        for (int i = 0; i < mBuffer.length; i++) {
            if (mBuffer[i] == 0) {
                return new String(mBuffer, 0, i);
            }
        }
        return null;
    }

    private void readIdent() throws IOException {
        mFile.seek(0);
        mFile.readFully(mBuffer, 0, EI_NIDENT);
        if ((mBuffer[0] != ELF_IDENT[0]) || (mBuffer[1] != ELF_IDENT[1])
                || (mBuffer[2] != ELF_IDENT[2]) || (mBuffer[3] != ELF_IDENT[3])) {
            throw new IllegalArgumentException("Invalid ELF file");
        }
        mClass = mBuffer[4];
        if (mClass == ELFCLASS32) {
            mWordSize = 4;
            mHalfWordSize = 2;
        } else {
            throw new IOException("Invalid executable type " + mClass + ": not ELFCLASS32!");
        }
        mEndian = mBuffer[5];
    }

    public Symbol getSymbol(String name) {
        if ((mSymTabOffset == 0) && (mSymTabSize == 0)) {
            return null;
        }
        if (mSymbols == null) {
            mSymbols = new HashMap<String, Symbol>();
            try {
                readSymbolTable(mSymbols, mStrTabOffset, mStrTabSize, mSymTabOffset, mSymTabSize);
            } catch (IOException e) {
                return null;
            }
        }
        return mSymbols.get(name);
    }

    public Symbol getDynamicSymbol(String name) {
        if ((mDynSymOffset == 0) && (mDynSymSize == 0)) {
            return null;
        }
        if (mDynamicSymbols == null) {
            mDynamicSymbols = new HashMap<String, Symbol>();
            try {
                readSymbolTable(mDynamicSymbols, mDynStrOffset, mDynStrSize, mDynSymOffset,
                        mDynSymSize);
            } catch (IOException e) {
                return null;
            }
        }
        return mDynamicSymbols.get(name);
    }

    public List<String> getDynamicSymbols() {
        getDynamicSymbol("");
        Set set = mDynamicSymbols.keySet();
        ArrayList<String> result = new ArrayList<>(set);
        Collections.sort(result);

        return result;
    }

    public static void main(String[] args) throws Exception {
        String soFile = System.getProperty("user.home") +
                "/Desktop/Scenarios/5 Sos/libsqlcipher_android.so";
        File resource = new File(soFile);
        ElfReader elf = ElfReader.read(resource);
        for (String dynVal : elf.getDynamicSymbols()) {
            System.out.println(dynVal);
        }
    }
}
