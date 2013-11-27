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

	public CloudNameRegistration checkCloudNameAvailable(XDI3Segment cloudName) throws Xdi2ClientException {

		XDI3Segment cloudNamePeerRootXri;
		XDI3Segment cloudNumberPeerRootXri;
		XDI3Segment cloudNumber;

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspInformation().getCspSecretToken());

		cloudNamePeerRootXri = XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(cloudName));

		XDI3Segment targetAddress = cloudNamePeerRootXri;

		message.createGetOperation(targetAddress);

		MessageResult messageResult = this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

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

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspInformation().getCspSecretToken());

		cloudNamePeerRootXri = XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(cloudName));

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(cloudNamePeerRootXri, XDIDictionaryConstants.XRI_S_REF, XDIConstants.XRI_S_VARIABLE);

		message.createSetOperation(targetStatement);

		MessageResult messageResult = this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

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

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getCspCloudNumber())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspInformation().getCspSecretToken());

		try {

			cloudXdiEndpoint = this.getCspInformation().getHostingEnvironmentCloudBaseXdiEndpoint() + URLEncoder.encode(cloudNameRegistration.getCloudNumber().toString(), "UTF-8");
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
		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		this.getXdiClientHostingEnvironmentRegistry().send(message.getMessageEnvelope(), null);

		log.debug("Cloud registered with Cloud Number " + cloudNameRegistration.getCloudNumber() + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);

		return new CloudRegistration(cloudXdiEndpoint);
	}

	public void registerCloudXdiUrl(CloudNameRegistration cloudNameRegistration, CloudRegistration cloudRegistration) throws Xdi2ClientException {

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getRespectNetworkCloudNumber())));
		message.setLinkContractXri(REGISTRAR_LINK_CONTRACT);
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspInformation().getCspSecretToken());

		XDI3Statement targetStatement = XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDI3Segment.create("$xdi<$uri>&")), cloudRegistration.getCloudXdiEndpoint());

		message.createSetOperation(targetStatement);

		this.getXdiClientRespectNetworkRegistrationService().send(message.getMessageEnvelope(), null);

		log.debug("Cloud XDI URL " + "registered with Cloud Number " + cloudNameRegistration.getCloudNumber() + " and Cloud XDI endpoint " + cloudRegistration.getCloudXdiEndpoint());
	}

	public void generateCloudSecretToken(CloudNameRegistration cloudNameRegistration, String secretToken) throws Xdi2ClientException {

		MessageEnvelope messageEnvelope = new MessageEnvelope();

		Message message = messageEnvelope.getMessage(this.getCspInformation().getCspCloudNumber(), true);
		message.setToAuthority(XDI3Segment.fromComponent(XdiPeerRoot.createPeerRootArcXri(this.getCspInformation().getCspCloudNumber())));
		message.setLinkContractXri(XDI3Segment.create("$do"));
		message.getContextNode().setDeepLiteral(XDI3Util.concatXris(XDIAuthenticationConstants.XRI_S_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), this.getCspInformation().getCspSecretToken());

		XDI3Statement[] targetStatementsDoDigestSecretToken = new XDI3Statement[] {
				XDI3Statement.fromLiteralComponents(XDI3Util.concatXris(cloudNameRegistration.getCloudNumberPeerRootXri(), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), secretToken)
		};

		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), Arrays.asList(targetStatementsDoDigestSecretToken).iterator());

		this.getXdiClientHostingEnvironmentRegistry().send(message.getMessageEnvelope(), null);

		log.debug("Secret Token generated with Cloud Number " + cloudNameRegistration.getCloudNumber());
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
