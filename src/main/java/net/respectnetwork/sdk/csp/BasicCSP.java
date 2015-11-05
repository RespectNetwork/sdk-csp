package net.respectnetwork.sdk.csp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.respectnetwork.sdk.csp.discount.NeustarRNCampaignCode;
import net.respectnetwork.sdk.csp.discount.NeustarRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkRNDiscountCode;
import net.respectnetwork.sdk.csp.discount.XDIDiscountCodeConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.constants.XDIClientConstants;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.Relation;
import xdi2.core.Statement;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.instance.GenericLinkContract;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.features.policy.PolicyAnd;
import xdi2.core.features.policy.PolicyUtil;
import xdi2.core.features.nodetypes.XdiAbstractInstanceUnordered;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.features.nodetypes.XdiInnerRoot;
import xdi2.core.features.signatures.KeyPairSignature;
import xdi2.core.features.signatures.Signatures;
import xdi2.core.features.timestamps.Timestamps;
import xdi2.core.impl.memory.MemoryGraph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.util.GraphUtil;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.iterators.IteratorArrayMaker;
import xdi2.core.util.iterators.MappingCloudNameIterator;
import xdi2.core.util.iterators.MappingCloudNumberIterator;
import xdi2.core.util.iterators.MappingRelationTargetXDIAddressIterator;
import xdi2.core.util.iterators.MappingXDIStatementIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.util.iterators.SelectingNotImpliedStatementIterator;
import xdi2.core.syntax.CloudName;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.syntax.XDIArc;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;


public class BasicCSP implements CSP {

	private static final Logger log = LoggerFactory.getLogger(BasicCSP.class);

	public static final XDIAddress XRI_S_AC_VERIFIED_DIGEST_PHONE = XDIAddress.create("<#verified><$digest>[<#phone>]");
	public static final XDIAddress XRI_S_AC_VERIFIED_DIGEST_EMAIL = XDIAddress.create("<#verified><$digest>[<#email>]");
	public static final XDIAddress XRI_S_AS_VERIFIED_PHONE = XDIAddress.create("<#verified><#phone>");
	public static final XDIAddress XRI_S_AS_VERIFIED_EMAIL = XDIAddress.create("<#verified><#email>");
	public static final XDIAddress XRI_S_IS_PHONE = XDIAddress.create("$is#phone");
	public static final XDIAddress XRI_S_IS_EMAIL = XDIAddress.create("$is#email");

	public static final XDIAddress XRI_S_IS_GUARDIAN = XDIAddress.create("$is#guardian");
	public static final XDIAddress XRI_S_GUARDIAN = XDIAddress.create("#guardian");

	public static final XDIAddress XRI_S_IS_BILLING_CONTACT = XDIAddress.create("$is#billing#contact");
	public static final XDIAddress XRI_S_BILLING_CONTACT = XDIAddress.create("#billing#contact");

	public static final XDIAddress XRI_S_MEMBER = XDIAddress.create("#member");
	public static final XDIAddress XRI_S_AS_MEMBER_EXPIRATION_TIME = XDIAddress.create("<#member><#expiration><$t>");

	public static final XDIAddress XRI_S_FIRST_MEMBER = XDIAddress.create("#first#member");

	public static final XDIAddress XRI_S_AS_AVAILABLE = XDIAddress.create("<#available>");
	
	public static final XDIAddress XRI_S_REGISTRAR = XDIAddress.create("#registrar");
    public static final XDIAddress XRI_S_AS_PHONE = XDIAddress.create("<#phone>");
    public static final XDIAddress XRI_S_AS_EMAIL = XDIAddress.create("<#email>");
    public static final XDIAddress XRI_S_AS_PROFILE_CARD_NAME = XDIAddress.create("<#card><#name>");
    public static final XDIAddress XRI_S_AS_PROFILE = XDIAddress.create("#profile$card");
    public static final XDIAddress XRI_S_AS_REGISTER_DATE = XDIAddress.create("<#registration><#date><$t>");


	private CSPInformation cspInformation;

	private XDIClient xdiClientCSPRegistry;
	private XDIClient xdiClientRNRegistrationService;

	public BasicCSP() {

	}

	public BasicCSP(CSPInformation cspInformation) {

		this.cspInformation = cspInformation;

		this.xdiClientCSPRegistry = new XDIHttpClient(cspInformation.getCspRegistryXdiEndpoint());
		((XDIHttpClient) this.xdiClientCSPRegistry).setFollowRedirects(true);
		this.xdiClientRNRegistrationService = new XDIHttpClient(cspInformation.getRnRegistrationServiceXdiEndpoint());
		((XDIHttpClient) this.xdiClientRNRegistrationService).setFollowRedirects(true);
	}

	@Override
	public void registerCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDIStatement targetStatementSet = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XDIAddress.create("<$xdi><$uri>")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		if (secretToken != null) {

			XDIStatement targetStatementDoDigestSecretToken = XDIStatement.fromLiteralComponents(
					XDIAddressUtil.concatXDIAddresses(XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()), XDIAuthenticationConstants.XDI_ADD_DIGEST_SECRET_TOKEN), 
					secretToken);

