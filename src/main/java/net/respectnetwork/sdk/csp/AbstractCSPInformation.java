package net.respectnetwork.sdk.csp;

import java.security.GeneralSecurityException;

import xdi2.core.features.linkcontracts.RootLinkContract;
import xdi2.core.features.signatures.KeyPairSignature;
import xdi2.core.xri3.CloudNumber;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;

public abstract class AbstractCSPInformation implements CSPInformation {

	@Override
	public MessageCollection createMessageCollectionToRN(MessageEnvelope messageEnvelope) {

		return messageEnvelope.getMessageCollection(this.getCspCloudNumber().getXri(), true);
	}

	@Override
	public MessageCollection createMessageCollectionToCSP(MessageEnvelope messageEnvelope) {

		return messageEnvelope.getMessageCollection(this.getCspCloudNumber().getXri(), true);
	}

	@Override
	public MessageCollection createMessageCollectionToCloud(MessageEnvelope messageEnvelope, CloudNumber cloudNumber) {

		return messageEnvelope.getMessageCollection(cloudNumber.getXri(), true);
	}

	@Override
	public void prepareMessageToRN(Message message) {

		message.setToPeerRootXri(this.getRnCloudNumber().getPeerRootXri());
		message.setLinkContractXri(this.getRnCspLinkContract());

		if (this.getRnCspSecretToken() != null) {

			message.setSecretToken(this.getRnCspSecretToken());
		}

		if (this.getCspPrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

			try {

				signature.sign(this.getCspPrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void prepareMessageToCSP(Message message) {

		message.setToPeerRootXri(this.getCspCloudNumber().getPeerRootXri());
		message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(this.getCspCloudNumber().getXri()));

		if (this.getCspSecretToken() != null) {

			message.setSecretToken(this.getCspSecretToken());
		}

		if (this.getCspPrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

			try {

				signature.sign(this.getCspPrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void prepareMessageToCloud(Message message, CloudNumber cloudNumber, String secretToken) {

		message.setToPeerRootXri(cloudNumber.getPeerRootXri());
		message.setLinkContractXri(RootLinkContract.createRootLinkContractXri(cloudNumber.getXri()));

		if (secretToken != null) {

			message.setSecretToken(secretToken);
		}

		if (this.getCspPrivateKey() != null) {

			KeyPairSignature signature = (KeyPairSignature) message.setSignature(KeyPairSignature.DIGEST_ALGORITHM_SHA, 256, KeyPairSignature.KEY_ALGORITHM_RSA, 2048);

			try {

				signature.sign(this.getCspPrivateKey());
			} catch (GeneralSecurityException ex) {

				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}
}
