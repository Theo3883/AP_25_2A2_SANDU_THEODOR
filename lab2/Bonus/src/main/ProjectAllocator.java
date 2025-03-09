package main;

import java.util.*;

public class ProjectAllocator {
    /**
     * Allocates projects to students using a greedy two-pass algorithm.
     *
     * @param students The list of students to allocate projects to
     * @return A mapping of students to their allocated projects
     */
    public Map<Student, Project> allocateProjects(List<Student> students) {
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