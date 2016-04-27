package encryptorPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ProGuardEnabler {

	public static void setMinifyEnabledToTrue(String filePath) {
		
		File f = new File(filePath);
		FileWriter fw = null;
		
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("minifyEnabled")) {
					line = line.replace("false", "true");
				}
				sb.append(line + "\n");
			}

			fw = new FileWriter(f, false);

			br.close();
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
