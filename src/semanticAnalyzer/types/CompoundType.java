package semanticAnalyzer.types;


public class CompoundType implements Type {
	private int sizeInBytes = 4;
	private String infoString;
	private String name;
	private boolean subtypeIsReference;
	
	private CompoundType(String name, boolean subtypeIsReference) {
		this.name = name;
		this.subtypeIsReference = subtypeIsReference;
	}
	public int getSize() {
		return sizeInBytes;
	}
	public String infoString() {
		return infoString;
	}
	public String getName() {
		return name;
	}
	public boolean getSubtypeIsReference() {
		return subtypeIsReference;
	}
	
	public static CompoundType makeParentType(Type childType) {
		if (childType instanceof PrimitiveType) return new CompoundType("ARRAY_" + childType.toString(), false);
		return new CompoundType("ARRAY_" + ((CompoundType) childType).getName(), true);
	}
	public static Type makeChildType(CompoundType parentType) {
		String[] arr = parentType.getName().split("_");
		if (arr.length == 2) return PrimitiveType.valueOf(arr[1]);
		String s = parentType.getName().substring(6);	// eg. if parentType's name is "ARRAY_ARRAY_INTEGER" then the child type's name is "ARRAY_INTEGER"
		if (arr.length == 3) return new CompoundType(s, false);
		return new CompoundType(s, true);
	}
}
