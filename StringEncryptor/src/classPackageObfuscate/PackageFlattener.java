package classPackageObfuscate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PackageFlattener {

	public static HashMap<String, Integer> packageList = new HashMap<String, Integer>();
	public static String packageRoot = "";
	private static String line = "";
	private static FileReader fr;
	private static BufferedReader br;
	private static FileWriter fw;
	private static BufferedWriter bw;

	public static void MoveJavaFile(String path, File f) {
		try {

			if (f.renameTo(new File(path + "\\xyz\\" + f.getName()))) {
				System.out.println("File is moved successful!");
			} else {
				System.out.println("File is failed to move! " + path + "\\xyz\\" + f.getName());
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void ManifestFixer(String path) {
		try {
			System.out.println("BEFORE");
			File manifestFile = new File(path + "\\AndroidManifest.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			System.out.println("AFTER");
			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(manifestFile);
			doc.getDocumentElement().normalize();

			packageRoot = doc.getDocumentElement().getAttribute("package");
			doc.getDocumentElement().setAttribute("package", "xyz");
			NodeList applicationTag = doc.getDocumentElement().getElementsByTagName("application");

			NodeList applicationChildren = applicationTag.item(0).getChildNodes();

			for (int x = 0; x < applicationChildren.getLength(); x++) {
				if (applicationChildren.item(x).getNodeName().equals("activity")
						|| applicationChildren.item(x).getNodeName().equals("service")
						|| applicationChildren.item(x).getNodeName().equals("provider")
						|| applicationChildren.item(x).getNodeName().equals("receiver")) {
					String oldAndroidName = applicationChildren.item(x).getAttributes().getNamedItem("android:name")
							.toString();
					String oldSection = oldAndroidName.substring(0, oldAndroidName.lastIndexOf("."));
					String newAndroidName = oldAndroidName.replace(oldSection, "");
					newAndroidName = newAndroidName.substring(0, newAndroidName.length()-1);
					System.out.println(newAndroidName);
					applicationChildren.item(x).getAttributes().getNamedItem("android:name")
							.setNodeValue(newAndroidName);
				}

			}

			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			// send DOM to file
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(path + "\\AndroidManifest.xml")));

		} catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void PackageFixer(String path) {

		File root = new File(path);
		File[] list = root.listFiles();
		
		System.out.println(packageList.keySet());
		for(String pkg : packageList.keySet()){
			System.out.println(pkg);
		}
		
		for (File f : list) {

			try {
				fr = new FileReader(f);
				br = new BufferedReader(fr);

				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {

					
					for(String pkg : packageList.keySet()){
						if(line.contains(pkg)) {
							line = line.replace(pkg, "xyz");
						}
					}
					if(line.contains(packageRoot)) {
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
}
