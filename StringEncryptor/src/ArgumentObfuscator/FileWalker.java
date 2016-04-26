package ArgumentObfuscator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileWalker {

	private List<String> javaFilePathsFound;
	
	public List<String> getFilePathsFound(String path){
		javaFilePathsFound = new ArrayList<String>();
		walk(path);
		return javaFilePathsFound;
	}
	
	public void walk(String path) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		// Recursively iterates through all files in the directory
		for (File f : list) {
			// Enter each directory
			if (f.isDirectory()) {
				walk(f.getAbsolutePath());
			} else {
				String extension = f.toString().substring(f.toString().lastIndexOf(".") + 1, f.toString().length());

				// Identify java files, excludes StringDecoder and GlobalList
				// classes
				if (extension.equals("java")
						&& !(f.getName().equals("StringDecoder.java") || f.getName().equals("GlobalList.java"))) {
					walk(f.getAbsolutePath());

					// Debug printing
//					System.out.println("JAVA CLASS FOUND:" + f.getAbsolutePath());
					javaFilePathsFound.add(f.getPath());

				}
			}
		}
	}
}