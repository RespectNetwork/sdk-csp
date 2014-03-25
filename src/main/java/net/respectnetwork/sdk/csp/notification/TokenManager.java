package net.respectnetwork.sdk.csp.notification;

public interface TokenManager {
    
    /**
     * Create a Token  and persist it for  future validation
     * 
     * @param tokenKey
     * @return token string
     * @throws TokenManagerException
     */
    public String createToken(TokenKey tokenKey) throws TokenException;
    
    /**
     * Check that a token is associated with a cloudNumber
     *  
     * @param token key
     * @param token
     * @return whether token was verified
     * @throws TokenManagerException
     */
    public boolean validateToken(TokenKey tokenKey, String token) throws TokenException;
    
    
    /**
     * Invalidate a token so that it can no longer be used for verification.
     *  
     * @param cloudNumber
     * @param token
     * @throws TokenManagerException
     */
    public void inValidateToken(TokenKey tokenKey) throws TokenException;

}
