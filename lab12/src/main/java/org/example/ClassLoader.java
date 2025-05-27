package org.example;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLoader {
    private final List<Class<?>> loadedClasses = new ArrayList<>();
    private final List<String> classPaths = new ArrayList<>();
    private URLClassLoader urlClassLoader;

    public void addClassPath(String path) {
        classPaths.add(path);
    }

    public List<Class<?>> loadClasses() throws Exception {
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

        for (String path : classPaths) {
            File file = new File(path);
            if (file.isDirectory()) {
                loadClassesFromDirectory(file);
            } else if (path.endsWith(".jar")) {
                loadClassesFromJar(path);
            } else if (path.endsWith(".class")) {
                loadClassFromFile(file);
            }
        }
        return loadedClasses;
    }

    private void addJarsFromDirectory(File directory, List<URL> urls) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addJarsFromDirectory(file, urls);
                } else if (file.getName().endsWith(".jar")) {
                    try {
                        urls.add(file.toURI().toURL());
                    } catch (Exception e) {
                        System.err.println("Error adding JAR to classpath: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void loadClassesFromDirectory(File directory) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadClassesFromDirectory(file);
                } else if (file.getName().endsWith(".class")) {
                    loadClassFromFile(file);
                }
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
                       System.err.println("Error loading class " + className + ": " + e.getMessage());
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
            } else {
                classPath = absolutePath.substring(absolutePath.indexOf("classes") + 8)
                                     .replace(File.separatorChar, '.')
                                     .replace(".class", "");
            }

            Class<?> clazz = urlClassLoader.loadClass(classPath);
            loadedClasses.add(clazz);
        } catch (Exception e) {
            System.err.println("Error loading class from file " + classFile.getName() + ": " + e.getMessage());
        }
    }
} 