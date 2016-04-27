package encryptorPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryCatchReplacer {

	private static final Pattern ifStatement = Pattern.compile("if\\ ?(.*)\\ ?\\{");
	private static final Pattern elseIfStatement = Pattern.compile("\\}\\ ?else if\\ ?\\(.*\\)\\ ?\\{");
	private static final Pattern elseStatement = Pattern.compile("\\}\\ ?else\\ ?\\{");
	private static final Pattern closeBracketPattern = Pattern.compile("\\}");
	private static final Pattern catchStatementPattern = Pattern.compile("\\}\\ ?catch(\\ ?.*)\\ ?\\{");
	private static final Pattern forLoopPattern = Pattern.compile("for\\ ?(.*)\\ ?\\{");
	private static final Pattern switchPattern = Pattern.compile("switch\\ ?(.*)\\ ?\\{");
	private static int closingBracketCount = 0;
	private static int exceptionCount = 0;
	private static FileReader fr;
	private static BufferedReader br;
	private static FileWriter fw;
	private static BufferedWriter bw;
	private static String line;

	public static void Replace(File f) {
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);

			StringBuilder sb = new StringBuilder();

			while ((line = br.readLine()) != null) {

				// search line for an if statement
				Matcher ifStatementMatcher = ifStatement.matcher(line.trim());

				// if it is an if statement, build try catch, else append
				// normally
				if (ifStatementMatcher.matches()) {
					// build try catch from if statement
					sb.append(buildTryCatch(line.trim()));
					// apply correct number of closing braces
					sb.append(new String(new char[closingBracketCount]).replace("\0", "}\n"));
					// reset closing bracket count
					closingBracketCount = 0;
				} else {
					sb.append(line + "\n");
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
			System.out.println(e.getMessage());
			System.out.println("Try - Error1!");
		} catch (IOException e) {
			System.out.println("Try - Error2!");
		}
	}

	private static StringBuilder buildTryCatch(String rootIfStatement) {
		StringBuilder builder = new StringBuilder();
		try {
			int forLoopAndSwitchCount = 0;
			int catchCount = 0;
			Stack<Boolean> closeCatchFirst = new Stack<Boolean>();
			boolean elseStatementPassed = false;
			builder.append("try {\n");
			builder.append(rootIfStatement + "\n");
			String currentLine = "";
			while ((currentLine = br.readLine()) != null) {
				String trimmedLine = currentLine.trim();
				Matcher nestedIfStatementMatcher = ifStatement.matcher(trimmedLine);
				Matcher elseIfMatcher = elseIfStatement.matcher(trimmedLine);
				Matcher elseMatcher = elseStatement.matcher(trimmedLine);
				Matcher closeBracketMatcher = closeBracketPattern.matcher(trimmedLine);
				Matcher catchMatcher = catchStatementPattern.matcher(trimmedLine);
				Matcher forLoopMatcher = forLoopPattern.matcher(trimmedLine);
				Matcher switchMatcher = switchPattern.matcher(trimmedLine);
				// if the line is a legitimate catch statement make sure an
				// extra end bracket is used
				if (catchMatcher.matches()) {
					catchCount++;
					closeCatchFirst.push(true);
				}
				// if line is a for loop, increment for loop counter to ensure correct closing brackets are used
				else if (forLoopMatcher.matches() || switchMatcher.matches()) {
					forLoopAndSwitchCount++;
					closeCatchFirst.push(false);
				}
				// if nested if statement, buildTryCatch from nested statement
				if (nestedIfStatementMatcher.matches()) {
					builder.append(buildTryCatch(trimmedLine));
					// append extra closed bracket to close nested loop
					builder.append("}\n");
					closingBracketCount--;
					// if else if statement, add catch message and buildTryCatch
					// from new else if statement
				} else if (elseIfMatcher.matches()) {
					builder.append(createNewCatchStatement());
					closingBracketCount++;
					// remove the "} else" part of the line
					trimmedLine = trimmedLine.replaceFirst("}\\ ?else\\ ?", "");
					builder.append(buildTryCatch(trimmedLine));
					break;
					// if else statement, add catch message
				} else if (elseMatcher.matches()) {
					builder.append(createNewCatchStatement());
					closingBracketCount++;
					elseStatementPassed = true;
					// if line is closing bracket, add catch message, close it,
					// and break out of loop
					// as the main if statement has completed
				} else if (closeBracketMatcher.matches()) {
					//continue if there are unresolved brackets up until this line
					boolean shouldContinue = forLoopAndSwitchCount > 0 || catchCount > 1;
					// if there is an unclosed for loop, only use the closed bracket for that loop
					if (forLoopAndSwitchCount > 0 && !closeCatchFirst.peek()) {
						closeCatchFirst.pop();
						forLoopAndSwitchCount--;
						builder.append("}\n");
					}
					// append an extra close bracket if there is a catch
					// statement in this if block
					else if (catchCount > 0 && closeCatchFirst.peek()) {
						closeCatchFirst.pop();
						catchCount--;
						builder.append("}\n");
						// create catch statement but don't increment bracket
						// count since the catch already
						// exists in the current if statement
						builder.append(createNewCatchStatement());
						// skip create new catch statement if this is the else
						// statement.
					}
					else if (!elseStatementPassed) {
						builder.append(createNewCatchStatement());
						closingBracketCount++;
					}
					
					if (shouldContinue) {
						continue;
					}
					break;
				} else {
					builder.append(trimmedLine + "\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder;
	}

	private static String createNewCatchStatement() {
		exceptionCount++;
		String catchStatement = "} else {\n" + "throw new NullPointerException();\n" + "}\n"
				+ "} catch(NullPointerException generatedException" + exceptionCount + ") {\n";
		return catchStatement;
	}
}
