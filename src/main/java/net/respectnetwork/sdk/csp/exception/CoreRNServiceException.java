package net.respectnetwork.sdk.csp.exception;

/**
 * Generic Respect Network Core Services Exception.
 */
public class CoreRNServiceException extends Exception {

    /** Generated Serial Id*/
    private static final long serialVersionUID = -2652621767494114242L;

    /**
     * Default Constructor
     */
    public CoreRNServiceException() {
    }

    /**
     * Constructor with Message Parameter
     * 
     * @param message
     */
    public CoreRNServiceException(String message) {
        super(message);
    }

    /**
     * Constructor with Throwable Parameter
     * 
     * @param throwable
     */
    public CoreRNServiceException(Throwable throwable) {
        super(throwable);
    }

    
    /**
     * Constructor with Message and Throwable Parameter
     * 
     * @param message
     * @param throwable
     */
    public CoreRNServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
