package main;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        // Test cases with different sizes
        int[] testSizes = {100, 500, 1000, 5000};

        for (int size : testSizes) {
            System.out.println("\nTesting with size: " + size);

            // Generate random test data
            ProjectManagement pm = generateRandomInstance(size);
            List<Student> students = Arrays.asList(pm.getStudents());

            // Measure memory before allocation
            long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Measure time
            ProjectAllocator allocator = new ProjectAllocator();
            long startTime = System.nanoTime();
            Map<Student, Project> allocation = allocator.allocateProjects(students);
            long endTime = System.nanoTime();

            // Measure memory after allocation
            long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            // Calculate statistics
            int allocatedStudents = (int) allocation.values().stream().filter(Objects::nonNull).count();
            double successRate = (double) allocatedStudents / size * 100;

            // Print results
            System.out.printf("Execution time: %.2f ms%n", (endTime - startTime) / 1_000_000.0);
            System.out.printf("Memory used: %.2f MB%n", (afterMemory - beforeMemory) / (1024.0 * 1024.0));
            System.out.printf("Students allocated: %d/%d (%.2f%%)%n", allocatedStudents, size, successRate);
        }
    }

    private static ProjectManagement generateRandomInstance(int size) {
        ProjectManagement pm = new ProjectManagement();

        // Generate teachers (1 teacher per 10 students)
        int teacherCount = Math.max(1, size / 10);
        Teacher[] teachers = new Teacher[teacherCount];
        for (int i = 0; i < teacherCount; i++) {
            teachers[i] = new Teacher(
                    "Teacher" + i,
                    generateRandomDate(),
                    new String[]{"Specialization" + i}
            );
            pm.addTeacher(teachers[i]);
        }

        // Generate projects (1.2 times the number of students)
        int projectCount = (int)(size * 1.2);
        Project[] projects = new Project[projectCount];
        for (int i = 0; i < projectCount; i++) {
            Teacher randomTeacher = teachers[ThreadLocalRandom.current().nextInt(teacherCount)];
            projects[i] = new Project(
                    "Project" + i,
                    "Description" + i,
                    randomTeacher
            );
            pm.addProject(projects[i]);
        }

        // Generate students with random preferences
        for (int i = 0; i < size; i++) {
            Student student = new Student(
                    "Student" + i,
                    generateRandomDate(),
                    "S" + String.format("%04d", i)
            );

            // Assign 2-4 random project preferences
            int prefCount = ThreadLocalRandom.current().nextInt(2, 5);
            List<Project> prefs = new ArrayList<>();
            for (int j = 0; j < prefCount; j++) {
                Project randomProject;
                do {
                    randomProject = projects[ThreadLocalRandom.current().nextInt(projectCount)];
                } while (prefs.contains(randomProject));
                prefs.add(randomProject);
            }
            student.setPreferences(prefs);
            pm.addStudent(student);
        }

        return pm;
    }

    private static String generateRandomDate() {
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.of(2005, 12, 31);
        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay).format(DateTimeFormatter.ISO_DATE);
    }
}