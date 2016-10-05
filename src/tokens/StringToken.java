package tokens;

import inputHandler.TextLocation;

public class StringToken extends TokenImp {
	protected String value;
	
	protected StringToken(TextLocation location, String lexeme) {
		super(location, lexeme);
	}
	protected void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	public static StringToken make(TextLocation location, String lexeme) {
		StringToken result = new StringToken(location, lexeme);
		result.setValue(lexeme.substring(1, lexeme.length()-1)); // don't include quotes in value
		return result;
	}
	
	@Override
	protected String rawString() {
		return "string, " + value;
	}
}