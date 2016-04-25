package encryptorPackage;

//Import packages
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.util.List;

import org.apache.commons.io.FileUtils;

import ArgumentObfuscator.ArgumentObfuscator;
import classPackageObfuscate.ClassRenamer;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

//Main class
public class ToolGUI {
	// Declare variables
	static JFrame frame1;
	static Container pane;
	static JButton runButton, inputButton, outputButton;
	static JLabel inputLabel, outputLabel, userMessage;
	static JTextField inputLocationAddress, outputLocationAddress;
	static Insets insets;
	static JPanel inputPanel, outputPanel;
	static JFileChooser inputFileChooser, outputFileChooser;
	static FileSystemView fsv;

	public static void main(String args[]) {
		// Set Look and Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}

		final JFrame fileChooserFrame = new JFrame("Centered");

		// Create the frame
		frame1 = new JFrame("SE 702 Obfuscation Tool");
		frame1.setSize(1000, 200);
		frame1.setResizable(false);
		pane = frame1.getContentPane();
		insets = pane.getInsets();
		pane.setLayout(new GridLayout(4, 1));

		// Create controls
		fsv = FileSystemView.getFileSystemView();

		inputLabel = new JLabel("Non-obfuscated Project Location:");
		inputLocationAddress = new JTextField(20);

		inputButton = new JButton("Choose Directory");

		inputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File input = createFileChooser(fileChooserFrame);
				inputLocationAddress.setText(input.getAbsolutePath());
			}
		});

		outputLabel = new JLabel("Obfuscated Output Project Location:");
		outputLocationAddress = new JTextField(20);

		outputButton = new JButton("Choose Directory");

		outputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File output = createFileChooser(fileChooserFrame);
				outputLocationAddress.setText(output.getAbsolutePath());
			}
		});

		runButton = new JButton("OBFUSCATE CODE");

		userMessage = new JLabel("Please enter the location paths...");

		// Add all components to panel
		inputPanel = new JPanel();
		inputPanel.add(inputLabel);
		inputPanel.add(inputLocationAddress);
		inputPanel.add(inputButton);
		pane.add(inputPanel);

		outputPanel = new JPanel();
		outputPanel.add(outputLabel);
		outputPanel.add(outputLocationAddress);
		outputPanel.add(outputButton);
		pane.add(outputPanel);

		userMessage.setHorizontalAlignment(JLabel.CENTER);
		pane.add(userMessage);

		pane.add(runButton);

		// Chester DEBUG TODO REMOVE
//		inputLocationAddress.setText("D:\\AndroidProjects\\geofencetasking\\GeofenceTasker");
//		outputLocationAddress.setText("D:\\Users\\Chester\\Desktop\\OneTime");
		
		// Set frame visible
		frame1.setVisible(true);

		// Button's action
		runButton.addActionListener(new btnConnectAction()); // Register action
	}

	private static File createFileChooser(final JFrame frame) {

		JFileChooser fileChooser = new JFileChooser();

		// pop up an "Open File" file chooser dialog
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);

		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	public static class btnConnectAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			ObfuscatorWorker obfWorker = null;

			runButton.setEnabled(false);
			userMessage.setText("Copying files to new location...");
			File srcDir = new File(inputLocationAddress.getText());
			File destDir = new File(outputLocationAddress.getText());

			if (!destDir.exists()) {
				destDir.mkdir();
			}

			obfWorker = new ObfuscatorWorker(srcDir, destDir, userMessage, runButton);
			obfWorker.execute();

		}
	}

}


/**
 * Class to start obfuscation work in the background.
 */
class ObfuscatorWorker extends SwingWorker<Void, String> {

	private File srcDir;
	private File destDir;
	private JLabel userMessage;
	private boolean errorFlag = false;
	private JButton runButton;

	public ObfuscatorWorker(File s, File d, JLabel uM, JButton rb) {
		userMessage = uM;
		srcDir = s;
		destDir = d;
		runButton = rb;
	}

	@Override
	protected Void doInBackground() {

		try {
			FileUtils.copyDirectory(srcDir, destDir);

			publish("Obfuscating...");

			ClassRenamer.renameClassesInXML(destDir.getAbsolutePath());
			
			RoundAboutEncryption.Obfuscate(destDir.getAbsolutePath() + "\\app\\src\\main");
			ArgumentObfuscator.ObfuscateArguments(destDir.getAbsolutePath() + "\\app\\src\\main\\java");

		} catch (Exception e) {
			errorFlag = true;
		}

		return null;
	}

	@Override
	protected void process(List<String> labelStrings) {
		for (String s : labelStrings) {
			userMessage.setText(s);
		}
	}

	@Override
	protected void done() {
		if (errorFlag == false) {
			userMessage.setText("Done! Now build release version in Android Studios!");
		} else {
			runButton.setEnabled(true);
			userMessage.setText("ERROR!");
			errorFlag = false;
		}
	}
}