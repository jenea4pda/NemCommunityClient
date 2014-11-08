package org.nem.ncc.ux;

import org.nem.core.crypto.KeyPair;
import org.nem.core.i18n.LanguageSupport;
import org.nem.core.model.*;
import org.nem.ncc.controller.viewmodels.KeyPairViewModel;
import org.nem.ncc.model.*;

import org.apache.commons.codec.binary.Base32;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;

public class NccVanityGeneratorGUI {
	private static final Logger LOGGER = Logger.getLogger(NccVanityGeneratorGUI.class.getName());

	private JFrame frmNemClient;
	private JTextField vanityText;
	private JCheckBox chckbxTestNetAddress;
	private final GenerateAction generate = new GenerateAction();
	private final Action stopGenerator = new StopGeneratorAction();
	private JList<KeyPairViewModel> addresses;
	private FutureTask<Void> asyncGenerator;

	public static void openWindow() {
		openWindow(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Launch the window.
	 */
	public static void openWindow(final int closeOperation) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NccVanityGeneratorGUI window = new NccVanityGeneratorGUI(closeOperation);
					window.frmNemClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static String normalizeVanityText(final String vanityText) {
		StringBuilder strB = new StringBuilder();
		char ch;
		Base32 encoder = new Base32(true);
		for (int i = 0; i < vanityText.length(); i++) {
			ch = Character.toUpperCase(vanityText.charAt(i));
			if (encoder.isInAlphabet((byte) ch)) {
				strB.append(ch);
			}
		}
		return strB.toString();
	}

	/**
	 * Create the application.
	 */
	public NccVanityGeneratorGUI(final int closeOperation) {
		initialize(closeOperation);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(final int closeOperation) {
		frmNemClient = new JFrame();
		frmNemClient.setDefaultCloseOperation(closeOperation);
		frmNemClient.setMinimumSize(new Dimension(0, 200));
		frmNemClient.setTitle(LanguageSupport.message("nem.vanity.generator.title"));
		frmNemClient.setBounds(100, 100, 571, 400);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 50, 0 };
		gridBagLayout.rowHeights = new int[] { 40, 86, 30, 0, 0, 38, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, Double.MIN_VALUE };
		frmNemClient.getContentPane().setLayout(gridBagLayout);

		JLabel lblNemVanityGenerator = new JLabel(LanguageSupport.message("nem.vanity.generator"));
		lblNemVanityGenerator.setFont(new Font("Arial", Font.BOLD, 16));
		GridBagConstraints gbc_lblNemVanityGenerator = new GridBagConstraints();
		gbc_lblNemVanityGenerator.gridwidth = 4;
		gbc_lblNemVanityGenerator.insets = new Insets(0, 0, 5, 0);
		gbc_lblNemVanityGenerator.gridx = 0;
		gbc_lblNemVanityGenerator.gridy = 0;
		frmNemClient.getContentPane().add(lblNemVanityGenerator, gbc_lblNemVanityGenerator);

		JTextPane txtpnTheVanityGenerator = new JTextPane();
		txtpnTheVanityGenerator.setContentType("text/plain; charset=UTF-8");
		txtpnTheVanityGenerator.setBackground(UIManager.getColor("Label.background"));
		txtpnTheVanityGenerator.setEditable(false);
		txtpnTheVanityGenerator.setFont(new Font("Arial", Font.PLAIN, 12));
		txtpnTheVanityGenerator.setText(LanguageSupport.message("nem.vanity.generator.explain"));
		GridBagConstraints gbc_txtpnTheVanityGenerator = new GridBagConstraints();
		gbc_txtpnTheVanityGenerator.gridwidth = 4;
		gbc_txtpnTheVanityGenerator.insets = new Insets(0, 0, 5, 0);
		gbc_txtpnTheVanityGenerator.fill = GridBagConstraints.BOTH;
		gbc_txtpnTheVanityGenerator.gridx = 0;
		gbc_txtpnTheVanityGenerator.gridy = 1;
		frmNemClient.getContentPane().add(txtpnTheVanityGenerator, gbc_txtpnTheVanityGenerator);

		vanityText = new JTextField();
		vanityText.setForeground(Color.GRAY);
		vanityText.setToolTipText(LanguageSupport.message("nem.vanity.generator.entry.tip"));
		vanityText.setText(LanguageSupport.message("nem.vanity.generator.entry.hint"));
		vanityText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String newText = vanityText.getText();
				if (newText == null) {
					generate.setEnabled(false);
					vanityText.setForeground(Color.GRAY);
					vanityText.setText(LanguageSupport.message("nem.vanity.generator.entry.hint"));

					return;
				}

				if (newText.trim().isEmpty()) {
					generate.setEnabled(false);
					vanityText.setForeground(Color.GRAY);
					vanityText.setText(LanguageSupport.message("nem.vanity.generator.entry.hint"));
				} else {
					generate.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (!generate.isEnabled()) {
					vanityText.setForeground(Color.BLACK);
					vanityText.setText("");
				}
			}
		});
		vanityText.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_vanityText = new GridBagConstraints();
		gbc_vanityText.fill = GridBagConstraints.HORIZONTAL;
		gbc_vanityText.insets = new Insets(0, 0, 5, 5);
		gbc_vanityText.gridx = 1;
		gbc_vanityText.gridy = 2;
		frmNemClient.getContentPane().add(vanityText, gbc_vanityText);
		vanityText.setColumns(10);

		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.setSize(new Dimension(150, 0));
		btnNewButton_1.setAction(generate);
		btnNewButton_1.setFont(new Font("Arial", Font.PLAIN, 12));

		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 2;
		frmNemClient.getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);

