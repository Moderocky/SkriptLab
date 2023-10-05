package mx.kenzie.skriptlab.error;

/**
 * Occurs when syntax was generated in an unusual (illegal) way,
 * creating a malformed result that cannot be executed or disposed of safely.
 * <p>
 * This indicates a fundamental misuse of the generator in a critical way,
 * so that the result syntax cannot be repaired and the generator cannot be expected to continue.
 */
public class AbnormalSyntaxCreationError extends Error {
    
    public AbnormalSyntaxCreationError() {
        super();
    }
    
    public AbnormalSyntaxCreationError(String message) {
        super(message);
    }
    
    public AbnormalSyntaxCreationError(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AbnormalSyntaxCreationError(Throwable cause) {
        super(cause);
    }
    
}
