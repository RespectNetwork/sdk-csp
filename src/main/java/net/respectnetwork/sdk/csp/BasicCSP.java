package net.respectnetwork.sdk.csp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.respectnetwork.sdk.csp.exception.CoreRNServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.constants.XDIClientConstants;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.ContextNode;
import xdi2.core.Literal;
import xdi2.core.Relation;
import xdi2.core.Statement;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.PublicLinkContract;
import xdi2.core.features.linkcontracts.RootLinkContract;
import xdi2.core.features.nodetypes.XdiAbstractMemberUnordered;
import xdi2.core.features.signatures.KeyPairSignature;
import xdi2.core.features.signatures.Signatures;
import xdi2.core.features.timestamps.Timestamps;
import xdi2.core.impl.memory.MemoryGraph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.util.XDI3Util;
import xdi2.core.util.iterators.IteratorArrayMaker;
import xdi2.core.util.iterators.MappingCloudNameIterator;
import xdi2.core.util.iterators.MappingCloudNumberIterator;
import xdi2.core.util.iterators.MappingRelationTargetContextNodeXriIterator;
import xdi2.core.util.iterators.NotNullIterator;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.core.xri3.CloudName;
import xdi2.core.xri3.CloudNumber;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.core.xri3.XDI3SubSegment;
import xdi2.core.xri3.XDI3XRef;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;
import xdi2.messaging.error.ErrorMessageResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BasicCSP implements CSP {

	private static final Logger log = LoggerFactory.getLogger(BasicCSP.class);

	public static final XDI3Segment XRI_S_AC_VERIFIED_DIGEST_PHONE = XDI3Segment.create("<#verified><$digest>[<#phone>]");
	public static final XDI3Segment XRI_S_AC_VERIFIED_DIGEST_EMAIL = XDI3Segment.create("<#verified><$digest>[<#email>]");
	public static final XDI3Segment XRI_S_AS_VERIFIED_PHONE = XDI3Segment.create("<#verified><#phone>");
	public static final XDI3Segment XRI_S_AS_VERIFIED_EMAIL = XDI3Segment.create("<#verified><#email>");
	public static final XDI3Segment XRI_S_IS_PHONE = XDI3Segment.create("$is#phone");
	public static final XDI3Segment XRI_S_IS_EMAIL = XDI3Segment.create("$is#email");
	
    public static final XDI3Segment XRI_S_IS_GUARDIAN = XDI3Segment.create("$is#guardian");
    public static final XDI3Segment XRI_S_GUARDIAN = XDI3Segment.create("#guardian");
    
    public static final XDI3Segment XRI_S_IS_BILLING_CONTACT = XDI3Segment.create("$is#billing#contact");
    public static final XDI3Segment XRI_S_BILLING_CONTACT = XDI3Segment.create("#billing#contact");

	public static final XDI3Segment XRI_S_MEMBER = XDI3Segment.create("#member");
	public static final XDI3Segment XRI_S_AS_MEMBER_EXPIRATION_TIME = XDI3Segment.create("<#member><#expiration><$t>");

	public static final XDI3Segment XRI_S_FIRST_MEMBER = XDI3Segment.create("#first#member");

	public static final XDI3Segment XRI_S_AS_AVAILABLE = XDI3Segment.create("<#available>");

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

		XDI3Statement targetStatementSet = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint);

		message.createSetOperation(targetStatementSet);

		if (secretToken != null) {

			XDI3Statement targetStatementDoDigestSecretToken = XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), 
					secretToken);

			message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), targetStatementDoDigestSecretToken);
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

		XDI3Segment targetAddress = XDI3Segment.fromComponent(cloudName.getPeerRootXri());

		message.createGetOperation(targetAddress);

		// send message and read result

		this.prepareMessageToRN(message);
		
		log.debug("checkCloudNameAvailableInRN :: Message envelope to RN \n" );
		printMessage(messageEnvelope);
		
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		Literal literal = messageResult.getGraph().getDeepLiteral(XDI3Util.concatXris(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XRI_S_AS_AVAILABLE, XDIConstants.XRI_S_VALUE));
		if (literal == null) throw new Xdi2ClientException("No availability literal found in result.", null);

		Boolean literalDataBoolean = literal.getLiteralDataBoolean();
		if (literalDataBoolean == null) throw new Xdi2ClientException("No availability boolean value found in result.", null);

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

		XDI3Statement targetStatement = XDI3Statement.fromComponents(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF, XDIConstants.XRI_S_VARIABLE);

		message.createGetOperation(targetStatement);

		// send message and read result

		this.prepareMessageToRN(message);
		
		log.debug("checkCloudNameAvailableInRN :: Message envelope to RN \n" );
		printMessage(messageEnvelope);
		
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		Relation relation = messageResult.getGraph().getDeepRelation(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);

		if (relation != null) {

			cloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
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

		XDI3Segment targetAddress1 = verifiedPhone == null ? null : XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedPhone, true)));
		XDI3Segment targetAddress2 = verifiedEmail == null ? null : XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedEmail, true)));

		if (targetAddress1 != null) message.createGetOperation(targetAddress1);
		if (targetAddress2 != null) message.createGetOperation(targetAddress2);

		// send message

		this.prepareMessageToRN(message);
		log.debug("checkPhoneAndEmailAvailableInRN :: Message to RN " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		Relation relation1 = targetAddress1 == null ? null : messageResult.getGraph().getDeepRelation(targetAddress1, XRI_S_IS_PHONE);
		Relation relation2 = targetAddress2 == null ? null : messageResult.getGraph().getDeepRelation(targetAddress2, XRI_S_IS_EMAIL);

		if (relation1 != null) {

			cloudNumbers[0] = CloudNumber.fromXri(relation1.getTargetContextNodeXri());
		}

		if (relation2 != null) {

			cloudNumbers[1] = CloudNumber.fromXri(relation2.getTargetContextNodeXri());
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

		XDI3Statement targetStatementSet1 = XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDIConstants.XRI_S_VARIABLE);

		message1.createSetOperation(targetStatementSet1);

		// send message 1 and read result

		this.prepareMessageToRN(message1);
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope1, null);

		Relation relation = messageResult.getGraph().getDeepRelation(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Number not registered.");

		CloudNumber cloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());

		// prepare message 2 to RN

		MessageEnvelope messageEnvelope2 = new MessageEnvelope();
		MessageCollection messageCollection2 = this.createMessageCollectionToCSP(messageEnvelope2);

		Message message2 = messageCollection2.createMessage();

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		XDI3Statement targetStatementSet2 = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(cloudName.getPeerRootXri())));

		Operation operation1 = message1.createSetOperation(targetStatementsSet.iterator());

		if (neustarRNDiscountCode != null) {

			operation1.setParameter(XDIDiscountCodeConstants.XRI_S_PARAMETER_NEUSTAR_RN_DISCOUNTCODE, neustarRNDiscountCode.toString());
		}

		// prepare message 2 to RN

		Message message2 = messageCollection.createMessage(-1);

		String cloudXdiEndpoint = this.makeCloudXdiEndpoint(cloudNumber);

		List<XDI3Statement> targetStatementsSet2 = new ArrayList<XDI3Statement> ();

		targetStatementsSet2.add(XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
				cloudXdiEndpoint));

		message2.createSetOperation(targetStatementsSet2.iterator());

		// prepare message 3 to RN

		Message message3 = messageCollection.createMessage(-1);

		List<XDI3Statement> targetStatementsSet3 = new ArrayList<XDI3Statement> ();

		if (verifiedPhone != null) {

			targetStatementsSet3.add(XDI3Statement.fromRelationComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedPhone, true))),
					XRI_S_IS_PHONE,
					cloudNumber.getXri()));
		}

		if (verifiedEmail != null) {

			targetStatementsSet3.add(XDI3Statement.fromRelationComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedEmail, true))),
					XRI_S_IS_EMAIL,
					cloudNumber.getXri()));
		}

		message3.createSetOperation(targetStatementsSet3.iterator());

		// send messages

		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);
		this.prepareMessageToRN(message3);
		
		log.debug("registerCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		Relation relation = messageResult.getGraph().getDeepRelation(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		if (relation == null) throw new RuntimeException("Cloud Name not registered.");

		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
		if (! registeredCloudNumber.equals(cloudNumber)) throw new RuntimeException("Registered Cloud Number " + registeredCloudNumber + " does not match requested Cloud Number " + cloudNumber);

		// done

		log.debug("In RN: Cloud Name " + cloudName + " registered with Cloud Number " + cloudNumber);
	}

	@Override
	public void registerCloudNameInCSP(CloudName cloudName, CloudNumber cloudNumber) throws Xdi2ClientException {

		// prepare message to CSP

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCSP(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(cloudName.getPeerRootXri())));

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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();
		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri())));
		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				cloudName.getXri(), 
				XDIDictionaryConstants.XRI_S_REF, 
				cloudNumber.getXri()));
		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				cloudNumber.getXri(), 
				XDIDictionaryConstants.XRI_S_IS_REF, 
				cloudName.getXri()));
		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()),
				XDILinkContractConstants.XRI_S_GET,
				XDI3Segment.fromComponent(XDI3SubSegment.fromComponents(null, false, false, null, XDI3XRef.fromComponents(XDIConstants.XS_ROOT, null, XDI3Statement.fromRelationComponents(cloudName.getXri(), XDIDictionaryConstants.XRI_S_REF, cloudNumber.getXri()), null, null, null, null)))));

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

		List<XDI3Statement> targetStatementsDel = new ArrayList<XDI3Statement> ();

		targetStatementsDel.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri())));

		targetStatementsDel.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(cloudName.getPeerRootXri())));

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

		XDI3Statement targetStatementSet = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		if (verifiedPhone != null) {

			targetStatementsSet.add(XDI3Statement.fromRelationComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_PHONE, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedPhone, true))),
					XRI_S_IS_PHONE,
					cloudNumber.getXri()));
		}

		if (verifiedEmail != null) {

			targetStatementsSet.add(XDI3Statement.fromRelationComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AC_VERIFIED_DIGEST_EMAIL, XDI3Segment.fromComponent(XdiAbstractMemberUnordered.createDigestArcXri(verifiedEmail, true))),
					XRI_S_IS_EMAIL,
					cloudNumber.getXri()));
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

		List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

		targetStatements.add(XDI3Statement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXri(),
				XRI_S_MEMBER,
				cloudNumber.getXri()));

		if (expirationTime != null) {

			targetStatements.add(XDI3Statement.fromComponents(
					this.getCspInformation().getRnCloudNumber().getXri(),
					XRI_S_MEMBER,
					XDI3Segment.fromComponent(
							XDI3SubSegment.fromComponents(null, false, false, null, 
									XDI3XRef.fromComponents(XDIConstants.XS_ROOT, null, 
											XDI3Statement.fromLiteralComponents(
													XDI3Util.concatXris(cloudNumber.getXri(), XRI_S_AS_MEMBER_EXPIRATION_TIME, XDIConstants.XRI_S_VALUE), 
													Timestamps.timestampToString(expirationTime)), 
													null, null, null, null)))));
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

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXri(),
				XRI_S_MEMBER,
				cloudNumber.getXri());

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

		List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

		targetStatements.add(XDI3Statement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXri(),
				XRI_S_FIRST_MEMBER,
				cloudNumber.getXri()));

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

		XDI3Statement targetStatement = XDI3Statement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXri(),
				XRI_S_FIRST_MEMBER,
				cloudNumber.getXri());

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

		XDI3Statement targetStatementSet = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(cloudNumber.getPeerRootXri(), XDI3Segment.create("<$xdi><$uri>&")), 
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

		XDI3Statement targetStatementsDoDigestSecretToken = XDI3Statement.fromLiteralComponents(
				XDI3Util.concatXris(XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()), XDIAuthenticationConstants.XRI_S_DIGEST_SECRET_TOKEN, XDIConstants.XRI_S_VALUE), 
				secretToken);

		message.createOperation(XDI3Segment.create("$do<$digest><$secret><$token>"), targetStatementsDoDigestSecretToken);

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

		XDI3Statement targetStatementGet = XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF,
				XDIConstants.XRI_S_VARIABLE);

		message.createGetOperation(targetStatementGet);

		// send message and read results

		this.prepareMessageToCSP(message);
		MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		ReadOnlyIterator<Relation> relations = messageResult.getGraph().getDeepRelations(XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()), XDIDictionaryConstants.XRI_S_IS_REF);

		CloudName[] cloudNames = new IteratorArrayMaker<CloudName> (
				new NotNullIterator<CloudName> (
						new MappingCloudNameIterator(
								new MappingRelationTargetContextNodeXriIterator(relations)
								))).array(CloudName.class);

		// done

		log.debug("In CSP: For Cloud Number " + cloudNumber + " found Cloud Names " + Arrays.asList(cloudNames));
		return cloudNames;
	}

	@Override
	public void authenticateInCloud(CloudNumber cloudNumber, String secretToken) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		XDI3Segment targetAddress = RootLinkContract.createRootLinkContractXri(cloudNumber.getXri());

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
	public void setCloudServicesInCloud(CloudNumber cloudNumber, String secretToken, Map<XDI3Segment, String> services) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> (services.size() * 2);

		for (Entry<XDI3Segment, String> entry : services.entrySet()) {

			targetStatementsSet.add(XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(cloudNumber.getXri(), entry.getKey(), XDIClientConstants.XRI_S_AS_URI, XDIConstants.XRI_S_VALUE),
					entry.getValue()));

			targetStatementsSet.add(XDI3Statement.fromRelationComponents(
					PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()),
					XDILinkContractConstants.XRI_S_GET,
					XDI3Util.concatXris(XDI3Segment.fromComponent(PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()).getFirstSubSegment()), cloudNumber.getXri(), entry.getKey(), XDIClientConstants.XRI_S_AS_URI)));
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
	public void setCloudServicesForCSPInCSP(CloudNumber cloudNumber, String secretToken, String cspXdiEndpoint, Map<XDI3Segment, String> services) throws Xdi2ClientException {

		// prepare message to Cloud

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToCloud(messageEnvelope, cloudNumber);

		Message message = messageCollection.createMessage();

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> (services.size() * 2);

		for (Entry<XDI3Segment, String> entry : services.entrySet()) {

			targetStatementsSet.add(XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(cloudNumber.getXri(), entry.getKey(), XDIClientConstants.XRI_S_AS_URI, XDIConstants.XRI_S_VALUE),
					entry.getValue()));

			targetStatementsSet.add(XDI3Statement.fromRelationComponents(
					PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()),
					XDILinkContractConstants.XRI_S_GET,
					XDI3Util.concatXris(XDI3Segment.fromComponent(PublicLinkContract.createPublicLinkContractXri(cloudNumber.getXri()).getFirstSubSegment()), cloudNumber.getXri(), entry.getKey(), XDIClientConstants.XRI_S_AS_URI)));
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		if (verifiedPhone != null) {

			targetStatementsSet.add(XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AS_VERIFIED_PHONE, XDIConstants.XRI_S_VALUE),
					verifiedPhone));
		}

		if (verifiedEmail != null) {

			targetStatementsSet.add(XDI3Statement.fromLiteralComponents(
					XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_AS_VERIFIED_EMAIL, XDIConstants.XRI_S_VALUE),
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
        
        XDI3Statement targetStatementGet = XDI3Statement.fromRelationComponents(
        		this.getCspInformation().getRnCloudNumber().getXri(),
                XRI_S_MEMBER,
                XDIConstants.XRI_S_VARIABLE);

        message.createGetOperation(targetStatementGet);

        // Send message and read result

        this.prepareMessageToRN(message);
        
        MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
        
        ReadOnlyIterator<Relation> relations =  (messageResult.getGraph()).getDeepRelations(this.getCspInformation().getRnCloudNumber().getXri(), XRI_S_MEMBER);
            
        while (relations.hasNext()) {
            relations.next();
            numberOfMembers++;
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

		return messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXri(), true);
	}

	private MessageCollection createMessageCollectionToCSP(MessageEnvelope messageEnvelope) {

		return messageEnvelope.getMessageCollection(this.getCspInformation().getCspCloudNumber().getXri(), true);
	}

	private MessageCollection createMessageCollectionToCloud(MessageEnvelope messageEnvelope, CloudNumber cloudNumber) {

		return messageEnvelope.getMessageCollection(cloudNumber.getXri(), true);
	}

	private void prepareMessageToRN(Message message) {

		message.setToPeerRootXri(this.getCspInformation().getRnCloudNumber().getPeerRootXri());
		message.setLinkContractXri(this.getCspInformation().getRnCspLinkContract());

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

		message.setToPeerRootXri(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(this.getCspInformation().getCspCloudNumber().getXri()));

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

		message.setToPeerRootXri(cloudNumber.getPeerRootXri());
		message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));

		if (secretToken != null) {

			message.setSecretToken(secretToken);
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
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

        //Create the Relational Entry in the Guardian  Graph
        targetStatements.add(XDI3Statement.fromRelationComponents(
                guardian.getXri(),
                XRI_S_IS_GUARDIAN,
                dependent.getXri()));
         
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
        List<XDI3Statement> targetStatements2 = new ArrayList<XDI3Statement> ();
        
     
        //Generating and Adding Dependent Statements
        List<XDI3Statement> dependentStatements =  createDependentXDI3Statements( guardian,  dependent, dependentBirthDate, guardianPrivateSigningKey);
        targetStatements2.addAll(dependentStatements);
        
        
        
        if (withConsent) {    
      
            //Generating and Adding Consent Statements
            List<XDI3Statement> consentStatements =  createConsentXDI3Statements(guardian,  dependent, guardianPrivateSigningKey);
            targetStatements2.addAll(consentStatements);
        } else {
            throw new Xdi2ClientException("Consent required for this operation", new ErrorMessageResult());
        }
                 
        message2.createSetOperation(targetStatements2.iterator());

        // send message
        String cloudXdiEndpoint2 = this.makeCloudXdiEndpoint(dependent);
        XDIClient xdiClientCloud2 = new XDIHttpClient(cloudXdiEndpoint2);
        log.debug("setGuardianshipInCloud :: Message2  " + messageEnvelope2.getGraph().toString());
        xdiClientCloud2.send(messageEnvelope2, null);

        // done
        log.debug("In Dependent User Cloud: Creating Guardian Relationship between " + dependent.toString() + " and " + guardian.toString() );
          
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

        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

        
        //Create the Relational Entry in the Guardian Sub Graph
        targetStatements.add(XDI3Statement.fromRelationComponents(
                guardian.getXri(),
                XRI_S_IS_GUARDIAN,
                dependent.getXri()));
        

        //Generating and Adding Dependent Statements
        List<XDI3Statement> dependentStatements =  createDependentXDI3Statements(guardian, dependent, dependentBirthDate, guardianPrivateSigningKey);
        targetStatements.addAll(dependentStatements);

                 
        if (withConsent) {     
            //Generating and Adding Consent Statements
            List<XDI3Statement> consentStatements =  createConsentXDI3Statements(guardian, dependent, guardianPrivateSigningKey);
            targetStatements.addAll(consentStatements);
        } else {
            throw new Xdi2ClientException("Consent required for this operation", new ErrorMessageResult());
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
        
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

        //Create the Relational Entry in the Guardian Sub Graph
        targetStatements.add(XDI3Statement.fromRelationComponents(
                guardian.getXri(),
                XRI_S_IS_GUARDIAN,
                dependent.getXri()));
        
        
        //Generating and Adding Dependent Statements
        List<XDI3Statement> dependentStatements =  createDependentXDI3Statements( guardian,  dependent, dependentBirthDate, guardianPrivateSigningKey);
        targetStatements.addAll(dependentStatements);
        
        if (withConsent) {     
            //Generating and Adding Consent Statements
            List<XDI3Statement> consentStatements =  createConsentXDI3Statements(guardian,  dependent, guardianPrivateSigningKey);
            targetStatements.addAll(consentStatements);
        } else {
            throw new Xdi2ClientException("Consent required for this operation", new ErrorMessageResult());
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

        XDI3Statement targetStatementGet = XDI3Statement.fromRelationComponents(
             guardian.getXri(),
                 XRI_S_IS_GUARDIAN,
                    XDIConstants.XRI_S_VARIABLE);
                    
       message.createGetOperation(targetStatementGet);

       // send message and read results

       this.prepareMessageToCSP(message);
        
       MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);

       ReadOnlyIterator<Relation> relations = messageResult.getGraph().getDeepRelations(
           guardian.getXri(), XRI_S_IS_GUARDIAN);

       CloudNumber[] theDependencies = new IteratorArrayMaker<CloudNumber> (
           new NotNullIterator<CloudNumber> (
               new MappingCloudNumberIterator(
                   new MappingRelationTargetContextNodeXriIterator(relations)
                       ))).array(CloudNumber.class);
       
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

        XDI3Statement targetStatementGet = XDI3Statement.fromRelationComponents(
            dependent.getXri(),
                XRI_S_GUARDIAN,
                    XDIConstants.XRI_S_VARIABLE);
                    
       message.createGetOperation(targetStatementGet);

       // send message and read results

       this.prepareMessageToCSP(message);
        
       MessageResult messageResult = this.getXdiClientCSPRegistry().send(messageEnvelope, null);

       Iterator<Relation> relations = messageResult.getGraph().getDeepRelations(
           dependent.getXri(), XRI_S_GUARDIAN);
       
       
       MappingRelationTargetContextNodeXriIterator contextNodeIterator = new MappingRelationTargetContextNodeXriIterator(relations);
        
       ArrayList<CloudNumber> theGuardianList = new ArrayList<CloudNumber>();
       while (contextNodeIterator.hasNext()) {
           XDI3Segment next = contextNodeIterator.next();
           // We can expect to  get inner roots as well that we want to discard from this list.
           // Only interested in Cloud Numbers.
           if (CloudNumber.isValid(next)) {
               theGuardianList.add(CloudNumber.create(next.toString()));
           } else {
               log.debug("Not a valid Clould Number ... : {}", next.toString());
           }        
       }
       
       CloudNumber[] theGuardians = theGuardianList.toArray( new CloudNumber[theGuardianList.size()]);
       
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
    private List<XDI3Statement> createConsentXDI3Statements(CloudNumber guardian, CloudNumber dependent, PrivateKey signingKey) {
        
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();
        String consentUUID = "<!:uuid:" + UUID.randomUUID() + ">";
        
                        
        MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();
        

        //[=]!:uuid:1111[<#consent>]<!:uuid:6545>/$is#/[+]:uuid:0000<#parental><#consent>
        XDI3Statement consentStatement = XDI3Statement.fromRelationComponents(
                XDI3Util.concatXris(guardian.getXri(), XDI3Segment.create("[<#consent>]"), XDI3Segment.create(consentUUID)),
                XDIDictionaryConstants.XRI_S_IS_TYPE,
                XDI3Util.concatXris(this.getCspInformation().getRnCloudNumber().getXri(), XDI3Segment.create("<#parental><#consent>")));
        
        g.setStatement(consentStatement);
        
        XDI3Segment consentSubjectTo = XDI3Util.concatXris(guardian.getXri(), XDI3Segment.create("[<#consent>]"), XDI3Segment.create(consentUUID));
     
        //[=]!:uuid:1111[<#consent>]<!:uuid:6545>/$to/[=]!:uuid:3333
        XDI3Statement consentStatementTo = XDI3Statement.fromRelationComponents(
                consentSubjectTo,
                XDI3Segment.create("$to"),
                dependent.getXri());
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
        
        //Converting from Statement to  XDI3Statement
        while(dependencyStatmentIterator.hasNext()){
            Statement next = dependencyStatmentIterator.next();
            XDI3Statement graphStatement = next.getXri();
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
     * @return XDI3Statements
     */
    private List<XDI3Statement> createDependentXDI3Statements(CloudNumber guardian, CloudNumber dependent, Date dependentBirthDate, PrivateKey signingKey) {
        
                
        XDI3SubSegment innerGraph = XDI3SubSegment.create("(" + dependent.getXri() +"/#guardian" + ")");
        
        MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();
        
                    
        //Create the Relational Entry in the Dependent Sub Graph
        //[=]!:uuid:3333/#guardian/[=]!:uuid:1111
        XDI3Statement guardianStatement = XDI3Statement.fromRelationComponents(
                dependent.getXri(),
                XRI_S_GUARDIAN,
                guardian.getXri());
        
        g.setStatement(guardianStatement);
        
        // ([=]!:uuid:3333/#guardian)[=]!:uuid:3333 /#guardian/([=]!:uuid:3333/#guardian)[=]!:uuid:1111
        

        XDI3Statement innerGuardianStatement = XDI3Statement.fromRelationComponents(
                XDI3Util.concatXris(innerGraph,dependent.getXri()),
                XRI_S_GUARDIAN,
                XDI3Util.concatXris(innerGraph, guardian.getXri()));
        
        g.setStatement(innerGuardianStatement);
        
        //Adding Date to Dependent's SubGraph
        //[=]!:uuid:3333<#birth><$t>&/&/"2000-04-10T22:22:22Z")
        
        String xdiDOBFormat = Timestamps.timestampToString(dependentBirthDate);
        XDI3Statement dobStatement = XDI3Statement.fromLiteralComponents(
                XDI3Util.concatXris(dependent.getXri(), XDI3Segment.create("<#birth><$t>&")), 
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

        XDI3Segment pk =  XDI3Segment.create("<$public><$key>");
        
        signatureContextNode
           .setDeepContextNode(pk)
           .setRelation(
                XDIDictionaryConstants.XRI_S_REF,
                XDI3Util.concatXris(guardian.getXri(), XDIAuthenticationConstants.XRI_S_MSG_SIG_KEYPAIR_PUBLIC_KEY));
               
               
        ContextNode c = g.getRootContextNode();
        
        Iterator<Statement> dependencyStatementIterator = c.getAllStatements();
        
        //Converting from Statement to XDI3Statement
        while(dependencyStatementIterator.hasNext()){
            Statement next = dependencyStatementIterator.next();
            XDI3Statement graphStatement = next.getXri();
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		
		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(cloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(cloudName.getPeerRootXri())));

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
		Relation relation = messageResult.getGraph().getDeepRelation(XDI3Segment.fromComponent(cloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		
		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered.");

		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
		if (! registeredCloudNumber.equals(cloudNumber)) throw new RuntimeException("Registered Cloud Number "
		    + registeredCloudNumber + " does not match requested Cloud Number " + cloudNumber);

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
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
		}
		
		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri())));

		message1.createSetOperation(targetStatementsSet.iterator());
				
			
		/*
		Set Billing Records
		   
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([+]!:uuid:5555/#billing#contact/[=]!:uuid:1111)
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555) 
		 */
		

		Message message2 = messageCollection.createMessage(-1);

		List<XDI3Statement> targetStatementBillingSet = new ArrayList<XDI3Statement> ();

		
		targetStatementBillingSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri()), 
				XRI_S_BILLING_CONTACT, 
				XDI3Segment.fromComponent(contactCloudNumber.getPeerRootXri())));

		targetStatementBillingSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(contactCloudNumber.getPeerRootXri()),
				XRI_S_IS_BILLING_CONTACT, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));

		message2.createSetOperation(targetStatementBillingSet.iterator());

				
		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);
		
		
		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		
		Relation relation = messageResult.getGraph().getDeepRelation(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		
		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered.");
		
		
		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
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
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
		}
		
		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri())));

		message1.createSetOperation(targetStatementsSet.iterator());
				
			
		/*
		Set Billing Records
		   
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([+]!:uuid:5555/#billing#contact/[=]!:uuid:1111)
        [+]!:uuid:9999[$msg]!:uuid:1234$do/$set/([=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555) 
		 */
		

		Message message2 = messageCollection.createMessage(-1);

		List<XDI3Statement> targetStatementBillingSet = new ArrayList<XDI3Statement> ();

		
		targetStatementBillingSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri()), 
				XRI_S_BILLING_CONTACT, 
				XDI3Segment.fromComponent(contactCloudNumber.getPeerRootXri())));

		targetStatementBillingSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(contactCloudNumber.getPeerRootXri()),
				XRI_S_IS_BILLING_CONTACT, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));

		message2.createSetOperation(targetStatementBillingSet.iterator());

				
		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);
		
		
		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		
		Relation relation = messageResult.getGraph().getDeepRelation(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		
		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered in CSP Graph.");
		
		
		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
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
			throw new Xdi2ClientException("Invalid CS in CloudName: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
		}
		
		char numberCS = businessCloudNumber.getCs();

		if ( numberCS != XDIConstants.CS_AUTHORITY_LEGAL ) {
			throw new Xdi2ClientException("Invalid CS in CloudNumber: Expecting " + XDIConstants.CS_AUTHORITY_LEGAL , new ErrorMessageResult());
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

		List<XDI3Statement> targetStatementsSet = new ArrayList<XDI3Statement> ();

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), 
				XDIDictionaryConstants.XRI_S_REF, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));

		targetStatementsSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri()),
				XDIDictionaryConstants.XRI_S_IS_REF, 
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri())));

		message1.createSetOperation(targetStatementsSet.iterator());
				
			
		/*
		    Set Billing Records
		    [=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555
	    */
		 
		
		Message message2 = messageCollection.createMessage(-1);

		List<XDI3Statement> targetStatementBillingSet = new ArrayList<XDI3Statement> ();

		/* [=]!:uuid:1111/$is#billing#contact/[+]!:uuid:5555 */
		targetStatementBillingSet.add(XDI3Statement.fromRelationComponents(
				XDI3Segment.fromComponent(contactCloudNumber.getPeerRootXri()),
				XRI_S_IS_BILLING_CONTACT, 
				XDI3Segment.fromComponent(businessCloudNumber.getPeerRootXri())));
		
		
		List<XDI3Statement> signedBillingStatments = createSignedBillingXDI3Statements(businessCloudNumber,
	    		 contactCloudNumber);
		targetStatementBillingSet.addAll(signedBillingStatments);
		
			
		message2.createSetOperation(targetStatementBillingSet.iterator());
			
		this.prepareMessageToRN(message1);
		this.prepareMessageToRN(message2);
		
		
		log.debug("registerBusinessCloudNameInRN :: Message " + messageEnvelope.getGraph().toString());
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

		log.debug("registerBusinessCloudNameInRN :: Message Response " + messageResult.getGraph().toString());
		
		Relation relation = messageResult.getGraph().getDeepRelation(
				XDI3Segment.fromComponent(businessCloudName.getPeerRootXri()), XDIDictionaryConstants.XRI_S_REF);
		
		if (relation == null) throw new RuntimeException("Additional Cloud Name not registered in CSP Graph.");
		
		
		CloudNumber registeredCloudNumber = CloudNumber.fromPeerRootXri(relation.getTargetContextNodeXri());
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
    private List<XDI3Statement> createSignedBillingXDI3Statements(CloudNumber businessCloudNumber,
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
    		throw new Xdi2ClientException(errorMsg, new ErrorMessageResult());
    	} catch (GeneralSecurityException e){
    		String errorMsg = "Problem Retrieving CSP Private Key" + e.getMessage();
    		log.warn(errorMsg);
    		throw new Xdi2ClientException(errorMsg, new ErrorMessageResult());
        }
  

        if (this.getCspInformation().getCspSignaturePrivateKey() != null) {
    	    log.debug("CSP Private Key is not null. Signing Messages with it.");
    	    signingKey = this.getCspInformation().getCspSignaturePrivateKey();
        } else {
    		String errorMsg = "Problem Retrieving CSP Private Key: Key is Null";
    		log.warn(errorMsg);
    		throw new Xdi2ClientException(errorMsg, new ErrorMessageResult());
        }
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();
        
                        
        MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();
        
        /* ([=]!:uuid:1111/$is#billing#contact) */
        XDI3Segment contactIsBC = XDI3Segment.create("(" + contactCloudNumber.getXri() +"/" + XRI_S_IS_BILLING_CONTACT +")");
        

        XDI3Statement contactBillingStatement = XDI3Statement.fromRelationComponents(
                XDI3Util.concatXris( contactIsBC, XDI3Segment.create(contactCloudNumber.toString())),
                XRI_S_IS_BILLING_CONTACT,
                XDI3Util.concatXris(contactIsBC, this.getCspInformation().getRnCloudNumber().getXri()));
        
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
        
        //Converting from Statement to  XDI3Statement
        while(billingContactStatmentIterator.hasNext()){
            Statement next = billingContactStatmentIterator.next();
            XDI3Statement graphStatement = next.getXri();
            targetStatements.add(graphStatement);
        }
        
        return targetStatements;
       
        
    }
	
	
}
