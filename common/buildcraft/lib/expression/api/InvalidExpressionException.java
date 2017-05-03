package buildcraft.lib.expression.api;

public class InvalidExpressionException extends Exception {
    private static final long serialVersionUID = 1288939405116211505L;

    public InvalidExpressionException(String message) {
        super(message);
    }

    public InvalidExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidExpressionException(Throwable cause) {
        super(cause);
    }
}
