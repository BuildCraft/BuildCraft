package buildcraft.lib.misc.data;

public class LoadingException extends Exception {
    private static final long serialVersionUID = -3439641111545783074L;

    public LoadingException() {}

    public LoadingException(String message) {
        super(message);
    }

    public LoadingException(Throwable cause) {
        super(cause);
    }

    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
