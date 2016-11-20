package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class ReleaseNode extends ParseNode {

	public ReleaseNode(Token token) {
		super(token);
	}
	public ReleaseNode(ParseNode node) {
		super(node);
	}
	
	public static ReleaseNode withChild(Token token, ParseNode child) {
		ReleaseNode node = new ReleaseNode(token);
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
