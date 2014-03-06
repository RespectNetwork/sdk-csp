package net.respectnetwork.sdk.csp.xdi;

import xdi2.core.xri3.CloudNumber;

public class XDIClientClouldNumberGenerator implements CloudNumberGenerator {

    @Override
    public CloudNumber generateCloudNumber(Character ch) {
        return CloudNumber.createRandom(ch);
    }
}
