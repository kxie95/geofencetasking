package classPackageObfuscate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class Start {
	public static void main(String[] args) {
		ClassRenamer cr = new ClassRenamer();
		cr.renameClasses("C:\\Users\\karen\\Desktop\\Android Studio Projects\\groupfour\\TaskerProguardTest");
	}
}
/**
 * Class which renames classes which extend of Android components.
 * These include Activity, Service, IntentService, BroadcastReceiver
 * and ContentProvider.
 */
public class ClassRenamer {
	// Relative path to AndroidManifest file.
	private static final String PATH_TO_MANIFEST = "\\app\\src\\main\\AndroidManifest.xml";
	
	// Relative path to java src code.
	private static final String PATH_TO_SRC = "\\app\\src\\main\\java";
	
	// Set with list of possible Android components
	private Set<String> androidComponents = new HashSet<String>(); 
	
	// Used to generate a random string.
	private SecureRandom random = new SecureRandom();
	
	// Name of the top level package.
	String packageName = "";
	// Names of components found in the manifest.
	Map<String, String> componentNames;
	
	/**
	 * Renames Android component classes and their references to a random String.
	 * @param rootDir Path of the root directory of the project.
	 */
	public void renameClasses(String rootDir) {
		// Add possible Android components to a hashset to be used to find elements in the XML.
		addComponents();
		
		File file = new File(rootDir);
		try {
			// Parse AndroidManifest.xml
			Document xmlDoc = parseXMLfile(file.getAbsolutePath() + PATH_TO_MANIFEST);
			
			// Get the top level package name.
			packageName = getPackageName(xmlDoc);
			
			// Get the Android components to be renamed.
			componentNames = getComponentNames(xmlDoc);
			for (String s : componentNames.keySet()) {
				System.out.println(s + " " + componentNames.get(s));
			}
			
			// Walk through files and rename.
			walkThroughFiles(rootDir + PATH_TO_SRC);
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	/**
	 * Recursively looks through all the files in a given path.
	 * @param srcPath Root of the path to start looking for files from.
	 * @throws IOException 
	 */
	private void walkThroughFiles(String srcPath) throws IOException {
		File dir = new File(srcPath);
		File [] files = dir.listFiles();
		for (File f : files) {
			// Keep looking if is directory.
			if (f.isDirectory()) {
				walkThroughFiles(f.getAbsolutePath());
			} else { // Else, it's a java file.
				System.out.println(f.getName());
				replaceReferencesInFile(f);
			}
		}
		
	}

	/**
	 * Replaces references of components in the componentName hash map with its randomly generated key.
	 * Does not handle duplicate class names.
	 * @param file
	 * @throws IOException 
	 */
	private void replaceReferencesInFile(File file) throws IOException {
	    // First check import statements to see which classes it contains.
		// Add these to a hashset? 
	}	

	private Map<String, String> getComponentNames(Document xmlDoc) {
		Map<String, String> componentNames = new HashMap<String, String>();
		
		// Get list of Android components
		for (String componentName : androidComponents) {
			// Get the component. Continue if it doesn't exist.
			NodeList nl = xmlDoc.getElementsByTagName(componentName);
			if (nl == null) {
				continue;
			}
			
			// Get the declared name of the component.
			for (int i = 0; i < nl.getLength(); i++) {
				NamedNodeMap attributes = nl.item(i).getAttributes();
				String nameValue = attributes.getNamedItem("android:name").getNodeValue();
				if (nameValue.startsWith(".")) {
					componentNames.put(packageName + nameValue, generateRandomName());
				} else {
					componentNames.put(nameValue, generateRandomName());
				}
			}
		}
		
		return componentNames;
	}
	
	/**
	 * Gets the package name declared in the Android Manifest xml.
	 * @param xmlDoc The parsed XML file.
	 * @return the package name
	 */
	private String getPackageName(Document xmlDoc) {
		return xmlDoc.getDocumentElement().getAttribute("package");
	}
	
	/**
	 * Parses an XML file given a file path.
	 * @param filePath Absolute path of the file.
	 * @return Document An XML representation of the document.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document parseXMLfile(String filePath) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(filePath);
		doc.getDocumentElement().normalize();
		return doc;
	}

	private String generateRandomName() {
		String randomString = new BigInteger(32, random).toString(32);
		// To ensure it begins with a letter
		Random r = new Random();
		char randomChar = (char) (r.nextInt(26) + 'a');

		return randomChar + randomString;
	}

	/**
	 * Method which checks a java file to see if it is an Android component.
	 * @return true if the file is an Android component.
	 */
	private boolean isAndroidComponent(File file) {
		// Use regex to find extending class as a string.
		String extendingClass = "";
		if (androidComponents.contains(extendingClass)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Adds Android components to a hash set as Strings.
	 */
	private void addComponents() {
		androidComponents.add("activity");
		androidComponents.add("service");
		androidComponents.add("provider");
		androidComponents.add("receiver");
	}
}
