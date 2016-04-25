package classPackageObfuscate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PackageFlattener {

	// Package tracking
	public static HashMap<String, Integer> packageList = new HashMap<String, Integer>();
	public static String packageRoot = "";

	// Reader/Writer components
	private static String line = "";
	private static FileReader fr;
	private static BufferedReader br;
	private static FileWriter fw;
	private static BufferedWriter bw;

	/** Method which moves the .Java files to the xyz package */
	public static void MoveJavaFile(String path, File f) {
		try {
			if (f.renameTo(new File(path + "\\xyz\\" + f.getName()))) {
				// System.out.println("File is moved successful!");
			} else {
				// System.out.println("File is failed to move! " + path +
				// "\\xyz\\" + f.getName());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/** Method which fixes up the Manifest file after moving the .java files */
	public static void ManifestFixer(String path) {
		try {
			// Read the Manifest.xml file as an XML
			File manifestFile = new File(path + "\\AndroidManifest.xml");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(manifestFile);
			doc.getDocumentElement().normalize();

			// Record the original package name
			packageRoot = doc.getDocumentElement().getAttribute("package");

			// Rename the package name
			doc.getDocumentElement().setAttribute("package", "xyz");

			// Get the "application" tags
			NodeList applicationTag = doc.getDocumentElement().getElementsByTagName("application");
			NodeList applicationChildren = applicationTag.item(0).getChildNodes();

			// If the tags are activity, service, provider or receiver then fix
			// the package reference
			for (int x = 0; x < applicationChildren.getLength(); x++) {
				if (applicationChildren.item(x).getNodeName().equals("activity")
						|| applicationChildren.item(x).getNodeName().equals("service")
						|| applicationChildren.item(x).getNodeName().equals("provider")
						|| applicationChildren.item(x).getNodeName().equals("receiver")) {

					// Remove old package reference from "android:name"
					String oldAndroidName = applicationChildren.item(x).getAttributes().getNamedItem("android:name")
							.toString();
					String oldSection = oldAndroidName.substring(0, oldAndroidName.lastIndexOf("."));
					String newAndroidName = oldAndroidName.replace(oldSection, "");
					newAndroidName = newAndroidName.substring(0, newAndroidName.length() - 1);

					// Debug
					// System.out.println(newAndroidName);

					// Set the new "android:name" with the updated value
					applicationChildren.item(x).getAttributes().getNamedItem("android:name")
							.setNodeValue(newAndroidName);
				}
			}

			// Build an xml writer
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// Write the updated xml file to the new location
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(path + "\\AndroidManifest.xml")));

		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** This fixes the package references in all the java files */
	public static void PackageFixer(String path) {

		File root = new File(path);
		File[] list = root.listFiles();

		// Debug package list
		// System.out.println(packageList.keySet());
		// for (String pkg : packageList.keySet()) {
		// System.out.println(pkg);
		// }

		// Iterate all the java files
		for (File f : list) {

			try {
				fr = new FileReader(f);
				br = new BufferedReader(fr);

				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {

					// If line contains a package reference, rename it
					for (String pkg : packageList.keySet()) {
						if (line.contains(pkg)) {
							line = line.replace(pkg, "xyz");
						}
					}

					// If line contains a package root reference, rename it
					if (line.contains(packageRoot)) {
						line = line.replace(packageRoot, "xyz");
					}
					sb.append(line + "\n");
				}

				try {
					fw = new FileWriter(f, false);
					bw = new BufferedWriter(fw);

					bw.write(sb.toString());
					br.close();

				} catch (FileNotFoundException e) {
					System.out.println("File was not found!");
				} catch (IOException e) {
					System.out.println("No file found!");
				}
				bw.close();
			} catch (FileNotFoundException e) {
				System.out.println("Error1!");
			} catch (IOException e) {
				System.out.println("Error2!");
			}
		}
	}

	/** Deletes the remnants of the old folders */
	public static void CleanUpOldPackages(String path) {
		File root = new File(path);
		File[] list = root.listFiles();
		for (File f : list) {
			if (f.isDirectory() && !f.getName().equals("xyz")) {
				f.delete();
			}
		}
	}
}
