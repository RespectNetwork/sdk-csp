package net.respectnetwork.sdk.csp.notification;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TokenKey implements Serializable {
    
    /** Serial ID*/
    private static final long serialVersionUID = 7525300202150885788L;

    /** Cloud Number */
    private String cloudNumber;
    
    /** Qualifier Key e.g. Phone/Email */
    private String tokenQualifier;

    /** email address or phone number */
    private String value;

    /**
     * Constructor using all fields.
     * 
     * @param cloudNumber
     * @param tokenQualifier
     */
    public TokenKey(String cloudNumber, String tokenQualifier, String value) {
        super();
        this.cloudNumber = cloudNumber;
        this.tokenQualifier = tokenQualifier;
        this.value = value;
    }

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
     * @return value
     */
    public String getValue() {
		return value;
	}

	/**
	 * @param value the email address or phone number
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
     * toString Impl.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [cloudNumber=").append(cloudNumber).append(", tokenQualifier=").append(tokenQualifier)
                .append(", value=").append(value).append("]");
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
        .append(value)
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
                .append(value, other.value)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
    

}