		addresses = new JList<KeyPairViewModel>();
		addresses.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				VanityAddressModel visitor = (VanityAddressModel) addresses.getModel();
				int index = e.getFirstIndex();
				StringSelection selection = new StringSelection(visitor.getClipboardFormatForElementAt(index));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
			}
		});
		addresses.setModel(new VanityAddressModel());
		addresses.setCellRenderer(new VanityAddressRenderer());

		chckbxTestNetAddress = new JCheckBox("Test net address");
		chckbxTestNetAddress.setSelected(true);
		chckbxTestNetAddress.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxTestNetAddress.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_chckbxTestNetAddress = new GridBagConstraints();
		gbc_chckbxTestNetAddress.anchor = GridBagConstraints.LINE_START;
		gbc_chckbxTestNetAddress.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxTestNetAddress.gridx = 1;
		gbc_chckbxTestNetAddress.gridy = 3;
		frmNemClient.getContentPane().add(chckbxTestNetAddress, gbc_chckbxTestNetAddress);

		addresses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addresses.setFont(new Font("Monospaced", Font.PLAIN, 12));

		JScrollPane scrollPane = new JScrollPane(addresses);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 4;
		frmNemClient.getContentPane().add(scrollPane, gbc_scrollPane);

		JButton btnEnough = new JButton("");
		btnEnough.setSize(new Dimension(150, 0));
		btnEnough.setEnabled(false);
		btnEnough.setAction(stopGenerator);
		btnEnough.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_btnEnough = new GridBagConstraints();
		gbc_btnEnough.insets = new Insets(0, 0, 5, 5);
		gbc_btnEnough.gridx = 2;
		gbc_btnEnough.gridy = 4;
		frmNemClient.getContentPane().add(btnEnough, gbc_btnEnough);

		generate.setEnabled(false);
		stopGenerator.setEnabled(false);
		vanityText.requestFocusInWindow();
	}

	private class GenerateAction extends AbstractAction {
		private VanityAddressGenerator vanity;
		private boolean enabled;

		public GenerateAction() {
			putValue(NAME, LanguageSupport.message("nem.vanity.generator.button.generate"));
			putValue(SHORT_DESCRIPTION, LanguageSupport.message("nem.vanity.generator.button.generate.tip"));
			setEnabled(false);
		}

		@Override
		public void setEnabled(boolean newValue) {
			enabled = newValue;
			super.setEnabled(newValue);
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public void actionPerformed(ActionEvent e) {
			if (asyncGenerator != null) {
				asyncGenerator.cancel(true);
				try {
					asyncGenerator.get();
				} catch (InterruptedException | ExecutionException | CancellationException e1) {
					// Nothing to do.
				}
			}
			byte version = chckbxTestNetAddress.isSelected() ? NetworkInfo.getTestNetworkInfo().getVersion() : NetworkInfo.getMainNetworkInfo().getVersion();
			String normalizedText = normalizeVanityText(vanityText.getText());
			vanityText.setText(normalizedText);

			vanity = new VanityAddressGenerator(KeyPair::new, 
					kp -> Address.fromPublicKey(version, kp.getPublicKey()));

			asyncGenerator =
			vanity.generateAsync(normalizedText, 1000000, token -> {
				((VanityAddressModel) addresses.getModel()).addressFound(token, normalizedText, version);
			}).getFuture();

			this.setEnabled(false);
			stopGenerator.setEnabled(true);
		}

		public void stopGenerating() {
			if (asyncGenerator != null) {
				asyncGenerator.cancel(true);
				try {
					asyncGenerator.get();
				} catch (InterruptedException | ExecutionException | CancellationException e1) {
					// Nothing to do
				}
			}
			asyncGenerator = null;

			this.setEnabled(true);
			stopGenerator.setEnabled(false);
		}
	}

	private class StopGeneratorAction extends AbstractAction {
		public StopGeneratorAction() {
			putValue(NAME, LanguageSupport.message("nem.vanity.generator.button.enough"));
			putValue(SHORT_DESCRIPTION, LanguageSupport.message("nem.vanity.generator.button.enough.tip"));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			generate.stopGenerating();
		}
	}
}