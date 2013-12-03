package net.respectnetwork.sdk.csp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.constants.XDIClientConstants;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.Relation;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDILinkContractConstants;
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

	private XDIClient xdiClientRNRegistrationService;
	private XDIClient xdiClientCSPRegistry;

	public BasicCSP() {

	}

	public BasicCSP(CSPInformation cspInformation) {

		this.cspInformation = cspInformation;

		this.xdiClientCSPRegistry = new XDIHttpClient(cspInformation.getCspRegistryXdiEndpoint());
		this.xdiClientRNRegistrationService = new XDIHttpClient(cspInformation.getRnRegistrationServiceXdiEndpoint());
	}

	public void registerCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		String cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(
						XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), 
						secretToken)
		};

		XDI3Statement[] targetStatementsSet = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(
						XDI3Util.concatXris(cloudNumber.getPeerRootXri(), 
								XDI3Segment.create("<$xdi><$uri>&")), cloudXdiEndpoint)
		};

		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());
		message.createSetOperation(Arrays.asList(targetStatementsSet).iterator());

		// send message

		this.getXdiClientCSPRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In CSP: Cloud registered with Cloud Number " + cloudNumber + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);
	}

	public CloudNumber checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException {

		CloudNumber cloudNumber;

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Segment targetAddress = cloudName.getPeerRootXri();

		message.createGetOperation(targetAddress);

		// send message

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(message.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);

		if (relation == null) {

			log.debug("Cloud Name " + cloudName + " is available");

			return null;
		}

		cloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());

		// done

		log.debug("In RN: Cloud Name " + cloudName + " is already registered with Cloud Number " + cloudNumber);

		return cloudNumber;
	}

	public CloudNumber registerCloudNameInRN(CloudName cloudName) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope1 = new MessageEnvelope();

		Message message1 = messageEnvelope1.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message1.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message1.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message1.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatementSet1 = XDI3Statement.fromRelationComponents(
				cloudName.getPeerRootXri(), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDIConstants.XRI_S_VARIABLE);

		message1.createSetOperation(targetStatementSet1);

		// send message 1 and read result

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(message1.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		CloudNumber cloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());

		// prepare message 2 to RN

		MessageEnvelope messageEnvelope2 = new MessageEnvelope();

		Message message2 = messageEnvelope2.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXri(), true).createMessage(-1);
		message2.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message2.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message2.setSecretToken(this.getCspInformation().getCspSecretToken());

		String cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		XDI3Statement targetStatementSet2 = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint);

		message2.createSetOperation(targetStatementSet2);

		// send message 2

		this.getXdiClientRNRegistrationService().send(message2.getMessageEnvelope(), null);

		// done

		log.debug("In RN: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);

		return cloudNumber;
	}

	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message 1 to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message1 = messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXri(), true).createMessage(-1);
		message1.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message1.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message1.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatementSet1 = XDI3Statement.fromRelationComponents(
				cloudName.getPeerRootXri(), 
				XDIDictionaryConstants.XRI_S_REF, 
				cloudNumber.getPeerRootXri());

		message1.createSetOperation(targetStatementSet1);

		// prepare message 2 to RN

		Message message2 = messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXri(), true).createMessage(-1);
		message2.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message2.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message2.setSecretToken(this.getCspInformation().getCspSecretToken());

		String cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		XDI3Statement targetStatementSet2 = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint);

		message2.createSetOperation(targetStatementSet2);

		// send messages

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(message1.getMessageEnvelope(), null);

		Relation relation = messageResult.getGraph().getDeepRelation(cloudName.getPeerRootXri(), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Name not registered.");

		if (! cloudNumber.equals(CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri()))) throw new RuntimeException("Registered Cloud Number " + XdiPeerRoot.getXriOfPeerRootArcXri(relation.getTargetContextNodeXri().getFirstSubSegment()) + " does not match requested Cloud Number " + cloudNumber);

		// done

		log.debug("In RN: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	public void registerCloudNameInCSP(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement[] targetStatementsSet = new XDI3Statement[] {
				XDI3Statement.fromRelationComponents(
						cloudName.getPeerRootXri(), 
						XDIDictionaryConstants.XRI_S_REF, 
						cloudNumber.getPeerRootXri())
		};

		message.createSetOperation(Arrays.asList(targetStatementsSet).iterator());

		// send message

		this.getXdiClientCSPRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In CSP: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	public boolean checkVerifiedContactInformationInRN(String email, String phone) throws Xdi2ClientException {

		return false;
	}

	public void setVerifiedContactInformationInRN(CloudNumber cloudNumber, String email, String phone) throws Xdi2ClientException {

	}

	public void setServicesInCloud(CloudNumber cloudNumber, Map<XDI3Segment, String> services) throws Xdi2ClientException {

		// prepare message to Cloud (via CSP)

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(cloudNumber.getPeerRootXri());
		message.setLinkContractXri(XDI3Segment.create("$do"));

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> (services.size() * 2);

		for (Entry<XDI3Segment, String> entry : services.entrySet()) {

			targetStatementsSet.add(XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(entry.getKey(), XDIClientConstants.XRI_S_URI, XDIConstants.XRI_S_VALUE),
					entry.getValue()));

			targetStatementsSet.add(XDI3Statement.fromRelationComponents(
					XDILinkContractConstants.XRI_S_PUBLIC_DO,
					XDILinkContractConstants.XRI_S_GET,
					XDI3Util.concatXris(entry.getKey(), XDIClientConstants.XRI_S_URI)));
		}

		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		this.getXdiClientCSPRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In Cloud (via CSP): For Cloud Number " + cloudNumber + " registered services " + services);
	}

	public void setCloudXdiEndpointInRN(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException {

		// auto-generate XDI endpoint

		if (cloudXdiEndpoint == null) {

			cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);
		}

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatementSet = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		// send message

		this.getXdiClientRNRegistrationService().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In RN: Cloud XDI endpoint " + cloudXdiEndpoint + " set for Cloud Number " + cloudNumber);
	}

	public void setCloudXdiEndpointInCSP(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException {

		// auto-generate XDI endpoint

		if (cloudXdiEndpoint == null) {

			cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);
		}

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		cloudXdiEndpoint = makeCloudXdiEndpoint(this.getCspInformation(), cloudNumber);

		XDI3Statement targetStatementSet = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		// send message

		this.getXdiClientCSPRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In CSP: Cloud XDI endpoint " + cloudXdiEndpoint + " set for Cloud Number " + cloudNumber);
	}

	public void setCloudSecretTokenInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.createMessage(this.getCspInformation().getCspCloudNumber().getXri());
		message.setToAuthority(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.setSecretToken(this.getCspInformation().getCspSecretToken());

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		// send message

		this.getXdiClientCSPRegistry().send(message.getMessageEnvelope(), null);

		// done

		log.debug("In CSP: Secret token set for Cloud Number " + cloudNumber);
	}

	/*
	 * Helper methods
	 */

	private static String makeCloudXdiEndpoint(CSPInformation cspInformation, CloudNumber cloudNumber) {

		try {

			return cspInformation.getCspCloudBaseXdiEndpoint() + URLEncoder.encode(cloudNumber.toString(), "UTF-8");
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

		this.xdiClientCSPRegistry = new XDIHttpClient(cspInformation.getCspRegistryXdiEndpoint());
		this.xdiClientRNRegistrationService = new XDIHttpClient(cspInformation.getRnRegistrationServiceXdiEndpoint());
	}

	public XDIClient getXdiClientRNRegistrationService() {

		return this.xdiClientRNRegistrationService;
	}

	public void setXdiClientRNRegistrationService(XDIClient xdiClientRNRegistrationService) {

		this.xdiClientRNRegistrationService = xdiClientRNRegistrationService;
	}

	public XDIClient getXdiClientCSPRegistry() {

		return this.xdiClientCSPRegistry;
	}

	public void setXdiCSPEnvironmentRegistry(XDIClient xdiClientCSPRegistry) {

		this.xdiClientCSPRegistry = xdiClientCSPRegistry;
	}
}
