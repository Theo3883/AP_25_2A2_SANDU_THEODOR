package main;

import java.util.Arrays;
import java.util.Objects;

/**
 * Manages collections of students, teachers, and projects
 * Provides methods for adding and retrieving entities while preventing duplicates
 */
public class ProjectManagement {
    private Student[] students;
    private Teacher[] teachers;
    private Project[] projects;

    private int studentNumber = 0;
    private int teacherNumber = 0;
    private int projectNumber = 0;

    /**
     * Constructs a new ProjectManagement instance
     */
    public ProjectManagement() {
        this.students = new Student[10];
        this.teachers = new Teacher[10];
        this.projects = new Project[10];
    }

    /**
     * Adds a student to the management system if not already present
     *
     * @param student The student to be added
     * @return true if the student was successfully added, false otherwise
     */
    public boolean addStudent(Student student) {
        if(student == null) return false;

        // Check if student already exists
        if(Arrays.stream(students, 0, studentNumber).anyMatch(s -> s != null && s.equals(student))) {
            return false;
        }

        // Expand array if needed
        if(studentNumber >= students.length) {
            students = Arrays.copyOf(students, students.length * 2);
        }

        students[studentNumber++] = student;
        return true;
    }

    /**
     * Adds a teacher to the management system if not already present.
     *
     * @param teacher The teacher to be added
     * @return true if the teacher was successfully added, false otherwise
     */
    public boolean addTeacher(Teacher teacher) {
        if(teacher == null) return false;

        if(Arrays.stream(teachers, 0, teacherNumber).anyMatch(t -> t != null && t.equals(teacher))) {
            return false;
        }
        if(teacherNumber >= teachers.length) {
            teachers = Arrays.copyOf(teachers, teachers.length * 2);
        }

        teachers[teacherNumber++] = teacher;
        return true;
    }

    /**
     * Adds a project to the management system if not already present.
     *
     * @param project The project to be added
     */
    public void addProject(Project project) {
        if(project == null) return;

        if(Arrays.stream(projects, 0, projectNumber).anyMatch(p -> p != null && p.equals(project))) {
            return;
        }


        if(projectNumber >= projects.length) {
            projects = Arrays.copyOf(projects, projects.length * 2);
        }

        projects[projectNumber++] = project;
    }

    /**
     * Returns an array containing all students in the system.
     *
     * @return Array of students without null elements
     */
    public Student[] getStudents() {
        return Arrays.copyOf(students, studentNumber);
    }

    /**
     * Returns an array containing all teachers in the system.
     *
     * @return Array of teachers without null elements
     */
    public Teacher[] getTeachers() {
        return Arrays.copyOf(teachers, teacherNumber);
    }

    /**
     * Returns an array containing all projects in the system.
     *
     * @return Array of projects without null elements
     */
    public Project[] getProjects() {
        return Arrays.copyOf(projects, projectNumber);
    }

    /**
     * Generates a hash code for this project management instance.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
      return Objects.hash(Arrays.hashCode(students), Arrays.hashCode(teachers), Arrays.hashCode(projects));
    }

    /**
     * Compares this ProjectManagement instance with another object for equality.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProjectManagement other = (ProjectManagement) obj;
        return Arrays.equals(students, other.students) &&
                Arrays.equals(teachers, other.teachers) &&
                Arrays.equals(projects, other.projects);
    }
}