package Main;

import java.util.List;

/**
 * Represents a student who will be allocated a project.
 * Each student has a name, age, and a list of preferred projects.
 */
public class Student {

    private final String name;
    private final int age;
    private List<Project> projects;

    /**
     * Creates a new student with the specified name and age.
     *
     * @param name The student's name
     * @param age The student's age
     */
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
        this.projects = null;
    }

    /**
     * Gets the student's name.
     *
     * @return The student's name
     */
    public String getName() {

        return name;
    }

    /**
     * Gets the student's age.
     *
     * @return The student's age
     */
    public int getAge() {

        return age;
    }

    /**
     * Gets the student's project preferences.
     *
     * @return The list of projects preferred by this student
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Sets the student's project preferences.
     *
     * @param projects The list of projects preferred by this student
     */
    public void setPreferences(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Returns a string representation of this student.
     *
     * @return A string containing the student's name, age and project preferences
     */
    @Override
    public String toString() {
        return "Student{" + "name='" + name + '\'' + ", age=" + age + ", projects=" + projects + '}';
    }
}