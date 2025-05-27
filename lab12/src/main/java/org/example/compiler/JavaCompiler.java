package org.example.compiler;

import javax.tools.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaCompiler {
    private final javax.tools.JavaCompiler compiler;
    private final StandardJavaFileManager fileManager;

    public JavaCompiler() {
        compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Java compiler not found. Ensure you are using a JDK, not just a JRE.");
        }
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public boolean compile(List<String> javaFilePaths) {
        try {
            List<File> files = new ArrayList<>();
            for (String path : javaFilePaths) {
                files.add(new File(path));
            }

            Iterable<? extends JavaFileObject> compilationUnits = 
                fileManager.getJavaFileObjectsFromFiles(files);
            
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add("target/classes");
            
            javax.tools.JavaCompiler.CompilationTask task = 
                compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            
            boolean success = task.call();
            
            if (!success) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    System.err.println(diagnostic.toString());
                }
            }
            
            return success;
        } catch (Exception e) {
            System.err.println("Compilation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                fileManager.close();
            } catch (Exception e) {
                System.err.println("Error closing file manager: " + e.getMessage());
            }
        }
    }
}
