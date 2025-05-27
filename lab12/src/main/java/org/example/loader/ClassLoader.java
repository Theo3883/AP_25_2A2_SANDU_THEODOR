package org.example.loader;

import org.apache.logging.log4j.Logger;
import org.example.compiler.JavaCompiler;
import org.example.util.LoggerUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLoader {
    private static final Logger logger = LoggerUtil.getInstance().createLogger(ClassLoader.class);
    private final List<Class<?>> loadedClasses = new ArrayList<>();
    private final List<String> classPaths = new ArrayList<>();
    private URLClassLoader urlClassLoader;
    private final JavaCompiler javaCompiler;

    public ClassLoader() {
        javaCompiler = new JavaCompiler();
        initializeClassLoader();
    }

    public void clear() {
        loadedClasses.clear();
        classPaths.clear();
        initializeClassLoader();
    }

    private void initializeClassLoader() {
        try {
            List<URL> urls = new ArrayList<>();

            File targetClasses = new File("target/classes");
            if (targetClasses.exists()) {
                urls.add(targetClasses.toURI().toURL());
            }

            File testClasses = new File("target/test-classes");
            if (testClasses.exists()) {
                urls.add(testClasses.toURI().toURL());
            }

            File mavenRepo = new File(System.getProperty("user.home") + "/.m2/repository");
            if (mavenRepo.exists()) {
                addJarsFromDirectory(mavenRepo, urls);
            }

            urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
        } catch (Exception e) {
            logger.error("Error initializing class loader: " + e.getMessage());
        }
    }

    public void addClassPath(String path) {
        classPaths.add(path);
    }

    public List<Class<?>> loadClasses() throws Exception {
        List<String> javaFiles = new ArrayList<>();

        for (String path : classPaths) {
            File file = new File(path);
            if (file.isDirectory()) {
                collectJavaFilesFromDirectory(file, javaFiles);
            } else if (path.endsWith(".java")) {
                javaFiles.add(path);
            }
        }

        if (!javaFiles.isEmpty()) {
            logger.info("Compiling " + javaFiles.size() + " Java files...");
            boolean success = javaCompiler.compile(javaFiles);
            if (!success) {
                logger.error("Failed to compile some Java files. See errors above.");
            } else {
                logger.info("Compilation successful!");
            }
            initializeClassLoader();
        }

        for (String path : classPaths) {
            File file = new File(path);
            if (file.isDirectory()) {
                loadClassesFromDirectory(file);
            } else if (path.endsWith(".jar")) {
                loadClassesFromJar(path);
            } else if (path.endsWith(".class")) {
                loadClassFromFile(file);
            } else if (path.endsWith(".java")) {
                String className = getClassNameFromJavaFile(path);
                if (className != null) {
                    try {
                        Class<?> clazz = urlClassLoader.loadClass(className);
                        loadedClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        logger.error("Could not load compiled class: " + className);
                    }
                }
            }
        }
        return loadedClasses;
    }

    private void collectJavaFilesFromDirectory(File directory, List<String> javaFiles) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectJavaFilesFromDirectory(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file.getAbsolutePath());
            }
        }
    }

    public String getClassNameFromJavaFile(String javaFilePath) {
        File file = new File(javaFilePath);
        String fileName = file.getName();
        String className = fileName.substring(0, fileName.lastIndexOf('.'));
        
        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    String packageName = line.substring(8, line.indexOf(';'));
                    return packageName + "." + className;
                }
            }
            
            // If no package declaration, return just the class name
            return className;
        } catch (Exception e) {
            logger.error("Error reading Java file: " + e.getMessage());
            return null;
        }
    }

    public Class<?> loadClassByName(String className) {
        try {
            Class<?> clazz = urlClassLoader.loadClass(className);
            loadedClasses.add(clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean isLoadableClassName(String className) {
        if (className.contains("/") || className.contains("\\") || className.contains(".class")
                || className.contains(".jar")) {
            return false;
        }
        try {
            return urlClassLoader.loadClass(className) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public List<String> listAvailableClasses() throws Exception {
        List<String> availableClasses = new ArrayList<>();
        File targetClasses = new File("target/classes");

        if (targetClasses.exists()) {
            collectClassNamesFromDirectory(targetClasses, "", availableClasses);
        }

        return availableClasses;
    }

    private void collectClassNamesFromDirectory(File directory, String packagePrefix, List<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                String newPrefix = packagePrefix.isEmpty() ? file.getName() : packagePrefix + "." + file.getName();
                collectClassNamesFromDirectory(file, newPrefix, classNames);
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().replace(".class", "");
                classNames.add(packagePrefix.isEmpty() ? className : packagePrefix + "." + className);
            }
        }
    }

    private void addJarsFromDirectory(File directory, List<URL> urls) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                addJarsFromDirectory(file, urls);
            } else if (file.getName().endsWith(".jar")) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (Exception e) {
                    logger.error("Error adding JAR to classpath: " + file.getAbsolutePath());
                }
            }
        }
    }

    private void loadClassesFromDirectory(File directory) throws Exception {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadClassesFromDirectory(file);
            } else if (file.getName().endsWith(".class")) {
                loadClassFromFile(file);
            }
        }

    }

    private void loadClassesFromJar(String jarPath) throws Exception {
        try (JarFile jar = new JarFile(jarPath)) {
            jar.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .map(JarEntry::getName)
                    .map(name -> name.replace('/', '.').replace(".class", ""))
                    .forEach(className -> {
                        try {
                            loadedClasses.add(urlClassLoader.loadClass(className));
                        } catch (Exception e) {
                            logger.error("Error loading class " + className + ": " + e.getMessage());
                        }
                    });
        }
    }

    private void loadClassFromFile(File classFile) throws Exception {
        try {
            String absolutePath = classFile.getAbsolutePath();
            String classPath;

            if (absolutePath.contains("test-classes")) {
                classPath = absolutePath.substring(absolutePath.indexOf("test-classes") + 13)
                        .replace(File.separatorChar, '.')
                        .replace(".class", "");
            } else if (absolutePath.contains("classes")) {
                classPath = absolutePath.substring(absolutePath.indexOf("classes") + 8)
                        .replace(File.separatorChar, '.')
                        .replace(".class", "");
            } else {
                String relPath = classFile.getPath();
                classPath = relPath.replace(File.separatorChar, '.')
                        .replace(".class", "");
            }

            if (classPath.startsWith(".")) {
                classPath = classPath.substring(1);
            }

            Class<?> clazz = urlClassLoader.loadClass(classPath);
            loadedClasses.add(clazz);
        } catch (Exception e) {
            logger.error("Error loading class from file " + classFile.getName() + ": " + e.getMessage());
        }
    }
}
