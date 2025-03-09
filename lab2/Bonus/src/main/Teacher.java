package main;

/**
 * Represents a teacher who can propose projects.
 * Extends the Person class with additional information about projects.
 */
public class Teacher extends Person {
    private final String[] projects;

    /**
     * Constructs a new Teacher with the specified name, date of birth, and projects.
     *
     * @param name The teacher's name
     * @param dateOfBirth The teacher's date of birth
     * @param projects Array of project names this teacher is involved with
     */
    public Teacher(String name, String dateOfBirth, String[] projects) {
        super(name, dateOfBirth);
        this.projects = projects;
    }

    /**
     * Gets the projects this teacher is involved with.
     *
     * @return Array of project names
     */
    public String[] getProjects() {
        return this.projects;
    }

    /**
     * Generates a hash code for this teacher.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
        return super.hashCode() + this.projects.length;
    }

    /**
     * Compares this Teacher with another object for equality.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Teacher teacher = (Teacher) obj;
        return super.equals(obj) && this.projects.length == teacher.projects.length;
    }
}
