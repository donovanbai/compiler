package parser;

import java.util.Arrays;

import lexicalAnalyzer.Keyword;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import lexicalAnalyzer.Scanner;
import logging.PikaLogger;
import parseTree.ParseNode;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.BlockStmtNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ExprListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStmtNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.NewArrayNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.UnaryOperatorNode;
import parseTree.nodeTypes.WhileStmtNode;
import tokens.CharToken;
import tokens.CommentToken;
import tokens.FloatToken;
import tokens.IdentifierToken;
import tokens.NullToken;
import tokens.NumberToken;
import tokens.StringToken;
import tokens.Token;


public class Parser {
	private Scanner scanner;
	private Token nowReading;
	private Token previouslyRead;
	
	public static ParseNode parse(Scanner scanner) {
		Parser parser = new Parser(scanner);
		return parser.parse();
	}
	public Parser(Scanner scanner) {
		super();
		this.scanner = scanner;
	}
	
	public ParseNode parse() {
		readToken();
		return parseProgram();
	}

	////////////////////////////////////////////////////////////
	// "program" is the start symbol S
	// S -> EXEC mainBlock
	
	private ParseNode parseProgram() {
		if(!startsProgram(nowReading)) {
			return syntaxErrorNode("program");
		}
		ParseNode program = new ProgramNode(nowReading);
		
		expect(Keyword.EXEC);
		ParseNode mainBlock = parseMainBlock();
		program.appendChild(mainBlock);
		
		if(!(nowReading instanceof NullToken)) {
			return syntaxErrorNode("end of program");
		}
		
		return program;
	}
	private boolean startsProgram(Token token) {
		return token.isLextant(Keyword.EXEC);
	}
	
	
	///////////////////////////////////////////////////////////
	// mainBlock
	
