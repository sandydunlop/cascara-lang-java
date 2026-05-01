package io.github.qishr.cascara.lang.java.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.qishr.cascara.common.util.JarFile;
import io.github.qishr.cascara.lang.java.exception.ClassLoadException;
import io.github.qishr.cascara.lang.java.model.JlsName;
import io.github.qishr.cascara.lang.java.model.MethodNode;
import io.github.qishr.cascara.lang.java.model.NameUtil;
import io.github.qishr.cascara.lang.java.modeler.StandardModeler;

public class JreUtil {

    protected JreUtil() {
        // Nothing to see here
    }

    public static JarFile loadJarFile(String fileName) throws IOException {
        return JarFile.load(Path.of(fileName));
    }

    public static Module loadUnnamedModule() {
        ClassLoader cl = JreUtil.class.getClassLoader().getSystemClassLoader();
        return cl.getUnnamedModule();
    }

    public static String getModulePath() {
        return System.getProperty("jdk.module.path");
    }

    public static Module loadModule(String moduleName) {
        for (Module module : ModuleLayer.boot().modules()) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static List<String> modules() throws ClassLoadException {
        Path packagePath = FileSystems.getFileSystem(URI.create("jrt:/"))
                .getPath("/modules");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packagePath)) {
            List<String> modules = new ArrayList<>();
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                modules.add(filename);
            }
            return modules;
        } catch (IOException e) {
            // Do nothing?
            throw new ClassLoadException(e.getMessage(), e);
        }
    }

    // public static List<Class<?>> loadClassesFromPackage(String packageName) throws ClassLoadException {
    //     List<Class<?>> classes = new ArrayList<>();
    //     // Dependency dependency = new Dependency(packageName);
    //     // StandardModeler standardModeler = new StandardModeler();
    //     try {
    //         String packageDir = packageName.replace('.', '/');
    //         Path packagePath = FileSystems.getFileSystem(URI.create("jrt:/"))
    //                                  .getPath("/modules/java.base/" + packageDir);
    //         try (DirectoryStream<Path> stream = Files.newDirectoryStream(packagePath)) {
    //             for (Path path : stream) {
    //                 String filename = path.getFileName().toString();
    //                 if (filename.endsWith(".class")) {
    //                     String binaryClassName = packageName + "." + filename.substring(0, filename.length() - 6);
    //                     Class<?> jreClass = JreUtil.loadClass(binaryClassName);
    //                     // TODO: classes inside classes need to be added to the containing class
    //                     classes.add(jreClass);
    //                 }
    //             }
    //         }
    //     } catch (IOException e) {
    //         // Do nothing?
    //         throw new ClassLoadException(e.getMessage(), e);
    //     } catch (IllegalAccessError e) {
    //         throw new ClassLoadException(e.getMessage(), e);
    //     } catch (Exception e) {
    //         throw new ClassLoadException(e.getMessage(), e);
    //     }
    //     return classes;
    // }

    public static List<Class<?>> loadClassesFromPackage(String packageName) throws ClassLoadException {
        List<Class<?>> classes = new ArrayList<>();
        String packageDir = packageName.replace('.', '/');

        try {
            FileSystem jrt = FileSystems.getFileSystem(URI.create("jrt:/"));
            Path modulesPath = jrt.getPath("/modules");

            // Iterate through all modules (java.base, java.desktop, etc.)
            try (DirectoryStream<Path> moduleStream = Files.newDirectoryStream(modulesPath)) {
                for (Path module : moduleStream) {
                    Path packagePath = module.resolve(packageDir);

                    if (Files.exists(packagePath)) {
                        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packagePath)) {
                            for (Path path : stream) {
                                String filename = path.getFileName().toString();
                                // Filter for top-level classes and ignore nested/anonymous (contains $)
                                if (filename.endsWith(".class") && !filename.contains("$")) {
                                    String className = packageName + "." + filename.substring(0, filename.length() - 6);
                                    try {
                                        classes.add(JreUtil.loadClass(className));
                                    } catch (Exception e) {
                                        // Some classes might fail to load due to visibility, skip them
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ClassLoadException(e.getMessage(), e);
        } catch (IllegalAccessError e) {
            throw new ClassLoadException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ClassLoadException(e.getMessage(), e);
        }
        return classes;
    }

    public static Class<?> loadClass(String qualifiedName) {
        Optional<Class<?>> candidate = tryLoadClass(qualifiedName);
        if (candidate.isPresent()) {
            return candidate.get();
        }
        JlsName name = NameUtil.createName(qualifiedName);
        for (int split = name.componentCount() - 1; split>0; split--) {
            name.setPackageComponentCount(split);
            qualifiedName = name.fullyQualifiedJvmBinaryName();
            candidate = tryLoadClass(qualifiedName);
            if (candidate.isPresent()) {
                return candidate.get();
            }
        }
        return null;
    }

    private static Optional<Class<?>> tryLoadClass(String qualifiedName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            Class<?> loadedClass = classLoader.loadClass(qualifiedName);
            return Optional.of(loadedClass);
        } catch (ClassNotFoundException e) {
            // Ignore it
        }
        return Optional.empty();
    }

    public static MethodNode[] getMethods(Class<?> jreClass) {
        StandardModeler modeller = new StandardModeler();
        Method[] methods = jreClass.getMethods();
        MethodNode[] methodNodes = new MethodNode[methods.length];
        for (int i=0; i<methods.length; i++) {
            methodNodes[i] = modeller.modelMethod(methods[i]);
        }
        return methodNodes;
    }
}
