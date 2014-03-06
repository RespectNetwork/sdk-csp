package net.respectnetwork.sdk.csp.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import xdi2.core.xri3.CloudNumber;

public class CSPUserCredential {
      
    /**
     * Cloud Number
     */
    private CloudNumber cloudNumber;  
    
    /**
     * Secret Token
     */
    private String secretToken;   
    
    
    public CSPUserCredential(CloudNumber cloudNumber, String secretToken) {
        this.cloudNumber = cloudNumber;
        this.secretToken = secretToken;
    }

    public CloudNumber getCloudNumber() {
        return cloudNumber;
    }

    public void setCloudNumber(CloudNumber cloudNumber) {
        this.cloudNumber = cloudNumber;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * To String Implementation.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSPCredential [cloudNumber=").append(cloudNumber).append(", secretToken=").append(secretToken)
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
        .append(secretToken)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CSPUserCredential){
            final CSPUserCredential other = (CSPUserCredential) obj;
            return new EqualsBuilder()
                .append(cloudNumber, other.cloudNumber)
                .append(secretToken, other.secretToken)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
}
