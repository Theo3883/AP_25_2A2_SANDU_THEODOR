package org.example.command;

import org.example.loader.ClassLoader;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BytecodeCommand extends Command {

    private final ClassLoader classLoader;

    public BytecodeCommand(ClassLoader classLoader) {
        super("bytecode", "Display and manipulate bytecode for a specified class");
        this.classLoader = classLoader;
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: Please provide a command and class name");
            System.out.println("Usage: bytecode <show|instrument|generate> <class name or file path>");
            return true;
        }

        String operation = args[0].toLowerCase();

        switch (operation) {
            case "show":
                if (args.length < 2) {
                    System.out.println("Error: Please provide a class name or file path");
                    System.out.println("Examples:");
                    System.out.println("  bytecode show com.example.MyClass");
                    System.out.println("  bytecode show path/to/MyClass.class");
                    System.out.println("  bytecode show path/to/MyClass.java");
                    return true;
                }
                return showBytecode(args[1]);
            case "instrument":
                if (args.length < 2) {
                    System.out.println("Error: Please provide a class name or file path");
                    System.out.println("Examples:");
                    System.out.println("  bytecode instrument com.example.MyClass");
                    System.out.println("  bytecode instrument path/to/MyClass.class");
                    System.out.println("  bytecode instrument path/to/MyClass.java");
                    return true;
                }
                return instrumentClass(args[1]);
            case "generate":
                return generateClass(args.length > 1 ? args[1] : "GeneratedClass");
            default:
                System.out.println("Error: Unknown operation: " + operation);
                System.out.println("Available operations: show, instrument, generate");
                return true;
        }
    }

    private boolean showBytecode(String input) {
        try {
            String className = resolveClassNameOrPath(input);
            if (className == null) {
                return true;
            }

            Class<?> clazz = classLoader.loadClassByName(className);
            if (clazz == null) {
                System.out.println("Error: Class not found: " + className);
                System.out.println("Make sure to use the fully qualified class name (e.g., org.example.MyClass)");
                return true;
            }

            System.out.println("\nBytecode for class: " + clazz.getName());
            System.out.println("-".repeat(80));

            String resourceName = clazz.getName().replace('.', '/') + ".class";
            java.net.URL url = clazz.getClassLoader().getResource(resourceName);

            if (url == null) {
                System.out.println("Error: Could not find class file for: " + className);
                return true;
            }

            byte[] classBytes = Files.readAllBytes(Paths.get(url.toURI()));

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ClassReader cr = new ClassReader(classBytes);
            TraceClassVisitor tcv = new TraceClassVisitor(null, new Textifier(), pw);
            cr.accept(tcv, ClassReader.SKIP_DEBUG);

            System.out.println(sw);
            return true;

        } catch (Exception e) {
            System.out.println("Error displaying bytecode: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private boolean instrumentClass(String input) {
        try {
            String className = resolveClassNameOrPath(input);
            if (className == null) {
                return true;
            }

            Class<?> clazz = classLoader.loadClassByName(className);
            if (clazz == null) {
                System.out.println("Error: Class not found: " + className);
                System.out.println("Make sure to use the fully qualified class name (e.g., org.example.MyClass)");
                return true;
            }
            System.out.println("\nInstrumenting class: " + clazz.getName());
            System.out.println("-".repeat(80));

            String resourceName = clazz.getName().replace('.', '/') + ".class";
            java.net.URL url = clazz.getClassLoader().getResource(resourceName);

            if (url == null) {
                System.out.println("Error: Could not find class file for: " + className);
                return true;
            }
            byte[] classBytes = Files.readAllBytes(Paths.get(url.toURI()));

            ClassReader cr = new ClassReader(classBytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);


            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                    if (name.equals("<init>") || name.equals("<clinit>")) {
                        return mv;
                    }

                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();

                            // Adds "Entering method: [className].[methodName]"
                            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                            mv.visitLdcInsn(">>> Entering method: " + clazz.getName() + "." + name);
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                        }
                    };
                }
            };

            cr.accept(cv, 0);
            byte[] instrumentedBytes = cw.toByteArray();

            String outputPath = "instrumented_" + clazz.getSimpleName() + ".class";
            Files.write(Paths.get(outputPath), instrumentedBytes);

            System.out.println("Class successfully instrumented and saved to: " + outputPath);
            System.out.println("Each method now logs when it's entered.");
            return true;

        } catch (Exception e) {
            System.out.println("Error instrumenting class: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    private String resolveClassNameOrPath(String input) {
        if (input.contains("/") || input.contains("\\") || input.endsWith(".class") || input.endsWith(".java")) {
            File file = new File(input);
            if (!file.exists()) {
                System.out.println("Error: File not found: " + input);
                return null;
            }

            if (input.endsWith(".java")) {
                classLoader.addClassPath(input);
                try {
                    classLoader.loadClasses(); //compiles
                } catch (Exception e) {
                    System.out.println("Error compiling Java file: " + e.getMessage());
                    return null;
                }

                String className = classLoader.getClassNameFromJavaFile(input);
                if (className == null) {
                    System.out.println("Error: Could not determine class name from file: " + input);
                    return null;
                }
                return className;
            }

            if (input.endsWith(".class")) {
                classLoader.addClassPath(input);
                try {
                    classLoader.loadClasses();
                } catch (Exception e) {
                    System.out.println("Error loading class file: " + e.getMessage());
                    return null;
                }

                String probableClassName = input.substring(0, input.length() - 6).replace('/', '.').replace('\\', '.');

                if (probableClassName.contains(".")) {
                    String simpleClassName = probableClassName.substring(probableClassName.lastIndexOf('.') + 1);
                    try {
                        for (String availableClass : classLoader.listAvailableClasses()) {
                            if (availableClass.endsWith("." + simpleClassName)) {
                                return availableClass;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Warning: Could not verify class name. Using: " + probableClassName);
                    }
                }
                return probableClassName;
            }
        }
        return input;
    }

    private boolean generateClass(String className) {
        try {
            System.out.println("\nGenerating new class: " + className);
            System.out.println("-".repeat(80));

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

            MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();

            MethodVisitor greetMethod = cw.visitMethod(Opcodes.ACC_PUBLIC, "greet", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
            greetMethod.visitCode();
            greetMethod.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            greetMethod.visitInsn(Opcodes.DUP);
            greetMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            greetMethod.visitLdcInsn("Hello, ");
            greetMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            greetMethod.visitVarInsn(Opcodes.ALOAD, 1);
            greetMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            greetMethod.visitLdcInsn("!");
            greetMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            greetMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            greetMethod.visitInsn(Opcodes.ARETURN);
            greetMethod.visitMaxs(0, 0);
            greetMethod.visitEnd();

            MethodVisitor mainMethod = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            mainMethod.visitCode();
            mainMethod.visitTypeInsn(Opcodes.NEW, className);
            mainMethod.visitInsn(Opcodes.DUP);
            mainMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false);
            mainMethod.visitVarInsn(Opcodes.ASTORE, 1);

            mainMethod.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mainMethod.visitVarInsn(Opcodes.ALOAD, 1);
            mainMethod.visitLdcInsn("World");
            mainMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "greet", "(Ljava/lang/String;)Ljava/lang/String;", false);
            mainMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            mainMethod.visitInsn(Opcodes.RETURN);
            mainMethod.visitMaxs(0, 0);
            mainMethod.visitEnd();
            cw.visitEnd();

            byte[] classBytes = cw.toByteArray();

            String outputPath = className + ".class";
            Files.write(Paths.get(outputPath), classBytes);

            System.out.println("Class successfully generated and saved to: " + outputPath);
            System.out.println("The class has:");
            System.out.println("- A constructor");
            System.out.println("- A 'greet' method that takes a name and returns a greeting");
            System.out.println("- A main method that creates an instance and calls greet()");

            return true;

        } catch (Exception e) {
            System.out.println("Error generating class: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
}
