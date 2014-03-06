/**
 * 
 */
package net.respectnetwork.sdk.csp.xdi;

import xdi2.core.xri3.CloudNumber;

/**
 * Interface for Cloud Number Generation
 */
public interface CloudNumberGenerator {
    
    public CloudNumber generateCloudNumber(Character ch);

}
