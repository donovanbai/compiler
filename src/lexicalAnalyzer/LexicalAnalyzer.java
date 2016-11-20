package lexicalAnalyzer;


import logging.PikaLogger;

import inputHandler.InputHandler;
import inputHandler.LocatedChar;
import inputHandler.LocatedCharStream;
import inputHandler.PushbackCharStream;
import inputHandler.TextLocation;
import tokens.CharToken;
import tokens.CommentToken;
import tokens.FloatToken;
import tokens.IdentifierToken;
import tokens.LextantToken;
import tokens.NullToken;
import tokens.NumberToken;
import tokens.StringToken;
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
	protected Token findNextToken(boolean prevIsLitOrBracket) {
		LocatedChar ch = nextNonWhitespaceChar();
		
		if (ch.isDigit()) {
			return scanNumber(ch);
		}
		else if (ch.isIdStart()) {
			return scanIdentifier(ch);
		}
		else if (isPunctuatorStart(ch)) {	
			if ((ch.getCharacter() == '+' || ch.getCharacter() == '-') &&  !prevIsLitOrBracket && input.peek().getCharacter() != '>' || ch.getCharacter() == '.' && input.peek().isDigit()) {
				return scanNumber(ch);
			}
			else {
				return PunctuatorScanner.scan(ch, input);
			}
		}
		else if (isCommentStart(ch)) {
			return scanComment(ch);
		}
		else if (isCharStart(ch)) { // ^
			LocatedChar ch2 = input.next();
			int asciiVal = ch2.getCharacter();
			if (asciiVal < 32 || asciiVal > 126) { // if character following ^ is invalid, assume ^ is an extra character and move on
				input.pushback(ch2);
				lexicalError(ch);
				return findNextToken(prevIsLitOrBracket);
			}
			LocatedChar ch3 = input.next();
			if (ch3.getCharacter() != '^') { // if character following ^c is not ^, assume ^ is an extra character and move on
				input.pushback(ch3);
				input.pushback(ch2);
				lexicalError(ch);
				return findNextToken(prevIsLitOrBracket);
			}
			StringBuffer buffer = new StringBuffer();
			char[] chars = {ch.getCharacter(), ch2.getCharacter(), ch3.getCharacter()};
			buffer.append(chars);
			return CharToken.make(ch.getLocation(), buffer.toString());
		}
		else if (isStringStart(ch)) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(ch.getCharacter());
			if (!appendRestOfString(buffer)) { // if newline was found in string, ignore everything from " to \n and move on
				return findNextToken(prevIsLitOrBracket);
			}
			return StringToken.make(ch.getLocation(), buffer.toString());
		}
		else if (isEndOfInput(ch)) {
			return NullToken.make(ch.getLocation());
		}
		else {
			lexicalError(ch);
			return findNextToken(prevIsLitOrBracket);
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
		appendSubsequentIdChars(buffer);

		String lexeme = buffer.toString();
		if(Keyword.isAKeyword(lexeme)) {
			return LextantToken.make(firstChar.getLocation(), lexeme, Keyword.forLexeme(lexeme));
		}
		else {
			if (lexeme.length() > 32) identifierLengthError(firstChar.getLocation());
			return IdentifierToken.make(firstChar.getLocation(), lexeme);
		}
	}
	private void appendSubsequentIdChars(StringBuffer buffer) { // [ a..zA..Z_$0..9 ]*
		LocatedChar c = input.next();
		while(c.isIdChar()) {
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
	
	
	// string lexical analysis
	private boolean appendRestOfString(StringBuffer buffer) {
		LocatedChar c = input.next();
		while(c.getCharacter() != '\n' && c.getCharacter() != '"') {
			buffer.append(c.getCharacter());
			c = input.next();
		}
		if (c.getCharacter() == '\n') {
			lexicalError(c);
			return false;
		}
		buffer.append(c.getCharacter());
		return true;
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
	
	private boolean isCharStart(LocatedChar lc) {
		return lc.getCharacter() == '^';
	}
	
	private boolean isStringStart(LocatedChar lc) {
		return lc.getCharacter() == '"';
	}
	//////////////////////////////////////////////////////////////////////////////
	// Error-reporting	

	private void lexicalError(LocatedChar ch) {
		PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: invalid character " + ch);
	}

	private void identifierLengthError(TextLocation loc) {
		PikaLogger log = PikaLogger.getLogger("compiler.lexicalAnalyzer");
		log.severe("Lexical error: identifer contains more than 32 characters at " + loc);
	}
}
