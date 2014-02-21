package net.respectnetwork.sdk.csp;

import java.security.PrivateKey;

import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;

/**
 * This interface represents information required for using the SDK.
 */
public interface CSPInformation {

	/*
	 * Information about the CSP
	 */

	public CloudNumber getCspCloudNumber();
	public String getCspRegistryXdiEndpoint();
	public String getCspCloudBaseXdiEndpoint();
	public String getCspSecretToken();
	public PrivateKey getCspPrivateKey();

	/*
	 * Information about RN
	 */

	public CloudNumber getRnCloudNumber();
	public String getRnRegistrationServiceXdiEndpoint();
	public XDI3Segment getRnCspLinkContract();
	public String getRnCspSecretToken();

	/*
	 * Convenience methods for constructing messages
	 */

	public MessageCollection createMessageCollectionToRN(MessageEnvelope messageEnvelope);
	public MessageCollection createMessageCollectionToCSP(MessageEnvelope messageEnvelope);
	public MessageCollection createMessageCollectionToCloud(MessageEnvelope messageEnvelope, CloudNumber cloudNumber);

	public void prepareMessageToRN(Message message);
	public void prepareMessageToCSP(Message message);
	public void prepareMessageToCloud(Message message, CloudNumber cloudNumber, String secretToken);
}
