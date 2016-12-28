/*
 * Copyright 2016 Google, Inc.
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

package com.google.classyshark.silverghost.contentreader.jar;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.jayce.JayceFormatPackageLoader;
import com.android.jack.library.JackLibraryFactory;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.Version;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.vfs.ReadZipFS;
import com.google.classyshark.silverghost.translator.java.MetaObject.AnnotationInfo;
import com.google.classyshark.silverghost.translator.java.MetaObject.ExceptionInfo;
import com.google.classyshark.silverghost.translator.java.MetaObject.ParameterInfo;
import com.google.classyshark.silverghost.translator.java.jayce.MetaObjectJayce;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

/**
 */
public class JayceReader extends JarReader {

    private final static com.android.sched.util.config.Config uninitializedConfig;
    private final static com.android.sched.util.config.Config threadConfig;

    static {
        try {
            System.out.println("Jack version: " + new Version("jack", Jack.class.getClassLoader())
                    .getVerboseVersion());
        } catch (Exception ignore) {
        }
        uninitializedConfig = ThreadConfig.getConfig();
        try {
            final Options options = new Options();
            RunnableHooks hooks = new RunnableHooks();
            options.checkValidity(hooks);
            ThreadConfig.setConfig(threadConfig = options.getConfig());
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize Jack options", e);
        }
    }

    private final static HashMap<String, JDefinedClassOrInterface> cache
            = new HashMap<>(32768, .75f);

    public JayceReader(File binaryArchive) {
        super(binaryArchive);
        reset();
    }

    // SourceFormatter
    private String getClassOrInterfaceName(final JClassOrInterface type) {
        JPackage enclosingPackage = type.getEnclosingPackage();
        assert enclosingPackage != null;
        return getName(enclosingPackage, type.getName());
    }

    public String getName(
            JPackage enclosingPackage, String classOrInterfaceSimpleName) {
        StringBuilder sb;
        if (!enclosingPackage.isDefaultPackage()) {
            sb = getNameInternal(enclosingPackage);
            sb.append(getPackageSeparator());
        } else {
            sb = new StringBuilder();
        }
        sb.append(classOrInterfaceSimpleName);
        return sb.toString();
    }

    protected StringBuilder getNameInternal(@Nonnull JPackage pack) {
        StringBuilder qualifiedName;
        if (pack.isTopLevelPackage()) {
            qualifiedName = new StringBuilder();
        } else {
            JPackage enclosingPackage = pack.getEnclosingPackage();
            assert enclosingPackage != null;
            qualifiedName = getNameInternal(enclosingPackage);
            if (qualifiedName.length() != 0) {
                qualifiedName.append(getPackageSeparator());
            }
            qualifiedName.append(pack.getName());
        }
        return qualifiedName;
    }

    private char getPackageSeparator() {
        return '.';
    }

    @Override
    public void read() {
        try {
            JayceFormatPackageLoader packageLoader = new JayceFormatPackageLoader(
                    JackLibraryFactory.getInputLibrary(
                            new ReadZipFS(
                                    new InputZipFile(binaryArchive.getAbsolutePath(),
                                            null,
                                            FileOrDirectory.Existence.MUST_EXIST,
                                            FileOrDirectory.ChangePermission.NOCHANGE))
                    ),
                    new JPackage("", null)
            );
            // simpler code since we load everything at once
            // dealing with package loader stresses memory less (tree traversal only)



            java.util.List<com.android.jack.load.PackageLoader> list = new LinkedList<>();

            list.add(packageLoader);

            visit(new JPackage("", null, list));



            allClassNames.addAll(cache.keySet());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(allClassNames);
    }

    private void visit(final JPackage jPackage) {
        for (final JDefinedClassOrInterface coi : jPackage.getTypes()) {
            cache.put(getClassOrInterfaceName(coi), coi);
        }
        for (final JPackage p : jPackage.getSubPackages()) {
            visit(p);
        }
    }

    public static MetaObjectJayce getMetaObjectJayce(final String className, final File jar) {
        return new MetaObjectJayce(className, cache.get(className));
    }

    public static void reset() {
        cache.clear();
    }

    public static void ensureThreadConfigInitialized() {
        if (ThreadConfig.getConfig() == uninitializedConfig) {
            System.err
                    .println("DEBUG JayceReader: UNINITIALIZED CONFIG in " + Thread.currentThread());
            // FIXME: dump stack trace? that's too noisy ATM
            ThreadConfig.setConfig(JayceReader.threadConfig);
        }
    }

    public static boolean isJackAndJillArchive(final File file) {
        try (final ZipFile zip = new ZipFile(file)) {
            return null != zip.getEntry("jack.properties");
        } catch (IOException ignore) {
        }
        return false;
    }

    //

    private interface Matcher<T> {

        boolean match(T object);
    }

    private static <E> List<E> filter(final List<E> list, final Matcher<E> matcher) {
        final List<E> out = new ArrayList<>(list.size());
        // not using streams api to maintain pre 1.8 compatibility
        for (final E object : list) {
            if (matcher.match(object)) {
                out.add(object);
            }
        }
        return out;
    }

    public static List<JMethod> getConstructors(final @Nonnull JDefinedClassOrInterface klazz) {

        return filter(klazz.getMethods(), new Matcher<JMethod>() {
            @Override
            public boolean match(JMethod object) {
                return object.getName().equals("<init>");
            }
        });

        //return filter(klazz.getMethods(),
        //        object -> object.getName().equals("<init>"));

    }

    public static List<JMethod> getMethods(final @Nonnull JDefinedClassOrInterface klazz) {

        return filter(klazz.getMethods(), new Matcher<JMethod>() {
            @Override
            public boolean match(JMethod object) {
                return !object.getName().equals("<init>");
            }
        });

        //return filter(klazz.getMethods(),
        //        object -> !object.getName().equals("<init>"));
    }

    public static ExceptionInfo[] toExceptionInfo(
            final ThrownExceptionMarker thrownExceptionMarker) {
        if (null != thrownExceptionMarker) {
            final List<JClass> exceptions = thrownExceptionMarker.getThrownExceptions();
            final int size = exceptions.size();
            final ExceptionInfo[] eis = new ExceptionInfo[size];
            for (int i = 0; i < size; i++) {
                final ExceptionInfo ei = eis[i] = new ExceptionInfo();
                ei.exceptionStr = exceptions.get(i).getName();

            }
            return eis;
        } else {
            return new ExceptionInfo[0];
        }
    }

    public static AnnotationInfo[] toAnnotationInfo(
            final @Nonnull Collection<JAnnotation> annotations) {
        final int size = annotations.size();
        final AnnotationInfo[] ais = new AnnotationInfo[size];
        int i = 0;
        for (final JAnnotation a : annotations) {
            final AnnotationInfo ai = ais[i++] = new AnnotationInfo();
            ai.annotationStr = a.toString();
        }
        return ais;
    }

    public static ParameterInfo[] toParameterInfo(final @Nonnull List<JParameter> parameters) {
        final int size = parameters.size();
        final ParameterInfo[] pis = new ParameterInfo[size];
        for (int i = 0; i < size; i++) {
            final JParameter jParameter = parameters.get(i);
            final ParameterInfo pi = pis[i] = new ParameterInfo();
            pi.parameterStr = jParameter.toString();
        }
        return pis;
    }
}
