package encryptorPackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import classPackageObfuscate.PackageFlattener;

public class RoundAboutEncryption {

	private static Map<String, String> fragmentDictionary = new HashMap<String, String>();

	public static void Obfuscate(String copiedFileLocation) throws Exception {

		// Root of copied File should be specified
		File programRootDirectory = new File(copiedFileLocation);

		// Creates a package to store the generated java files
		File generatedDirectory = new File(programRootDirectory.toString() + "\\java\\xyz");
		generatedDirectory.mkdir();

		// Create the globalList java file
		File fileWithListOfVariables = new File(programRootDirectory.toString() + "\\java\\xyz\\GlobalList.java");
		if (!fileWithListOfVariables.exists()) {
			fileWithListOfVariables.createNewFile();
		}

		// Create the decoder java file
		File fileWithStringDecoder = new File(programRootDirectory.toString() + "\\java\\xyz\\StringDecoder.java");
		if (!fileWithStringDecoder.exists()) {
			fileWithStringDecoder.createNewFile();
		}

		// CREATE THE GLOBAL-LIST CLASS

		FileWriter fw = new FileWriter(fileWithListOfVariables.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("package xyz;");
		bw.write("public class GlobalList { \n");

		// Randomly generate variables and strings of text, used for the key
		char[] charsToChooseFrom = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb;
		Random random;

		// Configurable options:
		int lengthOfVariableValue = 64;
		int numberOfVariables = 25;
		int numberOfLoops = (int) Math.sqrt(numberOfVariables);

		// Randomly chooses the variables which will be included in the key
		int fragmentNumber1 = (int) (Math.random() * numberOfVariables) + 1;
		int fragmentNumber2 = (int) (Math.random() * numberOfVariables) + 1;
		int fragmentNumber3 = (int) (Math.random() * numberOfVariables) + 1;
		int fragmentNumber4 = (int) (Math.random() * numberOfVariables) + 1;

		// System.out.println("NUMBER FRAGMENTS: " + fragmentNumber1 + ", " +
		// fragmentNumber2 + ", " + fragmentNumber3
		// + ", " + fragmentNumber4);

		// Loops through the alphabet
		for (int i = 0; i < numberOfLoops; i++) {

			// Loops through each character
			for (int j = 1; j < numberOfLoops + 1; j++) {

				// Assign character(s) to be the variable name
				String variableName = "";
				for (int k = 1; k <= j + 1; k++) {
					variableName = variableName + Character.toString(Character.toChars(i + 65)[0]);
				}

				// Build a random string of text for a variable
				sb = new StringBuilder();
				random = new Random();
				for (int q = 0; q < lengthOfVariableValue; q++) {
					char c = charsToChooseFrom[random.nextInt(charsToChooseFrom.length)];
					sb.append(c);
				}

				// If the loop corresponds to the fragment number, store it
				// in the dictionary
				if (i * numberOfLoops + j == fragmentNumber1 || i * numberOfLoops + j == fragmentNumber2
						|| i * numberOfLoops + j == fragmentNumber3 || i * numberOfLoops + j == fragmentNumber4) {
					fragmentDictionary.put(variableName, sb.toString());
				}

				// Write the generated variable name and value to the
				// GlobalList class
				bw.write("public static String " + variableName + " = \"" + sb.toString() + "\"; \n");
			}
		}
		bw.write("}");
		bw.close();

		// Retrieve set of dictionary keys
		String encryptionKeyString = "";
		Set<String> dictionaryKeys = fragmentDictionary.keySet();

		System.out.println("DICTIONARY STRINGS: " + dictionaryKeys.toString());

		// Iterate through keys generating the encryptedKeyString
		for (Iterator<String> x = dictionaryKeys.iterator(); x.hasNext();) {
			String dictKey = (String) x.next();
			String dictValue = (String) fragmentDictionary.get(dictKey);
			encryptionKeyString = encryptionKeyString + dictValue;
		}

		// System.out.println("ENCRYPTION KEY STRING: " + encryptionKeyString);

		// CREATE THE STRING-DECODER CLASS

		// Generate four variables for the global list
		fw = new FileWriter(fileWithStringDecoder.getAbsoluteFile());
		bw = new BufferedWriter(fw);

		// package
		bw.write("package xyz;");

		// imports
		bw.write("import java.security.Key; \n");
		bw.write("import android.util.Base64; \n"); // This is the android
													// base64 class
		bw.write("import javax.crypto.Cipher;  \n");
		bw.write("import java.security.MessageDigest; \n");
		bw.write("import java.util.Arrays; \n");
		bw.write("import javax.crypto.spec.SecretKeySpec;   \n");

		bw.write("public class StringDecoder { \n");

		bw.write("private static final String ALGORITHM = \"AES\";\n");

		// Iterates over the dictionary and generates a variable consisting
		// of the key fragments
		String globalListEncryptionKeyVariable = "";
		for (Iterator<String> x = dictionaryKeys.iterator(); x.hasNext();) {
			String dictKey = (String) x.next();
			globalListEncryptionKeyVariable = globalListEncryptionKeyVariable + "GlobalList." + dictKey;
			if (x.hasNext()) {
				globalListEncryptionKeyVariable = globalListEncryptionKeyVariable + "+";
			}
		}

		bw.write("private static String secretKey = " + globalListEncryptionKeyVariable + ";\n");

		// Generate the decoder class
		bw.write("public static String decrypt(final String encryptedValue) { \n");

		bw.write("String decryptedValue = null; \n");

		bw.write("try {\n");

		bw.write("final Key key = generateKeyFromString(secretKey);\n");
		bw.write("final Cipher c = Cipher.getInstance(ALGORITHM);\n");
		bw.write("c.init(Cipher.DECRYPT_MODE, key);\n");
		bw.write("final byte[] decorVal = Base64.decode(encryptedValue, Base64.DEFAULT);\n");
		bw.write("final byte[] decValue = c.doFinal(decorVal);\n");
		bw.write("decryptedValue = new String(decValue);\n");
		bw.write("} catch (Exception ex) {\n");

		bw.write("	}\n");

		bw.write("return decryptedValue;\n");
		bw.write("	}\n");

		// Generate the SHA key generator class
		bw.write("private static Key generateKeyFromString(final String secKey) {\n");

		bw.write("byte[] key;\n");

		bw.write("try {\n");
		bw.write("key = (secKey).getBytes(\"UTF-8\");\n");

		bw.write("MessageDigest sha = MessageDigest.getInstance(\"SHA-1\");\n");
		bw.write("key = sha.digest(key);\n");
		bw.write("key = Arrays.copyOf(key, 16);\n");

		bw.write("return new SecretKeySpec(key, ALGORITHM);\n");

		bw.write("} catch (Exception e) {\n");
		bw.write("e.printStackTrace();\n");
		bw.write("}\n");
		bw.write("return null;\n");
		bw.write("}\n");

		bw.write("}");

		bw.close();
		
		// Iterates over all the files, applying string encryption
		FileWalker fileWalker = new FileWalker(copiedFileLocation + "\\java");
		fileWalker.walk(copiedFileLocation + "\\java", encryptionKeyString);

		// Apply Package Obfuscation fixes
		PackageFlattener.ManifestFixer(copiedFileLocation);
		PackageFlattener.PackageFixer(copiedFileLocation + "\\java\\xyz");
		//PackageFlattener.CleanUpOldPackages(copiedFileLocation + "\\java");
	}

}
