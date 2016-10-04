package lexicalAnalyzer;


import logging.PikaLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import inputHandler.TextLocation;
import tokens.CommentToken;
import tokens.FloatToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.NumberToken;
import tokens.Token;

import static lexicalAnalyzer.PunctuatorScanningAids.*;

public class LexicalAnalyzer extends ScannerImp implements Scanner {
	public static LexicalAnalyzer make(String filename) {
		InputHandler handler = InputHandler.fromFilename(filename);
		PushbackCharStream charStream = PushbackCharStream.make(handler);
		return new LexicalAnalyzer(charStream);
	}

	public LexicalAnalyzer(PushbackCharStream input) {
		super(input);
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// Token-finding main dispatch	

	@Override
	protected Token findNextToken(boolean prevIsLitOrId) {
		LocatedChar ch = nextNonWhitespaceChar();
		
		if(ch.isDigit()) {
			return scanNumber(ch);
		}
		else if(ch.isLowerCase()) { // '_' counts as lower case
			return scanIdentifier(ch);
		}
		else if(isPunctuatorStart(ch)) {	
			if ((ch.getCharacter() == '+' || ch.getCharacter() == '-') &&  !prevIsLitOrId || ch.getCharacter() == '.' && input.peek().isDigit()) {
				return scanNumber(ch);
			}
			else {
				return PunctuatorScanner.scan(ch, input);
			}
		}
		else if(isEndOfInput(ch)) {
			return NullToken.make(ch.getLocation());
		}
		else if(isCommentStart(ch)) {		
			return scanComment(ch);
		}
		else {
			lexicalError(ch);
			return findNextToken(prevIsLitOrId);
		}
	}


	private LocatedChar nextNonWhitespaceChar() {
		LocatedChar ch = input.next();
		while(ch.isWhitespace()) {
			ch = input.next();
		}
		return ch;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Integer and float lexical analysis	

	private Token scanNumber(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentDigits(buffer);
		if (firstChar.getCharacter() == '.') { // current form is, for example, .001
			appendScientificNotation(buffer);
			return FloatToken.make(firstChar.getLocation(), buffer.toString());
		}
		else { // current form is, for example, 100 or +100
			if (appendFractionalPart(buffer)) { // if it's a float
				appendScientificNotation(buffer);
				return FloatToken.make(firstChar.getLocation(), buffer.toString());	// if number is a float		
			}
			else return NumberToken.make(firstChar.getLocation(), buffer.toString());	// if number is an integer
		}
	}
	private void appendSubsequentDigits(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isDigit()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	private void appendScientificNotation(StringBuffer buffer) {
		LocatedChar c = input.next();
		if (c.getCharacter() != 'E') {
			input.pushback(c);
			return;
		}		
		LocatedChar c2 = input.next();
		if (c2.getCharacter() == '+' || c2.getCharacter() == '-') {
			LocatedChar c3 = input.next();
			if (!c3.isDigit()) {
				input.pushback(c3);
				input.pushback(c2);
				input.pushback(c);
				return;
			}
			buffer.append(c.getCharacter());
			buffer.append(c2.getCharacter());
			buffer.append(c3.getCharacter());
			appendSubsequentDigits(buffer);
		}
		else if (c2.isDigit()) {
			buffer.append(c.getCharacter());
			buffer.append(c2.getCharacter());
			appendSubsequentDigits(buffer);			
		}
		else {
			input.pushback(c2);
			input.pushback(c);
		}
	}
	private boolean appendFractionalPart(StringBuffer buffer) { // returns true if input is a float, false otherwise
		LocatedChar c = input.next();
		LocatedChar c2 = input.next();
		if (c.getCharacter() != '.' || !c2.isDigit()) {
			input.pushback(c2);
			input.pushback(c);
			return false;
		}
		else {
			buffer.append(c.getCharacter());
			buffer.append(c2.getCharacter());
			appendSubsequentDigits(buffer);
			return true;
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Identifier and keyword lexical analysis	

	private Token scanIdentifier(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendSubsequentLowercase(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar.getLocation(), lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			return IdentifierToken.make(firstChar.getLocation(), lexeme);
		}
	}
	private void appendSubsequentLowercase(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.isLowerCase()) {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		input.pushback(c);
	}
	
	
	//comment lexical analysis
	private Token scanComment(LocatedChar firstChar) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(firstChar.getCharacter());
		appendRestOfComment(buffer);
		
		return CommentToken.make(firstChar.getLocation(), buffer.toString());
	}
	
	private void appendRestOfComment (StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.getCharacter() != '\n' && c.getCharacter() != '#') {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		if (c.getCharacter() == '\n') input.pushback(c);
		else buffer.append(c.getCharacter()); // append the hash that terminates the comment
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Punctuator lexical analysis	
	// old method left in to show a simple scanning method.
	// current method is the algorithm object PunctuatorScanner.java

	@SuppressWarnings("unused")
	private Token oldScanPunctuator(LocatedChar ch) {
		TextLocation location = ch.getLocation();
		
		switch(ch.getCharacter()) {
		case '*':
			return LextantToken.make(location, "*", Punctuator.MULTIPLY);
		case '+':
			return LextantToken.make(location, "+", Punctuator.ADD);
		case '>':
			return LextantToken.make(location, ">", Punctuator.GREATER);
		case ':':
			if(ch.getCharacter()=='=') {
				return LextantToken.make(location, ":=", Punctuator.ASSIGN);
			}
			else {
				throw new IllegalArgumentException("found : not followed by = in scanOperator");
			}
		case ',':
			return LextantToken.make(location, ",", Punctuator.SEPARATOR);
		case ';':
			return LextantToken.make(location, ";", Punctuator.TERMINATOR);
		default:
			throw new IllegalArgumentException("bad LocatedChar " + ch + "in scanOperator");
		}
	}

	

	//////////////////////////////////////////////////////////////////////////////
	// Character-classification routines specific to Pika scanning.	

	private boolean isPunctuatorStart(LocatedChar lc) {
		char c = lc.getCharacter();
		return isPunctuatorStartingCharacter(c);
	}

	private boolean isEndOfInput(LocatedChar lc) {
		return lc == LocatedCharStream.FLAG_END_OF_INPUT;
	}
	
	private boolean isCommentStart(LocatedChar lc) {
		return lc.getCharacter() == '#';
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	

	private void lexicalError(LocatedChar ch) {
		PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	
}
