package encryptorPackage;

//Import packages
import javax.swing.*;
import java.util.List;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

//Main class
public class ToolGUI {
	// Declare variables
	static JFrame frame1;
	static Container pane;
	static JButton runButton;
	static JLabel inputLabel, outputLabel, userMessage;
	static JTextField inputLocationAddress, outputLocationAddress;
	static Insets insets;
	static JPanel inputPanel, outputPanel;

	public static void main(String args[]) {
		// Set Look and Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}

		// Create the frame
		frame1 = new JFrame("SE 702 Obfuscation Tool");
		frame1.setSize(500, 200);
		frame1.setResizable(false);
		pane = frame1.getContentPane();
		insets = pane.getInsets();
		pane.setLayout(new GridLayout(4, 1));

		// Create controls
		inputLabel = new JLabel("Input location path of Non-obfuscated Project:");
		inputLocationAddress = new JTextField(30);
		outputLabel = new JLabel("Output location path of Obfuscated Project:");
		outputLocationAddress = new JTextField(30);

		runButton = new JButton("OBFUSCATE CODE");

		userMessage = new JLabel("Please enter the location paths...");

		// Add all components to panel
		inputPanel = new JPanel();
		inputPanel.add(inputLabel);
		inputPanel.add(inputLocationAddress);
		pane.add(inputPanel);

		outputPanel = new JPanel();
		outputPanel.add(outputLabel);
		outputPanel.add(outputLocationAddress);
		pane.add(outputPanel);

		userMessage.setHorizontalAlignment(JLabel.CENTER);
		pane.add(userMessage);
		
		pane.add(runButton);

		

		// Set frame visible
		frame1.setVisible(true);

		// Button's action
		runButton.addActionListener(new btnConnectAction()); // Register action
	}

	public static class btnConnectAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			userMessage.setText("Copying files to new location...");
			File srcDir = new File(inputLocationAddress.getText());
			File destDir = new File(outputLocationAddress.getText());

			if (!destDir.exists()) {
				destDir.mkdir();
			}

			ObfuscatorWorker obfWorker = new ObfuscatorWorker(srcDir, destDir, userMessage);
			obfWorker.execute();
		}
	}

}

class ObfuscatorWorker extends SwingWorker<Void, String> {

	private File srcDir;
	private File destDir;
	private JLabel userMessage;

	public ObfuscatorWorker(File s, File d, JLabel uM) {
		userMessage = uM;
		srcDir = s;
		destDir = d;
	}

	@Override
	protected Void doInBackground() {
		try {

			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		publish("Obfuscating...");
		RoundAboutEncryption.Obfuscate(destDir.getAbsolutePath() + "\\app\\src\\main\\java");
		return null;
	}
	
	@Override
	protected void process(List<String> labelStrings) {
	    for (String s : labelStrings){
	    	userMessage.setText(s);
	    }
	}


	@Override
	protected void done() {
		userMessage.setText("Done! Now build release version in Android Studios!");
	}
}