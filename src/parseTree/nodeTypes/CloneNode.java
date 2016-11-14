package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class CloneNode extends ParseNode {

	public CloneNode(Token token) {
		super(token);
	}
	public CloneNode(ParseNode node) {
		super(node);
	}
	
	public static CloneNode withChild(Token token, ParseNode child) {
		CloneNode node = new CloneNode(token);
		node.appendChild(child);
		return node;
	}
	
	////////////////////////////////////////////////////////////
	// no attributes

	
	///////////////////////////////////////////////////////////
	// boilerplate for visitors
	
	public void accept(ParseNodeVisitor visitor) {
		visitor.visitEnter(this);
		visitChildren(visitor);
		visitor.visitLeave(this);
	}
}