			message.createOperation(XDIAddress.create("$do<$digest><$secret><$token>"), targetStatementDoDigestSecretToken);
		}

		// send message

		this.prepareMessageToCSP(message);
		log.debug("registerCloudInCSP :: Message to CSP :" + messageEnvelope.getGraph().toString() );
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP: Cloud registered with Cloud Number " + cloudNumber + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);
	}

	@Override
	public boolean checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException {

		boolean available;

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIAddress targetAddress = XDIAddress.fromComponent(cloudName.getPeerRootXDIArc());

		message.createGetOperation(targetAddress);

		// send message and read result

		this.prepareMessageToRN(message);

		log.debug("checkCloudNameAvailableInRN :: Message envelope to RN \n" );
		printMessage(messageEnvelope);

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		LiteralNode literal = messageResult.getGraph().getDeepLiteralNode(XDIAddressUtil.concatXDIAddresses(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), XRI_S_AS_AVAILABLE));
		if (literal == null) throw new Xdi2ClientException("No availability literal found in result.");

		Boolean literalDataBoolean = literal.getLiteralDataBoolean();
		if (literalDataBoolean == null) throw new Xdi2ClientException("No availability boolean value found in result.");

		available = literalDataBoolean.booleanValue();

		// done

		log.debug("In RN: For Cloud Name " + cloudName + " found availability " + available);
		return available;
	}

	@Override
	public CloudNumber checkCloudNameInRN(CloudName cloudName) throws Xdi2ClientException {

		CloudNumber cloudNumber = null;

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatement = XDIStatement.fromComponents(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), XDIDictionaryConstants.XDI_ADD_REF, XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message.createGetOperation(targetStatement);

		// send message and read result

		this.prepareMessageToRN(message);

		log.debug("checkCloudNameAvailableInRN :: Message envelope to RN \n" );
		printMessage(messageEnvelope);

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
		ContextNode contextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()));
		if(contextNode == null) {
		    return cloudNumber;
		}
		Relation relation = contextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);
		if (relation != null) {

			cloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
		}

		// done

		log.debug("In RN: For Cloud Name " + cloudName + " found Cloud Number " + cloudNumber);
		return cloudNumber;
	}

	@Override
	public CloudNumber[] checkPhoneAndEmailAvailableInRN(String verifiedPhone, String verifiedEmail) throws Xdi2ClientException {

		CloudNumber[] cloudNumbers = new CloudNumber[2];

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIAddress targetAddress1 = verifiedPhone == null ? null : XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedPhone))));
		XDIAddress targetAddress2 = verifiedEmail == null ? null : XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedEmail))));

		if (targetAddress1 != null) message.createGetOperation(targetAddress1);
		if (targetAddress2 != null) message.createGetOperation(targetAddress2);

		// send message

		this.prepareMessageToRN(message);
		log.debug("checkPhoneAndEmailAvailableInRN :: Message to RN " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
	    ContextNode contextNode1 = targetAddress1 == null ? null : messageResult.getGraph().getDeepContextNode(targetAddress1);
	    Relation relation1 = contextNode1 == null ? null : contextNode1.getRelation(XRI_S_IS_PHONE);
		
	    ContextNode contextNode2 = targetAddress2 == null ? null : messageResult.getGraph().getDeepContextNode(targetAddress2);
	    Relation relation2 = contextNode2 == null ? null : contextNode2.getRelation(XRI_S_IS_EMAIL);
		if (relation1 != null) {

			cloudNumbers[0] = CloudNumber.fromPeerRootXDIArc(relation1.getTargetXDIAddress());
		}

		if (relation2 != null) {

			cloudNumbers[1] = CloudNumber.fromPeerRootXDIArc(relation2.getTargetXDIAddress());
		}

		// done

		log.debug("In RN: For verified phone " + verifiedPhone + " and verified email " + verifiedEmail + " found Cloud Numbers " + Arrays.asList(cloudNumbers));
		return cloudNumbers;
	}

	@Deprecated
	public CloudNumber registerCloudNameInRN(CloudName cloudName) throws Xdi2ClientException {

		// prepare message 1 to RN

		MessageEnvelope messageEnvelope1 = new MessageEnvelope();
		MessageCollection messageCollection1 = this.createMessageCollectionToRN(messageEnvelope1);

		Message message1 = messageCollection1.createMessage();

		XDIStatement targetStatementSet1 = XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message1.createSetOperation(targetStatementSet1);

		// send message 1 and read result

		this.prepareMessageToRN(message1);
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope1, null);
		ContextNode  contextnode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()));
		Relation relation = contextnode == null ? null : contextnode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);
		
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		CloudNumber cloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());

		// prepare message 2 to RN

		MessageEnvelope messageEnvelope2 = new MessageEnvelope();
		MessageCollection messageCollection2 = this.createMessageCollectionToCSP(messageEnvelope2);

		Message message2 = messageCollection2.createMessage();

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDIStatement targetStatementSet2 = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XDIAddress.create("<$xdi><$uri>")), 
				cloudXdiEndpoint);

		message2.createSetOperation(targetStatementSet2);

		// send message 2

		this.prepareMessageToRN(message2);
		this.getXdiClientRNRegistrationService().send(messageEnvelope2, null);

		// done

		log.debug("In RN: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);

		return cloudNumber;
	}

	@Override
	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail, NeustarRNDiscountCode neustarRNDiscountCode) throws Xdi2ClientException {

		// prepare message 1 to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message1 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc())));

		Operation setOperation = message1.createSetOperation(targetStatementsSet.iterator());

		if (neustarRNDiscountCode != null) {

		    setOperation.setParameter(XDIDiscountCodeConstants.XRI_S_PARAMETER_NEUSTAR_RN_DISCOUNTCODE, neustarRNDiscountCode.toString());
		}

		// prepare message 2 to RN

		Message message2 = messageCollection.createMessage(-1);

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		List<XDIStatement> targetStatementsSet2 = new ArrayList<XDIStatement> ();

		targetStatementsSet2.add(XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(cloudNumber.getPeerRootXDIArc(), XDIAddress.create("<$xdi><$uri>")), 
				cloudXdiEndpoint));

		message2.createSetOperation(targetStatementsSet2.iterator());

		// prepare message 3 to RN

		Message message3 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet3 = new ArrayList<XDIStatement> ();

		if (verifiedPhone != null) {

			targetStatementsSet3.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedPhone)))),
					XRI_S_IS_PHONE,
					cloudNumber.getXDIAddress()));
		}

		if (verifiedEmail != null) {

			targetStatementsSet3.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedEmail)))),
					XRI_S_IS_EMAIL,
					cloudNumber.getXDIAddress()));
		}

		message3.createSetOperation(targetStatementsSet3.iterator());

		// send messages

		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);
		this.prepareMessageToRN(message3);

		log.debug("registerCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		// Not required in the new native XDI registry. So commenting this for now and later on can be removed.
		/*Relation relation = messageResult.getGraph().getDeepRelation(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), XDIDictionaryConstants.XDI_ADD_REF);
		if (relation == null) throw new RuntimeException("Cloud Name not registered.");

		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
		if (! registeredCloudNumber.equals(cloudNumber)) throw new RuntimeException("Registered Cloud Number " + registeredCloudNumber + " does not match requested Cloud Number " + cloudNumber);
        */
		// done

		log.debug("In RN: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	@Override
	public void registerCloudNameInCSP(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc())));

		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		this.prepareMessageToCSP(message);
		log.debug("registerCloudNameInCSP :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	@Override
	public void registerCloudNameInCloud(CloudName cloudName, CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				cloudName.getXDIAddress(), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				cloudNumber.getXDIAddress()));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				cloudNumber.getXDIAddress(), 
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				cloudName.getXDIAddress()));

		Graph publicLinkContractGraph = MemoryGraphFactory.getInstance().openGraph();
		GraphUtil.setOwnerXDIAddress(publicLinkContractGraph, cloudNumber.getXDIAddress());
		PublicLinkContract publicLinkContract = PublicLinkContract.findPublicLinkContract(publicLinkContractGraph, true);
		publicLinkContract.setPermissionTargetXDIStatement(XDILinkContractConstants.XDI_ADD_GET, XDIStatement.fromRelationComponents(cloudName.getXDIAddress(), XDIDictionaryConstants.XDI_ADD_REF, cloudNumber.getXDIAddress()));

		for (XDIStatement publicLinkContractStatementXri : new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(publicLinkContractGraph.getAllStatements()))) {
			targetStatementsSet.add(publicLinkContractStatementXri);
		}

		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);

		log.debug("registerCloudNameInCloud :: Message  " + messageEnvelope.getGraph().toString());

		xdiClientCloud.send(messageEnvelope, null);

		// done

		log.debug("In Cloud: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	@Override
	public void deleteCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsDel = new ArrayList<XDIStatement> ();

		targetStatementsDel.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

		targetStatementsDel.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc())));

		message.createDelOperation(targetStatementsDel.iterator());

		// send message

		this.prepareMessageToRN(message);
		log.debug("deleteCloudNameInRN :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done

		log.debug("In RN: Cloud Name " + cloudName + " deleted.");
	}

	@Override
	public void setCloudXdiEndpointInRN(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException {

		// auto-generate XDI endpoint

		if (cloudXdiEndpoint == null) cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatementSet = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(cloudNumber.getPeerRootXDIArc(), XDIAddress.create("<$xdi><$uri>")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		// send message

		this.prepareMessageToRN(message);
		log.debug("setCloudXdiEndpointInRN :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done

		log.debug("In RN: Cloud XDI endpoint " + cloudXdiEndpoint + " set for Cloud Number " + cloudNumber);
	}

	@Override
	public void setPhoneAndEmailInRN(CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		if (verifiedPhone != null) {

			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedPhone)))),
					XRI_S_IS_PHONE,
					cloudNumber.getXDIAddress()));
		}

		if (verifiedEmail != null) {

			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(verifiedEmail)))),
					XRI_S_IS_EMAIL,
					cloudNumber.getXDIAddress()));
		}

		this.prepareMessageToRN(message);
		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done

		log.debug("In RN: Verified phone " + verifiedPhone + " and verified e-mail " + verifiedEmail + " set for Cloud Number " + cloudNumber);
	}

	@Override
	public void setRespectNetworkMembershipInRN(CloudNumber cloudNumber, Date expirationTime, RespectNetworkRNDiscountCode respectNetworkRNDiscountCode)  throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();

		targetStatements.add(XDIStatement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXDIAddress(),
				XRI_S_MEMBER,
				cloudNumber.getXDIAddress()));

		if (expirationTime != null) {

			Graph expirationTimeGraph = MemoryGraphFactory.getInstance().openGraph();
			XdiInnerRoot expirationTimeXdiInnerRoot = XdiCommonRoot.findCommonRoot(expirationTimeGraph).getInnerRoot(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_MEMBER, true);
			expirationTimeXdiInnerRoot.getContextNode().setStatement(XDIStatement.fromLiteralComponents(XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_MEMBER_EXPIRATION_TIME), Timestamps.timestampToString(expirationTime)));
			for (XDIStatement expirationTimeStatementXri : new MappingXDIStatementIterator(new SelectingNotImpliedStatementIterator(expirationTimeGraph.getAllStatements()))) {
				targetStatements.add(expirationTimeStatementXri);
			}
		}

		Operation operation = message.createSetOperation(targetStatements.iterator());

		if (respectNetworkRNDiscountCode != null) {

			operation.setParameter(XDIDiscountCodeConstants.XRI_S_PARAMETER_RESPECTNETWORK_RN_DISCOUNTCODE, respectNetworkRNDiscountCode.toString());
		}

		// send message

		this.prepareMessageToRN(message);
		log.debug("setRespectNetworkMembershipInRN :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done

		log.debug("In RN: Respect Network membership set for Cloud Number " + cloudNumber);
	}

	@Override
	public boolean checkRespectNetworkMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatement = XDIStatement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXDIAddress(),
				XRI_S_MEMBER,
				cloudNumber.getXDIAddress());

		message.createGetOperation(targetStatement);

		// Send message and read result

		this.prepareMessageToRN(message);
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		boolean member = messageResult.getGraph().containsStatement(targetStatement);

		// done

		log.debug("In RN: Respect Network membership " + member + " retrieved for Cloud Number " + cloudNumber);

		return member;
	}

	@Override
	public void setRespectFirstMembershipInRN(CloudNumber cloudNumber)  throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();

		targetStatements.add(XDIStatement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXDIAddress(),
				XRI_S_FIRST_MEMBER,
				cloudNumber.getXDIAddress()));

		message.createSetOperation(targetStatements.iterator());

		// send message

		this.prepareMessageToRN(message);
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done

		log.debug("In RN: Respect First membership set for Cloud Number " + cloudNumber);
	}

	@Override
	public boolean checkRespectFirstMembershipInRN(CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatement = XDIStatement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXDIAddress(),
				XRI_S_FIRST_MEMBER,
				cloudNumber.getXDIAddress());

		message.createGetOperation(targetStatement);

		// send message and read result

		this.prepareMessageToRN(message);
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		boolean member = messageResult.getGraph().containsStatement(targetStatement);

		// done

		log.debug("In RN: Respect First membership " + member + " retrieved for Cloud Number " + cloudNumber);

		return member;
	}

	@Override
	public void setCloudXdiEndpointInCSP(CloudNumber cloudNumber, String cloudXdiEndpoint) throws Xdi2ClientException {

		// auto-generate XDI endpoint

		if (cloudXdiEndpoint == null) cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatementSet = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(cloudNumber.getPeerRootXDIArc(), XDIAddress.create("<$xdi><$uri>")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		// send message

		this.prepareMessageToCSP(message);
		log.debug("setCloudXdiEndpointInCSP :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP: Cloud XDI endpoint " + cloudXdiEndpoint + " set for Cloud Number " + cloudNumber);
	}

	@Override
	public void setCloudSecretTokenInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatementsDoDigestSecretToken = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()), XDIAuthenticationConstants.XDI_ADD_DIGEST_SECRET_TOKEN), 
				secretToken);

		message.createOperation(XDIAddress.create("$do<$digest><$secret><$token>"), targetStatementsDoDigestSecretToken);

		// send message

		this.prepareMessageToCSP(message);
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP: Secret token set for Cloud Number " + cloudNumber);
	}

	@Override
	public CloudName[] checkCloudNamesInCSP(CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatementGet = XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF,
				XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message.createGetOperation(targetStatementGet);

		// send message and read results

		this.prepareMessageToCSP(message);
		MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);
		ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()));
		ReadOnlyIterator<Relation> relations = cotextNode == null ? null : cotextNode.getRelations(XDIDictionaryConstants.XDI_ADD_IS_REF);
		
		List<CloudName> cloudNames = new ArrayList<CloudName> ();

		for (Relation relation : relations) {
		    cloudNames.add(CloudName.fromPeerRootXDIArc(relation.getTargetXDIAddress()));
		}
	/*	CloudName[] cloudNames = new IteratorArrayMaker<CloudName> (
				new NotNullIterator<CloudName> (
						new MappingCloudNameIterator(
								new MappingRelationTargetXDIAddressIterator(relations)
								))).array(CloudName.class); */

		// done

		log.debug("In CSP: For Cloud Number " + cloudNumber + " found Cloud Names " + cloudNames.toString());
		return cloudNames.toArray(new CloudName[cloudNames.size()]);
	}

	@Override
	public void authenticateInCloud(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		XDIAddress targetAddress = RootLinkContract.createRootLinkContractXDIAddress(cloudNumber.getXDIAddress());

		message.createGetOperation(targetAddress);

		// send message

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		log.debug("authenticateInCloud :: cloudXdiEndpoint  :" + cloudXdiEndpoint );

		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
		log.debug("authenticateInCloud :: Message  :" + messageEnvelope.getGraph().toString() );
		xdiClientCloud.send(messageEnvelope, null);

		// done

		log.debug("In Cloud: Authenticated Cloud Number");
	}

	@Override
	public void setCloudServicesInCloud(CloudNumber cloudNumber, String secretToken, Map<XDIAddress, String> services) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> (services.size() * 2);

		for (Entry<XDIAddress, String> entry : services.entrySet()) {

			targetStatementsSet.add(XDIStatement.fromLiteralComponents(
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), entry.getKey(), XDIClientConstants.XDI_ADD_AS_URI),
					entry.getValue()));

			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					PublicLinkContract.createPublicLinkContractXDIAddress(cloudNumber.getXDIAddress()),
					XDILinkContractConstants.XDI_ADD_GET,
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), entry.getKey(), XDIClientConstants.XDI_ADD_AS_URI)));
		}

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);

		xdiClientCloud.send(messageEnvelope, null);

		// done

		log.debug("In Cloud: For Cloud Number " + cloudNumber + " registered services " + services);
	}


	@Override
	public void setCloudServicesForCSPInCSP(CloudNumber cloudNumber, String secretToken, String cspXdiEndpoint, Map<XDIAddress, String> services) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> (services.size() * 2);

		for (Entry<XDIAddress, String> entry : services.entrySet()) {

			targetStatementsSet.add(XDIStatement.fromLiteralComponents(
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), entry.getKey(), XDIClientConstants.XDI_ADD_AS_URI),
					entry.getValue()));

			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					PublicLinkContract.createPublicLinkContractXDIAddress(cloudNumber.getXDIAddress()),
					XDILinkContractConstants.XDI_ADD_GET,
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), entry.getKey(), XDIClientConstants.XDI_ADD_AS_URI)));
		}

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
		message.createSetOperation(targetStatementsSet.iterator());

		log.debug("setCloudServicesForCSPInCSP :: Message envelope to CSP \n" );
		printMessage(messageEnvelope);

		// send message

		XDIClient xdiCSPCloud = new XDIHttpClient(cspXdiEndpoint);

		xdiCSPCloud.send(messageEnvelope, null);

		// done

		log.debug("In Cloud: For Cloud Number " + cloudNumber + " registered services " + services);
	}

	@Override
	public void setPhoneAndEmailInCloud(CloudNumber cloudNumber, String secretToken, String verifiedPhone, String verifiedEmail) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		if (verifiedPhone != null) {

			targetStatementsSet.add(XDIStatement.fromLiteralComponents(
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_VERIFIED_PHONE),
					verifiedPhone));
		}

		if (verifiedEmail != null) {

			targetStatementsSet.add(XDIStatement.fromLiteralComponents(
					XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_VERIFIED_EMAIL),
					verifiedEmail));
		}

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
		message.createSetOperation(targetStatementsSet.iterator());

		// send message

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);

		xdiClientCloud.send(messageEnvelope, null);

		// done

		log.debug("In Cloud: Verified phone " + verifiedPhone + " and verified e-mail " + verifiedEmail + " set for Cloud Number " + cloudNumber);
	}

	@Override
    public void createDefaultProfile(CloudName cloudName, CloudNumber cloudNumber, String secretToken, String phone,
            String email) throws Xdi2ClientException {
        // prepare message to Cloud

        MessageEnvelope messageEnvelope = new MessageEnvelope();
        MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

        Message message = messageCollection.createMessage(-1);

        List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement>();
        List<XDIStatement> xdiStatements = new ArrayList<XDIStatement>();

        XDIAddress profileAddress = XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_PROFILE);
        targetStatementsSet.add(XDIStatement.fromRelationComponents(
                XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_PROFILE_CARD_NAME),
                XDIDictionaryConstants.XDI_ADD_REF,
                XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_PROFILE_CARD_NAME)));
        xdiStatements.add(XDIStatement.fromLiteralComponents(
                XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_PROFILE_CARD_NAME), "Main"));
        if (phone != null) {
            targetStatementsSet.add(XDIStatement.fromRelationComponents(
                    XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_PHONE),
                    XDIDictionaryConstants.XDI_ADD_REF,
                    XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_PHONE)));
            xdiStatements.add(XDIStatement.fromLiteralComponents(
                    XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_PHONE), phone));
        }

        if (email != null) {
            targetStatementsSet.add(XDIStatement.fromRelationComponents(
                    XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_EMAIL),
                    XDIDictionaryConstants.XDI_ADD_REF,
                    XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_EMAIL)));
            xdiStatements.add(XDIStatement.fromLiteralComponents(
                    XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_EMAIL), email));
        }

        String registrationDate = Timestamps.timestampToString(Calendar.getInstance().getTime());
        targetStatementsSet.add(XDIStatement.fromRelationComponents(
                XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_REGISTER_DATE),
                XDIDictionaryConstants.XDI_ADD_REF,
                XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), XRI_S_AS_REGISTER_DATE)));
        xdiStatements.add(XDIStatement.fromLiteralComponents(
                XDIAddressUtil.concatXDIAddresses(profileAddress, XRI_S_AS_REGISTER_DATE), registrationDate));

        this.prepareMessageToCloud(message, cloudNumber, secretToken);
        message.createSetOperation(targetStatementsSet.iterator());
        // send message

        String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);
        XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);
        if (log.isDebugEnabled()) {
            log.debug("createDefaultProfile :: Message envelope to Cloud \n");
            printMessage(messageEnvelope);
        }
        xdiClientCloud.send(messageEnvelope, null);

        MessageEnvelope messageEnvelope1 = new MessageEnvelope();
        MessageCollection messageCollection1 = this.createMessageCollectionToCloud(messageEnvelope1, cloudNumber);

        Message message1 = messageCollection1.createMessage(-1);
        this.prepareMessageToCloud(message1, cloudNumber, secretToken);
        message1.createSetOperation(xdiStatements.iterator());
        if (log.isDebugEnabled()) {
            log.debug("createDefaultProfile :: Message1 envelope to Cloud \n");
            printMessage(messageEnvelope1);
        }
        // send message
        xdiClientCloud.send(messageEnvelope1, null);
        // done
        log.debug("In Cloud: Verified phone " + phone + " and verified e-mail " + email + " set for Cloud Number "
                + cloudNumber);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getRespectFirstMemberCount()
			throws Xdi2ClientException {

		long numberOfMembers = 0;

		// prepare message to RN
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDIStatement targetStatementGet = XDIStatement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXDIAddress(),
				XRI_S_MEMBER,
				XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message.createGetOperation(targetStatementGet);

		// Send message and read result

		this.prepareMessageToRN(message);

		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(this.getCspInformation().getRnCloudNumber().getXDIAddress());
        ReadOnlyIterator<Relation> relations = cotextNode == null ? null : cotextNode.getRelations(XRI_S_MEMBER);
        if(relations != null) {
            while (relations.hasNext()) {
    			relations.next();
    			numberOfMembers++;
    		}
        }
		// done
		log.debug("In RN: getRespectFirstMemberCount = " + numberOfMembers);
		return numberOfMembers;
	}


	/*
	 * Helper methods
	 */

	private String makeCloudXdiEndpoint(CloudNumber cloudNumber) {

		try {
			return cspInformation.getCspCloudBaseXdiEndpoint() + URLEncoder.encode(cloudNumber.toString(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private MessageCollection createMessageCollectionToRN(MessageEnvelope messageEnvelope) {

		return messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXDIAddress(), true);
	}

	private MessageCollection createMessageCollectionToCSP(MessageEnvelope messageEnvelope) {

		return messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXDIAddress(), true);
	}

	private MessageCollection createMessageCollectionToCloud(MessageEnvelope messageEnvelope, CloudNumber cloudNumber) {

		return messageEnvelope.getMessageCollection(cloudNumber.getXDIAddress(), true);
	}

	private void prepareMessageToRN(Message message) {

		message.setToPeerRootXDIArc(this.getCspInformation().getRnCloudNumber().getPeerRootXDIArc());
		message.setLinkContractXDIAddress(this.getCspInformation().getRnCspLinkContract());

		if (this.getCspInformation().getRnCspSecretToken() != null && !this.getCspInformation().getRnCspSecretToken().isEmpty()) {

			message.setSecretToken(this.getCspInformation().getRnCspSecretToken());
		} else
		{
			log.debug("RN Secret token is null. Will try to retrieve CSP private key for signing messages.");
			try
			{
				BasicCSPInformation basicCSP = (BasicCSPInformation) this.getCspInformation();
				if(basicCSP.getCspSignaturePrivateKey() == null)
				{
					basicCSP.retrieveCspSignaturePrivateKey();
				}
			} catch (Xdi2ClientException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.getCspInformation().getCspSignaturePrivateKey() != null) {
			log.debug("CSP Private Key is not null. Signing Messages with it.");

			KeyPairSignature signature = (KeyPairSignature) message.createSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048, true);

			try {

				signature.sign(this.getCspInformation().getCspSignaturePrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
		else
		{
			log.debug("CSP Private Key is null. Cannot sign Messages to RN.");
		}
	}

	private void prepareMessageToCSP(Message message) {

		message.setToPeerRootXDIArc(this.getCspInformation().getCspCloudNumber().getPeerRootXDIArc());
		message.setLinkContractXDIAddress(RootLinkContract.createRootLinkContractXDIAddress(this.getCspInformation().getCspCloudNumber().getXDIAddress()));

		if (this.getCspInformation().getCspSecretToken() != null) {

			message.setSecretToken(this.getCspInformation().getCspSecretToken());
		}

		if (this.getCspInformation().getCspSignaturePrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.createSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048, true);

			try {

				signature.sign(this.getCspInformation().getCspSignaturePrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	private void prepareMessageToCloud(Message message, CloudNumber cloudNumber, String secretToken) {

		message.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
		message.setLinkContractXDIAddress(RootLinkContract.createRootLinkContractXDIAddress(cloudNumber.getXDIAddress()));

		if (secretToken != null) {

			message.setSecretToken(secretToken);
		}
	}


	/*
	 * Dependency  Methods
	 */


	/**
	 * {@inheritDoc}
	 */
	public void setGuardianshipInCloud(CSPInformation cspInformation, CloudNumber guardian,
			CloudNumber dependent, Date dependentBirthDate, boolean withConsent, String secretToken, PrivateKey guardianPrivateSigningKey, String dependentToken)
					throws Xdi2ClientException {

		// Prepare message to guardian Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, guardian);

		Message message = messageCollection.createMessage();

		//Update Guardian's Graph

		this.prepareMessageToCloud(message, guardian, secretToken);

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();

		//Create the Relational Entry in the Guardian  Graph
		targetStatements.add(XDIStatement.fromRelationComponents(
				guardian.getXDIAddress(),
				XRI_S_IS_GUARDIAN,
				dependent.getXDIAddress()));

		message.createSetOperation(targetStatements.iterator());

		// send message
		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(guardian);
		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);
		log.debug("setGuardianshipInCloud :: Message1  " + messageEnvelope.getGraph().toString());
		xdiClientCloud.send(messageEnvelope, null);

		// done
		log.debug("In Guardian User Cloud: Creating is Guardian Relationship between " + guardian.toString() + " and " + dependent.toString() );


		// Prepare message to Dependent's Cloud

		MessageEnvelope messageEnvelope2 = new MessageEnvelope();
		MessageCollection messageCollection2 = this.createMessageCollectionToCloud(messageEnvelope2, dependent);

		Message message2 = messageCollection2.createMessage();

		//Update Dependent's Graph

		this.prepareMessageToCloud(message2, dependent, dependentToken);        
		List<XDIStatement> targetStatements2 = new ArrayList<XDIStatement> ();


		//Generating and Adding Dependent Statements
		List<XDIStatement> dependentStatements =  createDependentXDIStatements( guardian,  dependent, dependentBirthDate, guardianPrivateSigningKey);
		targetStatements2.addAll(dependentStatements);



		if (withConsent) {    

			//Generating and Adding Consent Statements
			List<XDIStatement> consentStatements =  createConsentXDIStatements(guardian,  dependent, guardianPrivateSigningKey);
			targetStatements2.addAll(consentStatements);
		} else {
			throw new Xdi2ClientException("Consent required for this operation");
		}
		 // Set link contract and policies so that parent/guardian can fetch all
        // the information from child's cloud.
        Graph parentChildLinkContractGraph = MemoryGraphFactory.getInstance().openGraph();
        GenericLinkContract linkContract = GenericLinkContract.findGenericLinkContract(parentChildLinkContractGraph,
                dependent.getXDIAddress(), guardian.getXDIAddress(), XDIAddress.create(""), true);

        PolicyAnd policy = linkContract.getPolicyRoot(true).createAndPolicy(true);
        PolicyUtil.createSenderIsOperator(policy, guardian.getXDIAddress());
        PolicyUtil.createSignatureValidOperator(policy);

        linkContract.setPermissionTargetXDIAddress(XDILinkContractConstants.XDI_ADD_ALL,
                XDIAddressUtil.concatXDIAddresses(XDIConstants.XDI_ADD_ROOT));

        log.info("Sending following XDI data to child cloud: {}",
                parentChildLinkContractGraph.toString("XDI DISPLAY", null));
        MessageEnvelope msgEnvelope = new MessageEnvelope();
        Message parentChildMsg = msgEnvelope.createMessage(dependent.getXDIAddress());
        parentChildMsg.setToPeerRootXDIArc(dependent.getPeerRootXDIArc());
        parentChildMsg.setLinkContractXDIAddress(RootLinkContract.createRootLinkContractXDIAddress(dependent.getXDIAddress()));
        parentChildMsg.setSecretToken(dependentToken);
        parentChildMsg.createSetOperation(parentChildLinkContractGraph);

        message2.createSetOperation(targetStatements2.iterator());

        // send message
        String cloudXdiEndpoint2 = this.makeCloudXdiEndpoint(dependent);
        XDIClient xdiClientCloud2 = new XDIHttpClient(cloudXdiEndpoint2);
        log.debug("setGuardianshipInCloud :: Message2  " + messageEnvelope2.getGraph().toString());
        xdiClientCloud2.send(messageEnvelope2, null);
        log.debug("setGuardianshipInCloud :: parent child link contract ");
        printMessage(msgEnvelope);
        xdiClientCloud2.send(msgEnvelope, null);
        // done
        log.debug("In Dependent User Cloud: Creating Guardian Relationship between " + dependent.toString() + " and "
                + guardian.toString());
	}


	/**
	 * {@inheritDoc}
	 */
	public void setGuardianshipInCSP(CSPInformation cspInformation, CloudNumber guardian,
			CloudNumber dependent, Date dependentBirthDate, boolean withConsent, PrivateKey guardianPrivateSigningKey)
					throws Xdi2ClientException {

		// Prepare message to Guardian Sub Graph in CSP Graph

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();


		//Create the Relational Entry in the Guardian Sub Graph
		targetStatements.add(XDIStatement.fromRelationComponents(
				guardian.getXDIAddress(),
				XRI_S_IS_GUARDIAN,
				dependent.getXDIAddress()));


		//Generating and Adding Dependent Statements
		List<XDIStatement> dependentStatements =  createDependentXDIStatements(guardian, dependent, dependentBirthDate, guardianPrivateSigningKey);
		targetStatements.addAll(dependentStatements);


		if (withConsent) {     
			//Generating and Adding Consent Statements
			List<XDIStatement> consentStatements =  createConsentXDIStatements(guardian, dependent, guardianPrivateSigningKey);
			targetStatements.addAll(consentStatements);
		} else {
			throw new Xdi2ClientException("Consent required for this operation");
		}

		message.createSetOperation(targetStatements.iterator());


		// send message

		this.prepareMessageToCSP(message);
		log.debug("setGuardianshipInCSP :: Message  " + messageEnvelope.getGraph().toString());
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP Cloud: Creating  is Guardian Relationship between " + guardian.toString() + " and " + dependent.toString() );
	}



	/**
	 * {@inheritDoc}
	 */
	public void setGuardianshipInRN(CSPInformation cspInformation, CloudNumber guardian,
			CloudNumber dependent, Date dependentBirthDate, boolean withConsent, PrivateKey guardianPrivateSigningKey)
					throws Xdi2ClientException {

		// Prepare message to Guardian Sub Graph in RN Member Graph Service

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();


		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();

		//Create the Relational Entry in the Guardian Sub Graph
		targetStatements.add(XDIStatement.fromRelationComponents(
				guardian.getXDIAddress(),
				XRI_S_IS_GUARDIAN,
				dependent.getXDIAddress()));


		//Generating and Adding Dependent Statements
		List<XDIStatement> dependentStatements =  createDependentXDIStatements( guardian,  dependent, dependentBirthDate, guardianPrivateSigningKey);
		targetStatements.addAll(dependentStatements);

		if (withConsent) {     
			//Generating and Adding Consent Statements
			List<XDIStatement> consentStatements =  createConsentXDIStatements(guardian,  dependent, guardianPrivateSigningKey);
			targetStatements.addAll(consentStatements);
		} else {
			throw new Xdi2ClientException("Consent required for this operation");
		}

		message.createSetOperation(targetStatements.iterator());

		// send message

		this.prepareMessageToRN(message);
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		// done
		log.debug("In RN: Creating is Guardian Relationship between " + guardian.toString() + " and " + dependent.toString() );

	}


	/**
	 * {@inheritDoc}
	 */
	public CloudNumber[] getMyDependentsInCSP(CSPInformation cspInformation, CloudNumber guardian)
			throws Xdi2ClientException {

		// Prepare message to Guardian Sub Graph in CSP Graph

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);
		Message message = messageCollection.createMessage();

		XDIStatement targetStatementGet = XDIStatement.fromRelationComponents(
				guardian.getXDIAddress(),
				XRI_S_IS_GUARDIAN,
				XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message.createGetOperation(targetStatementGet);

		// send message and read results

		this.prepareMessageToCSP(message);

		MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(guardian.getXDIAddress());
        ReadOnlyIterator<Relation> relations = cotextNode == null ? null : cotextNode.getRelations(XRI_S_IS_GUARDIAN);
        CloudNumber[] theDependencies = null;
        if(relations != null) {
        theDependencies = new IteratorArrayMaker<CloudNumber> (
				new NotNullIterator<CloudNumber> (
						new MappingCloudNumberIterator(
								new MappingRelationTargetXDIAddressIterator(relations)
								))).array(CloudNumber.class);
        }
		if (theDependencies != null && theDependencies.length == 0) {
			theDependencies = null;
		}

		// done
		log.debug("In CSP Cloud: Getting dependencies of " + guardian.toString() );

		return theDependencies;

	}


	/**
	 * {@inheritDoc}
	 */
	public CloudNumber[] getMyGuardiansInCSP(CSPInformation cspInformation, CloudNumber dependent)
			throws Xdi2ClientException {

		// Prepare message to Guardian Sub Graph in CSP Graph

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);
		Message message = messageCollection.createMessage();

		XDIStatement targetStatementGet = XDIStatement.fromRelationComponents(
				dependent.getXDIAddress(),
				XRI_S_GUARDIAN,
				XDIConstants.XDI_ADD_COMMON_VARIABLE);

		message.createGetOperation(targetStatementGet);

		// send message and read results

		this.prepareMessageToCSP(message);

		MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(dependent.getXDIAddress());
        Iterator<Relation> relations = cotextNode == null ? null : cotextNode.getRelations(XRI_S_GUARDIAN);
        CloudNumber[] theGuardians = null;
        if(relations != null) {
    		MappingRelationTargetXDIAddressIterator contextNodeIterator = new MappingRelationTargetXDIAddressIterator(relations);
    
    		ArrayList<CloudNumber> theGuardianList = new ArrayList<CloudNumber>();
    		while (contextNodeIterator.hasNext()) {
    			XDIAddress next = contextNodeIterator.next();
    			// We can expect to  get inner roots as well that we want to discard from this list.
    			// Only interested in Cloud Numbers.
    			if (CloudNumber.isValid(next)) {
    				theGuardianList.add(CloudNumber.create(next.toString()));
    			} else {
    				log.debug("Not a valid Clould Number ... : {}", next.toString());
    			}        
    		}

    		theGuardians = theGuardianList.toArray( new CloudNumber[theGuardianList.size()]);
        }
		if (theGuardians != null && theGuardians.length == 0) {
			theGuardians = null;
		}

		// done
		log.debug("In CSP Cloud: Getting Guardians of " + dependent.toString() );

		return theGuardians;

	}



	/**
	 * Create the XDIStatement that represent a Guardian's Consent and Sign it.
	 * 
	 * [=]!:uuid:1111[<#consent>]<!:uuid:6545>/$is#/[+]:uuid:0000<#parental><#consent>
	 * [=]!:uuid:1111[<#consent>]<!:uuid:6545>/$to/[=]!:uuid:3333
	 * [=]!:uuid:1111[<#consent>]<!:uuid:6545><$sig>&/&/“...”
	 * 
	 * @param guardian
	 * @param dependent
	 * @param signingKey
	 * @return XDI3 Statements
	 */ 
	private List<XDIStatement> createConsentXDIStatements(CloudNumber guardian, CloudNumber dependent, PrivateKey signingKey) {


		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();
		String consentUUID = "<*!:uuid:" + UUID.randomUUID() + ">";


		MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();


		//[=]!:uuid:1111[<#consent>]<!:uuid:6545>/$is#/[+]:uuid:0000<#parental><#consent>
		XDIStatement consentStatement = XDIStatement.fromRelationComponents(
				XDIAddressUtil.concatXDIAddresses(guardian.getXDIAddress(), XDIAddress.create("[<#consent>]"), XDIAddress.create(consentUUID)),
				XDIDictionaryConstants.XDI_ADD_IS_TYPE,
				XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XDIAddress.create("<#parental><#consent>")));

		g.setStatement(consentStatement);

		XDIAddress consentSubjectTo = XDIAddressUtil.concatXDIAddresses(guardian.getXDIAddress(), XDIAddress.create("[<#consent>]"), XDIAddress.create(consentUUID));

		//[=]!:uuid:1111[<#consent>]<!:uuid:6545>/$to/[=]!:uuid:3333
		XDIStatement consentStatementTo = XDIStatement.fromRelationComponents(
				consentSubjectTo,
				XDIAddress.create("$to"),
				dependent.getXDIAddress());
		g.setStatement(consentStatementTo);


		//Sign the Context: //[=]!:uuid:1111[<#consent>]<!:uuid:6545>        
		ContextNode signingNode = g.getRootContextNode().getDeepContextNode(consentSubjectTo);

		// now create the signature and add it to the graph
		KeyPairSignature s = (KeyPairSignature) Signatures.createSignature(signingNode, "sha", 256, "rsa", 2048, true);

		try {
			s.sign(signingKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Problem Signing Dependent Graph");           
		}

		ContextNode c = g.getRootContextNode();

		Iterator<Statement> dependencyStatmentIterator = c.getAllStatements();

		//Converting from Statement to  XDIStatement
		while(dependencyStatmentIterator.hasNext()){
			Statement next = dependencyStatmentIterator.next();
			XDIStatement graphStatement = next.getXDIStatement();
			targetStatements.add(graphStatement);
		}

		return targetStatements;


	}


	/**
	 * Create Dependent XDI Statements and Sign them with the Guardian's Public Key.
	 * 
	 * ([=]!:uuid:3333/#guardian)[=]!:uuid:3333/#guardian/([=]!:uuid:3333/#guardian)[=]!:uuid:1111
	 * [=]!:uuid:3333<#birth><$t>&/&/"2000-04-10T22:22:22Z"
	 * 
	 * 
	 * @param guardian
	 * @param dependent
	 * @param dependentBirthDate
	 * @param signingKey
	 * @return XDIStatements
	 */
	private List<XDIStatement> createDependentXDIStatements(CloudNumber guardian, CloudNumber dependent, Date dependentBirthDate, PrivateKey signingKey) {


		XDIArc innerGraph = XDIArc.create("(" + dependent.getXDIAddress() +"/#guardian" + ")");

		MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();


		//Create the Relational Entry in the Dependent Sub Graph
		//[=]!:uuid:3333/#guardian/[=]!:uuid:1111
		XDIStatement guardianStatement = XDIStatement.fromRelationComponents(
				dependent.getXDIAddress(),
				XRI_S_GUARDIAN,
				guardian.getXDIAddress());

		g.setStatement(guardianStatement);

		// ([=]!:uuid:3333/#guardian)[=]!:uuid:3333 /#guardian/([=]!:uuid:3333/#guardian)[=]!:uuid:1111


		XDIStatement innerGuardianStatement = XDIStatement.fromRelationComponents(
				XDIAddressUtil.concatXDIAddresses(innerGraph,dependent.getXDIAddress()),
				XRI_S_GUARDIAN,
				XDIAddressUtil.concatXDIAddresses(innerGraph, guardian.getXDIAddress()));

		g.setStatement(innerGuardianStatement);

		//Adding Date to Dependent's SubGraph
		//[=]!:uuid:3333<#birth><$t>&/&/"2000-04-10T22:22:22Z")

		String xdiDOBFormat = Timestamps.timestampToString(dependentBirthDate);
		XDIStatement dobStatement = XDIStatement.fromLiteralComponents(
				XDIAddressUtil.concatXDIAddresses(dependent.getXDIAddress(), XDIAddress.create("<#birth><$t>")), 
				xdiDOBFormat);

		g.setStatement(dobStatement);


		//Sign the Context: ([=]!:uuid:3333/#guardian)[<$sig>]<[=]!:uuid:6666>&/&/”...”        
		ContextNode signingNode = g.getRootContextNode().getContextNode(innerGraph);

		// now create the signature and add it to the graph
		KeyPairSignature s = (KeyPairSignature) Signatures.createSignature(signingNode, "sha", 256, "rsa", 2048, false);


		try {
			s.sign(signingKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Problem Signing Dependent Graph");           
		}



		//Add Public Key to <$sig> 
		//([=]!:uuid:3333/#guardian)<$sig><$public><$key>/$ref/[=]!:uuid:1111$msg$sig$keypair<$public><$key>         

		ContextNode signatureContextNode = s.getContextNode();

		XDIAddress pk =  XDIAddress.create("<$public><$key>");

		signatureContextNode
		.setDeepContextNode(pk)
		.setRelation(
				XDIDictionaryConstants.XDI_ADD_REF,
				XDIAddressUtil.concatXDIAddresses(guardian.getXDIAddress(), XDIAuthenticationConstants.XDI_ADD_MSG_SIG_KEYPAIR_PRIVATE_KEY));


		ContextNode c = g.getRootContextNode();

		Iterator<Statement> dependencyStatementIterator = c.getAllStatements();

		//Converting from Statement to XDIStatement
		while(dependencyStatementIterator.hasNext()){
			Statement next = dependencyStatementIterator.next();
			XDIStatement graphStatement = next.getXDIStatement();
			targetStatements.add(graphStatement);
		}

		return targetStatements;

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
		((XDIHttpClient) this.xdiClientCSPRegistry).setFollowRedirects(true);
		this.xdiClientRNRegistrationService = new XDIHttpClient(cspInformation.getRnRegistrationServiceXdiEndpoint());
		((XDIHttpClient) this.xdiClientRNRegistrationService).setFollowRedirects(true);
	}

	public XDIClient getXdiClientCSPRegistry() {

		return this.xdiClientCSPRegistry;
	}

	public void setXdiClientCSPRegistry(XDIClient xdiClientCSPRegistry) {

		this.xdiClientCSPRegistry = xdiClientCSPRegistry;
	}

	public XDIClient getXdiClientRNRegistrationService() {

		return this.xdiClientRNRegistrationService;
	}

	public void setXdiClientRNRegistrationService(XDIClient xdiClientRNRegistrationService) {

		this.xdiClientRNRegistrationService = xdiClientRNRegistrationService;
	}
	@Override
	public String toString(){
		if(this.cspInformation != null){
			return this.cspInformation.toString();
		} else {
			return new String("cspInformation is null");
		}
	}
	public static void printMessage(MessageEnvelope messageEnvelope)
	{
		try {
			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
					messageEnvelope.getGraph(), System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Override
	public void registerAdditionalCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber,
			NeustarRNDiscountCode neustarRNDiscountCode, NeustarRNCampaignCode neustarRNCampaignCode)
					throws Xdi2ClientException {

		/* Call the Registration Service to 
           1) Register the Cloud Name ( with the Cloud Number)
           2) Create a Member Graph Billing Record

           CSP Cloud Number: [+]!:uuid:9999
           Respect Network Cloud Number: [+]!:uuid:0000
           Message Id: [=]!:uuid:1234
           User Cloud Number: [=]!:uuid:1111

           [+]!:uuid:0000[#billing]!:uuid:1234/$from/[+]!:uuid:0000
           [+]!:uuid:0000[#billing]!:uuid:1234/$to/[+]!:uuid:9999
           [+]!:uuid:0000[#billing]!:uuid:1234/#member/[=]!:uuid:1111
           [+]!:uuid:0000[#billing]!:uuid:1234<$t>&/&/"2014-02-01T21:25:04Z"
           [+]!:uuid:0000[#billing]!:uuid:1234/$is#/#debit
           [+]!:uuid:0000[#billing]!:uuid:1234<#action><#name>&/&/"Additional lifetime name"
           [+]!:uuid:0000[#billing]!:uuid:1234<#action><#code>&/&/"0010"
           [+]!:uuid:0000[#billing]!:uuid:1234<#amount>&/&/19
           [+]!:uuid:0000[#billing]!:uuid:1234<#currency>&/&/"USD"
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@0<#status>&/&/"pending"
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@0<$t>&/&/"2014-..."
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@0/$from/[+]!:uuid:9999
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@1<#status>&/&/"pending"
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@1<$t>&/&/"2014-..."
           [+]!:uuid:0000[#billing]!:uuid:1234[#status]@1/$from/[+]!:uuid:9999
		 */


		// prepare message 1 to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message1 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();


		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(cloudName.getPeerRootXDIArc())));

		Operation operation1 = message1.createSetOperation(targetStatementsSet.iterator());

		if (neustarRNDiscountCode != null) {
			operation1.setParameter(XDIDiscountCodeConstants.XRI_S_PARAMETER_NEUSTAR_RN_DISCOUNTCODE, neustarRNDiscountCode.toString());
		}
		if (neustarRNCampaignCode != null) {
			operation1.setParameter(XDIDiscountCodeConstants.XRI_S_PARAMETER_NEUSTAR_RN_CAMPAIGNCODE, neustarRNCampaignCode.toString());
		}


		this.prepareMessageToRN(message1);


		log.debug("registerAdditionalCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerAdditionalCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		/*Relation relation = messageResult.getGraph().getDeepRelation(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), XDIDictionaryConstants.XDI_ADD_REF);

		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered.");

		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
		if (! registeredCloudNumber.equals(cloudNumber)) throw new RuntimeException("Registered Cloud Number "
				+ registeredCloudNumber + " does not match requested Cloud Number " + cloudNumber);
        */
		// done

		log.debug("In RN: Additional Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);



	}

	@Override
	public void registerAdditionalCloudNameInCSP(CloudName cloudName,
			CloudNumber cloudNumber) throws Xdi2ClientException {

		/* 
		  We are adding the following to the CSP Graph 
		  (=alice.new)/$ref/([=]!:uuid:1111)
          ([=]!:uuid:1111)/$is$ref/(=alice.new)
		 */

		this.registerCloudNameInCSP(cloudName, cloudNumber);


	}

	@Override
	public void registerAdditionalCloudNameInCloud(CloudName cloudName,
			CloudNumber cloudNumber, String userToken) throws Xdi2ClientException {

		/*
		 (=alice.new)/$ref/([=]!:uuid:1111)
         =alice.new/$ref/[=]!:uuid:1111

         [=]!:uuid:1111/$is$ref/=alice.new
         [=]!:uuid:1111$to$anon$from$public$do/$get/(=alice/$ref/[=]!:uuid:1111))
		 */

		this.registerCloudNameInCloud(cloudName, cloudNumber, userToken);

		log.debug("Added Additional CloudName: " + cloudName + " to Cloud Number " + cloudNumber);



	}


	@Override
	public void registerBusinessNameInRN(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException {


		// Validate that we are dealing with "+" Names and Numbers.
		char nameCS = businessCloudName.getCs();

		if ( nameCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		//@TODO  Does the Contact have to  be an "=" Number 


		// prepare message 1 to RN

		/*
	         [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/((+biz.name)/$ref/([+]!:uuid:5555))
             [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/(([+]!:uuid:5555)/$is$ref/(+biz.name))

		 */

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message1 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc())));

		message1.createSetOperation(targetStatementsSet.iterator());


		/*
		Set Billing Records

        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([+]!:uuid:5555/#billing#contact/[=]!:uuid:1111)
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555) 
		 */


		Message message2 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementBillingSet = new ArrayList<XDIStatement> ();


		targetStatementBillingSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc()), 
				XRI_S_BILLING_CONTACT, 
				XDIAddress.fromComponent(contactCloudNumber.getPeerRootXDIArc())));

		targetStatementBillingSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(contactCloudNumber.getPeerRootXDIArc()),
				XRI_S_IS_BILLING_CONTACT, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));

		message2.createSetOperation(targetStatementBillingSet.iterator());


		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);


		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()));
        Relation relation = cotextNode == null ? null : cotextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);
		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered.");


		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
		if (! registeredCloudNumber.equals(businessCloudNumber)) throw new RuntimeException("Registered Cloud Number "
				+ registeredCloudNumber + " does not match requested Cloud Number " + businessCloudNumber);

		// done

		log.debug("In RN: Business Cloud Name " + businessCloudName + " registered with Cloud Number " + businessCloudNumber);

	}

	@Override
	public void registerBusinessNameInCSP(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException {


		// Validate that we are dealing with "+" Names and Numbers.
		char nameCS = businessCloudName.getCs();

		if ( nameCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		//@TODO  Does the Contact have to  be an "=" Number 


		// prepare message 1 to RN

		/*
	         [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/((+biz.name)/$ref/([+]!:uuid:5555))
             [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/(([+]!:uuid:5555)/$is$ref/(+biz.name))

		 */

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message1 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc())));

		message1.createSetOperation(targetStatementsSet.iterator());


		/*
		Set Billing Records

        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([+]!:uuid:5555/#billing#contact/[=]!:uuid:1111)
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555) 
		 */


		Message message2 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementBillingSet = new ArrayList<XDIStatement> ();


		targetStatementBillingSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc()), 
				XRI_S_BILLING_CONTACT, 
				XDIAddress.fromComponent(contactCloudNumber.getPeerRootXDIArc())));

		targetStatementBillingSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(contactCloudNumber.getPeerRootXDIArc()),
				XRI_S_IS_BILLING_CONTACT, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));

		message2.createSetOperation(targetStatementBillingSet.iterator());


		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);


		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()));
        Relation relation = cotextNode == null ? null : cotextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);

        if (relation == null) throw new RuntimeException("Additional Cloud Name not registered in CSP Graph.");


		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
		if (! registeredCloudNumber.equals(businessCloudNumber)) throw new RuntimeException("Registered Cloud Number "
				+ registeredCloudNumber + " does not match requested Cloud Number " + businessCloudNumber);

		// done

		log.debug("In CSP: Business Cloud Name " + businessCloudName + " registered with Cloud Number " + businessCloudNumber);

	}


	@Override
	public void registerBusinessNameInCloud(CloudName businessCloudName, CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException {



		// Validate that we are dealing with "+" Names and Numbers.
		char nameCS = businessCloudName.getCs();

		if ( nameCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL);
		}

		//@TODO  Does the Contact have to  be an "=" Number 


		// prepare message 1 to RN

		/*
	    	(+biz.name)/$ref/([+]!:uuid:5555)
		    ([+]!:uuid:5555)/$is$ref/(+biz.name)

		 */

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message1 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()), 
				XDIDictionaryConstants.XDI_ADD_REF, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));

		targetStatementsSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc()),
				XDIDictionaryConstants.XDI_ADD_IS_REF, 
				XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc())));

		message1.createSetOperation(targetStatementsSet.iterator());


		/*
		    Set Billing Records
		    [=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555
		 */


		Message message2 = messageCollection.createMessage(-1);

		List<XDIStatement> targetStatementBillingSet = new ArrayList<XDIStatement> ();

		/* [=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555 */
		targetStatementBillingSet.add(XDIStatement.fromRelationComponents(
				XDIAddress.fromComponent(contactCloudNumber.getPeerRootXDIArc()),
				XRI_S_IS_BILLING_CONTACT, 
				XDIAddress.fromComponent(businessCloudNumber.getPeerRootXDIArc())));


		List<XDIStatement> signedBillingStatments = createSignedBillingXDIStatements(businessCloudNumber,
				contactCloudNumber);
		targetStatementBillingSet.addAll(signedBillingStatments);


		message2.createSetOperation(targetStatementBillingSet.iterator());

		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);


		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
        
		ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(businessCloudName.getPeerRootXDIArc()));
        Relation relation = cotextNode == null ? null : cotextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);

        if (relation == null) throw new RuntimeException("Additional Cloud Name not registered in CSP Graph.");


		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
		if (! registeredCloudNumber.equals(businessCloudNumber)) throw new RuntimeException("Registered Cloud Number "
				+ registeredCloudNumber + " does not match requested Cloud Number " + businessCloudNumber);

		// done

		log.debug("In CSP: Business Cloud Name " + businessCloudName + " registered with Cloud Number " + businessCloudNumber);

	}


	/**
	 * Create the XDIStatement that represent a Billing Contact and Sign it.
	 * 
	 * 
	 * @param businessCloudNumber uuid:5555
	 * @param contactCloudNumber uuid:1111
	 * @param signingKey 
	 * @return XDI3 Statements
	 */ 
	private List<XDIStatement> createSignedBillingXDIStatements(CloudNumber businessCloudNumber,
			CloudNumber contactCloudNumber) throws Xdi2ClientException {

		/*
		([=]!:uuid:1111/$is#billing#contact)[=]!:uuid:1111/
		$is#billing#contact/
		([=]!:uuid:1111/$is#billing#contact)[+]!:uuid:5555


		([=]!:uuid:1111/$is#billing#contact)[<$sig>]<!:uuid:7846>&/&/”...”
		([=]!:uuid:1111/$is#billing#contact)[<$sig>]<!:uuid:7846>/$is#/([=]!:uuid:1111/$is#billing#contact)$sha$256$rsa$2048	
		([=]!:uuid:1111/$is#billing#contact)[<$sig>]<!:uuid:7846><$public><$key>/$ref/([=]!:uuid:1111/$is#billing#contact)[+]!:uuid:9999$sig$keypair<$public><$key>
		 */

		PrivateKey signingKey;

		try
		{
			BasicCSPInformation basicCSP = (BasicCSPInformation) this.getCspInformation();
			if(basicCSP.getCspSignaturePrivateKey() == null)
			{
				basicCSP.retrieveCspSignaturePrivateKey();
			}

		} catch (Xdi2ClientException e) {
			String errorMsg = "Problem Retrieving CSP Private Key" + e.getMessage();
			log.warn(errorMsg);
			throw new Xdi2ClientException(errorMsg);
		} catch (GeneralSecurityException e){
			String errorMsg = "Problem Retrieving CSP Private Key" + e.getMessage();
			log.warn(errorMsg);
			throw new Xdi2ClientException(errorMsg);
		}


		if (this.getCspInformation().getCspSignaturePrivateKey() != null) {
			log.debug("CSP Private Key is not null. Signing Messages with it.");
			signingKey = this.getCspInformation().getCspSignaturePrivateKey();
		} else {
			String errorMsg = "Problem Retrieving CSP Private Key: Key is Null";
			log.warn(errorMsg);
			throw new Xdi2ClientException(errorMsg);
		}

		List<XDIStatement> targetStatements = new ArrayList<XDIStatement> ();


		MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();

		/* ([=]!:uuid:1111/$is#billing#contact) */
		XDIAddress contactIsBC = XDIAddress.create("(" + contactCloudNumber.getXDIAddress() +"/" + XRI_S_IS_BILLING_CONTACT +")");


		XDIStatement contactBillingStatement = XDIStatement.fromRelationComponents(
				XDIAddressUtil.concatXDIAddresses( contactIsBC, XDIAddress.create(contactCloudNumber.toString())),
				XRI_S_IS_BILLING_CONTACT,
				XDIAddressUtil.concatXDIAddresses(contactIsBC, this.getCspInformation().getRnCloudNumber().getXDIAddress()));

		g.setStatement(contactBillingStatement);


		//Sign the Context: //[=]!:uuid:1111[<#consent>]<!:uuid:6545>        
		ContextNode signingNode = g.getRootContextNode().getDeepContextNode(contactIsBC);

		// now create the signature and add it to the graph
		KeyPairSignature s = (KeyPairSignature) Signatures.createSignature(signingNode, "sha", 256, "rsa", 2048, true);

		try {
			s.sign(signingKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Problem Signing Billing Contact  Graph");           
		}

		ContextNode c = g.getRootContextNode();

		Iterator<Statement> billingContactStatmentIterator = c.getAllStatements();

		//Converting from Statement to  XDIStatement
		while(billingContactStatmentIterator.hasNext()){
			Statement next = billingContactStatmentIterator.next();
			XDIStatement graphStatement = next.getXDIStatement();
			targetStatements.add(graphStatement);
		}

		return targetStatements;


	}

	@Override
	public void updatePhoneInRN(CloudNumber cloudNumber, String verifiedPhone, String oldVerifiedPhone) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		if (oldVerifiedPhone != null) {
			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(oldVerifiedPhone)))),
					XRI_S_IS_PHONE,
					cloudNumber.getXDIAddress()));
		}

		this.prepareMessageToRN(message);
		message.createDelOperation(targetStatementsSet.iterator());

		// send message
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		//update(set) new phone number
		setPhoneAndEmailInRN(cloudNumber, verifiedPhone, null);
		// done

		log.debug("Updated RN: Verified phone " + oldVerifiedPhone + " with new  Verified phone " + verifiedPhone + " for Cloud Number " + cloudNumber);		
	}

	@Override
	public void updateEmailInRN(CloudNumber cloudNumber, String verifiedEmail, String oldVerifiedEmail) throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();

		if (oldVerifiedEmail != null) {
			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddressUtil.concatXDIAddresses(this.getCspInformation().getRnCloudNumber().getXDIAddress(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDIAddress.fromComponent(XdiAbstractInstanceUnordered.createXDIArc(true, true, false, XDIArc.literalFromDigest(oldVerifiedEmail)))),
					XRI_S_IS_EMAIL,
					cloudNumber.getXDIAddress()));
		}

		this.prepareMessageToRN(message);
		message.createDelOperation(targetStatementsSet.iterator());

		// send message
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		//update(set) new email
		setPhoneAndEmailInRN(cloudNumber, null, verifiedEmail);
		// done

		log.debug("Updated RN: Verified phone " + oldVerifiedEmail + " with new  Verified phone " + verifiedEmail + " for Cloud Number " + cloudNumber);	
		
	}

	@Override
	public CloudNumber getMemberRegistrar(CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();


		XDIStatement targetAddress = XDIStatement.fromRelationComponents(cloudNumber.getXDIAddress(),XRI_S_REGISTRAR, XDIConstants.XDI_ADD_COMMON_VARIABLE);
		message.createGetOperation(targetAddress);

		// send message and read results

		this.prepareMessageToRN(message);
		
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(cloudNumber.getXDIAddress());
        Relation relation = cotextNode == null ? null : cotextNode.getRelation(XRI_S_REGISTRAR);

		CloudNumber cspCloudNumber = null;
		if (relation != null) {
			cspCloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
		}

		// done

		log.debug("In RN: For Cloud Number " + cloudNumber + " found CSP Cloud Number " + cspCloudNumber);
		return cspCloudNumber;
	
	}

	@Override
	public void transferCloudInCSP(CloudNumber cloudNumber, CloudName[] cloudNames, String secretToken) throws Xdi2ClientException{		
		//register cloud in CSP
		registerCloudInCSP(cloudNumber, secretToken);
		
		//register cloudNames in CSP
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();
		List<XDIStatement> targetStatementsSet = new ArrayList<XDIStatement> ();
		for(CloudName cloudName:cloudNames){
			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), 
					XDIDictionaryConstants.XDI_ADD_REF, 
					XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc())));

			targetStatementsSet.add(XDIStatement.fromRelationComponents(
					XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc()),
					XDIDictionaryConstants.XDI_ADD_IS_REF, 
					XDIAddress.fromComponent(cloudName.getPeerRootXDIArc())));
		}
		message.createSetOperation(targetStatementsSet.iterator());
		// send message

		this.prepareMessageToCSP(message);
		log.debug("registerCloudNamesInCSP :: Message  "+ messageEnvelope.getGraph().toString());
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		//set registrar
		setCloudXdiEndpointInCSP(cloudNumber, null);
	}
	
	@Override
	public void deleteCloudInCSP(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException{
		// prepare message to CSP
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);
		Message message = messageCollection.createMessage();
		
		XDIAddress targetAddress = XDIAddress.fromComponent(cloudNumber.getPeerRootXDIArc());
		message.createDelOperation(targetAddress);

		// send message
		this.prepareMessageToCSP(message);
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);
	}
	
	
	@Override
	public void changeMemberRegistrarInRN(CloudNumber cloudNumber) throws Xdi2ClientException {
		//check if cloudNumber has a registrar assignd already
		CloudNumber currentMemberRegistrar = getMemberRegistrar(cloudNumber);
		//delete existing registrar
		if(currentMemberRegistrar!=null){
			deleteMemberRegistrar(cloudNumber, currentMemberRegistrar);
		}
		currentMemberRegistrar = getMemberRegistrar(cloudNumber);
		//if delete successfull update with new registrar
		if(currentMemberRegistrar==null){
			// prepare message to CSP
	
			MessageEnvelope messageEnvelope = new MessageEnvelope();
			MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);
	
			Message message = messageCollection.createMessage();
	
	
			XDIStatement targetAddress = XDIStatement.fromRelationComponents(cloudNumber.getXDIAddress(),XRI_S_REGISTRAR,getCspInformation().getCspCloudNumber().getXDIAddress());
			message.createSetOperation(targetAddress);
	
			// send message and read results
	
			this.prepareMessageToRN(message);
			
			this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
	
			log.debug("In RN: updated member registrar for:" + cloudNumber +" with CSP Cloud Number: "+getCspInformation().getCspCloudNumber().getXDIAddress());
		}
	}
	
	/**
	 * deletes current member registrar for cloud number
	 * @param cloudNumber
	 * @param oldMemberRegistrar
	 * @throws Xdi2ClientException
	 */
	private void deleteMemberRegistrar(CloudNumber cloudNumber, CloudNumber currentMemberRegistrar) throws Xdi2ClientException {

		// prepare message to CSP
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDIStatement> targetStatementsDel = new ArrayList<XDIStatement> ();
		XDIStatement targetAddress = XDIStatement.fromRelationComponents(cloudNumber.getXDIAddress(),XRI_S_REGISTRAR,currentMemberRegistrar.getXDIAddress());
		targetStatementsDel.add(targetAddress);
		message.createDelOperation(targetStatementsDel.iterator());

		// send message and read results
		this.prepareMessageToRN(message);
		this.getXdiClientRNRegistrationService().send(messageEnvelope, null);


		log.debug("Deleted current member registrar for:" + cloudNumber);
	
	}
	
    @Override
    public CloudNumber checkCloudNameInCSP(CloudName cloudName) throws Xdi2ClientException {
        CloudNumber cloudNumber = null;
        // prepare message to RN
        MessageEnvelope messageEnvelope = new MessageEnvelope();
        MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

        Message message = messageCollection.createMessage();

        XDIStatement targetStatement = XDIStatement.fromComponents(
                XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()), XDIDictionaryConstants.XDI_ADD_REF,
                XDIConstants.XDI_ADD_COMMON_VARIABLE);

        message.createGetOperation(targetStatement);

        // send message and read result
	    this.prepareMessageToCSP(message);

	    log.debug("checkCloudNameInCSP :: Message envelope to CSP \n");
	    printMessage(messageEnvelope);

	    MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);
        ContextNode cotextNode = messageResult.getGraph().getDeepContextNode(XDIAddress.fromComponent(cloudName.getPeerRootXDIArc()));
        Relation relation = cotextNode == null ? null : cotextNode.getRelation(XDIDictionaryConstants.XDI_ADD_REF);

	    if (relation != null) {
           cloudNumber = CloudNumber.fromPeerRootXDIArc(relation.getTargetXDIAddress());
	    }

	   // done
	   log.debug("In CSP: For Cloud Name " + cloudName + " found Cloud Number " + cloudNumber);
       return cloudNumber;
	}

}
