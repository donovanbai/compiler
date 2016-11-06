package lexicalAnalyzer;

import inputHandler.PushbackCharStream;
import parser.Parser;
import tokens.*;

public abstract class ScannerImp implements Scanner {
	private Token nextToken;
	protected final PushbackCharStream input;
	
	protected abstract Token findNextToken(boolean prevIsLitOrBracket); // previous token is literal or bracket

	public ScannerImp(PushbackCharStream input) {
		super();
		this.input = input;
		nextToken = findNextToken(false);
	}

	// Iterator<Token> implementation
	@Override
	public boolean hasNext() {
		return !(nextToken instanceof NullToken);
	}

	@Override
	public Token next() {
		Token result = nextToken;
		if (Parser.startsLiteral(result) || result.isLextant(Punctuator.RRB, Punctuator.RSB)) {
			nextToken = findNextToken(true);
		}
		else nextToken = findNextToken(false);
		return result;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}