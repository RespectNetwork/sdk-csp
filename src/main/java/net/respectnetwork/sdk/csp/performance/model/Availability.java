package net.respectnetwork.sdk.csp.performance.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class Availability implements Serializable{
    


	/** Generated Serial Id */
	private static final long serialVersionUID = -9148951088986605987L;


    /** Cloud Name */
    private String cloudname;
    
    /** Availability */
    private int available;
    
    /** Error */
    private String error;

     
    /**
     * @return the firstName
     */
    public String getCloudname() {
        return cloudname;
    }
    /**
     * @param cloudname the cloudname to set
     */
    public void setCloudname(String cloudname) {
        this.cloudname = cloudname;
    }
    
    /**
     * @return the available
     */
    public int getAvailable() {
        return available;
    }
    /**
     * @param firstName the firstName to set
     */
    public void setAvailable(int available) {
        this.available = available;
    }

  
    
    public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Availability [cloudname=").append(cloudname)
               .append("available=").append(available)
                .append("]");
        return builder.toString();
    }
    
    /**
     * Hash Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(cloudname)
        .append(available)
        .toHashCode();
    }


    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Availability){
            final Availability other = (Availability) obj;
            return new EqualsBuilder()
                .append(cloudname, other.cloudname)
                .append(available, other.available)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
}
