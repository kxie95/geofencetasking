package ArgumentObfuscator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;

/* This file contains the file writing methods for the argument obfuscator */

public class ObfuscationFileWriter {
    // Using bufferedwriter to write to file
	public void WriteToFile(Map<Integer, String> lineExpMap, String inputFilePath, String tempFilePath) throws IOException{
    	BufferedReader rd = null;
        BufferedWriter wt = null;
        File inputFile = null;
        File tempFile = null;

        try {
        	inputFile = new File(inputFilePath);
        	tempFile = new File(tempFilePath);
            rd = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(inputFilePath), "UTF-8")
                    );

            wt = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                            		tempFilePath), "UTF-8")
                    );
            
	            int count = 0;
	
	            for (String line; (line = rd.readLine()) != null;) {
	
	                count++;
	                
	                // Check if currently on one of the line numbers
	                String targetLine = IsOnLineNo(count, lineExpMap);
	                
	                // If there is an expression to write on this line
	                if (targetLine != null){
	                	wt.write(targetLine + "\n");
//	                	count++;
	                } 
	
	                wt.write(line);
	                wt.newLine();
	            }
            
        } finally {
            wt.close();
            rd.close();
            if (inputFile!=null){
            	inputFile.delete();
            	tempFile.renameTo(inputFile);
            }
            System.out.println("Changes written to file.");
        }
    }
	
	// Using compilationunit from javaparser to write to file
    public void writeCuToFile(CompilationUnit cu, String inputFilePath, String tempFilePath) throws IOException{
        BufferedWriter bw = null;
        File inputFile = null;
        File tempFile = null;
        try{
	        // Write the argument changes to file
	        //Specify the file name and path here
        	inputFile = new File(inputFilePath);
	   	 	tempFile = new File(tempFilePath);
		   	 if (!tempFile.exists()) {
			     tempFile.createNewFile();
			  }
		   	FileWriter fw = new FileWriter(tempFile);
		   	bw = new BufferedWriter(fw);
		   	bw.write(cu.toString());
        } catch (IOException e){
        	e.printStackTrace();
        } finally {
        	if (bw!=null){
        		bw.close();
        		if (inputFile!=null){
        			inputFile.delete();
                	tempFile.renameTo(inputFile);
        			System.out.println("Argument changes written to file.");
        		}
        	}
        }
    }
    
	
    private static String IsOnLineNo(int count, Map<Integer, String> map){
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
        	if (entry.getKey() == count){
        		return entry.getValue();
        	}
        }
        return null;
    }
}