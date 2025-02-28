package Main;

/**
 * Represents a project that can be allocated to a student.
 * Each project has a title and a type (THEORETICAL or PRACTICAL).
 */
public class Project {
    private final String title;
    private final Type type;

    /**
     * Creates a new project with the specified title and type.
     *
     * @param title The title of the project
     * @param type The type of the project (THEORETICAL or PRACTICAL)
     */
    public Project(String title, Type type) {
        this.title = title;
        this.type = type;
    }

    /**
     * Gets the project's title.
     *
     * @return The project title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the project's type.
     *
     * @return The project type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns a string representation of this project.
     *
     * @return A string containing the project's title and type
     */
    @Override
    public String toString() {
        return "Project{" + "title='" + title + '\'' + ", type=" + type + '}';
    }
}