	// mainBlock -> { statement* }
	private ParseNode parseMainBlock() {
		if(!startsMainBlock(nowReading)) {
			return syntaxErrorNode("mainBlock");
		}
		ParseNode mainBlock = new MainBlockNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		
		while(startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			mainBlock.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return mainBlock;
	}
	private boolean startsMainBlock(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	
	///////////////////////////////////////////////////////////
	// statements
	
	// statement-> declaration | printStmt
	private ParseNode parseStatement() {
		if(!startsStatement(nowReading)) {
			return syntaxErrorNode("statement");
		}
		if(startsDeclaration(nowReading)) {
			return parseDeclaration();
		}
		if(startsPrintStatement(nowReading)) {
			return parsePrintStatement();
		}
		if(startsAssignment(nowReading)) {
			return parseAssignment();
		}
		if(startsBlockStmt(nowReading)) {
			return parseBlockStmt();
		}
		if(startsIfStmt(nowReading)) {
			return parseIfStmt();
		}
		if(startsWhileStmt(nowReading)) {
			return parseWhileStmt();
		}
		return syntaxErrorNode("statement");
	}
	private boolean startsStatement(Token token) {
		return startsPrintStatement(token) || startsDeclaration(token) || startsAssignment(token) || startsBlockStmt(token) || startsIfStmt(token) || startsWhileStmt(token);
	}
	
	// printStmt -> PRINT printExpressionList .
	private ParseNode parsePrintStatement() {
		if(!startsPrintStatement(nowReading)) {
			return syntaxErrorNode("print statement");
		}
		PrintStatementNode result = new PrintStatementNode(nowReading);
		
		readToken();
		result = parsePrintExpressionList(result);
		
		expect(Punctuator.TERMINATOR);
		return result;
	}
	private boolean startsPrintStatement(Token token) {
		return token.isLextant(Keyword.PRINT);
	}	

	// This adds the printExpressions it parses to the children of the given parent
	// printExpressionList -> printExpression* bowtie (,|;)  (note that this is nullable)

	private PrintStatementNode parsePrintExpressionList(PrintStatementNode parent) {
		while(startsPrintExpression(nowReading) || startsPrintSeparator(nowReading)) {
			parsePrintExpression(parent);
			parsePrintSeparator(parent);
		}
		return parent;
	}
	

	// This adds the printExpression it parses to the children of the given parent
	// printExpression -> (expr | nl)?     (nullable)
	
	private void parsePrintExpression(PrintStatementNode parent) {
		if(startsExpression(nowReading)) {
			ParseNode child = parseExpression();
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Keyword.NEWLINE)) {
			readToken();
			ParseNode child = new NewlineNode(previouslyRead);
			parent.appendChild(child);
		}
		// else we interpret the printExpression as epsilon, and do nothing
	}
	private boolean startsPrintExpression(Token token) {
		return startsExpression(token) || token.isLextant(Keyword.NEWLINE) ;
	}
	
	
	// This adds the printExpression it parses to the children of the given parent
	// printExpression -> expr? ,? nl? 
	
	private void parsePrintSeparator(PrintStatementNode parent) {
		if(!startsPrintSeparator(nowReading) && !nowReading.isLextant(Punctuator.TERMINATOR)) {
			ParseNode child = syntaxErrorNode("print separator");
			parent.appendChild(child);
			return;
		}
		
		if(nowReading.isLextant(Punctuator.SPACE)) {
			readToken();
			ParseNode child = new SpaceNode(previouslyRead);
			parent.appendChild(child);
		}
		else if(nowReading.isLextant(Punctuator.SEPARATOR)) {
			readToken();
		}		
		else if(nowReading.isLextant(Punctuator.TERMINATOR)) {
			// we're at the end of the bowtie and this printSeparator is not required.
			// do nothing.  Terminator is handled in a higher-level nonterminal.
		}
	}
	private boolean startsPrintSeparator(Token token) {
		return token.isLextant(Punctuator.SEPARATOR, Punctuator.SPACE) ;
	}
	
	
	// declaration -> CONST identifier := expression .
	//			   -> VAR identifier := expression .
	private ParseNode parseDeclaration() {
		if(!startsDeclaration(nowReading)) {
			return syntaxErrorNode("declaration");
		}
		Token declarationToken = nowReading;
		readToken();
		
		ParseNode identifier = parseIdentifier();
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return DeclarationNode.withChildren(declarationToken, identifier, initializer);
	}
	private boolean startsDeclaration(Token token) {
		return token.isLextant(Keyword.CONST) || token.isLextant(Keyword.VAR);
	}

	// assignmentStatement -> target := expression .
	private ParseNode parseAssignment() {
		if(!startsAssignment(nowReading)) {
			return syntaxErrorNode("assignment");
		}
		Token assignmentToken = nowReading;
		ParseNode identifier = parseIdentifier();		
		expect(Punctuator.ASSIGN);
		ParseNode initializer = parseExpression();
		expect(Punctuator.TERMINATOR);
		
		return AssignmentNode.withChildren(assignmentToken, identifier, initializer);
	}
	private boolean startsAssignment(Token token) {
		return token instanceof IdentifierToken;
	}
	
	private ParseNode parseBlockStmt() {
		if (!startsBlockStmt(nowReading)) return syntaxErrorNode("block statement");
		BlockStmtNode blockStmt = new BlockStmtNode(nowReading);
		expect(Punctuator.OPEN_BRACE);
		while(startsStatement(nowReading)) {
			ParseNode statement = parseStatement();
			blockStmt.appendChild(statement);
		}
		expect(Punctuator.CLOSE_BRACE);
		return blockStmt;
	}
	private boolean startsBlockStmt(Token token) {
		return token.isLextant(Punctuator.OPEN_BRACE);
	}
	
	private ParseNode parseIfStmt() {
		if (!startsIfStmt(nowReading)) return syntaxErrorNode("if statement");
		IfStmtNode node = new IfStmtNode(nowReading);
		readToken();
		expect(Punctuator.LRB);
		ParseNode condition = parseExpression();
		node.appendChild(condition);
		expect(Punctuator.RRB);
		ParseNode thenBlock = parseBlockStmt();
		node.appendChild(thenBlock);
		// check if there is an else block
		if(Keyword.forLexeme(nowReading.getLexeme()) == Keyword.ELSE) {
			readToken();
			ParseNode elseBlock = parseBlockStmt();
			node.appendChild(elseBlock);
		}
		return node;
	}
	private boolean startsIfStmt(Token token) {
		return Keyword.forLexeme(token.getLexeme()) == Keyword.IF;
	}
	
	private ParseNode parseWhileStmt() {
		if (!startsWhileStmt(nowReading)) return syntaxErrorNode("while statement");
		WhileStmtNode node = new WhileStmtNode(nowReading);
		readToken();
		expect(Punctuator.LRB);
		ParseNode condition = parseExpression();
		node.appendChild(condition);
		expect(Punctuator.RRB);
		ParseNode doBlock = parseBlockStmt();
		node.appendChild(doBlock);
		return node;
	}
	private boolean startsWhileStmt(Token token) {
		return Keyword.forLexeme(token.getLexeme()) == Keyword.WHILE;
	}
	
	///////////////////////////////////////////////////////////
	// expressions
	// expr						-> orExpression
	// orExpression				-> andExpression [|| andExpression]*  (left-assoc)
	// andExpressions           -> comparisonExpression [&& comparisonExpression]*  (left-assoc)
	// comparisonExpression     -> additiveExpression [> additiveExpression]?
	// additiveExpression       -> multiplicativeExpression [(+|-) multiplicativeExpression]*  (left-assoc)
	// multiplicativeExpression -> notExpression [(MULT|/|//) notExpression]*  (left-assoc)
	// notExpression			-> [!]* arrayIndexingExpr (right-assoc)
	// arrayIndexingExpr		-> atomicExpression | identifier[expr]
	// atomicExpression         -> literal | (expr) | [expr|type] | [expr (,expr)*] | new[]()
	// literal                  -> intNumber | identifier | booleanConstant

	private ParseNode parseExpression() {		
		if(!startsExpression(nowReading)) {
			return syntaxErrorNode("expression");
		}
		return parseOrExpression();
	}
	
	private boolean startsExpression(Token token) {
		return startsOrExpression(token);
	}
	
	private ParseNode parseOrExpression() {
		if (!startsOrExpression(nowReading)) {
			return syntaxErrorNode("or expression");
		}
		ParseNode left = parseAndExpression();
		while (nowReading.isLextant(Punctuator.OR)) {
			Token orToken = nowReading;
			readToken();
			ParseNode right = parseAndExpression();
			
			left = BinaryOperatorNode.withChildren(orToken, left, right);
		}
		return left;
	}

	private boolean startsOrExpression(Token token) {
		return startsAndExpression(token);
	}
	
	private ParseNode parseAndExpression() {
		if (!startsAndExpression(nowReading)) {
			return syntaxErrorNode("and expression");
		}
		ParseNode left = parseComparisonExpression();
		while (nowReading.isLextant(Punctuator.AND)) {
			Token andToken = nowReading;
			readToken();
			ParseNode right = parseComparisonExpression();
			
			left = BinaryOperatorNode.withChildren(andToken, left, right);
		}
		return left;
	}

	private boolean startsAndExpression(Token token) {
		return startsComparisonExpression(token);
	}
	
	// comparisonExpression -> additiveExpression [> additiveExpression]?
	private ParseNode parseComparisonExpression() {
		if(!startsComparisonExpression(nowReading)) {
			return syntaxErrorNode("comparison expression");
		}
		
		ParseNode left = parseAdditiveExpression();
		if(nowReading.isLextant(Punctuator.GREATER) || nowReading.isLextant(Punctuator.LESS) || nowReading.isLextant(Punctuator.GREATER_OR_EQ) || nowReading.isLextant(Punctuator.LESS_OR_EQ) || nowReading.isLextant(Punctuator.EQUAL) || nowReading.isLextant(Punctuator.NOT_EQUAL)){
			Token compareToken = nowReading;
			readToken();
			ParseNode right = parseAdditiveExpression();
			
			return BinaryOperatorNode.withChildren(compareToken, left, right);
		}
		return left;

	}
	private boolean startsComparisonExpression(Token token) {
		return startsAdditiveExpression(token);
	}

	// additiveExpression -> multiplicativeExpression [(+|-) multiplicativeExpression]*  (left-assoc)
	private ParseNode parseAdditiveExpression() {
		if(!startsAdditiveExpression(nowReading)) {
			return syntaxErrorNode("additiveExpression");
		}
		
		ParseNode left = parseMultiplicativeExpression();
		while(nowReading.isLextant(Punctuator.ADD) || nowReading.isLextant(Punctuator.SUBTRACT)) {
			Token additiveToken = nowReading;
			readToken();
			ParseNode right = parseMultiplicativeExpression();
			
			left = BinaryOperatorNode.withChildren(additiveToken, left, right);
		}
		return left;
	}
	private boolean startsAdditiveExpression(Token token) {
		return startsMultiplicativeExpression(token);
	}	

	// multiplicativeExpression -> notExpression [(MULT|/) notExpression]*  (left-assoc)
	private ParseNode parseMultiplicativeExpression() {
		if(!startsMultiplicativeExpression(nowReading)) {
			return syntaxErrorNode("multiplicativeExpression");
		}
		
		ParseNode left = parseNotExpression();
		while(nowReading.isLextant(Punctuator.MULTIPLY) || nowReading.isLextant(Punctuator.DIVIDE) || nowReading.isLextant(Punctuator.OVER) || nowReading.isLextant(Punctuator.EXPRESS_OVER) || nowReading.isLextant(Punctuator.RATIONALIZE)) {
			Token multiplicativeToken = nowReading;
			readToken();
			ParseNode right = parseNotExpression();
			
			left = BinaryOperatorNode.withChildren(multiplicativeToken, left, right);
		}
		return left;
	}
	private boolean startsMultiplicativeExpression(Token token) {
		return startsNotExpression(token);
	}
	
	private ParseNode parseNotExpression() {
		if(!startsNotExpression(nowReading)) {
			return syntaxErrorNode("notExpression");
		}
		if (nowReading.isLextant(Punctuator.NOT)) {
			Token notToken = nowReading;
			readToken();		
			ParseNode child = parseNotExpression();
			return UnaryOperatorNode.withChild(notToken, child);
		}
		else {
			return parseArrayIndexingExpr();
		}
	}
	private boolean startsNotExpression(Token token) {
		return startsArrayIndexingExpr(token) || token.isLextant(Punctuator.NOT);
	}
	
	private ParseNode parseArrayIndexingExpr() {
		if(!startsArrayIndexingExpr(nowReading)) {
			return syntaxErrorNode("arrayIndexingExpression");
		}
		if (!startsIdentifier(nowReading)) return parseAtomicExpression();
		ParseNode id = parseIdentifier();
		if (nowReading.isLextant(Punctuator.LSB)) {	// this is array indexing
			Token indexToken = nowReading;
			readToken();
			ParseNode expr = parseExpression();
			expect(Punctuator.RSB);
			return BinaryOperatorNode.withChildren(indexToken, id, expr);
		}
		return id;
	}
	private boolean startsArrayIndexingExpr(Token token) {
		return startsAtomicExpression(token);
	}
	
	// atomicExpression -> literal
	private ParseNode parseAtomicExpression() {
		if(!startsAtomicExpression(nowReading)) {
			return syntaxErrorNode("atomic expression");
		}
		if(startsLiteral(nowReading)) {
			return parseLiteral();
		}
		else if(isLRB(nowReading)){
			readToken();
			ParseNode result = parseExpression();
			expect(Punctuator.RRB);
			return result;
		}
		else if(isLSB(nowReading)){
			Token exprListToken = nowReading;
			readToken();
			ParseNode result = parseExpression();
			if (nowReading.isLextant(Punctuator.PIPE)) {	// cast
				Token castToken = nowReading;
				readToken();
				Token typeToken = nowReading;
				if (!Keyword.isAType(typeToken.getLexeme())) {
					return syntaxErrorNode("atomic expression");
				}
				readToken();
				expect(Punctuator.RSB);
				return BinaryOperatorNode.withChildren(castToken, result, new TypeNode(typeToken));
			}
			else {	// exprList
				ExprListNode exprList = new ExprListNode(exprListToken);
				exprList.appendChild(result);
				while (nowReading.isLextant(Punctuator.SEPARATOR)) {
					readToken();
					ParseNode expr = parseExpression();
					exprList.appendChild(expr);
				}
				expect(Punctuator.RSB);
				return exprList;
			}
		}
		else {	// Keyword NEW
			Token newToken = nowReading;
			readToken();
			expect(Punctuator.LSB);
			if (!Keyword.isAType(nowReading.getLexeme())) {
				return syntaxErrorNode("atomic expression");
			}
			TypeNode typeNode = new TypeNode(nowReading);
			readToken();
			expect(Punctuator.RSB);
			expect(Punctuator.LRB);
			ParseNode expr = parseExpression();
			expect(Punctuator.RRB);
			NewArrayNode arrayNode = new NewArrayNode(newToken);
			arrayNode.appendChild(typeNode);
			arrayNode.appendChild(expr);
			return arrayNode;
		}
	}
	private boolean startsAtomicExpression(Token token) {
		return startsLiteral(token) || isLRB(token) || isLSB(token) || Keyword.forLexeme(token.getLexeme()) == Keyword.NEW;
	}
	
	// literal -> integer | float | boolean | char | string | identifier
	private ParseNode parseLiteral() {
		if(!startsLiteral(nowReading)) {
			return syntaxErrorNode("literal");
		}
		
		if(startsIntNumber(nowReading)) {
			return parseIntNumber();
		}
		if(startsIdentifier(nowReading)) {
			return parseIdentifier();
		}
		if(startsBooleanConstant(nowReading)) {
			return parseBooleanConstant();
		}
		if(startsFloat(nowReading)) {
			return parseFloat();
		}
		if(startsChar(nowReading)) {
			return parseChar();
		}
		if(startsString(nowReading)) {
			return parseString();
		}
		
		return syntaxErrorNode("literal");
	}
	public static boolean startsLiteral(Token token) {
		return startsIntNumber(token) || startsIdentifier(token) || startsBooleanConstant(token) || startsFloat(token) || startsChar(token) || startsString(token);
	}

	// number (terminal)
	private ParseNode parseIntNumber() {
		if(!startsIntNumber(nowReading)) {
			return syntaxErrorNode("integer constant");
		}
		readToken();
		return new IntegerConstantNode(previouslyRead);
	}
	
	private ParseNode parseFloat() {
		if(!startsFloat(nowReading)) {
			return syntaxErrorNode("floating constant");
		}
		readToken();
		return new FloatConstantNode(previouslyRead);
	}
	
	private ParseNode parseChar() {
		if(!startsChar(nowReading)) {
			return syntaxErrorNode("character constant");
		}
		readToken();
		return new CharConstantNode(previouslyRead);
	}
	
	private ParseNode parseString() {
		if(!startsString(nowReading)) {
			return syntaxErrorNode("string constant");
		}
		readToken();
		return new StringConstantNode(previouslyRead);
	}
	
	private static boolean startsIntNumber(Token token) {
		return token instanceof NumberToken;
	}
	
	private static boolean startsFloat(Token token) {
		return token instanceof FloatToken;
	}
	
	private static boolean startsChar(Token token) {
		return token instanceof CharToken;
	}
	
	private static boolean startsString(Token token) {
		return token instanceof StringToken;
	}
	
	private static boolean isLRB(Token token) {
		return token.isLextant(Punctuator.LRB);
	}
	
	private static boolean isLSB(Token token) {
		return token.isLextant(Punctuator.LSB);
	}

	// identifier (terminal)
	private ParseNode parseIdentifier() {	// returns an IdentifierNode, or a BinaryOperatorNode if expression is a[i] = n
		if(!startsIdentifier(nowReading)) {
			return syntaxErrorNode("identifier");
		}
		IdentifierNode idNode = new IdentifierNode(nowReading);
		readToken();
		if (!nowReading.isLextant(Punctuator.LSB)) return idNode;
		Token indexToken = nowReading;
		readToken();
		ParseNode expr = parseExpression();
		expect(Punctuator.RSB);
		return BinaryOperatorNode.withChildren(indexToken, idNode, expr);
	}
	private static boolean startsIdentifier(Token token) {
		return token instanceof IdentifierToken;
	}

	// boolean constant (terminal)
	private ParseNode parseBooleanConstant() {
		if(!startsBooleanConstant(nowReading)) {
			return syntaxErrorNode("boolean constant");
		}
		readToken();
		return new BooleanConstantNode(previouslyRead);
	}
	private static boolean startsBooleanConstant(Token token) {
		return token.isLextant(Keyword.TRUE, Keyword.FALSE);
	}

	private void readToken() {
		previouslyRead = nowReading;
		nowReading = scanner.next();
		while (nowReading.getClass() == CommentToken.class) { // skip comment tokens
			nowReading = scanner.next();
		}
	}	
	
	// if the current token is one of the given lextants, read the next token.
	// otherwise, give a syntax error and read next token (to avoid endless looping).
	private void expect(Lextant ...lextants ) {
		if(!nowReading.isLextant(lextants)) {
			syntaxError(nowReading, "expecting " + Arrays.toString(lextants));
		}
		readToken();
	}	
	private ErrorNode syntaxErrorNode(String expectedSymbol) {
		syntaxError(nowReading, "expecting " + expectedSymbol);
		ErrorNode errorNode = new ErrorNode(nowReading);
		readToken();
		return errorNode;
	}
	private void syntaxError(Token token, String errorDescription) {
		String message = "" + token.getLocation() + " " + errorDescription;
		error(message);
	}
	private void error(String message) {
		PikaLogger log = PikaLogger.getLogger("compiler.Parser");
		log.severe("syntax error: " + message);
	}	
}

