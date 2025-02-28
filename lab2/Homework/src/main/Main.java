package main;

/**
 * Main class demonstrating the functionality of the project management system.
 * Creates and manipulates teachers, students, projects, and solutions.
 */

public class Main {
    public static void main(String[] args) {
        // Create a project management system
        ProjectManagement pm = new ProjectManagement();

        // Create teachers
        Teacher teacher1 = new Teacher("John Smith", "1980-05-15", new String[] {"Web Development", "Mobile Apps"});
        Teacher teacher2 = new Teacher("Mary Johnson", "1975-11-23", new String[] {"Database Design", "AI"});

        // Add teachers to the system
        pm.addTeacher(teacher1);
        pm.addTeacher(teacher2);

        // Try to add the same teacher again (should return false)
        boolean result = pm.addTeacher(teacher1);
        System.out.println("Adding same teacher again: " + (result ? "Succeeded" : "Failed"));

        // Create students
        Student student1 = new Student("Alice Brown", "2000-03-10", "S001");
        Student student2 = new Student("Bob Wilson", "2001-07-22", "S002");
        Student student3 = new Student("Carol Davis", "1999-12-05", "S003");

        // Add students to the system
        pm.addStudent(student1);
        pm.addStudent(student2);
        pm.addStudent(student3);

        // Create projects
        Project project1 = new Project("Web Application", "Build a responsive web app", teacher1);
        Project project2 = new Project("Mobile App", "Create an Android app", teacher2);

        // Add projects to the system
        pm.addProject(project1);
        pm.addProject(project2);


        // Add students to projects
        project1.addStudent(student1);
        project1.addStudent(student2);
        project2.addStudent(student3);

        // Create solutions
        Solution solution1 = new Solution(project1);
        solution1.addImplementation("Frontend implementation with React");
        solution1.addImplementation("Backend implementation with Spring Boot");

        Solution solution2 = new Solution(project2);
        solution2.addImplementation("Android app using Kotlin");

        // Print information
        System.out.println("\nTeachers in the system: " + pm.getTeachers().length);
        System.out.println("Students in the system: " + pm.getStudents().length);
        System.out.println("Projects in the system: " + pm.getProjects().length);

        System.out.println("\nProject 1: " + project1.getName());
        System.out.println("Description: " + project1.getDescription());
        System.out.println("Proposer: " + project1.getProposer().getName());

        System.out.println("\nStudents enrolled in Project 1:");
        for (Student s : project1.getStudents()) {
            if (s != null) {
                System.out.println("- " + s.getName() + " (" + s.getRegistrationNumber() + ")");
            }
        }

        System.out.println("\nSolution implementations for Project 1:");
        for (String impl : solution1.getImplementations()) {
            System.out.println("- " + impl);
        }
    }
}