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

import net.respectnetwork.sdk.csp.discount.CloudNameDiscountCode;
import net.respectnetwork.sdk.csp.discount.RespectNetworkMembershipDiscountCode;
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

	public static final XDI3Segment XRI_S_AC_VERIFIED_DIGEST_PHONE = XDI3Segment.create("<+verified><$digest>[<+phone>]");
	public static final XDI3Segment XRI_S_AC_VERIFIED_DIGEST_EMAIL = XDI3Segment.create("<+verified><$digest>[<+email>]");
	public static final XDI3Segment XRI_S_AS_VERIFIED_PHONE = XDI3Segment.create("<+verified><+phone>");
	public static final XDI3Segment XRI_S_AS_VERIFIED_EMAIL = XDI3Segment.create("<+verified><+email>");
	public static final XDI3Segment XRI_S_IS_PHONE = XDI3Segment.create("$is+phone");
	public static final XDI3Segment XRI_S_IS_EMAIL = XDI3Segment.create("$is+email");
	
    public static final XDI3Segment XRI_S_IS_GUARDIAN = XDI3Segment.create("$is+guardian");
    public static final XDI3Segment XRI_S_GUARDIAN = XDI3Segment.create("+guardian");

	public static final XDI3Segment XRI_S_MEMBER = XDI3Segment.create("+member");
	public static final XDI3Segment XRI_S_AS_MEMBER_EXPIRATION_TIME = XDI3Segment.create("<+member><+expiration><$t>");

	public static final XDI3Segment XRI_S_FIRST_MEMBER = XDI3Segment.create("+first+member");

	public static final XDI3Segment XRI_S_PARAMETER_CLOUDNAME_DISCOUNTCODE = XDI3Segment.create("<+([@]!:uuid:e9b5165b-fa7b-4387-a685-7125d138a872)><+(RNDiscountCode)>");
	public static final XDI3Segment XRI_S_PARAMETER_RESPECT_NETWORK_MEMBERSHIP_DISCOUNTCODE = XDI3Segment.create("<+([@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa)><+(RNDiscountCode)>");
	
	public static final CloudNumber AT_RESPECT_CLOUD_NUMBER = CloudNumber.create("[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa");


	private CSPInformation cspInformation;

	private XDIClient xdiClientCSPRegistry;
	private XDIClient xdiClientRNRegistrationService;

	public BasicCSP() {

	}

	public BasicCSP(CSPInformation cspInformation) {

		this.cspInformation = cspInformation;

		this.xdiClientCSPRegistry = new XDIHttpClient(cspInformation.getCspRegistryXdiEndpoint());
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
		this.getXdiClientCSPRegistry().send(messageEnvelope, null);

		// done

		log.debug("In CSP: Cloud registered with Cloud Number " + cloudNumber + " and Secret Token and Cloud XDI endpoint " + cloudXdiEndpoint);
	}

	@Override
	public CloudNumber checkCloudNameAvailableInRN(CloudName cloudName) throws Xdi2ClientException {

		CloudNumber cloudNumber = null;

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		XDI3Segment targetAddress = XDI3Segment.fromComponent(cloudName.getPeerRootXri());

		message.createGetOperation(targetAddress);

		// send message and read result

		this.prepareMessageToRN(message);
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
	public void registerCloudNameInRN(CloudName cloudName, CloudNumber cloudNumber, String verifiedPhone, String verifiedEmail, CloudNameDiscountCode cloudNameDiscountCode) throws Xdi2ClientException {

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

		if (cloudNameDiscountCode != null) {

			operation1.setParameter(XRI_S_PARAMETER_CLOUDNAME_DISCOUNTCODE, cloudNameDiscountCode.toString());
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
		MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);

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
	public void setRespectNetworkMembershipInRN(CloudNumber cloudNumber, Date expirationTime, RespectNetworkMembershipDiscountCode respectNetworkMembershipDiscountCode)  throws Xdi2ClientException {

		// prepare message to RN

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = this.createMessageCollectionToRN(messageEnvelope);

		Message message = messageCollection.createMessage();

		List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

		targetStatements.add(XDI3Statement.fromRelationComponents(
				this.getCspInformation().getRnCloudNumber().getXri(),
				XRI_S_MEMBER,
				cloudNumber.getXri()));

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

		Operation operation = message.createSetOperation(targetStatements.iterator());

		if (respectNetworkMembershipDiscountCode != null) {

			operation.setParameter(XRI_S_PARAMETER_RESPECT_NETWORK_MEMBERSHIP_DISCOUNTCODE, respectNetworkMembershipDiscountCode.toString());
		}

		// send message

		this.prepareMessageToRN(message);
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

		XDIClient xdiClientCloud = new XDIHttpClient(cloudXdiEndpoint);

		this.prepareMessageToCloud(message, cloudNumber, secretToken);
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
					XDI3Util.concatXris(cloudNumber.getXri(), entry.getKey(), XDIClientConstants.XRI_S_AS_URI)));
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
                AT_RESPECT_CLOUD_NUMBER.getXri(),
                XRI_S_FIRST_MEMBER,
                XDIConstants.XRI_S_VARIABLE);

        message.createGetOperation(targetStatementGet);

        // Send message and read result

        this.prepareMessageToRN(message);
        
        MessageResult messageResult = this.getXdiClientRNRegistrationService().send(messageEnvelope, null);
        
        ReadOnlyIterator<Relation> relations =  (messageResult.getGraph()).getDeepRelations(AT_RESPECT_CLOUD_NUMBER.getXri(), XRI_S_FIRST_MEMBER);
            
        while (relations.hasNext()) {
            relations.next();
            numberOfMembers++;
        }

        // done
        log.debug("In RN: getRespectFirstMemberCount = " + numberOfMembers);
        return numberOfMembers;
    }
	

    public long getRespectFirstMemberCountJSON(String secretToken)
        throws CoreRNServiceException {

        long numberOfMembers = 0;

        String jsonMessage = "{\"([@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa[$msg]!:uuid:1234$do/$get)[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/+first+member\":[\"([@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa[$msg]!:uuid:1234$do/$get){}\"],\"[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa[$msg]!:uuid:1234/$do\":[\"[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa$to+registrar$from$do\"],\"[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa[$msg]!:uuid:1234/$is()\":[\"([@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa)\"],\"[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa[$msg]!:uuid:1234<$secret><$token>&/&\":\"" + secretToken  + "\"}";

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    cspInformation.getRnRegistrationServiceXdiEndpoint());

            StringEntity input = new StringEntity(jsonMessage);
            input.setContentType("application/xdi+json");
            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new CoreRNServiceException("Failed getRespectFirstMemberCount JSON Request : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                log.debug("Output for  Registration Service: {}", output);

                JsonElement jelement = new JsonParser().parse(output);
                JsonObject jobject = jelement.getAsJsonObject();
                String lookupKey = "[@]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa/+first+member";
                JsonArray jarray = jobject.getAsJsonArray(lookupKey);
                
                
                if (jarray.equals(null)) {
                    numberOfMembers = 0;
                } else {
                    numberOfMembers = jarray.size();
                }
                
            }
            httpClient.getConnectionManager().shutdown();

        } catch (MalformedURLException e) {
            String errorMessage = String.format("Exception Processing JSON Message to Reg. Service: %s", e.getMessage());
            log.warn(errorMessage);
            throw new CoreRNServiceException(errorMessage);
        } catch (IOException e) {
            String errorMessage = String.format("Exception Processing JSON Message to Reg. Service: %s", e.getMessage());
            log.warn(errorMessage);
            throw new CoreRNServiceException(errorMessage);            
        } catch (IllegalStateException e) {
            String errorMessage = String.format("Exception Processing JSON Message to Reg. Service: %s", e.getMessage());
            log.warn(errorMessage);
            throw new CoreRNServiceException(errorMessage);            
        }

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

		if (this.getCspInformation().getRnCspSecretToken() != null) {

			message.setSecretToken(this.getCspInformation().getRnCspSecretToken());
		}

		if (this.getCspInformation().getCspSignaturePrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

			try {

				signature.sign(this.getCspInformation().getCspSignaturePrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	private void prepareMessageToCSP(Message message) {

		message.setToPeerRootXri(this.getCspInformation().getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(this.getCspInformation().getCspCloudNumber().getXri()));

		if (this.getCspInformation().getCspSecretToken() != null) {

			message.setSecretToken(this.getCspInformation().getCspSecretToken());
		}

		if (this.getCspInformation().getCspSignaturePrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

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

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

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
        CloudNumber dependent, Date dependentBirthDate, boolean withConsent, String secretToken, PrivateKey guardianPrivateSigningKey)
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
        xdiClientCloud.send(messageEnvelope, null);

        // done
        log.debug("In Guardian User Cloud: Creating is Guardian Relationship between " + guardian.toString() + " and " + dependent.toString() );
         
        
        // Prepare message to Dependent's Cloud

        MessageEnvelope messageEnvelope2 = new MessageEnvelope();
        MessageCollection messageCollection2 = this.createMessageCollectionToCloud(messageEnvelope2, dependent);

        Message message2 = messageCollection2.createMessage();
        
        //Update Dependent's Graph

        this.prepareMessageToCloud(message2, dependent, secretToken);        
        List<XDI3Statement> targetStatements2 = new ArrayList<XDI3Statement> ();
        
     
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
                 
        message2.createSetOperation(targetStatements2.iterator());

        // send message
        String cloudXdiEndpoint2 = this.makeCloudXdiEndpoint(dependent);
        XDIClient xdiClientCloud2 = new XDIHttpClient(cloudXdiEndpoint2);
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
    
    
    private List<XDI3Statement> createConsentXDI3Statements(CloudNumber guardian, CloudNumber dependent, PrivateKey signingKey) {
        
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();
        String consentUUID = "<!:uuid:" + UUID.randomUUID() + ">";
        
                        
        MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();
        

        //[=]!:uuid:1111[<+consent>]<!:uuid:6545>/$is+/[@]:uuid:0000<+parental><+consent>
        XDI3Statement consentStatement = XDI3Statement.fromRelationComponents(
                XDI3Util.concatXris(guardian.getXri(), XDI3Segment.create("[<+consent>]"), XDI3Segment.create(consentUUID)),
                XDI3Segment.create("$is+"),
                XDI3Util.concatXris(AT_RESPECT_CLOUD_NUMBER.getXri(), XDI3Segment.create("<+parental><+consent>")));
        
        g.setStatement(consentStatement);
        
        XDI3Segment consentSubjectTo = XDI3Util.concatXris(guardian.getXri(), XDI3Segment.create("[<+consent>]"), XDI3Segment.create(consentUUID));
     
        //[=]!:uuid:1111[<+consent>]<!:uuid:6545>/$to/[=]!:uuid:3333
        XDI3Statement consentStatementTo = XDI3Statement.fromRelationComponents(
                consentSubjectTo,
                XDI3Segment.create("$to"),
                dependent.getXri());
        g.setStatement(consentStatementTo);
        
          
        //Sign the Context: //[=]!:uuid:1111[<+consent>]<!:uuid:6545>        
        ContextNode signingNode = g.getRootContextNode().getDeepContextNode(consentSubjectTo);

        // now create the signature and add it to the graph
        KeyPairSignature s = (KeyPairSignature) Signatures.setSignature(signingNode, "sha", 256, "rsa", 2048);

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
    
    
    private List<XDI3Statement> createDependentXDI3Statements(CloudNumber guardian, CloudNumber dependent, Date dependentBirthDate, PrivateKey signingKey) {
        
                
        XDI3SubSegment innerGraph = XDI3SubSegment.create("(" + dependent.getXri() +"/+guardian" + ")");
        
        MemoryGraph g = MemoryGraphFactory.getInstance().openGraph();
        
        List<XDI3Statement> targetStatements = new ArrayList<XDI3Statement> ();

                    
        //Create the Relational Entry in the Dependent Sub Graph
        //[=]!:uuid:3333/+guardian/[=]!:uuid:1111
        XDI3Statement guardianStatement = XDI3Statement.fromRelationComponents(
                dependent.getXri(),
                XRI_S_GUARDIAN,
                guardian.getXri());
        
        g.setStatement(guardianStatement);
        
        //([=]!:uuid:3333/+guardian)[=]!:uuid:3333/+guardian/[=]!:uuid:1111
        XDI3Statement innerGuardianStatement = XDI3Statement.fromRelationComponents(
                XDI3Util.concatXris(innerGraph, dependent.getXri()),
                XRI_S_GUARDIAN,
                guardian.getXri());
        
        g.setStatement(innerGuardianStatement);
        
        //Adding Date to  Dependent's SubGraph
        XDI3Statement dobStatement = XDI3Statement.fromLiteralComponents(
                XDI3Util.concatXris(dependent.getXri(), XDI3Segment.create("<+birth><$date>&")), 
                dependentBirthDate.toString());
        
        g.setStatement(dobStatement);
        

        
        //Sign the Context: ([=]!:uuid:3333/+guardian)<$sig>&/&/”...”        
        ContextNode signingNode = g.getRootContextNode().getContextNode(innerGraph);

        // now create the signature and add it to the graph

        KeyPairSignature s = (KeyPairSignature) Signatures.setSignature(signingNode, "sha", 256, "rsa", 2048);

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
}
