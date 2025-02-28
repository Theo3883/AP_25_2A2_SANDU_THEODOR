package Main;

import java.util.*;

/**
 * Main class that implements a student-project allocation algorithm based on Hall's theorem.
 * This algorithm allocates projects to students based on their preferences while checking
 * if Hall's condition is satisfied.
 */

public class Main {
    public static void main(String[] args) {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student("John", 20));
        students.add(new Student("Jane", 21));
        students.add(new Student("Jack", 22));
        students.add(new Student("Jill", 23));

        Project P1 = new Project("Project 1", Type.THEORETICAL);
        Project P2 = new Project("Project 2", Type.PRACTICAL);
        Project P3 = new Project("Project 3", Type.THEORETICAL);
        Project P4 = new Project("Project 4", Type.PRACTICAL);

        List<Project> projects = new ArrayList<>();
        projects.add(P1);
        projects.add(P2);
        projects.add(P3);
        projects.add(P4);

        students.get(0).setPreferences(List.of(P1, P2));
        students.get(1).setPreferences(List.of(P1, P3));
        students.get(2).setPreferences(List.of(P3, P4));
        students.get(3).setPreferences(List.of(P1, P4));

        Random random = new Random();
        System.out.println("One student is " + students.get(random.nextInt(students.size())).getName());
        System.out.println("One project is " + projects.get(random.nextInt(projects.size())).getTitle()+ "\n");

        Map<Student, Project> allocation = allocateProjects(students, projects);

        System.out.println("Project Allocations:");
        for (Map.Entry<Student, Project> entry : allocation.entrySet()) {
            System.out.println(entry.getKey().getName() + " -> " + entry.getValue().getTitle());
        }

    }

    /**
     * Allocates projects to students based on their preferences.
     * Uses a two-pass algorithm:
     * 1. First does a simple greedy assignment
     * 2. Then tries to reassign projects to unmatched students when possible
     *
     * @param students The list of students to allocate projects to
     * @param projects The list of available projects
     * @return A mapping of students to their allocated projects
     */
    private static Map<Student, Project> allocateProjects(List<Student> students, List<Project> projects) {
        // Check if Hall's condition is satisfied
        if (!checkHallCondition(students)) {
            System.out.println("Hall's condition not satisfied - some students may not get projects");
        }

        Map<Student, Project> allocation = new HashMap<>();
        Set<Project> assignedProjects = new HashSet<>();

        // First pass - simple greedy assignment
        for (Student student : students) {
            for (Project project : student.getProjects()) {
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
                // Find a student who has alternatives
                for (Project wantedProject : student.getProjects()) {
                    boolean reassigned = false;

                    for (Map.Entry<Student, Project> entry : allocation.entrySet()) {
                        Student otherStudent = entry.getKey();
                        Project otherProject = entry.getValue();

                        if (otherProject.equals(wantedProject)) {
                            // Check if this student has alternatives
                            for (Project alternative : otherStudent.getProjects()) {
                                if (!assignedProjects.contains(alternative)) {
                                    // Reassign other student
                                    allocation.put(otherStudent, alternative);
                                    assignedProjects.add(alternative);
                                    // Assign the current student
                                    allocation.put(student, wantedProject);
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

    /**
     * Checks if Hall's condition is satisfied for the given students and their preferences.
     * Hall's theorem states that a complete matching exists if and only if for every subset
     * of students, the number of distinct projects they collectively prefer is at least
     * equal to the number of students in that subset.
     *
     * @param students The list of students to check
     * @return true if Hall's condition is satisfied, false otherwise
     */
    private static boolean checkHallCondition(List<Student> students) {
        for (int mask = 1; mask < (1 << students.size()); mask++) {
            Set<Project> preferredProjects = new HashSet<>();
            int subsetSize = 0;

            for (int i = 0; i < students.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    subsetSize++;
                    preferredProjects.addAll(students.get(i).getProjects());
                }
            }

            if (preferredProjects.size() < subsetSize) {
                return false;
            }
        }

        return true;
    }
}