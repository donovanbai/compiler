package semanticAnalyzer.types;


public class CompoundType implements Type {
	private int sizeInBytes = 4;
	private String infoString;
	private String name;
	
	public CompoundType(String name) {
		this.name = name;
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
	public static CompoundType makeParentType(Type childType) {
		if (childType instanceof PrimitiveType) return new CompoundType("ARRAY_" + childType.toString());
		return new CompoundType("ARRAY_" + ((CompoundType) childType).getName());
	}
	public static Type makeChildType(CompoundType parentType) {
		String[] arr = parentType.getName().split("_");
		if (arr.length == 2) return PrimitiveType.valueOf(arr[1]);
		String s = parentType.getName().substring(6);	// eg. if parentType's name is "ARRAY_ARRAY_INTEGER" then the child type's name is "ARRAY_INTEGER"
		return new CompoundType(s);
	}
}
