package semanticAnalyzer.types;


public enum TypeLiteral implements Type {
	TYPE_BOOL(0),
	TYPE_CHAR(0),
	TYPE_STRING(0),
	TYPE_INT(0),
	TYPE_FLOAT(0),
	TYPE_RAT(0);

	private int sizeInBytes;
	private String infoString;
	
	private TypeLiteral(int size) {
		this.sizeInBytes = size;
		this.infoString = toString();
	}
	private TypeLiteral(int size, String infoString) {
		this.sizeInBytes = size;
		this.infoString = infoString;
	}
	public int getSize() {
		return sizeInBytes;
	}
	public String infoString() {
		return infoString;
	}
}
