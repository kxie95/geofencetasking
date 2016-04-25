package ArgumentObfuscator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ArgumentObfuscator {

	private static List<Type> argTypes = new ArrayList<Type>();
	private static MethodCallVisitor mcv = new MethodCallVisitor();
	private static MethodVisitor mdv = new MethodVisitor();
    
    /* Traverse through all the files and return list of names of methods that we will change*/
    public static void ObfuscateArguments(String dirPath) throws ParseException, IOException {
    	FileWalker fw = new FileWalker();
    	List<String> javaFilePathsFound = new ArrayList<String>();
    	javaFilePathsFound = fw.getFilePathsFound(dirPath);
    	// Get names of all methods to modify from all the java files found
    	Set<String> namesOfMethodsToModify = getMethodNamesToModify(javaFilePathsFound);
    	// Go through each class and make changes
    	for (String path: javaFilePathsFound){
    		String tempFilePath = File.createTempFile("tempfile", ".java").getAbsolutePath();
        	// Write in Ag initialisers
        	// Uses filewriter to write to file
    		writeAgInitialisers(path, tempFilePath, namesOfMethodsToModify);
        	// Write in changes to method declarations and initialisers
        	// Uses filewriter to write to file
        	writeMethodDecChange(path, tempFilePath, namesOfMethodsToModify);
        	// Write in argument changes i.e. replace method call argument with Agn
        	// Uses javaparser CU to write to file
        	writeArgumentChange(path, tempFilePath, namesOfMethodsToModify);
    	}
        
    }
    
    private static Set<String> getMethodNamesToModify(List<String> filePaths) throws ParseException, IOException {
    	/* - Get all method declarations
    	 - Get all method calls
    	 - Check for overlaps, exclude superclasses and method declarations with no parameters
    	 - Return list of method names */
    	Set<String> namesOfMethodsDeclared = new HashSet<String>();
    	Set<String> namesOfMethodsCalled = new HashSet<String>();
    	Set<String> namesOfMethodsToModify = new HashSet<String>();
    	
    	for (String path: filePaths){
    		FileInputStream in = new FileInputStream(path);
    		CompilationUnit cu;
            try {
                // parse the file
                cu = JavaParser.parse(in);
            } finally {
                in.close();
            }
            // Get list of all method calls
            List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
            // Get list of all method declarations
            List<MethodDeclaration> methodDeclarations = mdv.getMethods(cu, null);
            for (MethodCallExpr m: methodCalls){
            	namesOfMethodsCalled.add(m.getName());
            }
            for (MethodDeclaration md:methodDeclarations){
            	namesOfMethodsDeclared.add(md.getName());
            } 
    	}
    	// Check for overlaps to determine which methods will be changed
    	for (String md: namesOfMethodsDeclared){
    		for (String mc: namesOfMethodsCalled){
    			if (mc.equals(md)){
    				namesOfMethodsToModify.add(md);
    			}
    		}
    	}
        return namesOfMethodsToModify;
    }
    
    private static void writeMethodDecChange(String inputFilePath, String tempFilePath, Set<String> namesOfMethodsToModify) throws ParseException, IOException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = mdv.getMethods(cu, null);
        // Retrieve list of methods and calls that we are going to change
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls, namesOfMethodsToModify);
		// Retrieve list of method declarations we are going to change
        List<MethodDeclaration> methodList = methodsAndCallsToModify[1];
        // Initialise map of line number and expression
        Map<Integer, String> lineExpMap = new HashMap<Integer, String>();
        for (MethodDeclaration m:methodList){
        	// For now, skip methods that don't have parameters
        	if (m.getParameters().isEmpty()){
        		continue;
        	}
        	// Keep counter of types in arguments
        	Map<Type, Integer> argTypeFreq = new HashMap<Type, Integer>();
        	// Need to make type/name mapping list
        	List<ParameterMap> paramMap = new ArrayList<ParameterMap>();
        	// Populate parameter list with method parameters
        	List<Parameter> paramList = new ArrayList<Parameter>();
        	paramList = m.getParameters();
        	int argCount = 0;
        	for (Parameter p: paramList){
        		/* OLD INDEX ASSIGNMENT*/
        		// Check type counter to get index
        		// If there was already an existing parameter type then increment counter
//        		int typeCount = argTypeFreq.containsKey(p.getType()) ? argTypeFreq.get(p.getType()) : 0;
//        		argTypeFreq.put(p.getType(), typeCount + 1);
        		
        		/* NEW INDEX ASSIGNMENT */
        		// Make map of parameter type, name and count (which will be the index)
        		ParameterMap pmap = new ParameterMap(p.getType(), p.getId().getName(), argCount);
        		argCount++;
        		paramMap.add(pmap);
        	}
        	// Initialise argument name
        	String argName = "x";
        	// Initialise string of calls
        	String statementString = "";
        	for (ParameterMap pm:paramMap){
        		// Initialise variables
        		VariableDeclarator vd = new VariableDeclarator(new VariableDeclaratorId(pm.getName()));
        		List<VariableDeclarator> vdList = new ArrayList<VariableDeclarator>();
        		vdList.add(vd);
        		VariableDeclarationExpr vexpr = new VariableDeclarationExpr(new ClassOrInterfaceType(pm.getType().toString()), vdList);
        		ExpressionStmt vexprStmt = new ExpressionStmt(vexpr);
        		// Create expression for value assignment
        		// Expression for the variable name (which is the argument of the method)
        		NameExpr varNameExpr = new NameExpr(argName);
        		// Expression for the argument name of the method call
        		NameExpr argNameExpr = new NameExpr(pm.getName());
        		// Expression for the index of the method call
        		NameExpr argIndexExpr = new NameExpr(""+pm.getTypeCount());
        		List<Expression> argExprList = new ArrayList<Expression>();
        		argExprList.add(argIndexExpr);
        		MethodCallExpr getVarExpr = new MethodCallExpr(varNameExpr, "getArg", argExprList);
        		// Cast the method call
        		CastExpr castExpr = new CastExpr(new ClassOrInterfaceType(pm.getType().toString()), getVarExpr);
        		// Make assignment expression for getting the value back
        		AssignExpr assignExpr = new AssignExpr(argNameExpr, castExpr, AssignExpr.Operator.assign);
        		// Create the string for the statements to write into file
        		statementString = statementString + vexprStmt.toString() + "\n" + assignExpr.toString() + ";" + "\n";
        	}
        	// Print the statement the line after the method declaration
        	lineExpMap.put(m.getBeginLine()+1+m.getAnnotations().size(), statementString);
        }
        
		// Iterate through map, write to file
        ObfuscationFileWriter obsFileWriter = new ObfuscationFileWriter();
		obsFileWriter.WriteToFile(lineExpMap, inputFilePath, tempFilePath);
    }
    
    private static boolean paramTypeIsInArgList(Parameter p){
    	for (Type t: argTypes){
    		if (p.getType().equals(t)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private static void writeAgInitialisers(String inputFilePath, String tempFilePath, Set<String> namesOfMethodsToModify) throws ParseException, IOException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        // Retrieve all methods and method calls in java file
        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = mdv.getMethods(cu, null);
        // Retrieve list of methods and method calls to change
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls, namesOfMethodsToModify);
        // Need this so it will work even if there's more than one method call in a single method
        int numberOfCalls = 0;
        // Initialise map of line number and expression
        Map<Integer, String> lineExpMap = new HashMap<Integer, String>();
        for (MethodCallExpr mc: (List<MethodCallExpr>) methodsAndCallsToModify[0]){
        	if (mc.getArgs()==null){
        		continue;
        	}
        	// Initialise method call argument(s)
        	NameExpr nExpr = new NameExpr("ag" + numberOfCalls);
        	if (mc.getArgs() != null){
        		// Create the expression for initialising arg variable
        		ExpressionStmt es = CreateNewObjectVariable(mc.getArgs(), nExpr.getName());
        		// Add line number and expression statement (in string) to map
        		lineExpMap.put(mc.getBeginLine(), es.toStringWithoutComments());
        	}
        	// Move on to next ag
        	numberOfCalls++;
        }
        
        // Sort the map by line number
		lineExpMap = sortMap(lineExpMap);
		// Iterate through map, write to file
		ObfuscationFileWriter obsFileWriter = new ObfuscationFileWriter();
		obsFileWriter.WriteToFile(lineExpMap, inputFilePath, tempFilePath);

    }
    
    private static void writeArgumentChange(String inputFilePath, String tempFilePath, Set<String> namesOfMethodsToModify) throws IOException, ParseException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
    
        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = mdv.getMethods(cu, null);
        // Retrieve list of method calls to modify arguments
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls, namesOfMethodsToModify);
        
        // Need this so it will work even if there's more than one method call in a single method
        int numberOfCalls = 0;
        // Initialise map of line number and expression
        Map<Integer, String> lineExpMap = new HashMap<Integer, String>();
        for (MethodCallExpr mc: (List<MethodCallExpr>) methodsAndCallsToModify[0]){
        	if (mc.getArgs()==null){
        		continue;
        	}
        	// Initialise method call argument(s)
        	List<Expression> ls = new ArrayList<Expression>();
        	NameExpr nExpr = new NameExpr("ag" + numberOfCalls);
        	ls.add(nExpr);
        	// Move on to next ag
        	numberOfCalls++;
        	// Set argument of method call
        	mc.setArgs(ls);
        }
        List<MethodDeclaration> methodList = methodsAndCallsToModify[1];
        for (MethodDeclaration m:methodList){
        	// For now, skip methods that don't have parameters
        	if (m.getParameters().isEmpty()){
        		continue;
        	}
        	// Create new list of parameters
        	List<Parameter> paramList = new ArrayList<Parameter>();
        	// Initialise argument name
        	String argName = "x";
        	paramList.add(new Parameter(new ClassOrInterfaceType("Ag"), new VariableDeclaratorId(argName)));
        	// Replace method parameters with Ag x
        	m.setParameters(paramList);
        }
        // Write the compilation unit to a file
        ObfuscationFileWriter obsFileWriter = new ObfuscationFileWriter();
        obsFileWriter.writeCuToFile(cu,inputFilePath,tempFilePath);
    }
    
    // Sort the line number to expression map by ascending line numbers
    private static Map<Integer, String> sortMap(Map<Integer, String> unsortedMap){
    	Map<Integer, String> sortedMap = new TreeMap<Integer, String>(
				new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}

			});
		sortedMap.putAll(unsortedMap);
		return sortedMap;
    }
    
    // Create a new variable initialisation creation node i.e. Ag x = new Ag();
    // Parameters: List of arguments and name of variable
    private static ExpressionStmt CreateNewObjectVariable(List<Expression> args, String varName){
        VariableDeclarator vd = new VariableDeclarator();
        // Set name of variable
        VariableDeclaratorId variableName = new VariableDeclaratorId(varName);
        // Set class type of variable
        ClassOrInterfaceType variableType = new ClassOrInterfaceType("Ag");
        // Create list of arguments for initialising the Ag variable
        List<Expression> argExps = new ArrayList<Expression>();
        for (Expression a:args){
            argExps.add(a);
        }
        // Create a new object initialisation node i.e. x = new Ag();
        // Parameters: scope, class type, list of argument expressions
        ObjectCreationExpr objectCreationExp = new ObjectCreationExpr(null,variableType,argExps);
        vd.setId(variableName);
        vd.setInit(objectCreationExp);
        // Initialize list of variable declarations
        List<VariableDeclarator> vdl = new ArrayList();
        vdl.add(vd);
        // Create variable declaration line
        VariableDeclarationExpr vde = new VariableDeclarationExpr(variableType,vdl);
        ExpressionStmt vexpr = new ExpressionStmt(vde);
        return vexpr;
    }

    /**
     * Simple visitor implementation for visiting method call nodes.
     */
    private static class MethodCallVisitor extends VoidVisitorAdapter {
    	
    	List<MethodCallExpr> methodCallList;
    	
    	public List<MethodCallExpr> getMethodCalls(CompilationUnit cu, Object arg){
    		methodCallList = new ArrayList<MethodCallExpr>();
    		visit(cu, arg);
    		return methodCallList;
    	}
    	
        @Override
        public void visit(MethodCallExpr n, Object arg){
    		//if it's a call to the superclass don't add it in
        	if (n.getScope() != null){
        		if (n.getScope().toString().equals("super")){
        			return;
        		}
        	}
			methodCallList.add(n);
        }
    }
    
    /**
     * Simple visitor implementation for visiting method declaration nodes.
     */
    private static class MethodVisitor extends VoidVisitorAdapter {
    	
    	List<MethodDeclaration> methodList;
    	
    	public List<MethodDeclaration> getMethods(CompilationUnit cu, Object arg){
    		methodList = new ArrayList<MethodDeclaration>();
    		visit(cu,arg);
    		return methodList;
    	}
    	
    	@Override
    	public void visit(MethodDeclaration n, Object arg) {
    		// if no parameters don't add it in
    		// if has override annotation don't add it in
    		if (!n.getParameters().isEmpty() && !hasOverrideAnnotation(n)){
    			methodList.add(n);
    		}
    	}
    	
    	private static boolean hasOverrideAnnotation(MethodDeclaration n){
    		List<AnnotationExpr> annos = n.getAnnotations();
    		if (!annos.isEmpty()){
    			for (AnnotationExpr an: annos){
    				if (an.toString().equals("@Override")){
    					return true;
    				}
    				return false;
    			}
    		}
    		return false;
    	}
    }
    
    /* Get target list of method declarations and calls that we want to change
     Only include overlapping method declarations and calls
     Exclude calls that have super scope and those that have no arguments*/
    private static List[] getMethodsAndCallsToModify(List<MethodDeclaration> methods, List<MethodCallExpr> methodCalls, Set<String> namesOfMethodsToModify){
        List<MethodCallExpr> methodCallsToModify = new ArrayList<MethodCallExpr>();
        List<MethodDeclaration> methodsToModify = new ArrayList<MethodDeclaration>();
        for (String s: namesOfMethodsToModify){
        	for (MethodDeclaration md: methods){
        		// Check for overlapping names
        		if (md.getName().equals(s)){
        			// Add to method declaration target list
        			methodsToModify.add(md);
        		}
        	}
        }
        for (String s: namesOfMethodsToModify){
        	for (MethodCallExpr mc: methodCalls){
        		// Check for overlapping names
        		if (mc.getName().equals(s)){
        			// Add to method call target list
        			methodCallsToModify.add(mc);
        		}
        	}
        }
        // Return both target lists of method declarations and method calls
        return new List[] {methodCallsToModify,methodsToModify};
    }
}