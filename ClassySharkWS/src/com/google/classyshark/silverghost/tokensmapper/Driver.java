package com.google.classyshark.silverghost.tokensmapper;

import java.io.File;

public class Driver {
    public static void main(String[] args) throws Exception {

        MappingReader mr = new MappingReader(new File("/Users/bfarber/Desktop/mapping.txt"));
        mr.pump(new ClassySharkMappingProcessor());
    }
}
