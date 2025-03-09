package main;

import java.util.Objects;
/**
 * Represents a person with basic identifying information.
 * This class serves as a base class for more specific person types like Student and Teacher.
 */
public class Person {
    private final String name;
    private final String dateOfBirth;

    /**
     * Constructs a new Person with the specified name and date of birth.
     *
     * @param name The person's name
     * @param dateOfBirth The person's date of birth
     */
    public Person(String name, String dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }
    /**
     * Gets the name of this person.
     *
     * @return The person's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the date of birth of this person.
     *
     * @return The person's date of birth
     */
    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    /**
     * Generates a hash code for this person.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.dateOfBirth);
    }

    /**
     * Compares this Person with another object for equality.
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
        Person person = (Person) obj;
        return Objects.equals(this.name, person.name) && Objects.equals(this.dateOfBirth, person.dateOfBirth);
    }

}
