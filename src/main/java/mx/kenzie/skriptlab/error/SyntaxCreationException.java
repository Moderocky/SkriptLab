package mx.kenzie.skriptlab.error;

/**
 * An expected error in syntax creation.
 */
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
    
}
