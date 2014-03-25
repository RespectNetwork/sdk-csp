package net.respectnetwork.sdk.csp.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class UserProfile {
      
    /** Name */
    private String name;    
    
    /** Nick Name */
    private String nickName;  
    
    /** Street */
    private String street;

    /** City */
    private String city;

    /** State */
    private String state;

    /** PostalCode */
    private String postalcode; 


    /**
     * @return the firstName
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Email
     */
    private String email;  
    
    /**
     * Mobile Phone
     */
    private String phone;
    
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String name) {
        this.name = name;
    }

    /**
     * @return the nickName
     */
    public String getNickName() {
        return nickName;
    }
    /**
     * @param nickName the nickName to set
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    

    /**
     * 
     * @return
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * 
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * 
     * @return
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * 
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    
    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }
    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }
    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }
    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }
    /**
     * @return the state
     */
    public String getState() {
        return state;
    }
    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
    /**
     * @return the postalcode
     */
    public String getPostalcode() {
        return postalcode;
    }
    /**
     * @param postalcode the postalcode to set
     */
    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }
    /**
     * toString Impl.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageForm [email=").append(email).append(", phone=").append(phone)
                .append(", name=").append(name)
                .append(", nickName=").append(nickName)
                .append(", street=").append(street)
                .append(", city=").append(city)
                .append(", state=").append(state)
                .append(", postalcode=").append(postalcode)                
                .append("]");
        return builder.toString();
    }
    
    /**
     * HashCode Implementation using apache-lang
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
        .append(name)
        .append(nickName)
        .append(street)
        .append(city)
        .append(state)
        .append(postalcode)
        .append(email)
        .append(phone)
        .toHashCode();
    }
    
    /**
     * Equals Implementation using apache-lang
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserProfile){
            final UserProfile other = (UserProfile) obj;
            return new EqualsBuilder()
                .append(name, other.name)
                .append(nickName, other.nickName)
                .append(street, other.street)
                .append(city, other.city)
                .append(state, other.state)
                .append(postalcode, other.postalcode)                
                .append(email, other.email)
                .append(phone, other.phone)
                .isEquals();
        } else{
            return false;
        }
    }
    
    
    
}
