package net.respectnetwork.sdk.csp.notification;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTokenManager implements TokenManager {
    
    /** Length of the Token Generated */
    private static int TOKEN_SIZE = 6;
    
    
    private static final Logger logger = LoggerFactory
            .getLogger(BasicTokenManager.class);
    
    /** Token Cache */
    private Cache tokenCache; 

    
    /**
     * @return the tokenCache
     */
    public Cache getTokenCache() {
        return tokenCache;
    }

    /**
     * @param tokenCache the tokenCache to set
     */
    public void setTokenCache(Cache tokenCache) {
        this.tokenCache = tokenCache;
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
        
        // Add the token to  the cache : key =  cloudnumber;
        Element element = new Element(tokenKey, randAN);
        logger.debug("Adding {} : {} to token cache", tokenKey.toString(), randAN);
        tokenCache.put(element);        
        return randAN;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateToken(TokenKey tokenKey, String token)
        throws TokenException {
        
        for (Object key: tokenCache.getKeys()) {
            logger.debug("KEY = " + key.toString());
            Element element = tokenCache.get(key);
            if(element != null ){
                logger.debug("Value = " + element.getObjectValue().toString());
            } else {
                logger.debug("No value found for  key: {}", key.toString() );
            }
        }

        boolean foundMatch = false;
        Element element = tokenCache.get(tokenKey);
        if (element != null) {
            logger.debug("Cache hit for {} : {}", tokenKey);

            String value = (String)element.getObjectValue();
            
            if (value != null && value.equals(token)) {
                logger.debug("Cached token = {}", token);

                foundMatch = true;
            }
        } else {
            logger.debug("No Cache hit for {} : {}", tokenKey);
        }
                
        return foundMatch;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void inValidateToken(TokenKey tokenKey)
        throws TokenException {
        
        tokenCache.remove(tokenKey);        
        
    }

}
