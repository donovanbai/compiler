package semanticAnalyzer;

import java.util.Arrays;
import java.util.List;

import lexicalAnalyzer.Lextant;
import logging.PikaLogger;
import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.BlockStmtNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStmtNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeBoolNode;
import parseTree.nodeTypes.TypeCharNode;
import parseTree.nodeTypes.TypeFloatNode;
import parseTree.nodeTypes.TypeIntNode;
import parseTree.nodeTypes.TypeRatNode;
import parseTree.nodeTypes.TypeStringNode;
import parseTree.nodeTypes.UnaryOperatorNode;
import parseTree.nodeTypes.WhileStmtNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.Scope;
import symbolTable.SymbolTable;
import tokens.Token;

class SemanticAnalysisVisitor extends ParseNodeVisitor.Default {
	@Override
	public void visitLeave(ParseNode node) {
		throw new RuntimeException("Node class unimplemented in SemanticAnalysisVisitor: " + node.getClass());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructs larger than statements
	@Override
	public void visitEnter(ProgramNode node) {
		enterProgramScope(node);
	}
	public void visitLeave(ProgramNode node) {
		leaveScope(node);
	}
	public void visitEnter(MainBlockNode node) {
	}
	public void visitLeave(MainBlockNode node) {
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// helper methods for scoping.
	private void enterProgramScope(ParseNode node) {
		Scope scope = Scope.createProgramScope();
		node.setScope(scope);
	}	
	private void enterSubscope(ParseNode node) {
		Scope baseScope = node.getLocalScope();
		Scope scope = baseScope.createSubscope();
		node.setScope(scope);
	}		
	private void leaveScope(ParseNode node) {
		node.getScope().leave();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// statements, declarations, assignments
	@Override
	public void visitLeave(PrintStatementNode node) {
	}
	@Override
	public void visitLeave(DeclarationNode node) {
		IdentifierNode identifier = (IdentifierNode) node.child(0);
		ParseNode initializer = node.child(1);
		
		Type declarationType = initializer.getType();
		node.setType(declarationType);
		
		identifier.setType(declarationType);
		String constOrVar = node.getToken().getLexeme();
		if (constOrVar.equals("const")) addBinding(identifier, declarationType, false);
		else addBinding(identifier, declarationType, true);
	}
	@Override
	public void visitLeave(AssignmentNode node) {
		// check if identifier is mutable
		IdentifierNode identifierNode = (IdentifierNode) node.child(0);
		String identifier = identifierNode.getToken().getLexeme();

		boolean foundIdentifier = false;
		boolean isMutable = false;
		ParseNode current = node;
		while (current != null) {
			SymbolTable table = current.getLocalScope().getSymbolTable();
			if (table.containsKey(identifier)) {
				foundIdentifier = true;
				Binding binding = table.lookup(identifier);
				if (binding.isMutable()) isMutable = true;
				break;
			}
			current = current.getParent();
		}
		
		if (!foundIdentifier) {
			logError("undeclared identifier at " + node.getToken().getLocation());
		}
		else if (!isMutable) {
			logError("assignment statement contains an immutable identifier at " + node.getToken().getLocation());
		}
		else {
			ParseNode initializer = node.child(1);
			// check if types match
			Type identifierType = identifierNode.getType();
			Type initializerType = initializer.getType();
			if (identifierType != initializerType) {
				logError("bad assignment: types don't match at " + node.getToken().getLocation());
			}
			else {
				node.setType(initializerType);
				
			}
		}
	}
	@Override
	public void visitEnter(BlockStmtNode node) {
		enterSubscope(node);
	}
	@Override
	public void visitLeave(BlockStmtNode node) {
		leaveScope(node);
	}
	@Override
	public void visitEnter(IfStmtNode node) {
		
	}
	@Override
	public void visitLeave(IfStmtNode node) {
		
	}
	@Override
	public void visitEnter(WhileStmtNode node) {
		
	}
	@Override
	public void visitLeave(WhileStmtNode node) {
		
	}

	///////////////////////////////////////////////////////////////////////////
	// expressions
	@Override
	public void visitLeave(BinaryOperatorNode node) {
		assert node.nChildren() == 2;
		ParseNode left  = node.child(0);
		ParseNode right = node.child(1);
		List<Type> childTypes = Arrays.asList(left.getType(), right.getType());
		
		Lextant operator = node.getOperator();
		FunctionSignature signature = FunctionSignature.signatureOf(operator, childTypes);
		
		if(signature.accepts(childTypes)) {
				node.setType(signature.resultType());
		}
		else {
			typeCheckError(node, childTypes);
			node.setType(PrimitiveType.ERROR);
		}
	}
	
	@Override
	public void visitLeave(UnaryOperatorNode node) {
		assert node.nChildren() == 1;
		ParseNode child  = node.child(0);
		List<Type> childTypes = Arrays.asList(child.getType());
		
		Lextant operator = node.getOperator();
		FunctionSignature signature = FunctionSignature.signatureOf(operator, childTypes);
		
		if(signature.accepts(childTypes)) {
			node.setType(signature.resultType());
		}
		else {
			typeCheckError(node, childTypes);
			node.setType(PrimitiveType.ERROR);
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// simple leaf nodes
	@Override
	public void visit(BooleanConstantNode node) {
		node.setType(PrimitiveType.BOOLEAN);
	}
	@Override
	public void visit(ErrorNode node) {
		node.setType(PrimitiveType.ERROR);
	}
	@Override
	public void visit(IntegerConstantNode node) {
		node.setType(PrimitiveType.INTEGER);
	}
	@Override
	public void visit(FloatConstantNode node) {
		node.setType(PrimitiveType.FLOATING);
	}
	@Override
	public void visit(CharConstantNode node) {
		node.setType(PrimitiveType.CHARACTER);
	}
	@Override
	public void visit(StringConstantNode node) {
		node.setType(PrimitiveType.STRING);
	}
	@Override
	public void visit(TypeBoolNode node){
		node.setType(PrimitiveType.TYPE_BOOL);
	}
	@Override
	public void visit(TypeCharNode node){
		node.setType(PrimitiveType.TYPE_CHAR);
	}
	@Override
	public void visit(TypeFloatNode node){
		node.setType(PrimitiveType.TYPE_FLOAT);
	}
	@Override
	public void visit(TypeIntNode node){
		node.setType(PrimitiveType.TYPE_INT);
	}
	@Override
	public void visit(TypeStringNode node){
		node.setType(PrimitiveType.TYPE_STRING);
	}
	@Override
	public void visit(TypeRatNode node){
		node.setType(PrimitiveType.TYPE_RAT);
	}

	@Override
	public void visit(NewlineNode node) {
	}
	@Override
	public void visit(SpaceNode node) {
	}
	///////////////////////////////////////////////////////////////////////////
	// IdentifierNodes, with helper methods
	@Override
	public void visit(IdentifierNode node) {
		if(!isBeingDeclared(node)) {		
			Binding binding = node.findVariableBinding();
			
			node.setType(binding.getType());
			node.setBinding(binding);
		}
		// else parent DeclarationNode does the processing.
	}
	private boolean isBeingDeclared(IdentifierNode node) {
		ParseNode parent = node.getParent();
		return (parent instanceof DeclarationNode) && (node == parent.child(0));
	}
	private void addBinding(IdentifierNode identifierNode, Type type, boolean mutable) {
		Scope scope = identifierNode.getLocalScope();
		Binding binding = scope.createBinding(identifierNode, type, mutable);
		identifierNode.setBinding(binding);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// error logging/printing

	private void typeCheckError(ParseNode node, List<Type> operandTypes) {
		Token token = node.getToken();
		
		logError("operator " + token.getLexeme() + " not defined for types " 
				 + operandTypes  + " at " + token.getLocation());	
	}
	private void logError(String message) {
		PikaLogger log = PikaLogger.getLogger("compiler.semanticAnalyzer");
		log.severe(message);
	}
}