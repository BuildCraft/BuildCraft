package buildcraft.lib.expression;

@SuppressWarnings("serial")
public class InvalidExpressionException extends Exception {
    public InvalidExpressionException(String message) {
        super(message);
    }
}