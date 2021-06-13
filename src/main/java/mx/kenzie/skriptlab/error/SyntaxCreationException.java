package mx.kenzie.skriptlab.error;

public class SyntaxCreationException extends RuntimeException {
    public SyntaxCreationException() {
        super();
    }
    
    public SyntaxCreationException(String message) {
        super(message);
    }
    
    public SyntaxCreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SyntaxCreationException(Throwable cause) {
        super(cause);
    }
    
    protected SyntaxCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
