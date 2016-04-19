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

package com.google.classyshark.silverghost.translator.java;

import com.google.classyshark.silverghost.translator.Translator;
import com.google.classyshark.silverghost.translator.TranslatorFactory;
import com.google.classyshark.silverghost.translator.java.clazz.QualifiedTypesMap;
import com.google.classyshark.silverghost.translator.java.clazz.reflect.MetaObjectClass;
import com.google.classyshark.silverghost.tokensmapper.ProguardMapper;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Is a function : (class name, archive file) --> class source, as list of tokens with tag
 */
public class JavaTranslator implements Translator {

    private MetaObject metaObject;
    private List<ELEMENT> sourceCode;
    private QualifiedTypesMap namesMapper;

    /**
     * used for testing
     *
     * @param clazz
     */
    public JavaTranslator(Class clazz) {
        this.metaObject = new MetaObjectClass(clazz);
        sourceCode = new ArrayList<>();
        namesMapper = new QualifiedTypesMap();
    }

    public JavaTranslator(String className, File archiveFile) {
        this.metaObject =
                MetaObjectFactory.buildMetaObject(className, archiveFile);
        sourceCode = new ArrayList<>();
        namesMapper = new QualifiedTypesMap();
    }

    @Override
    public String getClassName() {
        return metaObject.getName();
    }

    @Override
    public void addMapper(ProguardMapper reverseMappings) {
        this.metaObject =
               new MetaObjectWithMapper(this.metaObject, reverseMappings);
    }

    @Override
    public void apply() {
        MetaObject.ConstructorInfo constructors[];
        MetaObject.MethodInfo methods[];
        MetaObject.FieldInfo fields[];
        MetaObject.InterfaceInfo interfaces[];

        String className, packageName;

        className = metaObject.getName();
        if (className.lastIndexOf(".") != -1) {
            packageName = className.substring(0, className.lastIndexOf("."));

            sourceCode.add(new ELEMENT("\npackage ", TAG.MODIFIER));
            sourceCode.add(new ELEMENT(packageName, TAG.IDENTIFIER));
            sourceCode.add(new ELEMENT(";\n", TAG.MODIFIER));
        }

        interfaces = metaObject.getInterfaces();
        fields = metaObject.getDeclaredFields();
        constructors = metaObject.getDeclaredConstructors();
        methods = metaObject.getDeclaredMethods();

        fillTypes(interfaces, constructors, methods, fields);

        namesMapper.removeType(className);
        fillSource(interfaces, constructors, methods, fields, metaObject);
    }

    @Override
    public String toString() {
        if (sourceCode == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (ELEMENT word : sourceCode) {
                sb.append(word.text);
            }
            return sb.toString();
        }
    }

    @Override
    public List<ELEMENT> getElementsList() {
        return Collections.unmodifiableList(this.sourceCode);
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<>(namesMapper.getFullTypes());
    }

    private void fillTypes(MetaObject.InterfaceInfo[] interfaces,
                           MetaObject.ConstructorInfo[] constructors,
                           MetaObject.MethodInfo[] methods,
                           MetaObject.FieldInfo[] fields) {
        for (MetaObject.InterfaceInfo iface : interfaces) {
            namesMapper.addType(iface.interfaceStr);
        }

        for (MetaObject.FieldInfo field : fields) {
            namesMapper.addType(field.typeName);
        }

        for (MetaObject.ConstructorInfo constructor : constructors) {
            MetaObject.ParameterInfo parameterTypes[] = constructor.parameterTypes;
            if (parameterTypes.length > 0) {
                for (MetaObject.ParameterInfo parameterInfo : parameterTypes) {
                    namesMapper.addType(parameterInfo.parameterStr);
                }
            }
        }

        for (MetaObject.MethodInfo method : methods) {
            namesMapper.addType(method.returnType);
            MetaObject.ParameterInfo parameters[] = method.parameterTypes;
            if (parameters.length > 0) {
                for (MetaObject.ParameterInfo parameter : parameters) {
                    namesMapper.addType(parameter.parameterStr);
                }
            }

            MetaObject.ExceptionInfo[] exceptions = method.exceptionTypes;

            for (MetaObject.ExceptionInfo exception : exceptions) {
                namesMapper.addType(exception.exceptionStr);
            }
        }
    }

    private void fillSource(MetaObject.InterfaceInfo[] interfaces,
                            MetaObject.ConstructorInfo[] constructors,
                            MetaObject.MethodInfo[] methods,
                            MetaObject.FieldInfo[] fields,
                            MetaObject metaObject) {
        fillImports(namesMapper, sourceCode);
        fillClassDecl(interfaces, metaObject, sourceCode, namesMapper);
        fillFields(fields, sourceCode, namesMapper);
        fillCtors(constructors, metaObject, sourceCode, namesMapper);
        fillMethods(methods, sourceCode, namesMapper);
    }

