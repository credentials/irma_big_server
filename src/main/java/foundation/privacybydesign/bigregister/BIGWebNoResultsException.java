package foundation.privacybydesign.bigregister;

/**
 * Thrown when the BIG search gives no results
 */
public class BIGWebNoResultsException extends Exception {
    public BIGWebNoResultsException() {
        super("No results");
    }
}
