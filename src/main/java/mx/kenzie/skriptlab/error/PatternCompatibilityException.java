package mx.kenzie.skriptlab.error;

/**
 * Thrown when a syntax annotation (e.g. @Effect) is incompatible with its method signature.
 * <p>
 * This typically happens when:
 * - a pattern has fewer inputs than the method has parameters
 * - a pattern has more inputs than the method has parameters
 * - a method is dynamic but the pattern has no input to call the method from.
 */
public class PatternCompatibilityException extends RuntimeException {
    
    public PatternCompatibilityException() {
        super();
    }
    
    public PatternCompatibilityException(String message) {
        super(message);
    }
    
    public PatternCompatibilityException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PatternCompatibilityException(Throwable cause) {
        super(cause);
    }
    
}
