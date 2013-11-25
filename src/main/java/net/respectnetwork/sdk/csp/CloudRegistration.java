package net.respectnetwork.sdk.csp;

import java.io.Serializable;

public class CloudRegistration implements Serializable {

	private static final long serialVersionUID = 1277934180298856390L;

	private String cloudXdiEndpoint;

	CloudRegistration(String cloudXdiEndpoint) {

		this.cloudXdiEndpoint = cloudXdiEndpoint;
	}

	public String getCloudXdiEndpoint() {
		
		return this.cloudXdiEndpoint;
	}

	public void setCloudXdiEndpoint(String cloudXdiEndpoint) {

		this.cloudXdiEndpoint = cloudXdiEndpoint;
	}
}