    private static void fillImports(QualifiedTypesMap namesMapper, List<ELEMENT> words) {
        List<String> imports = namesMapper.getFullTypes();
        for (String importStr : imports) {
            words.add(new ELEMENT("\nimport ", TAG.MODIFIER));
            words.add(new ELEMENT(importStr + ";", TAG.DOCUMENT));
        }

        words.add(new ELEMENT("\n\n", TAG.IDENTIFIER));
    }

    private static void fillClassDecl(MetaObject.InterfaceInfo[] interfaces,
                                      MetaObject metaObject,
                                      List<ELEMENT> words,
                                      QualifiedTypesMap namesMapper) {
        MetaObject.AnnotationInfo[] annotations = metaObject.getAnnotations();
        for (MetaObject.AnnotationInfo annot : annotations) {
            words.add(new ELEMENT("@" + annot.annotationStr + " \n", TAG.ANNOTATION));
        }

        int mod = metaObject.getModifiers();
        words.add(new ELEMENT(Modifier.toString(mod), TAG.MODIFIER));

        if (!Modifier.isInterface(mod)) {
            words.add(new ELEMENT(" class", TAG.MODIFIER));
        }

        words.add(new ELEMENT(" " + namesMapper.getTypeNull(metaObject.getName()),
                TAG.IDENTIFIER));
        words.add(new ELEMENT(metaObject.getClassGenerics(metaObject.getName()),
                TAG.IDENTIFIER));

        if (metaObject.getSuperclass() != null) {
            words.add(new ELEMENT(" extends ", TAG.MODIFIER));
            words.add(new ELEMENT(namesMapper.getType(metaObject.getSuperclass()),
                    TAG.IDENTIFIER));
            words.add(new ELEMENT(metaObject.getSuperclassGenerics(), TAG.IDENTIFIER));
        }

        if (interfaces.length != 0) {
            words.add(new ELEMENT(" implements ", TAG.MODIFIER));
            for (MetaObject.InterfaceInfo iface : interfaces) {
                words.add(new ELEMENT(namesMapper.getType(iface.interfaceStr),
                        TAG.IDENTIFIER));
                words.add(new ELEMENT(iface.genericsStr, TAG.IDENTIFIER));
                words.add(new ELEMENT(", ", TAG.IDENTIFIER));
            }
            words.remove(words.size() - 1);
        }
        words.add(new ELEMENT("\n{", TAG.IDENTIFIER));
    }

    private static void fillFields(MetaObject.FieldInfo[] fields,
                                   List<ELEMENT> words,
                                   QualifiedTypesMap namesMapper) {
        MetaObject.AnnotationInfo[] annotations;
        words.add(new ELEMENT("\n"
                + "    //======================== F I E L D S ==================\n\n",
                TAG.DOCUMENT));

        for (MetaObject.FieldInfo field : fields) {
            int md = field.modifiers;
            annotations = field.annotations;

            for (MetaObject.AnnotationInfo annot : annotations) {
                words.add(new ELEMENT("\n      @" + annot.annotationStr + " ",
                        TAG.ANNOTATION));
            }

            words.add(new ELEMENT("\n      " + Modifier.toString(md) + " ", TAG.MODIFIER));
            words.add(new ELEMENT(namesMapper.getTypeNull(field.typeName) + " ",
                    TAG.MODIFIER));

            words.add(new ELEMENT(field.name, TAG.IDENTIFIER));
            words.add(new ELEMENT(field.genericStr, TAG.DOCUMENT));
            words.add(new ELEMENT(";", TAG.DOCUMENT));
        }
    }

    private static void fillCtors(MetaObject.ConstructorInfo[] constructors,
                                  MetaObject metaObject, List<ELEMENT> words,
                                  QualifiedTypesMap namesMapper) {
        words.add(new ELEMENT("\n\n"
                + "    //======================== C O N S T R U C T O R S ======\n\n",
                TAG.DOCUMENT));

        String x = namesMapper.getTypeNull(metaObject.getName());

        for (MetaObject.ConstructorInfo constructor : constructors) {
            int md = constructor.modifiers;
            words.add(new ELEMENT("    " + Modifier.toString(md) + " ", TAG.MODIFIER));
            words.add(new ELEMENT(x, TAG.IDENTIFIER));

            MetaObject.ParameterInfo parameterTypes[] = constructor.parameterTypes;
            words.add(new ELEMENT("(", TAG.DOCUMENT));
            if (parameterTypes.length > 0) {
                for (int j = 0; j < parameterTypes.length; j++) {
                    words.add(new ELEMENT(
                            namesMapper.getTypeNull(parameterTypes[j].parameterStr),
                            TAG.DOCUMENT));
                    words.add(new ELEMENT(parameterTypes[j].genericStr, TAG.DOCUMENT));
                    if (j < (parameterTypes.length - 1)) {
                        words.add(new ELEMENT(", ", TAG.DOCUMENT));
                        words.add(new ELEMENT("\n        ", TAG.DOCUMENT));
                    }
                }
            }
            words.add(new ELEMENT(") { ... }\n", TAG.DOCUMENT));
        }
    }

