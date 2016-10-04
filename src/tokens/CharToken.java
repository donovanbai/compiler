package tokens;

import inputHandler.TextLocation;

public class CharToken extends TokenImp {
	protected char value;
	
	protected CharToken(TextLocation location, String lexeme) {
		super(location, lexeme);
	}
	protected void setValue(char value) {
		this.value = value;
	}
	public char getValue() {
		return value;
	}
	
	public static CharToken make(TextLocation location, String lexeme) {
		CharToken result = new CharToken(location, lexeme);
		result.setValue(lexeme.charAt(1)); // character is at index 1 of the string "^c^"
		return result;
	}
	
	@Override
	protected String rawString() {
		return "character, " + value;
	}
}
