package net.respectnetwork.sdk.csp;

public class CloudRegistration {

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
