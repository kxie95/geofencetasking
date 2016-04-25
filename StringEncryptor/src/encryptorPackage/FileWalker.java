package encryptorPackage;

import java.io.File;

import classPackageObfuscate.PackageFlattener;

public class FileWalker {

	private String startingPath;
	String packageTracker = "";

	public FileWalker(String sPath) {
		startingPath = sPath;
	}

	// Recursively iterates through all files in the directory
	public void walk(String path, String keyString) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			// Enter each directory
			if (f.isDirectory() && !f.getName().equals("xyz")) {
				// Append onto the packageTracker
				packageTracker = packageTracker + f.getName() + ".";
				walk(f.getAbsolutePath(), keyString);
			} else {
				String extension = f.toString().substring(f.toString().lastIndexOf(".") + 1, f.toString().length());

				// Identify java files
				if (extension.equals("java")) {
					
					// Debug printing
					 System.out.println("JAVA CLASS FOUND:" + f.getAbsoluteFile());

					// Store the package 
					PackageFlattener.packageList.put(packageTracker.substring(0, packageTracker.length() - 1), 1);

					// Apply replacer and mover to the java class
					StringReplacer.Replace(f, keyString);
					PackageFlattener.MoveJavaFile(startingPath, f);
				}
			}
		}
		// Keep the package but remove the last directory from the tracker
		try {
			packageTracker = packageTracker.substring(0, packageTracker.length() - 1);
			packageTracker = packageTracker.substring(0, packageTracker.lastIndexOf("."));
			packageTracker = packageTracker + ".";
		}
		catch(Exception e){
			// Ignore out of bounds exceptions
		}
		
	}
}