package parseTree.nodeTypes;

import parseTree.ParseNode;
import parseTree.ParseNodeVisitor;
import tokens.Token;

public class LengthNode extends ParseNode {

	public LengthNode(Token token) {
		super(token);
	}
	public LengthNode(ParseNode node) {
		super(node);
	}
	
	public static LengthNode withChild(Token token, ParseNode child) {
		LengthNode node = new LengthNode(token);
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
