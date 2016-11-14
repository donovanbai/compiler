package lexicalAnalyzer;

import tokens.LextantToken;
import tokens.Token;


public enum Keyword implements Lextant {
	CONST("const"),
	PRINT("print"),
	NEWLINE("_n_"),
	TRUE("_true_"),
	FALSE("_false_"),
	EXEC("exec"),
	NULL_KEYWORD(""),
	VAR("var"),
	BOOL("bool"),
	CHAR("char"),
	STRING("string"),
	INT("int"),
	FLOAT("float"),
	RAT("rat"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	NEW("new"),
	CLONE("clone"),
	RELEASE("release"),
	LENGTH("length");

	private String lexeme;
	private Token prototype;
	
	
	private Keyword(String lexeme) {
		this.lexeme = lexeme;
		this.prototype = LextantToken.make(null, lexeme, this);
	}
	public String getLexeme() {
		return lexeme;
	}
	public Token prototype() {
		return prototype;
	}
	
	public static Keyword forLexeme(String lexeme) {
		for(Keyword keyword: values()) {
			if(keyword.lexeme.equals(lexeme)) {
				return keyword;
			}
		}
		return NULL_KEYWORD;
	}
	public static boolean isAKeyword(String lexeme) {
		return forLexeme(lexeme) != NULL_KEYWORD;
	}
	public static boolean isAType(String lexeme) {
		Keyword k = forLexeme(lexeme);
		return k == BOOL || k == CHAR || k == STRING || k == INT || k == FLOAT || k == RAT;
	}
	
	/*   the following hashtable lookup can replace the serial-search implementation of forLexeme() above. It is faster but less clear. 
	private static LexemeMap<Keyword> lexemeToKeyword = new LexemeMap<Keyword>(values(), NULL_KEYWORD);
	public static Keyword forLexeme(String lexeme) {
		return lexemeToKeyword.forLexeme(lexeme);
	}
	*/
}
