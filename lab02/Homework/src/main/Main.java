package main;

import java.util.*;

/**
 * Main class demonstrating the functionality of the project management system
 * and implementing a student-project allocation algorithm based on Hall's theorem.
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

        // Create projects
        Project project1 = new Project("Web Application", "Build a responsive web app", teacher1);
        Project project2 = new Project("Mobile App", "Create an Android app", teacher2);
        Project project3 = new Project("Database System", "Design a relational database", teacher2);
        Project project4 = new Project("AI Project", "Implement a machine learning model", teacher1);

        // Add projects to the system
        pm.addProject(project1);
        pm.addProject(project2);
        pm.addProject(project3);
        pm.addProject(project4);

        // Create students with preferences
        Student student1 = new Student("Alice Brown", "2000-03-10", "S001");
        student1.setPreferences(Arrays.asList(project1, project2));

        Student student2 = new Student("Bob Wilson", "2001-07-22", "S002");
        student2.setPreferences(Arrays.asList(project1, project3));

        Student student3 = new Student("Carol Davis", "1999-12-05", "S003");
        student3.setPreferences(Arrays.asList(project3, project4));

        Student student4 = new Student("David Miller", "2000-08-17", "S004");
        student4.setPreferences(Arrays.asList(project1, project4));

        // Add students to the system
        pm.addStudent(student1);
        pm.addStudent(student2);
        pm.addStudent(student3);
        pm.addStudent(student4);

        // Create list of students
        List<Student> studentList = Arrays.asList(student1, student2, student3, student4);

        // Allocate projects to students
        Map<Student, Project> allocation = allocateProjects(studentList);

        // Print allocations
        System.out.println("\nProject Allocations:");
        for (Map.Entry<Student, Project> entry : allocation.entrySet()) {
            System.out.println(entry.getKey().getName() + " -> " +
                    (entry.getValue() != null ? entry.getValue().getName() : "No project"));

            // Add the student to the project
            if (entry.getValue() != null) {
                entry.getValue().addStudent(entry.getKey());
            }
        }

        // Create solutions for allocated projects
        Map<Project, Solution> solutions = new HashMap<>();
        for (Project project : new HashSet<>(allocation.values())) {
            if (project != null) {
                Solution solution = new Solution(project);
                solution.addImplementation("Initial implementation for " + project.getName());
                solutions.put(project, solution);
            }
        }

        // Print project details after allocation
        System.out.println("\nProject details after allocation:");
        for (Project project : pm.getProjects()) {
            System.out.println("\nProject: " + project.getName());
            System.out.println("Description: " + project.getDescription());
            System.out.println("Proposer: " + project.getProposer().getName());

            System.out.println("Students enrolled:");
            for (Student s : project.getStudents()) {
                if (s != null) {
                    System.out.println("- " + s.getName() + " (" + s.getRegistrationNumber() + ")");
                }
            }

            if (solutions.containsKey(project)) {
                System.out.println("Solution implementations:");
                for (String impl : solutions.get(project).getImplementations()) {
                    System.out.println("- " + impl);
                }
            }
        }
    }

    /**
     * Allocates projects to students based on their preferences.
     * Uses a two-pass algorithm:
     * 1. First does a simple greedy assignment
     * 2. Then tries to reassign projects to unmatched students when possible
     *
     * @param students The list of students to allocate projects to
     * @return A mapping of students to their allocated projects
     */
    private static Map<Student, Project> allocateProjects(List<Student> students) {
        Map<Student, Project> allocation = new HashMap<>();
        Set<Project> assignedProjects = new HashSet<>();

        // First pass - simple greedy assignment
        for (Student student : students) {
            List<Project> projectPreferences = student.getProjects();
            for (Project project : projectPreferences) {
                if (!assignedProjects.contains(project)) {
                    allocation.put(student, project);
                    assignedProjects.add(project);
                    break;
                }
            }
        }

        // Second pass - try to find projects for unassigned students
        for (Student student : students) {
            if (!allocation.containsKey(student)) {
                List<Project> projectPreferences = student.getProjects();
                for (Project wantedProject : projectPreferences) {
                    boolean reassigned = false;

                    for (Map.Entry<Student, Project> entry : allocation.entrySet()) {
                        Student otherStudent = entry.getKey();
                        Project otherProject = entry.getValue();

                        if (otherProject.equals(wantedProject)) {
                            // Check if this student has alternatives
                            for (Project alternative : otherStudent.getProjects()) {
                                if (!assignedProjects.contains(alternative) &&
                                        !alternative.equals(otherProject)) {
                                    // Reassign other student
                                    allocation.put(otherStudent, alternative);
                                    assignedProjects.add(alternative);
                                    assignedProjects.remove(wantedProject);
                                    // Assign the current student
                                    allocation.put(student, wantedProject);
                                    assignedProjects.add(wantedProject);
                                    reassigned = true;
                                    break;
                                }
                            }
                        }

                        if (reassigned) break;
                    }

                    if (reassigned) break;
                }
            }
        }

        return allocation;
    }

}