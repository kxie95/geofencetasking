package classPackageObfuscate;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Class which renames classes which extend of Android components.
 * These include Activity, Service, IntentService, BroadcastReceiver
 * and ContentProvider.
 */
public class ClassRenamer {
	
	// Set with list of possible Android components
	private static Set<String> androidComponents = new HashSet<String>(); 
	
	// Used to generate a random string.
	private static SecureRandom random = new SecureRandom();

	
	public static void renameClasses(String copiedFileLocation) {
		addComponents();
		
		File rootDirectory = new File(copiedFileLocation);
		
		// Recursively look through files
		File file = new File(""); // TODO: Get each file.
		
		// If the java file is an Android component
		if (isAndroidComponent(file)) {
			// Get the name of the class.
			String name = file.getName().replace(".java", "");
			
			// Generate a random obfuscated name.
			String randomName = generateRandomName();
			
			// Replace references of the old name with the new one.
			renameClassAndReferences(name, randomName);
		}
	}
	
	private static void renameClassAndReferences(String name, String randomName) {
		// TODO Auto-generated method stub
		
	}

	private static String generateRandomName() {
		String randomString = new BigInteger(130, random).toString(32);
		// To ensure it begins with a letter
		return "Z" + randomString;
	}

	/**
	 * Method which checks a java file to see if it is an Android component.
	 * @return true if the file is an Android component.
	 */
	private static boolean isAndroidComponent(File file) {
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
	private static void addComponents() {
		androidComponents.add("Activity");
		androidComponents.add("Service");
		androidComponents.add("IntentService");
		androidComponents.add("ContentProvider");
		androidComponents.add("BroadcastReceiver");
	}
}
