package main;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a solution to a project.
 * Contains implementation details and extends ProjectManagement functionality.
 */
public class Solution extends ProjectManagement{
    private final Project project;
    private int numberOfImplementations=0;
    private String[] implementations;

    /**
     * Constructs a new Solution for the specified project
     *
     * @param project The project this solution is for
     */
    public Solution(Project project) {
        super();
        this.project = project;
        this.implementations = new String[10];
    }
    /**
     * Gets the project associated with this solution.
     *
     * @return The project this solution is for
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * Adds an implementation detail to this solution
     *
     * @param implementation A string describing an implementation detail
     */
    public void addImplementation(String implementation) {
        implementations[numberOfImplementations++] = implementation;
    }

    /**
     * Gets all implementation details for this solution.
     *
     * @return Array of implementation details without null elements
     */
    public String[] getImplementations() {
        return Arrays.copyOf(implementations, numberOfImplementations);
    }

    /**
     * Generates a hash code for this solution.
     *
     * @return A hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),project, numberOfImplementations, Arrays.hashCode(implementations));
    }

    /**
     * Compares this Solution with another object for equality.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Solution)) {
            return false;
        }
        Solution other = (Solution) obj;
        return Objects.equals(project, other.project) && Objects.equals(numberOfImplementations, other.numberOfImplementations)
                && Arrays.equals(implementations, other.implementations);
    }

}
