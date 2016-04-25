package ArgumentObfuscator;

import com.github.javaparser.ast.type.Type;

public class ParameterMap {
	private final Type type;
	private final String name;
	private final int typeCount;

	public ParameterMap(Type pType, String pName, int pTypeCount) {
		type = pType;
		name = pName;
		typeCount = pTypeCount;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getTypeCount() {
		return typeCount;
	}
}