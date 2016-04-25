package classPackageObfuscate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	
	private String rootDir;
	
	/**
	 * Renames Android component classes and their references to a random String.
	 * @param rootDir Path of the root directory of the project.
	 * @throws TransformerException 
	 */
	public void renameClassesInXML(String rootDir) {
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
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Recursively looks through all the files in a given path.
	 * @param srcPath Root of the path to start looking for files from.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	private void walkThroughFiles(String srcPath) throws IOException, TransformerException, ParserConfigurationException, SAXException {
		File dir = new File(srcPath);
		File [] files = dir.listFiles();
		for (File f : files) {
			// Keep looking if is directory.
			if (f.isDirectory()) {
				walkThroughFiles(f.getAbsolutePath());
			} else { // Else, it's a java file.
				readFileAndReplace(f);
			}
		}
		
	}
	
	/**
	 * Replaces class names which are components with obfuscated names.
	 * @param file
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	public void readFileAndReplace(File file) {
		String absolutePath = file.getAbsolutePath();
		
		// Check if the file is a component.
		boolean isComponent = isComponent(absolutePath);
		
		// For imports relevant to this file.
		Map<String, String> imports = new HashMap<String, String>();
		
		// Full class declaration of this file.
		String fullDeclaration = getDeclaredNameFromAbsolutePath(absolutePath);
		String className = getClassNameFromPackage(fullDeclaration);
		String obfuscatedClassName = null;
		
		// Add itself to imports if its a component.
		if (isComponent) {
			imports.put(className, 
					getClassNameFromPackage(componentNames.get(fullDeclaration)));
			obfuscatedClassName = getClassNameFromPackage(componentNames.get(fullDeclaration));
		}
		
		try {
			// Read file as a string.
			String fileString = FileUtils.readFileToString(file);
			
			// Get imports part of file.
			String importsInFile = fileString.substring(0, fileString.indexOf("{"));
			for (String line : importsInFile.split("\n")) {
				// Get imports that are components.
				if (line.startsWith("import")) {
					for (String component : componentNames.keySet()) {
						if (line.contains(component)) {
							imports.put(getClassNameFromPackage(component), 
									getClassNameFromPackage(componentNames.get(component)));
						}
					}
				}
			}
			System.out.println("---------");
			System.out.println(absolutePath);
			printMap(imports);
			System.out.println("---------");
			
			System.out.println("REPLACING FULLY DECLARED");
			replaceFullyDeclaredNames(file);
			
			if (isComponent) {
				System.out.println("REPLACING CLASS NAME");
				replaceClassName(className, obfuscatedClassName, new File("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\abc.java"));
			}
			
			if (!imports.isEmpty()) {
				replaceImports(imports, new File("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\abc.java"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replaces class names of the declared imports.
	 * @param imports
	 * @param file
	 * @throws IOException
	 */
	private void replaceImports(Map<String, String> imports, File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			for (String imp : imports.keySet()) {
				if (line.contains(imp)) {
					line = line.replace(imp, imports.get(imp));
				}
			}
			sb.append(line + "\n");
		}

		FileWriter fw = new FileWriter(new File("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\abc.java"), false); // true to append
		try {
			fw.write(sb.toString());
			fw.close();
		} finally {
			fw.close();
		}
	}
	
	/**
	 * Replaces the class's name to another string.
	 * @param from The original class's name as a String.
	 * @param to The name to rename the class to.
	 * @param file The file to replace all references of this class for.
	 * @throws IOException
	 */
	private void replaceClassName(String from, String to, File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.contains(from)) {
				line = line.replace(from, to);
			}
			sb.append(line + "\n");
		}

		FileWriter fw = new FileWriter(new File("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\abc.java"), false); // true to append
		try {
			fw.write(sb.toString());
			fw.close();
		} finally {
			fw.close();
		}	
	}

	/**
	 * Replaces any references which have been fully declared (full package name included) to its obfuscated name.
	 * These obfuscated names are stored in componentNames.
	 * @param file To replace references in.
	 * @throws IOException
	 */
	private void replaceFullyDeclaredNames(File file) throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			for (String componentName : componentNames.keySet()) {
				if (line.contains(componentName)) {
					line = line.replace(componentName, componentNames.get(componentName));
				}
			}
			
			// Can remove \n to make it less readable
			sb.append(line + "\n");
		}

		FileWriter fw = new FileWriter(new File("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\abc.java"), false); // true to append
		try {
			fw.write(sb.toString());
			fw.close();
		} finally {
			fw.close();
		}

	}

	/**
	 * Checks if a file is an Android component.
	 * @param absolutePath of the file.
	 * @return true if the component is an Android component.
	 */
	private boolean isComponent(String absolutePath) {
		return componentNames.containsKey(getDeclaredNameFromAbsolutePath(absolutePath));
	}
	
	/**
	 * Gets the class name from a package declaration separated by dots.
	 * @param fullDeclaration of the class
	 * @return String of the class name
	 */
	private String getClassNameFromPackage(String fullDeclaration) {
		return fullDeclaration.substring(fullDeclaration.lastIndexOf(".") + 1);
	}
	
	/**
	 * Finds the declared name in dot format from an absolute path.
	 * @param absolutePath of the Java file.
	 * @return String of the declared name in separated by dots.
	 */
	private String getDeclaredNameFromAbsolutePath(String absolutePath) {
		int indexOfPkgFolder = absolutePath.indexOf("\\main\\java");
		if (indexOfPkgFolder == -1) {
			indexOfPkgFolder = absolutePath.indexOf("/main/java");
		}
		indexOfPkgFolder += 11; // To get the root package folder within the java folder.
		String declaredName = absolutePath.substring(indexOfPkgFolder);
		
		// Replace both in case of OS differences.
		declaredName = declaredName.replace("/", "."); 
		declaredName = declaredName.replace("\\", ".");
		declaredName = declaredName.replace(".java", "");
		
		return declaredName;
	}

	/**
	 * Puts names of Android components as keys from an AndroidManifest.xml file 
	 * and generates a random value for the class name.
	 * @param xmlDoc
	 * @return Map of the Android components
	 * @throws TransformerException 
	 * @throws FileNotFoundException 
	 */
	private Map<String, String> getComponentNames(Document xmlDoc) throws FileNotFoundException, TransformerException {
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
				// Get the name attribute.
				NamedNodeMap attributes = nl.item(i).getAttributes();
				Node nameAttribute = attributes.getNamedItem("android:name");
				
				// Get its value, extract its package, and make a random name for it.
				String nameValue = nameAttribute.getNodeValue();
				String pkg = nameValue.substring(0, nameValue.lastIndexOf("."));
				String randomName = generateRandomName();
				
				// Save the names to replace in componentNames and replace with obfuscated name.
				if (nameValue.startsWith(".")) {
					componentNames.put(packageName + nameValue, packageName + pkg + "." + randomName);
				} else {
					componentNames.put(nameValue, pkg + "." + randomName);
				}
				nameAttribute.setTextContent(pkg + "." + randomName);
			}
		}
		
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		
		// send DOM to file
		tr.transform(new DOMSource(xmlDoc), new StreamResult(new FileOutputStream("C:\\Users\\karen\\Desktop\\Obfuscated stuff\\AndroidManifest.xml")));
		
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

	/**
	 * Generates a random string, to be used as a class name.
	 * @return
	 */
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
	
	/**
	 * Convenience method for printing the contents of a map.
	 * @param map
	 */
	private void printMap(Map<String, String> map) {
		for (String key : map.keySet()) {
			System.out.println(key + " " + map.get(key));
		}
	}
}
