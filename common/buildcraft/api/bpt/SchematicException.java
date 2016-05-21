package buildcraft.api.bpt;

public class SchematicException extends Exception {
    private static final long serialVersionUID = -8094725539652792561L;

    public SchematicException() {}

    public SchematicException(String message) {
        super(message);
    }

    public SchematicException(Throwable cause) {
        super(cause);
    }

    public SchematicException(String message, Throwable cause) {
        super(message, cause);
    }

}
