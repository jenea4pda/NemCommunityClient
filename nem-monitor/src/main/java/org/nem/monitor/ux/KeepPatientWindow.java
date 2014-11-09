package org.nem.monitor.ux;

import org.nem.monitor.config.LanguageSupport;
import org.nem.monitor.node.NemNodeType;
import org.nem.monitor.visitors.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

// TODO 20141108 J-T: i guess i would say comment this class
// TODO 20141108 J-T: since this is in the ux package i'm less concerned about tests (since we aren't set up for e2e tests)

/**
 * Window visualizes the progress of the starting the NEM components NCC and
 * NIS. When NCC is started and local NIS is required and also started the
 * Browser window displaying the NCC front-end. When NCC is started and a remote
 * NIS is configured, the browser is also started. Upon starting the browser,
 * the window is disposed.
 *
 */
public class KeepPatientWindow extends JFrame {
	public static final KeepPatientWindow window = new KeepPatientWindow();

	private JPanel contentPane;
	// TODO 20141108 J-T since these are really label,progress-bar pairs, i
	// would suggest having a private internal class and two instance (one for
	// each pair)
	// > i also don't like the progress bar names ;)
	// TDO 20141109 T-J: Names corrected, factoring-out not yet done.
	private JLabel lblNisServer;
	private JProgressBar nccProgressBar;
	private JProgressBar nisProgressBar;
	private JLabel lblNccServer;

	/**
	 * Launch the nem monitor. This is targeted for being started via command
	 * line.
	 */
	public static void main(String[] args) {
		openWindow();
	}

	/**
	 * Opens the nem monitor window.
	 */
	public static void openWindow() {
		EventQueue.invokeLater(() -> window.setVisible(true));
	}

	/**
	 * Create the frame.
	 */
	public KeepPatientWindow() {
		final Color nemGreen = new Color(0x41ce7c);
		final Color nemOrange = Color.ORANGE;

		setIconImage(Toolkit.getDefaultToolkit().getImage(
				KeepPatientWindow.class.getClassLoader().getResource(
						"icon_23.png")));
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
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
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

		nccProgressBar = new JProgressBar();
		nccProgressBar.setBackground(nemOrange);
		nccProgressBar.setForeground(nemGreen);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		contentPane.add(nccProgressBar, gbc_progressBar);

		lblNisServer = new JLabel("NIS server");
		lblNisServer.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblNisServer = new GridBagConstraints();
		gbc_lblNisServer.insets = new Insets(0, 0, 5, 0);
		gbc_lblNisServer.anchor = GridBagConstraints.WEST;
		gbc_lblNisServer.gridx = 0;
		gbc_lblNisServer.gridy = 3;
		contentPane.add(lblNisServer, gbc_lblNisServer);

		nisProgressBar = new JProgressBar();
		nisProgressBar.setBackground(nemOrange);
		nisProgressBar.setForeground(nemGreen);
		GridBagConstraints gbc_progressBar_1 = new GridBagConstraints();
		gbc_progressBar_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar_1.gridx = 0;
		gbc_progressBar_1.gridy = 4;
		contentPane.add(nisProgressBar, gbc_progressBar_1);
		setTitle(LanguageSupport.message("window.title"));

		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2
				- getSize().height / 2);

	}

	// TODO 20141108 J-T typo in "description"
	// TODO 20141108 J-T the lambdas don't need {}
	// TODO 20141109 T-J done.

	/**
	 * Creates a new visitor that updates the status message of NCC server
	 * 
	 * @return visitor, visitor that updates the NCC status.
	 */
	public NodeStatusToStatusDescriptionAdapter addNccDescriptionUpdater() {
		return new NodeStatusToStatusDescriptionAdapter(NemNodeType.NCC,
				description -> lblNccServer.setText(description
						.getStatusMessage()));
	}

	/**
	 * Creates a new visitor that updates the status message of NIS server
	 * 
	 * @return visitor, visitor that updates the NIS status.
	 */
	public NodeStatusToStatusDescriptionAdapter addNisDescriptionUpdater() {
		return new NodeStatusToStatusDescriptionAdapter(NemNodeType.NIS,
				description -> lblNisServer.setText(description
						.getStatusMessage()));
	}

	/**
	 * Creates a new visitor that updates the progress bar (0-100) of NCC server
	 * 
	 * @return visitor, visitor that updates the NCC progress bar.
	 */
	public NodeStatusToPercentageAdapter addNccProgressUpdater() {
		return new NodeStatusToPercentageAdapter(NemNodeType.NCC,
				percentage -> nccProgressBar.setValue(percentage));
	}

	/**
	 * Creates a new visitor that updates the progress bar (0-100) of NCC server
	 * 
	 * @return visitor, visitor that updates the NCC progress bar.
	 */
	public NodeStatusToPercentageAdapter addNisProgressUpdater() {
		return new NodeStatusToPercentageAdapter(NemNodeType.NIS,
				percentage -> nisProgressBar.setValue(percentage));
	}

	/**
	 * In case localNis is true, then the NIS progress bar is hidden and the 
	 * NIS text is updated.
	 * 
	 * @param localNis defines whether NIS is locally used by NCC
	 */
	public void updateLocalNisInformation(Boolean localNis) {
		if (!localNis) {
			lblNisServer.setText(LanguageSupport
					.message("window.ncc.uses.remote.nis"));
			nisProgressBar.setVisible(false);
		}
	}
}
