package net.respectnetwork.sdk.csp.notification;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnotherBasicTokenManager implements TokenManager {
    
    /** Length of the Token Generated */
    private static int TOKEN_SIZE = 6;
    
    
    private static final Logger logger = LoggerFactory
            .getLogger(AnotherBasicTokenManager.class);
    
    /** Token Cache */
    private static ConcurrentHashMap<String,String> tokenCache = new ConcurrentHashMap<String,String>(); 

    
    /**
     * @return the tokenCache
     */
    public static ConcurrentHashMap<String,String> getTokenCache() {
        return tokenCache;
    }

    /**
     * @param tokenCache the tokenCache to set
     */
    public void setTokenCache(ConcurrentHashMap<String,String> tokenCache) {
        AnotherBasicTokenManager.tokenCache = tokenCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createToken(TokenKey tokenKey)
            throws TokenException {
        
        String randAN = RandomStringUtils.randomAlphanumeric(TOKEN_SIZE).toUpperCase();
        int i = 0;
        do
        {
           if(randAN.contains("0")|| 
                 (randAN.contains("1") && randAN.contains("I")) ||
                 randAN.contains("O") )
           {
              randAN = RandomStringUtils.randomAlphanumeric(TOKEN_SIZE).toUpperCase();             
           } else
           {
              break ;
           }
           i++;
        } while(i < 5);
        
        logger.debug("Generating new Token: {}", randAN);
        
        
        logger.debug("Adding {} : {} to token cache", tokenKey.toString(), randAN);
        tokenCache.put(tokenKey.toString(),randAN);        
        return randAN;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateToken(TokenKey tokenKey, String token)
        throws TokenException {
        if(tokenCache.get(tokenKey.toString()) != null && tokenCache.get(tokenKey.toString()).equalsIgnoreCase(token))
        {
           return true;
        }
                
        return false;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void inValidateToken(TokenKey tokenKey)
        throws TokenException {

        if(tokenKey!=null) {
        tokenCache.remove(tokenKey.toString());
        }
        
    }

}
