package foundation.privacybydesign.bigregister;

/**
 * Thrown when the BIG search gives too many results
 */
public class BIGWebTooManyException extends Exception {
    public BIGWebTooManyException() {
        super("Too many results");
    }
}
