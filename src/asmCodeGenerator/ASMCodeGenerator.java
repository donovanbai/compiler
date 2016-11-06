package asmCodeGenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.BlockStmtNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStmtNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.UnaryOperatorNode;
import parseTree.nodeTypes.WhileStmtNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import symbolTable.Binding;
import symbolTable.MemoryLocation;
import symbolTable.Scope;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;

// do not call the code generator if any errors have occurred during analysis.
public class ASMCodeGenerator {
	ParseNode root;

	public static ASMCodeFragment generate(ParseNode syntaxTree) {
		ASMCodeGenerator codeGenerator = new ASMCodeGenerator(syntaxTree);
		return codeGenerator.makeASM();
	}
	public ASMCodeGenerator(ParseNode root) {
		super();
		this.root = root;
	}
	
	public ASMCodeFragment makeASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		
		code.append( RunTime.getEnvironment() );
		code.append( globalVariableBlockASM() );
		code.append( programASM() );
//		code.append( MemoryManager.codeForAfterApplication() );
		
		return code;
	}
	private ASMCodeFragment globalVariableBlockASM() {
		assert root.hasScope();
		Scope scope = root.getScope();
		int globalBlockSize = scope.getAllocatedSize();
		
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		code.add(DLabel, RunTime.GLOBAL_MEMORY_BLOCK);
		code.add(DataZ, globalBlockSize);
		return code;
	}
	private ASMCodeFragment programASM() {
		ASMCodeFragment code = new ASMCodeFragment(GENERATES_VOID);
		
		code.add(    Label, RunTime.MAIN_PROGRAM_LABEL);
		code.append( programCode());
		code.add(    Halt );
		
		return code;
	}
	private ASMCodeFragment programCode() {
		CodeVisitor visitor = new CodeVisitor();
		root.accept(visitor);
		return visitor.removeRootCode(root);
	}


	protected class CodeVisitor extends ParseNodeVisitor.Default {
		private Map<ParseNode, ASMCodeFragment> codeMap;
		ASMCodeFragment code;
		
		public CodeVisitor() {
			codeMap = new HashMap<ParseNode, ASMCodeFragment>();
		}


		////////////////////////////////////////////////////////////////////
        // Make the field "code" refer to a new fragment of different sorts.
		private void newAddressCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_ADDRESS);
			codeMap.put(node, code);
		}
		private void newValueCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VALUE);
			codeMap.put(node, code);
		}
		private void newVoidCode(ParseNode node) {
			code = new ASMCodeFragment(GENERATES_VOID);
			codeMap.put(node, code);
		}

	    ////////////////////////////////////////////////////////////////////
        // Get code from the map.
		private ASMCodeFragment getAndRemoveCode(ParseNode node) {
			ASMCodeFragment result = codeMap.get(node);
			codeMap.remove(result);
			return result;
		}
	    public  ASMCodeFragment removeRootCode(ParseNode tree) {
			return getAndRemoveCode(tree);
		}		
		ASMCodeFragment removeValueCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			makeFragmentValueCode(frag, node);
			return frag;
		}		
		private ASMCodeFragment removeAddressCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isAddress();
			return frag;
		}		
		ASMCodeFragment removeVoidCode(ParseNode node) {
			ASMCodeFragment frag = getAndRemoveCode(node);
			assert frag.isVoid();
			return frag;
		}
		
	    ////////////////////////////////////////////////////////////////////
        // convert code to value-generating code.
		private void makeFragmentValueCode(ASMCodeFragment code, ParseNode node) {
			assert !code.isVoid();
			
			if(code.isAddress()) {
				turnAddressIntoValue(code, node);
			}	
		}
		private void turnAddressIntoValue(ASMCodeFragment code, ParseNode node) {
			if(node.getType() == PrimitiveType.INTEGER) {
				code.add(LoadI);
			}	
			else if(node.getType() == PrimitiveType.BOOLEAN) {
				code.add(LoadC);
			}	
			else if (node.getType() == PrimitiveType.FLOATING) {
				code.add(LoadF);
			}
			else if (node.getType() == PrimitiveType.CHARACTER) {
				code.add(LoadC);
			}
			else if (node.getType() == PrimitiveType.STRING) {
				//code.add(LoadC);
			}
			/*else if (node.getType() == PrimitiveType.RATIONAL) {
				code.add(LoadI);
			}*/
			else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}
		
	    ////////////////////////////////////////////////////////////////////
        // ensures all types of ParseNode in given AST have at least a visitLeave	
		public void visitLeave(ParseNode node) {
			assert false : "node " + node + " not handled in ASMCodeGenerator";
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructs larger than statements
		public void visitLeave(ProgramNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		public void visitLeave(MainBlockNode node) {
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}

		///////////////////////////////////////////////////////////////////////////
		// statements, declarations, assignments

		public void visitLeave(PrintStatementNode node) {
			newVoidCode(node);
			new PrintStatementGenerator(code, this).generate(node);	
		}
		public void visit(NewlineNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.NEWLINE_PRINT_FORMAT);
			code.add(Printf);
		}
		public void visit(SpaceNode node) {
			newVoidCode(node);
			code.add(PushD, RunTime.SPACE_PRINT_FORMAT);
			code.add(Printf);
		}
		

		public void visitLeave(DeclarationNode node) {
			newVoidCode(node);
			if (node.child(1).getType() == PrimitiveType.STRING) {
				ASMCodeFragment rvalue = removeVoidCode(node.child(1));
				code.append(rvalue);
			}
			else if (node.child(1).getType() == PrimitiveType.RATIONAL) {
				appendAddressOfNumerator(code, (IdentifierNode)node.child(0));
				ASMCodeFragment rvalue = removeValueCode(node.child(1));
				code.append(rvalue);
				appendAddressOfDenominator(code, (IdentifierNode)node.child(0));
				code.add(Exchange);
				code.add(StoreI);
				code.add(StoreI);
			}
			else {
				ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
				ASMCodeFragment rvalue = removeValueCode(node.child(1));
				
				code.append(lvalue);
				code.append(rvalue);
				
				Type type = node.getType();
				code.add(opcodeForStore(type));
			}
		}
		
		public void visitLeave(AssignmentNode node){
			newVoidCode(node);
			if (node.child(1).getType() == PrimitiveType.STRING) {
				ASMCodeFragment rvalue = removeVoidCode(node.child(1));
				code.append(rvalue);
			}
			else if (node.child(1).getType() == PrimitiveType.RATIONAL) {
				appendAddressOfNumerator(code, (IdentifierNode)node.child(0));
				ASMCodeFragment rvalue = removeValueCode(node.child(1));
				code.append(rvalue);
				appendAddressOfDenominator(code, (IdentifierNode)node.child(0));
				code.add(Exchange);
				code.add(StoreI);
				code.add(StoreI);
			}
			else {
				ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
				ASMCodeFragment rvalue = removeValueCode(node.child(1));
				
				code.append(lvalue);
				code.append(rvalue);
				
				Type type = node.getType();
				code.add(opcodeForStore(type));
			}
		}
		public void appendAddressOfDenominator(ASMCodeFragment code, IdentifierNode node) {
			assert node.getType() == PrimitiveType.RATIONAL;
			MemoryLocation m = node.getBinding().getMemoryLocation();
			String baseAddress = m.getBaseAddress();
			int offset = m.getOffset();
			m.getAccessor().generateAddress(code, baseAddress, offset+4, "");
		}
		public void appendAddressOfNumerator(ASMCodeFragment code, IdentifierNode node) {
			assert node.getType() == PrimitiveType.RATIONAL;
			MemoryLocation m = node.getBinding().getMemoryLocation();
			String baseAddress = m.getBaseAddress();
			int offset = m.getOffset();
			m.getAccessor().generateAddress(code, baseAddress, offset, "");
		}
		
		public void visitLeave(BlockStmtNode node){
			newVoidCode(node);
			for(ParseNode child : node.getChildren()) {
				ASMCodeFragment childCode = removeVoidCode(child);
				code.append(childCode);
			}
		}
		
		public void visitLeave(IfStmtNode node) {
			newVoidCode(node);
			
			Labeller labeller = new Labeller("ifStmt");
			String joinLabel = labeller.newLabel("join");
			String elseLabel = labeller.newLabel("else");
			
			ASMCodeFragment conditionCode = removeValueCode(node.child(0));
			code.append(conditionCode);
			if (node.nChildren() == 2) {	// if there is no else block
				code.add(JumpFalse, joinLabel);
			}
			else {							// if there is an else block
				code.add(JumpFalse, elseLabel);
			}
			
			ASMCodeFragment thenCode = removeVoidCode(node.child(1));
			code.append(thenCode);
			
			if (node.nChildren() == 3) {	// if there is an else block
				code.add(Jump, joinLabel);
				code.add(Label, elseLabel);
				ASMCodeFragment elseCode = removeVoidCode(node.child(2));
				code.append(elseCode);
			}
			
			code.add(Label, joinLabel);
		}
		
		public void visitLeave(WhileStmtNode node) {
			newVoidCode(node);
			
			Labeller labeller = new Labeller("whileStmt");
			String startLabel = labeller.newLabel("start");
			String joinLabel = labeller.newLabel("join");
			
			code.add(Label, startLabel);
			ASMCodeFragment conditionCode = removeValueCode(node.child(0));
			code.append(conditionCode);
			code.add(JumpFalse, joinLabel);
			
			ASMCodeFragment doCode = removeVoidCode(node.child(1));
			code.append(doCode);
			code.add(Jump, startLabel);
			code.add(Label, joinLabel);
		}
		
		private ASMOpcode opcodeForStore(Type type) {
			if (type == PrimitiveType.INTEGER) {
				return StoreI;
			}
			if (type == PrimitiveType.BOOLEAN) {
				return StoreC;
			}
			if (type == PrimitiveType.FLOATING) {
				return StoreF;
			}
			if (type == PrimitiveType.CHARACTER) {
				return StoreC;
			}
			if (type == PrimitiveType.STRING) {
				return Nop;
			}
			assert false: "Type " + type + " unimplemented in opcodeForStore()";
			return null;
		}


		///////////////////////////////////////////////////////////////////////////
		// expressions
		public void visitLeave(BinaryOperatorNode node) {
			Lextant operator = node.getOperator();

			if(operator == Punctuator.GREATER) {
				visitGreaterOperatorNode(node);
			}
			else if (operator == Punctuator.LESS) {
				visitLessOperatorNode(node);
			}
			else if (operator == Punctuator.GREATER_OR_EQ) {
				visitGreaterOrEqOperatorNode(node);
			}
			else if (operator == Punctuator.LESS_OR_EQ) {
				visitLessOrEqOperatorNode(node);
			}
			else if (operator == Punctuator.EQUAL) {
				visitEqualOperatorNode(node);
			}
			else if (operator == Punctuator.NOT_EQUAL) {
				visitNotEqualOperatorNode(node);
			}
			else if (operator == Punctuator.PIPE) {
				visitPipeOperatorNode(node);
			}
			else if (operator == Punctuator.OVER) {
				visitOverOperatorNode(node);
			}
			else if (operator == Punctuator.EXPRESS_OVER) {
				visitExpressOverOperatorNode(node);
			}
			else {	// +  -  *  /  &&  ||
				visitNormalBinaryOperatorNode(node);
			}
		}
		private void visitGreaterOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFPos, trueLabel);
			else code.add(JumpPos, trueLabel);
			
			code.add(Jump, falseLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitLessOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFNeg, trueLabel);
			else code.add(JumpNeg, trueLabel);
			
			code.add(Jump, falseLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitGreaterOrEqOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFNeg, falseLabel);
			else code.add(JumpNeg, falseLabel);
			
			code.add(Jump, trueLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitLessOrEqOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFPos, falseLabel);
			else code.add(JumpPos, falseLabel);
			
			code.add(Jump, trueLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitEqualOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String startLabel = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, startLabel);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFZero, trueLabel);
			else code.add(JumpFalse, trueLabel);
			
			code.add(Jump, falseLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitNotEqualOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");
			
			String arg1Label = labeller.newLabel("arg1");
			String arg2Label  = labeller.newLabel("arg2");
			String subLabel   = labeller.newLabel("sub");
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.add(Label, arg1Label);
			code.append(arg1);
			code.add(Label, arg2Label);
			code.append(arg2);
			code.add(Label, subLabel);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getType() == PrimitiveType.FLOATING) code.add(JumpFZero, falseLabel);
			else code.add(JumpFalse, falseLabel);
			
			code.add(Jump, trueLabel);

			code.add(Label, trueLabel);
			code.add(PushI, 1);
			code.add(Jump, joinLabel);
			code.add(Label, falseLabel);
			code.add(PushI, 0);
			code.add(Jump, joinLabel);
			code.add(Label, joinLabel);
		}
		private void visitPipeOperatorNode(BinaryOperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg = removeValueCode(node.child(0));
			code.append(arg);
			Type type1 = node.child(0).getType();
			Type type2 = node.child(1).getType();
			if (type1 == PrimitiveType.RATIONAL && type2 == PrimitiveType.TYPE_INT) {
				code.add(Divide);
			}
			else if (type1 == PrimitiveType.RATIONAL && type2 == PrimitiveType.TYPE_FLOAT) {
				code.add(ConvertF);
				code.add(Exchange);
				code.add(ConvertF);
				code.add(Exchange);
				code.add(FDivide);
			}
			else if (type1 == PrimitiveType.INTEGER && type2 == PrimitiveType.TYPE_FLOAT) {
				code.add(ConvertF);
			}
			else if ((type1 == PrimitiveType.CHARACTER || type1 == PrimitiveType.INTEGER) && type2 == PrimitiveType.TYPE_RAT) {
				code.add(PushI, 1);
			}
			else if (type1 == PrimitiveType.FLOATING && type2 == PrimitiveType.TYPE_INT) {
				code.add(ConvertI);
			}
		}
		private void visitOverOperatorNode(BinaryOperatorNode node) {			
			newValueCode(node);
			
			ASMCodeFragment numerator = removeValueCode(node.child(0));
			ASMCodeFragment denominator = removeValueCode(node.child(1));
			code.append(numerator);			
			code.append(denominator);		// n, d
			code.add(Duplicate);
			code.add(JumpFalse, RunTime.ZERO_DENOMINATOR_RUNTIME_ERROR);
			code.add(Duplicate);			// n, d, d
			code.add(Memtop);
			code.add(PushI, 4);								
			code.add(Subtract);				// n, d, d, m-4
			code.add(Exchange); 			// n, d, m-4, d
			code.add(StoreI); 				// n, d					m-4: d
			code.add(Exchange);				// d, n
			code.add(Duplicate); 			// d, n, n
			code.add(Memtop);
			code.add(PushI, 8);								
			code.add(Subtract);				// d, n, n, m-8
			code.add(Exchange);				// d, n, m-8, n
			code.add(StoreI);				// d, n					m-8: n
			
			simplifyRational(12, 8, 4);
		}
		private void visitExpressOverOperatorNode(BinaryOperatorNode node) {
			newValueCode(node);		
			ASMCodeFragment frag1 = removeValueCode(node.child(0));
			ASMCodeFragment frag2 = removeValueCode(node.child(1));
			code.append(frag1);
			code.append(frag2);
			if (node.child(0).getType() == PrimitiveType.RATIONAL) { // convert to a floating 
				code.add(Memtop);
				code.add(PushI, 4);
				code.add(Subtract);
				code.add(Exchange);
				code.add(StoreI);
				code.add(ConvertF);
				code.add(Exchange);
				code.add(ConvertF);
				code.add(Exchange);
				code.add(FDivide);
				code.add(Memtop);
				code.add(PushI, 4);
				code.add(Subtract);
				code.add(LoadI);
			}
			code.add(ConvertF);
		//code.add(PStack);
			code.add(FMultiply);
		//code.add(PStack);
			code.add(ConvertI);
		}
		private void visitNormalBinaryOperatorNode(BinaryOperatorNode node) {	// +  -  *  /  &&  ||
			newValueCode(node);
			Lextant operator = node.getOperator();
			if (node.child(0).getType() == PrimitiveType.RATIONAL) {			
				if (operator == Punctuator.ADD || operator == Punctuator.SUBTRACT) {
					// a/b + c/d = (ad+cb)/bd
					
					ASMCodeFragment arg1 = removeValueCode(node.child(0));
					ASMCodeFragment arg2 = removeValueCode(node.child(1));
					code.append(arg1);		// a, b
					code.append(arg2); 		// a, b, c, d
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);		// a, b, c, d, m-4
					code.add(Exchange); 	// a, b, c, m-4, d
					code.add(StoreI); 		// a, b, c				m-4: d
					code.add(Memtop);
					code.add(PushI, 8);
					code.add(Subtract);		// a, b, c, m-8
					code.add(Exchange); 	// a, b, m-8, c
					code.add(StoreI);		// a, b					m-8: c
					code.add(Memtop);
					code.add(PushI, 12);
					code.add(Subtract);		// a, b, m-12
					code.add(Exchange); 	// a, m-12, b
					code.add(StoreI);		// a					m-12: b
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);		// a, m-4
					code.add(LoadI); 		// a, d
					code.add(Multiply); 	// ad
					code.add(Memtop);
					code.add(PushI, 8);
					code.add(Subtract);		// ad, m-8
					code.add(LoadI); 		// ad, c
					code.add(Memtop);
					code.add(PushI, 12);
					code.add(Subtract);		// ad, c, m-12
					code.add(LoadI); 		// ad, c, b
					code.add(Multiply);		// ad, cb
					if (operator == Punctuator.ADD) code.add(Add);
					else code.add(Subtract);
					code.add(Memtop);
					code.add(PushI, 12);
					code.add(Subtract);		// ad+cb, m-12
					code.add(LoadI); 		// ad+cb, b
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);		// ad+cb, b, m-4
					code.add(LoadI); 		// ad+cb, b, d
					code.add(Multiply); 	// ad+cb, bd
					code.add(Duplicate); 	// ad+cb, bd, bd
					code.add(Memtop);
					code.add(PushI, 16);
					code.add(Subtract);		// ad+cb, bd, bd, m-16
					code.add(Exchange); 	// ad+cb, bd, m-16, bd
					code.add(StoreI); 		// ad+cb, bd				m-16: bd
					code.add(Exchange); 	// bd, ad+cb
					code.add(Duplicate); 	// bd, ad+cb, ad+cb
					code.add(Memtop);
					code.add(PushI, 20);
					code.add(Subtract);
					code.add(Exchange);
					code.add(StoreI); 		// bd, ad+cb				m-20: ad+cb
					
					simplifyRational(24, 20, 16);
				}
				else {	// if dividing, just swap n2 and d2 before multiplying
					ASMCodeFragment arg1 = removeValueCode(node.child(0));
					ASMCodeFragment arg2 = removeValueCode(node.child(1));
					code.append(arg1);		// n1, d1
					code.append(arg2); 		// n1, d1, n2, d2
					if (operator == Punctuator.DIVIDE) code.add(Exchange);
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);		// n1, d1, n2, d2, m-4
					code.add(Exchange); 	// n1, d1, n2, m-4, d2
					code.add(StoreI); 		// n1, d1, n2				m-4: d2
					code.add(Exchange); 	// n1, n2, d1
					code.add(Memtop);
					code.add(PushI, 8);
					code.add(Subtract);		// n1, n2, d1, m-8
					code.add(Exchange); 	// n1, n2, m-8, d1
					code.add(StoreI); 		// n1, n2					m-8: d1
					code.add(Multiply); 	// N
					code.add(Memtop);
					code.add(PushI, 8);
					code.add(Subtract);		// N, m-8
					code.add(LoadI); 		// N, d1
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);		// N, d1, m-4
					code.add(LoadI);		// N, d1, d2
					code.add(Multiply); 	// N, D
					code.add(Duplicate); 	// N, D, D
					code.add(Memtop);
					code.add(PushI, 12);
					code.add(Subtract);		// N, D, D, m-12
					code.add(Exchange); 	// N, D, m-12, D
					code.add(StoreI); 		// N, D					m-12: D
					code.add(Exchange); 	// D, N
					code.add(Duplicate); 	// D, N, N
					code.add(Memtop);
					code.add(PushI, 16);
					code.add(Subtract);		// D, N, N, m-16
					code.add(Exchange);		// D, N, m-16, N
					code.add(StoreI);		// D, N					m-16: N
					
					simplifyRational(20, 16, 12);
				}
			}
			else {
				ASMCodeFragment arg1 = removeValueCode(node.child(0));
				ASMCodeFragment arg2 = removeValueCode(node.child(1));			
				code.append(arg1);
				code.append(arg2);
				Type type1 = node.child(0).getType();
				Type type2 = node.child(1).getType();
				if (operator == Punctuator.DIVIDE && type1 == PrimitiveType.INTEGER) {
					code.add(Duplicate);
					code.add(JumpFalse, RunTime.INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
				}
				else if (operator == Punctuator.DIVIDE && type1 == PrimitiveType.FLOATING) {
					code.add(Duplicate);
					code.add(JumpFZero, RunTime.FLOATING_DIVIDE_BY_ZERO_RUNTIME_ERROR);
				}
							
				List<Type> childTypes = Arrays.asList(type1, type2);
				ASMOpcode opcode = opcodeForOperator(node.getOperator(), childTypes);
				code.add(opcode);							// type-dependent! (opcode is different for floats and for ints)
			}
		}
		private ASMOpcode opcodeForOperator(Lextant lextant, List<Type >types) {
			assert(lextant instanceof Punctuator);
			FunctionSignature signature = FunctionSignature.signatureOf(lextant, types);
			return (ASMOpcode)signature.getVariant();
		}
		
		public void visitLeave(UnaryOperatorNode node) {
			Lextant operator = node.getOperator();

			if(operator == Punctuator.NOT) {
				visitNotOperatorNode(node);
			}
		}
		
		private void visitNotOperatorNode(UnaryOperatorNode node) {
			newValueCode(node);
			ASMCodeFragment arg = removeValueCode(node.child(0));			
			code.append(arg);			
			code.add(ASMOpcode.BNegate);	
		}

		///////////////////////////////////////////////////////////////////////////
		// leaf nodes (ErrorNode not necessary)
		public void visit(BooleanConstantNode node) {
			newValueCode(node);
			code.add(PushI, node.getValue() ? 1 : 0);
		}
		public void visit(IdentifierNode node) {
			newAddressCode(node);
			Binding binding = node.getBinding();
			
			binding.generateAddress(code);
		}		
		public void visit(IntegerConstantNode node) {
			newValueCode(node);
			
			code.add(PushI, node.getValue());
		}
		public void visit(FloatConstantNode node) {
			newValueCode(node);
			
			code.add(PushF, node.getValue());
		}
		public void visit(CharConstantNode node) {
			newValueCode(node);
			
			code.add(PushI, node.getValue());
		}
		public void visit(StringConstantNode node) {
			newVoidCode(node);
			
			if (node.getParent() instanceof DeclarationNode) {
				DeclarationNode parent = (DeclarationNode)node.getParent();
				code.add(DLabel, parent.child(0).getToken().getLexeme());
			}
			else {
				code.add(DLabel, "str");
			}
			for (int i = 0; i < node.getValue().length(); i++) {
				int asciiVal = node.getValue().charAt(i);
				code.add(DataC, asciiVal);
			}
			code.add(DataC, 0);
		}
		private void simplifyRational(int tempOffset, int nOffset, int dOffset) {
			// simplifies numerator and denominator already on stack
		
			Labeller labeller = new Labeller("gcd");					
			String startLabel = labeller.newLabel("start");
			String falseLabel = labeller.newLabel("false");
			
			code.add(Label, startLabel);
			code.add(Duplicate);
			code.add(Memtop);
			code.add(PushI, tempOffset);								
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI);
			code.add(Duplicate);
			code.add(JumpFalse, falseLabel);
			code.add(Exchange);
			code.add(Memtop);
			code.add(PushI, tempOffset);
			code.add(Subtract);
			code.add(LoadI);
			code.add(Remainder);
			code.add(Jump, startLabel);
			
			code.add(Label, falseLabel);
			code.add(Pop);			// gcd
			code.add(Duplicate); 	// gcd, gcd
			code.add(Memtop);
			code.add(PushI, tempOffset);								
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI); 		// gcd					m-12: gcd
			code.add(Memtop);
			code.add(PushI, nOffset);								
			code.add(Subtract);
			code.add(LoadI); 		// gcd, n
			code.add(Exchange);		// n, gcd
			code.add(Divide); 		// N
			code.add(Memtop);
			code.add(PushI, dOffset);								
			code.add(Subtract);
			code.add(LoadI); 		// N, d
			code.add(Memtop);
			code.add(PushI, tempOffset);								
			code.add(Subtract);
			code.add(LoadI);		// N, d, gcd
			code.add(Divide); 		// N, D
		}
	}

}
