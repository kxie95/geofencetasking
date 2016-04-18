package encryptorPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class StringReplacer {

	private static FileReader fr;
	private static BufferedReader br;
	private static FileWriter fw;
	private static BufferedWriter bw;
	private static String line;
	private static boolean packageBool;
	private static final String ALGORITHM = "AES";

	public static void Replace(File f, String keyString) {

		packageBool = false;

		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);

			StringBuffer sb = new StringBuffer();

			while ((line = br.readLine()) != null) {

				// Search line for any quotations marks as these are strings it can encrypt. This will also ignore any escaped quotation marks (\")
				Pattern p = Pattern.compile("\"(([^\\\\\"]|\\\\\"|\\\\(?!\"))*)\"");

				Matcher m = p.matcher(line);

				while (m.find()) {
					System.out.println("FOUND: " + m.group());

					try {
						// Replaces all strings found with the StringDecryptor method and an encrypted version of the string
						line = line.replaceAll(m.group().toString(), "StringDecoder.decrypt(\""
								+ encrypt(m.group().substring(1, m.group().length() - 1), keyString) + "\")");

						// DEBUG STATEMENTS FOR TESTING
						System.out.println("OUTPUTTED LINE IS: " + line);
						String testEncryption = encrypt(m.group().substring(1, m.group().length() - 1), keyString);
						System.out.println("ENCRYPTED STRING: " + testEncryption);
						String testDecryption = decrypt(testEncryption, keyString);
						System.out.println("DECRYPTED STRING: " + testDecryption);

						// If there is a pattern syntax and it can't handle the
						// string input, just ignore it
					} catch (PatternSyntaxException e) {

					}

				}
				// Can remove \n to make it less readable
				sb.append(line + "\n");

				// Add package name to file so it can user String Decoder
				// method.
				if (packageBool == false) {
					sb.append("import SDC.StringDecoder;\n");
					packageBool = true;
				}
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

	// Encrypts the string using the secret key
	public static String encrypt(final String valueEnc, final String secKey) {

		String encryptedValue = null;

		try {
			final Key key = generateKeyFromString(secKey);
			final Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			final byte[] encValue = c.doFinal(valueEnc.getBytes());
			encryptedValue = Base64.getEncoder().encodeToString(encValue);
		} catch (Exception ex) {
			System.out.println("The Exception is=" + ex);

		}
		return encryptedValue;
	}

	// Generates an actual key from the secret key
	private static Key generateKeyFromString(final String secKey) {

		byte[] key;

		try {
			key = (secKey).getBytes("UTF-8");

			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16); // use only first 128 bit

			return new SecretKeySpec(key, "AES");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// NOT USED, BUT GOOD TO KEEP FOR TESTING
	public static String decrypt(final String encryptedValue, final String secretKey) {

		String decryptedValue = null;

		try {

			final Key key = generateKeyFromString(secretKey);
			final Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			final byte[] decorVal = Base64.getDecoder().decode(encryptedValue);
			final byte[] decValue = c.doFinal(decorVal);
			decryptedValue = new String(decValue);
		} catch (Exception ex) {
			System.out.println("The Exception is: " + ex);
		}
		return decryptedValue;
	}
}
