package encryptorPackage;

import java.io.File;

import classPackageObfuscate.ClassRenamer;
import classPackageObfuscate.PackageFlattener;

public class FileWalker {

	private String startingPath;
	private String packageTracker = "";

	// Constructor which assigns the starting path.
	public FileWalker(String sPath) {
		startingPath = sPath;
	}

	// Recursively iterates through all files in the directory
	public void walk(String path, String keyString) {

		File root = new File(path);
		File[] list = root.listFiles();

		// If no files in directory, return
		if (list == null)
			return;

		// Loop through all files in the directory
		for (File f : list) {

			if (f.isDirectory() && !f.getName().equals("xyz")) {

				// Append directory onto the packageTracker
				packageTracker = packageTracker + f.getName() + ".";
				walk(f.getAbsolutePath(), keyString);

			} else {

				// Check for .java extension
				String extension = f.toString().substring(f.toString().lastIndexOf(".") + 1, f.toString().length());
				if (extension.equals("java")) {

					// Debug printing
					// System.out.println("JAVA CLASS FOUND:" +
					// f.getAbsoluteFile());

					// Store the package in a HashMap for later use
					PackageFlattener.packageList.put(packageTracker.substring(0, packageTracker.length() - 1), 1);

					// Encrypt strings
					StringReplacer.Replace(f, keyString);

					// Swap if-else statements with try-catch
					TryCatchReplacer.Replace(f);

					// Rename Android components.
					ClassRenamer.readFileAndReplace(f);

					// Move packages to one location
					PackageFlattener.MoveJavaFile(startingPath, f);
				}
			}
		}
		// Maintaining the package reference
		try {
			packageTracker = packageTracker.substring(0, packageTracker.length() - 1);
			packageTracker = packageTracker.substring(0, packageTracker.lastIndexOf("."));
			packageTracker = packageTracker + ".";
		} catch (Exception e) {
			// Ignore out of bounds exceptions
		}
	}
}