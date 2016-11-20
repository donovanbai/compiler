package parseTree;

import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.BlockStmtNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.CloneNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.NewArrayNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ErrorNode;
import parseTree.nodeTypes.ExprListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStmtNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LengthNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.TypeNode;
import parseTree.nodeTypes.UnaryOperatorNode;
import parseTree.nodeTypes.WhileStmtNode;

// Visitor pattern with pre- and post-order visits
public interface ParseNodeVisitor {
	
	// non-leaf nodes: visitEnter and visitLeave
	void visitEnter(BinaryOperatorNode node);
	void visitLeave(BinaryOperatorNode node);
	
	void visitEnter(MainBlockNode node);
	void visitLeave(MainBlockNode node);

	void visitEnter(DeclarationNode node);
	void visitLeave(DeclarationNode node);

	void visitEnter(AssignmentNode node);
	void visitLeave(AssignmentNode node);
	
	void visitEnter(ParseNode node);
	void visitLeave(ParseNode node);
	
	void visitEnter(PrintStatementNode node);
	void visitLeave(PrintStatementNode node);
	
	void visitEnter(ProgramNode node);
	void visitLeave(ProgramNode node);
	
	void visitEnter(BlockStmtNode node);
	void visitLeave(BlockStmtNode node);
	
	void visitEnter(UnaryOperatorNode node);
	void visitLeave(UnaryOperatorNode node);
	
	void visitEnter(IfStmtNode node);
	void visitLeave(IfStmtNode node);
	
	void visitEnter(WhileStmtNode node);
	void visitLeave(WhileStmtNode node);
	
	void visitEnter(ExprListNode node);
	void visitLeave(ExprListNode node);
	
	void visitEnter(NewArrayNode node);
	void visitLeave(NewArrayNode node);
	
	void visitEnter(CloneNode node);
	void visitLeave(CloneNode node);
	
	void visitEnter(LengthNode node);
	void visitLeave(LengthNode node);

	// leaf nodes: visitLeaf only
	void visit(BooleanConstantNode node);
	void visit(ErrorNode node);
	void visit(IdentifierNode node);
	void visit(IntegerConstantNode node);
	void visit(NewlineNode node);
	void visit(SpaceNode node);
	void visit(FloatConstantNode node);
	void visit(CharConstantNode node);
	void visit(StringConstantNode node);
	void visit(TypeNode node);
	
	public static class Default implements ParseNodeVisitor
	{
		public void defaultVisit(ParseNode node) {	}
		public void defaultVisitEnter(ParseNode node) {
			defaultVisit(node);
		}
		public void defaultVisitLeave(ParseNode node) {
			defaultVisit(node);
		}		
		public void defaultVisitForLeaf(ParseNode node) {
			defaultVisit(node);
		}
		
		public void visitEnter(BinaryOperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(BinaryOperatorNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(DeclarationNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(DeclarationNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(AssignmentNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(AssignmentNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(MainBlockNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(MainBlockNode node) {
			defaultVisitLeave(node);
		}				
		public void visitEnter(ParseNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ParseNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(PrintStatementNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(PrintStatementNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ProgramNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ProgramNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(BlockStmtNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(BlockStmtNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(UnaryOperatorNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(UnaryOperatorNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(IfStmtNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(IfStmtNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(WhileStmtNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(WhileStmtNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(ExprListNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(ExprListNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(NewArrayNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(NewArrayNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(CloneNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(CloneNode node) {
			defaultVisitLeave(node);
		}
		public void visitEnter(LengthNode node) {
			defaultVisitEnter(node);
		}
		public void visitLeave(LengthNode node) {
			defaultVisitLeave(node);
		}

		public void visit(BooleanConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(ErrorNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IdentifierNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(IntegerConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(NewlineNode node) {
			defaultVisitForLeaf(node);
		}	
		public void visit(SpaceNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(FloatConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(CharConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(StringConstantNode node) {
			defaultVisitForLeaf(node);
		}
		public void visit(TypeNode node) {
			defaultVisitForLeaf(node);
		}
	}
}