    private static void fillMethods(MetaObject.MethodInfo[] methods,
                                    List<ELEMENT> words,
                                    QualifiedTypesMap namesMapper) {
        MetaObject.AnnotationInfo[] annotations;
        words.add(new ELEMENT("\n"
                + "    //======================== M E T H O D S ================\n\n",
                TAG.DOCUMENT));

        for (MetaObject.MethodInfo method : methods) {
            int md = method.modifiers;

            annotations = method.annotations;

            for (MetaObject.AnnotationInfo annot : annotations) {
                words.add(new ELEMENT("    @" + annot.annotationStr + " \n", TAG.ANNOTATION));
            }

            words.add(new ELEMENT("    " + Modifier.toString(md) + " ", TAG.MODIFIER));
            words.add(new ELEMENT(namesMapper.getTypeNull(method.returnType) + " ",
                    TAG.DOCUMENT));
            words.add(new ELEMENT(method.genericReturnType, TAG.DOCUMENT));
            words.add(new ELEMENT(method.name, TAG.IDENTIFIER));

            MetaObject.ParameterInfo parameterTypes[] = method.parameterTypes;
            words.add(new ELEMENT("(", TAG.DOCUMENT));
            if (parameterTypes.length > 0) {
                for (int j = 0; j < parameterTypes.length; j++) {
                    words.add(new ELEMENT(
                            namesMapper.getType(parameterTypes[j].parameterStr),
                            TAG.DOCUMENT));
                    words.add(new ELEMENT(parameterTypes[j].genericStr, TAG.DOCUMENT));
                    if (j < (parameterTypes.length - 1)) {
                        words.add(new ELEMENT(", ", TAG.DOCUMENT));
                        words.add(new ELEMENT("\n        ", TAG.DOCUMENT));
                    }
                }
            }

            words.add(new ELEMENT(") ", TAG.DOCUMENT));

            MetaObject.ExceptionInfo[] exceptionTypes = method.exceptionTypes;

            if (exceptionTypes.length > 0) {
                words.add(new ELEMENT(" throws ", TAG.MODIFIER));

                for (MetaObject.ExceptionInfo aXType : exceptionTypes) {
                    words.add(new ELEMENT(namesMapper.getType(aXType.exceptionStr),
                            TAG.IDENTIFIER));
                    words.add(new ELEMENT(", ",
                            TAG.IDENTIFIER));
                }
                words.remove(words.size() - 1);
            }
            words.add(new ELEMENT("{ ... }\n", TAG.DOCUMENT));
        }
        words.add(new ELEMENT("\n} ", TAG.DOCUMENT));
    }

    public static void testJar() {
        final File testFile = new File(System.getProperty("user.home") + "/Desktop/" + "ClassyShark.jar");
        String textClass = "com.google.classyshark.gui.panel.reducer.Reducer.class";
        Translator sourceGenerator = TranslatorFactory.createTranslator(textClass, testFile);
        sourceGenerator.apply();

        System.out.println(sourceGenerator.toString());
    }

    public static void testSystemClass() {
        Translator translator = new JavaTranslator(Enum.class);
        translator.apply();
        System.out.print(translator);
    }

    public static void testCustomClass() {
        final File testFile = new File(System.getProperty("user.home") + "/Desktop/Scenarios/3 Class/Reducer.class");
        String textClass = "com.google.classyshark.gui.panel.reducer.Reducer.class";
        Translator translator = TranslatorFactory.createTranslator(textClass, testFile);
        translator.apply();

        System.out.println(translator.toString());
    }

    public static void testInnerClass() {
        final File testFile = new File(System.getProperty("user.home") + "/Desktop/Scenarios/3 Class/Reducer$1.class");
        String textClass = "com.google.classyshark.gui.panel.reducer.Reducer$1.class";
        Translator translator = TranslatorFactory.createTranslator(textClass, testFile);
        translator.apply();

        System.out.println(translator.toString());
    }

    public static void main(String[] args) throws Exception {
        testJar();
        testSystemClass();
        testCustomClass();
        testInnerClass();
    }
}