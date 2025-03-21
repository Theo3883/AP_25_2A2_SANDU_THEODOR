package main;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student
 * Extends the Person class with additional student-specific information.
 */
public class Student extends Person {
    private final String registrationNumber;
    private List<Project> projects;

    /**
     * Constructs a new Student with the specified name, date of birth, and registration number.
     *
     * @param name The student's name
     * @param dateOfBirth The student's date of birth
     * @param registrationNumber The student's unique registration number
     */
    public Student(String name, String dateOfBirth, String registrationNumber) {
        super(name, dateOfBirth);
        this.registrationNumber = registrationNumber;
        this.projects = new ArrayList<>();
    }

    /**
     * Gets the registration number of this student.
     *
     * @return The student's registration number
     */
    public String getRegistrationNumber() {
        return this.registrationNumber;
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
     * Generates a hash code for this student.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
        return super.hashCode() + this.registrationNumber.hashCode();
    }

    /**
     * Compares this Student with another object for equality.
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
        Student student = (Student) obj;
        return super.equals(obj) && this.registrationNumber.equals(student.registrationNumber);
    }
}
