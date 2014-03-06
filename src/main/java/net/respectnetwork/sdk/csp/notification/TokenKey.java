package net.respectnetwork.sdk.csp.notification;

import java.io.Serializable;

import net.respectnetwork.sdk.csp.model.UserProfile;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TokenKey implements Serializable {
    
    /** Serial ID*/
    private static final long serialVersionUID = 7525300202150885788L;

    /** Cloud Number */
    private String cloudNumber;
    
    /** Qualifier Key e.g. Phone/Email */
    private String tokenQualifier;

    /**
     * Constructor using all fields.
     * 
     * @param cloudNumber
     * @param tokenQualifier
     */
    public TokenKey(String cloudNumber, String tokenQualifier) {
        super();
        this.cloudNumber = cloudNumber;
        this.tokenQualifier = tokenQualifier;
    }

    /**
     * @return the cloudNumber
     */
    public String getCloudNumber() {
        return cloudNumber;
    }

    /**
     * @param cloudNumber the cloudNumber to set
     */
    public void setCloudNumber(String cloudNumber) {
        this.cloudNumber = cloudNumber;
    }

    /**
     * @return the tokenQualifier
     */
    public String getTokenQualifier() {
        return tokenQualifier;
    }

    /**
     * @param tokenQualifier the tokenQualifier to set
     */
    public void setTokenQualifier(String tokenQualifier) {
        this.tokenQualifier = tokenQualifier;
    }
    
    /**
     * toString Impl.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [cloudNumber=").append(cloudNumber).append(", tokenQualifier=").append(tokenQualifier)
                .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(cloudNumber)
        .append(tokenQualifier)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TokenKey){
            final TokenKey other = (TokenKey) obj;
            return new EqualsBuilder()
                .append(cloudNumber, other.cloudNumber)
                .append(tokenQualifier, other.tokenQualifier)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
    

}
