package mx.kenzie.skriptlab.error;

/**
 * Represents an issue interpreting a syntax pattern.
 */
public class PatternDigestException extends RuntimeException {
    
    public PatternDigestException() {
        super();
    }
    
    public PatternDigestException(String message) {
        super(message);
    }
    
    public PatternDigestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PatternDigestException(Throwable cause) {
        super(cause);
    }
    
}
