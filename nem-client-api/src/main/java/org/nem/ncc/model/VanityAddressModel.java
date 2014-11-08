package org.nem.ncc.model;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.Address;
import org.nem.ncc.controller.viewmodels.KeyPairViewModel;
import org.nem.ncc.model.VanityAddressGenerator.GenerateToken;

import java.util.*;

import javax.swing.AbstractListModel;

/**
 * A swing model that holds the list of generated vanity addresses
 */
public class VanityAddressModel extends AbstractListModel<KeyPairViewModel> {
	final private List<KeyPairViewModel> vanityAddresses;

	/**
	 * Creates a new Model
	 *
	 */
	public VanityAddressModel() {
		this.vanityAddresses = new ArrayList<KeyPairViewModel>();
	}

	static private String getClipboardFormat(final KeyPairViewModel vanityAddress) {
		return String.format("%s / %s", vanityAddress.getAddressText(), vanityAddress.getKeyPair().getPrivateKey().toString());
	}

	/**
	 * A new vanity address was found. Save the address and notify listeners
	 *
	 * @param address
	 * @param privateKey
	 */
	public void addressFound(final GenerateToken token, final String vanityText, final byte version) {
		KeyPair keyPair = token.getBestKeyPair();
		vanityAddresses.add(new KeyPairViewModel(keyPair, vanityText, version));
		int index = vanityAddresses.size() - 1;
		fireIntervalAdded(this, index, index);
	}

	/**
	 * Get a string that of the element at the provided index which is used for the clipboard.
	 *
	 * @param index
	 * @return string containing the address and the private key.
	 */
	public String getClipboardFormatForElementAt(int index) {
		KeyPairViewModel vanityAddress = vanityAddresses.get(index);
		return getClipboardFormat(vanityAddress);
	}

	@Override
	public int getSize() {
		return vanityAddresses.size();
	}

	@Override
	public KeyPairViewModel getElementAt(int index) {
		return vanityAddresses.get(index);
	}
}