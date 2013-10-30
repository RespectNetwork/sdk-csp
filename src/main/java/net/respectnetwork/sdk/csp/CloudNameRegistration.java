package net.respectnetwork.sdk.csp;

import xdi2.core.xri3.XDI3Segment;

public class CloudNameRegistration {

	private XDI3Segment cloudName;
	private XDI3Segment cloudNamePeerRootXri;
	private XDI3Segment cloudNumber;
	private XDI3Segment cloudNumberPeerRootXri;

	CloudNameRegistration(XDI3Segment cloudName, XDI3Segment cloudNamePeerRootXri, XDI3Segment cloudNumber, XDI3Segment cloudNumberPeerRootXri) {

		this.cloudName = cloudName;
		this.cloudNamePeerRootXri = cloudNamePeerRootXri;
		this.cloudNumber = cloudNumber;
		this.cloudNumberPeerRootXri = cloudNumberPeerRootXri;
	}

	public XDI3Segment getCloudName() {

		return this.cloudName;
	}

	public void setCloudName(XDI3Segment cloudName) {

		this.cloudName = cloudName;
	}

	public XDI3Segment getCloudNamePeerRootXri() {

		return this.cloudNamePeerRootXri;
	}

	public void setCloudNamePeerRootXri(XDI3Segment cloudNamePeerRootXri) {

		this.cloudNamePeerRootXri = cloudNamePeerRootXri;
	}

	public XDI3Segment getCloudNumber() {

		return this.cloudNumber;
	}

	public void setCloudNumber(XDI3Segment cloudNumber) {

		this.cloudNumber = cloudNumber;
	}

	public XDI3Segment getCloudNumberPeerRootXri() {

		return this.cloudNumberPeerRootXri;
	}

	public void setCloudNumberPeerRootXri(XDI3Segment cloudNumberPeerRootXri) {

		this.cloudNumberPeerRootXri = cloudNumberPeerRootXri;
	}
}
