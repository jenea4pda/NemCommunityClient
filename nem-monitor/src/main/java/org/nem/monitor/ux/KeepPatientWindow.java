package org.nem.monitor.ux;

import org.nem.monitor.config.LanguageSupport;
import org.nem.monitor.node.NemNodeType;
import org.nem.monitor.visitors.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

// TODO 20141108 J-T: i guess i would say comment this class
// TODO 20141108 J-T: since this is in the ux package i'm less concerned about tests (since we aren't set up for e2e tests)

public class KeepPatientWindow extends JFrame {
	public static final KeepPatientWindow window = new KeepPatientWindow();
	
	private JPanel contentPane;
	// TODO 20141108 J-T since these are really label,progress-bar pairs, i would suggest having a private internal class and two instance (one for each pair)
	// > i also don't like the progress bar names ;)
	private JLabel lblNisServer;
	private JProgressBar progressBar;
	private JProgressBar progressBar_1;
	private JLabel lblNccServer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		openWindow();
	}

	public static void openWindow() {
		EventQueue.invokeLater(() -> window.setVisible(true));
	}

	/**
	 * Create the frame.
	 */
	public KeepPatientWindow() {
		final Color nemGreen = new Color(0x41ce7c);
		final Color nemOrange = Color.ORANGE;

		setIconImage(Toolkit.getDefaultToolkit().getImage(KeepPatientWindow.class.getClassLoader().getResource("icon_23.png")));
		setBackground(nemGreen);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 379, 187);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(nemGreen);
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 60, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JTextPane lblNewLabel = new JTextPane();
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setSelectionColor(nemOrange);
		lblNewLabel.setMinimumSize(new Dimension(6, 20));
		lblNewLabel.setEditable(false);
		lblNewLabel.setBackground(nemGreen);
		lblNewLabel.setText(LanguageSupport.message("window.explanation"));
		lblNewLabel.setRequestFocusEnabled(false);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);

		lblNccServer = new JLabel("NCC server");
		lblNccServer.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNccServer.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblNccServer = new GridBagConstraints();
		gbc_lblNccServer.insets = new Insets(0, 0, 5, 0);
		gbc_lblNccServer.anchor = GridBagConstraints.WEST;
		gbc_lblNccServer.gridx = 0;
		gbc_lblNccServer.gridy = 1;
		contentPane.add(lblNccServer, gbc_lblNccServer);

		progressBar = new JProgressBar();
		progressBar.setBackground(nemOrange);
		progressBar.setForeground(nemGreen);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		contentPane.add(progressBar, gbc_progressBar);

		lblNisServer = new JLabel("NIS server");
		lblNisServer.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblNisServer = new GridBagConstraints();
		gbc_lblNisServer.insets = new Insets(0, 0, 5, 0);
		gbc_lblNisServer.anchor = GridBagConstraints.WEST;
		gbc_lblNisServer.gridx = 0;
		gbc_lblNisServer.gridy = 3;
		contentPane.add(lblNisServer, gbc_lblNisServer);

		progressBar_1 = new JProgressBar();
		progressBar_1.setBackground(nemOrange);
		progressBar_1.setForeground(nemGreen);
		GridBagConstraints gbc_progressBar_1 = new GridBagConstraints();
		gbc_progressBar_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar_1.gridx = 0;
		gbc_progressBar_1.gridy = 4;
		contentPane.add(progressBar_1, gbc_progressBar_1);
		setTitle(LanguageSupport.message("window.title"));
		
		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

	}

	// TODO 20141108 J-T typo in "description"
	// TODO 20141108 J-T the lambdas don't need {}

	public NodeStatusToStatusDescriptionAdapter addNccDesriptionUpdater() {
		return new NodeStatusToStatusDescriptionAdapter(NemNodeType.NCC, description -> {
			lblNccServer.setText(description.getStatusMessage());
		});
	}
	public NodeStatusToStatusDescriptionAdapter addNisDesriptionUpdater() {
		return new NodeStatusToStatusDescriptionAdapter(NemNodeType.NIS, description -> {
			lblNisServer.setText(description.getStatusMessage());
		});
	}
	
	public NodeStatusToPercentageAdapter addNccProgressUpdater() {
		return new NodeStatusToPercentageAdapter(NemNodeType.NCC, percentage -> {
			progressBar.setValue(percentage);
		});
	}
	public NodeStatusToPercentageAdapter addNisProgressUpdater() {
		return new NodeStatusToPercentageAdapter(NemNodeType.NIS, percentage -> {
			progressBar_1.setValue(percentage);
		});
	}
	
	public void updateLocalNisInformation(Boolean localNis) {
		if(!localNis) {
			lblNisServer.setText(LanguageSupport.message("window.ncc.uses.remote.nis"));
			progressBar_1.setVisible(false);
		}
	}
}
