package encryptorPackage;

import java.io.File;

public class FileWalker {

	public void walk(String path, String keyString) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		// Recursively iterates through all files in the directory
		for (File f : list) {
			// Enter each directory
			if (f.isDirectory()) {
				walk(f.getAbsolutePath(), keyString);
			} else {
				String extension = f.toString().substring(f.toString().lastIndexOf(".") + 1, f.toString().length());

				// Identify java files, excludes StringDecoder and GlobalList
				// classes
				if (extension.equals("java")
						&& !(f.getName().equals("StringDecoder.java") || f.getName().equals("GlobalList.java"))) {
					walk(f.getAbsolutePath(), keyString);

					// Debug printing
					System.out.println("JAVA CLASS FOUND:" + f.getAbsoluteFile());

					// Apply replacer to the java class
					StringReplacer.Replace(f, keyString);
					TryCatchReplacer.Replace(f);
				}
			}
		}
	}
}