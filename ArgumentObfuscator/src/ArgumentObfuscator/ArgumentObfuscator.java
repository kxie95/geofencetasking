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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.github.javaparser.ast.expr.AssignExpr;
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

	private PackageDeclaration packageDecl;
	private static List<Type> argTypes = new ArrayList<Type>();
	private static List<ImportDeclaration> importList = new ArrayList<ImportDeclaration>();
	private static ObfuscationFileWriter obsFileWriter = new ObfuscationFileWriter();
	
    public static void main(String[] args) throws Exception {
    	//Change these later
    	String inputFilePath = "C:/Users/alyssaong/Documents/eclipse workspace/overloadobfuscation/src/data/AchievementServiceCopy.java";
    	String tempFilePath = "C:/Users/alyssaong/Documents/eclipse workspace/overloadobfuscation/src/data/AchievementServiceCopyTemp.java";
    	String dirPath = "C:/Users/alyssaong/Documents/eclipse workspace/overloadobfuscation/src/data/";
    	String classFilePath =  "C:/Users/alyssaong/Documents/eclipse workspace/overloadobfuscation/src/data/Ag.java";
    	// Write in Ag initialisers
    	// Uses filewriter to write to file
    	writeAgInitialisers(inputFilePath, tempFilePath);

    	// Write in changes to method declarations and initialisers
    	// Uses filewriter to write to file
    	writeMethodDecChange(inputFilePath, tempFilePath);

    	// Write in argument changes i.e. replace method call argument with Agn
    	// Uses javaparser CU to write to file
    	writeArgumentChange(inputFilePath, tempFilePath);
    	
    	// Write class files for Ag
    	// Gonna manually do this one so don't need
//    	writeAgClassFile(inputFilePath, classFilePath);
    }
    
    private static void writeMethodDecChange(String inputFilePath, String tempFilePath) throws ParseException, IOException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        MethodCallVisitor mcv = new MethodCallVisitor();
        
        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = new MethodVisitor().getMethods(cu, null);
        // Retrieve list of method calls to modify arguments
//        List<MethodCallExpr> methodCallsToModify = getMethodCallsToModify(methods, methodCalls);
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls);
		// Create method declaration and variable initialisation changes
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
//        	System.out.println(paramList);
        	//TODO: find out why the last one has an Ag parameter
        	for (Parameter p: paramList){
        		// Check type counter to get index
        		// If there was already an existing parameter type then increment counter
        		int typeCount = argTypeFreq.containsKey(p.getType()) ? argTypeFreq.get(p.getType()) : 0;
        		argTypeFreq.put(p.getType(), typeCount + 1);
        		// Make map of parameter type, name and type count (which will be the index)
        		ParameterMap pmap = new ParameterMap(p.getType(), p.getId().getName(), typeCount);
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
        		argExprList.add(argNameExpr);
        		argExprList.add(argIndexExpr);
        		MethodCallExpr getVarExpr = new MethodCallExpr(varNameExpr, "getArg", argExprList);
        		// Make assignment expression for getting the value back
        		AssignExpr assignExpr = new AssignExpr(argNameExpr, getVarExpr, AssignExpr.Operator.assign);
        		// Create the string for the statements to write into file
        		statementString = statementString + vexprStmt.toString() + "\n" + assignExpr.toString() + ";" + "\n";
        	}
        	// Print the statement the line after the method declaration
        	lineExpMap.put(m.getBeginLine()+1+m.getAnnotations().size(), statementString);
        }
		// Iterate through map, write to file
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
    
    private static void writeAgInitialisers(String inputFilePath, String tempFilePath) throws ParseException, IOException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
        importList = cu.getImports();
        MethodCallVisitor mcv = new MethodCallVisitor();
        
        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = new MethodVisitor().getMethods(cu, null);
        // Retrieve list of method calls to modify arguments
