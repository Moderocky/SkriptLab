package mx.kenzie.skriptlab.error;

public class SyntaxLoadException extends RuntimeException {
    public SyntaxLoadException() {
        super();
    }
    
    public SyntaxLoadException(String message) {
        super(message);
    }
    
    public SyntaxLoadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SyntaxLoadException(Throwable cause) {
        super(cause);
    }
    
    protected SyntaxLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
