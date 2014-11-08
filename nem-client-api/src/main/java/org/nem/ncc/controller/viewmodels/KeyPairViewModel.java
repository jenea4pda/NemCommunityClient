package org.nem.ncc.controller.viewmodels;

import org.nem.core.crypto.*;
import org.nem.core.model.*;
import org.nem.core.serialization.*;
import org.nem.ncc.exceptions.NccException;

/**
 * Simple key pair view model.
 */
public class KeyPairViewModel implements SerializableEntity {
	private final KeyPair keyPair;
	private final String addressText;
	private final byte networkVersion;
	private final String vanityText;

	/**
	 * Creates a key pair view model.
	 *
	 * @param keyPair The key pair.
	 * @param networkVersion The network version.
	 */
	public KeyPairViewModel(final KeyPair keyPair, final byte networkVersion) {
		this.keyPair = keyPair;
		this.networkVersion = networkVersion;
		this.addressText = Address.fromPublicKey(this.networkVersion, this.keyPair.getPublicKey()).getEncoded();
		this.vanityText = "";
	}

	/**
	 * Creates a key pair view model.
	 *
	 * @param keyPair The key pair.
	 * @param networkVersion The network version.
	 */
	public KeyPairViewModel(final KeyPair keyPair, final String vanityText, final byte networkVersion) {
		this.keyPair = keyPair;
		this.networkVersion = networkVersion;
		this.addressText = Address.fromPublicKey(this.networkVersion, this.keyPair.getPublicKey()).getEncoded();
		this.vanityText = vanityText;
	}

	/**
	 * Deserializes a key pair view model.
	 *
	 * @param deserializer The deserializer.
	 */
	public KeyPairViewModel(final Deserializer deserializer) {
		final PrivateKey privateKey = PrivateKey.fromHexString(deserializer.readString("privateKey"));
		final PublicKey publicKey = PublicKey.fromHexString(deserializer.readOptionalString("publicKey"));
		final Address address = Address.fromEncoded(deserializer.readString("address"));
		this.addressText = address.getEncoded();
		this.vanityText = deserializer.readString("vanityText");

		this.networkVersion = NetworkInfo.fromAddress(address).getVersion();
		if (!addressIsDerivedFromPublicKey(publicKey, this.networkVersion, address)) {
			throw new NccException(NccException.Code.PUBLIC_KEY_ADDRESS_MISMATCH);
		}

		this.keyPair = new KeyPair(privateKey);
		if (!this.keyPair.getPublicKey().equals(publicKey)) {
			throw new NccException(NccException.Code.PRIVATE_KEY_PUBLIC_KEY_MISMATCH);
		}
	}

	/**
	 * Gets the key pair.
	 *
	 * @return The key pair.
	 */
	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	/**
	 * Gets the network version.
	 *
	 * @return The network version.
	 */
	public byte getNetworkVersion() {
		return this.networkVersion;
	}

	public String getAddressText() {
		return addressText;
	}

	public String getVanityText() {
		return vanityText;
	}

	private static boolean addressIsDerivedFromPublicKey(final PublicKey publicKey, final byte networkVersion, final Address address) {
		final Address derivedAddress = Address.fromPublicKey(networkVersion, publicKey);
		return derivedAddress.equals(address);
	}

	@Override
	public void serialize(final Serializer serializer) {
		serializer.writeString("privateKey", this.keyPair.getPrivateKey().toString());
		serializer.writeString("publicKey", this.keyPair.getPublicKey().toString());
		serializer.writeString("address", Address.fromPublicKey(this.networkVersion, this.keyPair.getPublicKey()).getEncoded());
		serializer.writeString("vanityText", this.vanityText);
	}
}
