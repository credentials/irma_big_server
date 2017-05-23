package foundation.privacybydesign.bigregister;

/**
 * Thrown when there is a syntax error in the returned response by the BIG register.
 */
public class BIGFormatException extends Exception {
    public BIGFormatException(String message) {
        super(message);
    }
}