//        List<MethodCallExpr> methodCallsToModify = getMethodCallsToModify(methods, methodCalls);
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls);
        
        // Need this so it will work even if there's more than one method call in a single method
        int numberOfCalls = 0;
        // Initialise map of line number and expression
        Map<Integer, String> lineExpMap = new HashMap<Integer, String>();
        // TODO: Change this to declare the argument at the start instead later
        for (MethodCallExpr mc: (List<MethodCallExpr>) methodsAndCallsToModify[0]){
        	if (mc.getArgs()==null){
        		continue;
        	}
        	// TODO: Ignore those with scope of super if (scope){continue;} !! very important
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
		obsFileWriter.WriteToFile(lineExpMap, inputFilePath, tempFilePath);

    }
    
    private static void writeArgumentChange(String inputFilePath, String tempFilePath) throws IOException, ParseException{
    	FileInputStream in = new FileInputStream(inputFilePath);
        CompilationUnit cu;
        try {
            // parse the file
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }

        MethodCallVisitor mcv = new MethodCallVisitor();
        
        List<MethodCallExpr> methodCalls = mcv.getMethodCalls(cu, null);
        List<MethodDeclaration> methods = new MethodVisitor().getMethods(cu, null);
        // Retrieve list of method calls to modify arguments
//        List<MethodCallExpr> methodCallsToModify = getMethodCallsToModify(methods, methodCalls);
        List[] methodsAndCallsToModify = getMethodsAndCallsToModify(methods, methodCalls);
        
        // Need this so it will work even if there's more than one method call in a single method
        int numberOfCalls = 0;
        // Initialise map of line number and expression
        Map<Integer, String> lineExpMap = new HashMap<Integer, String>();
        for (MethodCallExpr mc: (List<MethodCallExpr>) methodsAndCallsToModify[0]){
        	// TODO: Ignore those with scope of super if (scope){continue;}
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
        obsFileWriter.writeCuToFile(cu,inputFilePath,tempFilePath);
    }
    
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
//        System.out.println(vexpr.getChildrenNodes());
        return vexpr;
    }

    /**
     * Simple visitor implementation for visiting MethodCall nodes.
     */
    private static class MethodCallVisitor extends VoidVisitorAdapter {
    	
    	List<MethodCallExpr> methodCallList = new ArrayList<MethodCallExpr>();
    	
    	public List<MethodCallExpr> getMethodCalls(CompilationUnit cu, Object arg){
    		visit(cu, arg);
    		return methodCallList;
    	}
    	
        @Override
        public void visit(MethodCallExpr n, Object arg){
    		//TODO: if it's a call to the superclass don't add it in
        	methodCallList.add(n);
        }
    }
    
    private static class MethodVisitor extends VoidVisitorAdapter {
    	
    	List<MethodDeclaration> methodList = new ArrayList<MethodDeclaration>();
    	
    	public List<MethodDeclaration> getMethods(CompilationUnit cu, Object arg){
    		visit(cu,arg);
    		return methodList;
    	}
    	
    	@Override
    	public void visit(MethodDeclaration n, Object arg) {
    		methodList.add(n);
      }
    }
    
    private static List[] getMethodsAndCallsToModify(List<MethodDeclaration> methods, List<MethodCallExpr> methodCalls){
        List<MethodCallExpr> methodCallsToModify = new ArrayList<MethodCallExpr>();
        List<MethodDeclaration> methodsToModify = new ArrayList<MethodDeclaration>();
    	for (MethodCallExpr mc: methodCalls){
    		for (MethodDeclaration m: methods){
        		if (m.getName().equals(mc.getName())){
        			// method call mc is in method m
        			methodCallsToModify.add(mc);
        			
        			// Check for duplicates
        			boolean listAlreadyHasMethod = false;
            		for (MethodDeclaration md: methodsToModify){
            			if (md.getName().equals(mc.getName())){
            				listAlreadyHasMethod = true;
            				break;
            			}
            		}
            		if (!listAlreadyHasMethod){
            			methodsToModify.add(m);
            		}
        			
        		}
        	}
        }
        return new List[] {methodCallsToModify,methodsToModify};
    }
    
}