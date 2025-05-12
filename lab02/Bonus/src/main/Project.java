package main;

import java.util.Objects;

/**
 * Represents an project that extends the ProjectManagement functionality.
 * A project has a name, description, and is proposed by a teacher.
 */
public class Project extends ProjectManagement {

    private final String name;
    private final String description;
    private final Teacher proposer;

    /**
     * Constructs a new Project with the specified name, description, and proposing teacher.
     *
     * @param name The project name
     * @param description The project description
     * @param proposer The teacher who proposed this project
     */
    public Project(String name, String description, Teacher proposer) {
        super();
        this.name = name;
        this.description = description;
        this.proposer = proposer;
    }

    /**
     * Gets the name of this project.
     *
     * @return The project name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the description of this project.
     *
     * @return The project description
     */
    public String getDescription() {
        return this.description;
    }

    public Teacher getProposer() {
        return this.proposer;
    }

    /**
     * Generates a hash code for this project.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, proposer);
    }

    /**
     * Compares this Project with another object for equality.
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
        Project other = (Project) obj;
        return Objects.equals(name, other.name) && Objects.equals(description, other.description)
                && Objects.equals(proposer, other.proposer);

    }
}
