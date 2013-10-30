package net.respectnetwork.sdk.csp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.Relation;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.util.XDI3Util;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;

public class CSP {

	private static final Logger log = LoggerFactory.getLogger(CSP.class);

	public static final String RESPECT_NETWORK_REGISTRAR_XDI_ENDPOINT = "http://registration.respectnetwork.net/registration";
	public static final XDI3Segment RESPECT_NETWORK_CLOUD_NUMBER = XDI3Segment.create("[@]!:uuid:299089fd-9d81-3c59-2990-89fd9d813c59");

	private static XDIClient xdiClientRespectNetworkRegistrar = new XDIHttpClient(RESPECT_NETWORK_REGISTRAR_XDI_ENDPOINT);

	private XDI3Segment cspCloudNumber;
	private String cspSecretToken;
	private String hostingEnvironmentRegistryXdiEndpoint;
	private String hostingEnvironmentCloudBaseXdiEndpoint;

	private XDIClient xdiClientHostingEnvironmentRegistry;

	public CSP(XDI3Segment cspCloudNumber, String cspSecretToken, String hostingEnvironmentRegistryXdiEndpoint, String hostingEnvironmentCloudBaseXdiEndpoint) {

		this.cspCloudNumber = cspCloudNumber;
		this.cspSecretToken = cspSecretToken;
		this.hostingEnvironmentRegistryXdiEndpoint = hostingEnvironmentRegistryXdiEndpoint;
		this.hostingEnvironmentCloudBaseXdiEndpoint = hostingEnvironmentCloudBaseXdiEndpoint;

		this.xdiClientHostingEnvironmentRegistry = new XDIHttpClient(hostingEnvironmentRegistryXdiEndpoint);
	}

