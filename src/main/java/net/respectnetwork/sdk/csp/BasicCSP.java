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
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;

public class BasicCSP implements CSP {

	private static final Logger log = LoggerFactory.getLogger(BasicCSP.class);

	public static final XDI3Segment REGISTRAR_LINK_CONTRACT = XDI3Segment.create("+registrar$do");

	private CSPInformation cspInformation;

	private XDIClient xdiClientRespectNetworkRegistrationService;
	private XDIClient xdiClientHostingEnvironmentRegistry;

	public BasicCSP(CSPInformation cspInformation) {

		this.cspInformation = cspInformation;

		this.xdiClientHostingEnvironmentRegistry = new XDIHttpClient(cspInformation.getHostingEnvironmentRegistryXdiEndpoint());
		this.xdiClientRespectNetworkRegistrationService = new XDIHttpClient(cspInformation.getRespectNetworkRegistrationServiceXdiEndpoint());
	}

	public void registerCloud(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		String cloudXdiEndpoint;

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getCspCloudNumber().getXri())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		XDI3Statement[] targetStatementsSet = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), cloudXdiEndpoint)
		};

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createSetOperation(Arrays.asList(targetStatementsSet).iterator());
		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		this.getXdiClientHostingEnvironmentRegistry().send(message.getMessageEnvelope(), null);

		log.debug("Cloud registered with Cloud Number " + cloudNumber + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);
	}

	public CloudNumber checkCloudNameAvailable(CloudName cloudName) throws Xdi2ClientException {

		CloudNumber cloudNumber;

		// prepare message

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber().getXri())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Segment targetAddress = cloudName.getPeerRootXri();

		message.createGetOperation(targetAddress);

		// send message and read result

		MessageResult messageResult = this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);

		if (relation == null) {

			log.debug("Cloud Name " + cloudName + " is available");

			return null;
		}

		cloudNumber = CloudNumber.fromXri(XdiPeerRoot.getXriOfPeerRootArcXri(relation.getTargetContextNodeXri().getFirstSubSegment()));

		// done

		log.debug("Cloud Name " + cloudName + " is already registered with Cloud Number " + cloudNumber);

		return cloudNumber;
	}

	public CloudNumber registerCloudName(CloudName cloudName) throws Xdi2ClientException {

		CloudNumber cloudNumber;

		// prepare message

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber().getXri())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF, XDIConstants.XRI_S_VARIABLE);

		message.createSetOperation(targetStatement);

		// send message and read result

		MessageResult messageResult = this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		cloudNumber = CloudNumber.fromXri(XdiPeerRoot.getXriOfPeerRootArcXri(relation.getTargetContextNodeXri().getFirstSubSegment()));

		// done

		log.debug("Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);

		return cloudNumber;
	}

	public void registerCloudName(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber().getXri())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF, cloudNumber.getPeerRootXri());

		message.createSetOperation(targetStatement);

		// send message and read result

		MessageResult messageResult = this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		cloudNumber = CloudNumber.fromXri(XdiPeerRoot.getXriOfPeerRootArcXri(relation.getTargetContextNodeXri().getFirstSubSegment()));

		// done

		log.debug("Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	public void setCloudXdiEndpoint(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException {

		// prepare message

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber().getXri())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatement = XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), cloudXdiEndpoint);

		message.createSetOperation(targetStatement);

		// send message

		this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

		// done

		log.debug("Cloud XDI endpoint " + cloudXdiEndpoint + " set for Cloud Number " + cloudNumber);
	}

	public void setCloudXdiEndpoint(CloudNumber cloudNumber) throws Xdi2ClientException {

		String cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		this.setCloudXdiEndpoint(cloudNumber, cloudXdiEndpoint);
	}

	public void setCloudSecretToken(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber().getXri(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getCspCloudNumber().getXri())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		// send message

		this.getXdiClientHostingEnvironmentRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("Secret token set for Cloud Number " + cloudNumber);
	}

	/*
	 * Helper methods
	 */

	private static String makeCloudXdiEndpoint(CSPInformation cspInformation, CloudNumber cloudNumber) {

		try {

			return cspInformation.getHostingEnvironmentCloudBaseXdiEndpoint() + URLEncoder.encode(cloudNumber.toString(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	/*
	 * Getters and setters
	 */

	public CSPInformation getCspInformation() {

		return this.cspInformation;
	}

	public void setCspInformation(CSPInformation cspInformation) {

		this.cspInformation = cspInformation;
	}

	public XDIClient getXdiClientRespectNetworkRegistrationService() {

		return this.xdiClientRespectNetworkRegistrationService;
	}

	public void setXdiClientRespectNetworkRegistrationService(XDIClient xdiClientRespectNetworkRegistrationService) {

		this.xdiClientRespectNetworkRegistrationService = xdiClientRespectNetworkRegistrationService;
	}

	public XDIClient getXdiClientHostingEnvironmentRegistry() {

		return this.xdiClientHostingEnvironmentRegistry;
	}

	public void setXdiClientHostingEnvironmentRegistry(XDIClient xdiClientHostingEnvironmentRegistry) {

		this.xdiClientHostingEnvironmentRegistry = xdiClientHostingEnvironmentRegistry;
	}
}
