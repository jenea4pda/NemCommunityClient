package org.nem.ncc.ux;

import org.nem.ncc.controller.viewmodels.KeyPairViewModel;

import java.awt.Component;
import javax.swing.*;

public class VanityAddressRenderer extends JLabel implements ListCellRenderer<KeyPairViewModel> {

	@Override
	public Component getListCellRendererComponent(
			JList<? extends KeyPairViewModel> list,
			KeyPairViewModel value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
		String vanityText = value.getVanityText();
		String address = value.getAddressText();
		int ind = address.indexOf(vanityText);
		String colorText = isSelected ? "e0e0e0" : "ffffff";
		setText(String
				.format(
						"<html><span style='color : #41ce7c; font-family : Monospaced; font-size : 18; background-color: #%s'>%s<span style='color : #e1a92b'>%s</span>%s</span></html>",
						colorText, address.substring(0, ind), vanityText, address.substring(ind + vanityText.length())));

		return this;
	}
}