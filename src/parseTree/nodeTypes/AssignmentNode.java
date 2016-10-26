package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.IdentifierToken;
import tokens.Token;

public class AssignmentNode extends ParseNode {

	public AssignmentNode(Token token) {
		super(token);
		assert(token instanceof IdentifierToken);
	}

	public AssignmentNode(ParseNode node) {
		super(node);
	}
	
	////////////////////////////////////////////////////////////
	// convenience factory
	
	public static AssignmentNode withChildren(Token token, ParseNode identifier, ParseNode initializer) {
		AssignmentNode node = new AssignmentNode(token);
		node.appendChild(identifier);
		node.appendChild(initializer);
		return node;
	}
	
	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
			
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
