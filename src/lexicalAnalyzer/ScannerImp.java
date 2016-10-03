package lexicalAnalyzer;

import inputHandler.PushbackCharStream;
import tokens.*;

public abstract class ScannerImp implements Scanner {
	private Token nextToken;
	protected final PushbackCharStream input;
	
	protected abstract Token findNextToken(boolean prevIsLitOrId); // previous token is literal or identifier

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
		if (result.getClass() == NumberToken.class || result.getClass() == IdentifierToken.class) {
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