	public CloudNameRegistration checkCloudNameAvailable(XDI3Segment cloudName) throws Xdi2ClientException {

		XDI3Segment cloudNamePeerRootXri;
		XDI3Segment cloudNumberPeerRootXri;
		XDI3Segment cloudNumber;

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspCloudNumber(), true);
		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(RESPECT_NETWORK_CLOUD_NUMBER)));
		message.setLinkContractXri(XDI3Segment.create("+registrar$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspSecretToken());

		cloudNamePeerRootXri = XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(cloudName));

		XDI3Segment targetAddress = cloudNamePeerRootXri;

		message.createGetOperation(targetAddress);

		MessageResult messageResult = xdiClientRespectNetworkRegistrar.send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudNamePeerRootXri, XDIDictionaryConstants.XRI_S_REF);

		if (relation == null) {

			log.debug("Cloud Name " + cloudName + " is available");

			return null;
		} else {

			cloudNumberPeerRootXri = relation.getTargetContextNodeXri();
			cloudNumber = XdiPeerRoot.getXriOfPeerRootArcXri(cloudNumberPeerRootXri.getFirstSubSegment());

			log.debug("Cloud Name " + cloudName + " is already registered with Cloud Number " + cloudNumber);

			return new CloudNameRegistration(cloudName, cloudNamePeerRootXri, cloudNumber, cloudNumberPeerRootXri);
		}
	}

	public CloudNameRegistration registerCloudName(XDI3Segment cloudName) throws Xdi2ClientException {

		XDI3Segment cloudNamePeerRootXri;
		XDI3Segment cloudNumberPeerRootXri;
		XDI3Segment cloudNumber;

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspCloudNumber(), true);
		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(RESPECT_NETWORK_CLOUD_NUMBER)));
		message.setLinkContractXri(XDI3Segment.create("+registrar$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspSecretToken());

		cloudNamePeerRootXri = XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(cloudName));

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(cloudNamePeerRootXri, XDIDictionaryConstants.XRI_S_REF, XDIConstants.XRI_S_VARIABLE);

		message.createSetOperation(targetStatement);

		MessageResult messageResult = xdiClientRespectNetworkRegistrar.send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudNamePeerRootXri, XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		cloudNumberPeerRootXri = relation.getTargetContextNodeXri();
		cloudNumber = XdiPeerRoot.getXriOfPeerRootArcXri(cloudNumberPeerRootXri.getFirstSubSegment());

		log.debug("Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);

		return new CloudNameRegistration(cloudName, cloudNamePeerRootXri, cloudNumber, cloudNumberPeerRootXri);
	}

	public CloudRegistration registerCloud(CloudNameRegistration cloudNameRegistration, String secretToken) throws Xdi2ClientException {

		String cloudXdiEndpoint;

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspCloudNumber(), true);
		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspCloudNumber())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspSecretToken());

		try {

			cloudXdiEndpoint = this.getHostingEnvironmentCloudBaseXdiEndpoint() + URLEncoder.encode(cloudNameRegistration.getCloudNumber().toString(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		XDI3Statement[] targetStatementsSet = new XDI3Statement[] {
				XDI3Statement.fromRelationComponents(cloudNameRegistration.getCloudNamePeerRootXri(), XDIDictionaryConstants.XRI_S_REF, cloudNameRegistration.getCloudNumberPeerRootXri()),
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDI3Segment.create("$xdi<$uri>&")), cloudXdiEndpoint)
		};

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createSetOperation(Arrays.asList(targetStatementsSet).iterator());
		message.createOperation(XDI3Segment.create("$do$digest$secret<$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		xdiClientHostingEnvironmentRegistry.send(message.getMessageEnvelope(), null);

		log.debug("Cloud registered with Cloud Number " + cloudNameRegistration.getCloudNumber() + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);

		return new CloudRegistration(cloudXdiEndpoint);
	}

	public void registerCloudXdiUrl(CloudNameRegistration cloudNameRegistration, CloudRegistration cloudRegistration) throws Xdi2ClientException {

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspCloudNumber(), true);
		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(RESPECT_NETWORK_CLOUD_NUMBER)));
		message.setLinkContractXri(XDI3Segment.create("+registrar$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspSecretToken());

		XDI3Statement targetStatement = XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDI3Segment.create("$xdi<$uri>&")), cloudRegistration.getCloudXdiEndpoint());

		message.createSetOperation(targetStatement);

		xdiClientRespectNetworkRegistrar.send(message.getMessageEnvelope(), null);

		log.debug("Cloud XDI URL " + "registered with Cloud Number " + cloudNameRegistration.getCloudNumber() + " and Cloud XDI endpoint " + cloudRegistration.getCloudXdiEndpoint());
	}

	public void generateCloudSecretToken(CloudNameRegistration cloudNameRegistration, String secretToken) throws Xdi2ClientException {

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspCloudNumber(), true);
		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspCloudNumber())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspSecretToken());

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createOperation(XDI3Segment.create("$do$digest$secret<$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		xdiClientHostingEnvironmentRegistry.send(message.getMessageEnvelope(), null);

		log.debug("Secret Token generated with Cloud Number " + cloudNameRegistration.getCloudNumber());
	}

	/*
	 * Getters and setters
	 */

	public XDI3Segment getCspCloudNumber() {

		return this.cspCloudNumber;
	}

	public void setCspCloudNumber(XDI3Segment cspCloudNumber) {

		this.cspCloudNumber = cspCloudNumber;
	}

	public String getCspSecretToken() {

		return this.cspSecretToken;
	}

	public void setCspSecretToken(String cspSecretToken) {

		this.cspSecretToken = cspSecretToken;
	}

	public String getHostingEnvironmentRegistryXdiEndpoint() {

		return this.hostingEnvironmentRegistryXdiEndpoint;
	}

	public void setHostingEnvironmentRegistryXdiEndpoint(String hostingEnvironmentRegistryXdiEndpoint) {

		this.hostingEnvironmentRegistryXdiEndpoint = hostingEnvironmentRegistryXdiEndpoint;
	}

	public String getHostingEnvironmentCloudBaseXdiEndpoint() {

		return this.hostingEnvironmentCloudBaseXdiEndpoint;
	}

	public void setHostingEnvironmentCloudBaseXdiEndpoint(String hostingEnvironmentCloudBaseXdiEndpoint) {

		this.hostingEnvironmentCloudBaseXdiEndpoint = hostingEnvironmentCloudBaseXdiEndpoint;
	}

	public XDIClient getXdiClientHostingEnvironmentRegistry() {

		return this.xdiClientHostingEnvironmentRegistry;
	}

	public void setXdiClientHostingEnvironmentRegistry(XDIClient xdiClientHostingEnvironmentRegistry) {

		this.xdiClientHostingEnvironmentRegistry = xdiClientHostingEnvironmentRegistry;
	}
}
