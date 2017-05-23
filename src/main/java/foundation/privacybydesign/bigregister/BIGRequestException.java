package foundation.privacybydesign.bigregister;

/**
 * Exception is thrown when something goes wrong while doing a BIG search.
 * This is likely a problem in the connection, in how we did the request, or on their end.
 */
public class BIGRequestException extends Exception {
    public BIGRequestException(String message) {
        super(message);
    }
}
