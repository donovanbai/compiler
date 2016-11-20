package asmCodeGenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.codeStorage.ASMOpcode;
import asmCodeGenerator.runtime.MemoryManager;
import asmCodeGenerator.runtime.RunTime;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;
import parseTree.*;
import parseTree.nodeTypes.AssignmentNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.BlockStmtNode;
import parseTree.nodeTypes.BooleanConstantNode;
import parseTree.nodeTypes.CharConstantNode;
import parseTree.nodeTypes.CloneNode;
import parseTree.nodeTypes.MainBlockNode;
import parseTree.nodeTypes.NewArrayNode;
import parseTree.nodeTypes.DeclarationNode;
import parseTree.nodeTypes.ExprListNode;
import parseTree.nodeTypes.FloatConstantNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.IfStmtNode;
import parseTree.nodeTypes.IntegerConstantNode;
import parseTree.nodeTypes.LengthNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.ProgramNode;
import parseTree.nodeTypes.ReleaseNode;
import parseTree.nodeTypes.SpaceNode;
import parseTree.nodeTypes.StringConstantNode;
import parseTree.nodeTypes.UnaryOperatorNode;
import parseTree.nodeTypes.WhileStmtNode;
import semanticAnalyzer.signatures.FunctionSignature;
import semanticAnalyzer.types.CompoundType;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import semanticAnalyzer.types.TypeLiteral;
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
		
		code.append(MemoryManager.codeForInitialization());
		code.append( RunTime.getEnvironment() );
		code.append( globalVariableBlockASM() );
		code.append( programASM() );
		code.append( MemoryManager.codeForAfterApplication() );
		
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
		ASMCodeFragment removeAddressCode(ParseNode node) {
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
			else if (node.getType() instanceof CompoundType) {
				code.add(LoadI);
			}
			else {
				assert false : "node " + node;
			}
			code.markAsValue();
		}
		private ASMOpcode opcodeForLoad(Type type) {
			
			if(type == PrimitiveType.INTEGER) {
				return LoadI;
			}	
			if(type == PrimitiveType.BOOLEAN) {
				return LoadC;
			}	
			if (type == PrimitiveType.FLOATING) {
				return LoadF;
			}
			if (type == PrimitiveType.CHARACTER) {
				return LoadC;
			}
			if (type instanceof CompoundType) {
				return LoadI;
			}
			assert false: "Type " + type + " unimplemented in opcodeForLoad()";
			return null;
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
			else if (node.child(1).getType() instanceof CompoundType) {
				ASMCodeFragment lvalue = removeAddressCode(node.child(0));	
				ASMCodeFragment rvalue = removeAddressCode(node.child(1));
				
				code.append(lvalue);
				code.append(rvalue);
				
				Type type = node.getType();
				code.add(opcodeForStore(type));
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
				code.append(removeAddressCode(node.child(0))); 	// a
				code.append(removeValueCode(node.child(1))); 	// a n d
				code.add(Memtop);
				code.add(PushI, 4);
				code.add(Subtract); 			// a n d m-4
				code.add(Exchange); 			// a n m-4 d
				code.add(StoreI); 				// a n
				code.add(Exchange); 			// n a
				code.add(Duplicate); 			// n a a
				code.add(PushI, 4); 			// n a a 4
				code.add(Add); 					// n a a+4
				code.add(Memtop);
				code.add(PushI, 4);
				code.add(Subtract);  			// n a a+4 m-4
				code.add(LoadI); 				// n a a+4 d
				code.add(StoreI); 				// n a
				code.add(Exchange); 			// a n
				code.add(StoreI); 				//
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
			if (type instanceof CompoundType) {
				return StoreI;
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
			else if (operator == Punctuator.RATIONALIZE) {
				visitRationalizeOperatorNode(node);
			}
			else if (operator == Punctuator.LSB) {
				visitIndexingOperatorNode(node);
			}
			else {	// +  -  *  /  &&  ||
				visitNormalBinaryOperatorNode(node);
			}
		}
		private void visitGreaterOperatorNode(BinaryOperatorNode node) {

			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			
			Labeller labeller = new Labeller("compare");

			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);
			
			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFPos, trueLabel);
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
			
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);
			
			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFNeg, trueLabel);
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
			
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);

			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFNeg, falseLabel);
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
			
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);

			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFPos, falseLabel);
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
			
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);

			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFZero, trueLabel);
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
			
			String trueLabel  = labeller.newLabel("true");
			String falseLabel = labeller.newLabel("false");
			String joinLabel  = labeller.newLabel("join");
			
			newValueCode(node);
			code.append(arg1);
			code.append(arg2);

			// check 1st operand for promotion to float
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				code.add(Exchange);
				code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
				code.add(ConvertF); 	// convert 1st operand to floating
				code.add(Memtop);
				code.add(PushI, node.child(1).getType().getSize());
				code.add(Subtract);
				turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
			}
			
			// check 2nd operand for promotion to float
			if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF); 	// convert 2nd operand to floating
			}
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(FSubtract);
			else code.add(Subtract);
			
			if (node.child(0).getPromotedType() == PrimitiveType.FLOATING) code.add(JumpFZero, falseLabel);
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
			
			// check operand for promotion to float or rat
			if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
				code.add(ConvertF);
			}
			else if (node.child(0).getType() != PrimitiveType.RATIONAL && node.child(0).getPromotedType() == PrimitiveType.RATIONAL) {
				code.add(PushI, 1);
			}
			
			Type type1 = node.child(0).getPromotedType();
			Type type2 = node.child(1).getType();
			if (type1 == PrimitiveType.RATIONAL && type2 == TypeLiteral.TYPE_INT) {
				code.add(Divide);
			}
			else if (type1 == PrimitiveType.RATIONAL && type2 == TypeLiteral.TYPE_FLOAT) {
				code.add(ConvertF);
				code.add(Exchange);
				code.add(ConvertF);
				code.add(Exchange);
				code.add(FDivide);
			}
			else if (type1 == PrimitiveType.INTEGER && type2 == TypeLiteral.TYPE_FLOAT) {
				code.add(ConvertF);
			}
			else if ((type1 == PrimitiveType.CHARACTER || type1 == PrimitiveType.INTEGER) && type2 == TypeLiteral.TYPE_RAT) {
				code.add(PushI, 1);
			}
			else if (type1 == PrimitiveType.FLOATING && type2 == TypeLiteral.TYPE_INT) {
				code.add(ConvertI);
			}
			else if (type1 == PrimitiveType.FLOATING && type2 == TypeLiteral.TYPE_RAT) {
				code.add(PushF, 223092870.0);
				code.add(FMultiply);
				code.add(ConvertI);
				code.add(Duplicate);
				code.add(Memtop);
				code.add(PushI, 4);
				code.add(Subtract);
				code.add(Exchange);
				code.add(StoreI);
				code.add(PushI, 223092870);
				code.add(Duplicate);
				code.add(Memtop);
				code.add(PushI, 8);
				code.add(Subtract);
				code.add(Exchange);
				code.add(StoreI);
				
				simplifyRational(12, 4, 8);
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
			code.add(FMultiply);
			code.add(ConvertI);
		}
		private void visitRationalizeOperatorNode(BinaryOperatorNode node) {			
			newValueCode(node);
			
			ASMCodeFragment frag1 = removeValueCode(node.child(0));
			ASMCodeFragment frag2 = removeValueCode(node.child(1));
			code.append(frag1);
			code.append(frag2);
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI);
			if (node.child(0).getType() == PrimitiveType.RATIONAL) { // convert to a floating 			
				code.add(ConvertF);
				code.add(Exchange);
				code.add(ConvertF);
				code.add(Exchange);
				code.add(FDivide);
			}
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI);
			code.add(ConvertF);
			code.add(FMultiply);
			code.add(ConvertI);
			code.add(Duplicate);
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI);
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI);
			
			simplifyRational(12, 8, 4);
		}
		private void visitIndexingOperatorNode(BinaryOperatorNode node) {
			// index of element = starting address + 16 + index*size of element
			newAddressCode(node);
			ASMCodeFragment arg1 = removeValueCode(node.child(0)); 	
			ASMCodeFragment arg2 = removeValueCode(node.child(1));	
			code.append(arg1); 		// a
			code.append(arg2); 		// a i
			// check if index is >= 0
			code.add(Duplicate); 	// a i i
			code.add(JumpNeg, RunTime.INVALID_INDEX_RUNTIME_ERROR);
			// check if index < length
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);		// a i m-4
			code.add(Exchange); 	// a m-4 i
			code.add(StoreI); 		// a
			code.add(Duplicate); 	// a a
			code.add(PushI, 12); 	// a a 12
			code.add(Add); 			// a a+12
			code.add(LoadI); 		// a length
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);		// a length m-4
			code.add(LoadI); 		// a length i
			code.add(Subtract); 	// a length-i
			code.add(Duplicate); 	// a length-i length-i
			code.add(JumpFalse, RunTime.INVALID_INDEX_RUNTIME_ERROR); 	// a length-i
			code.add(JumpNeg, RunTime.INVALID_INDEX_RUNTIME_ERROR);		// a
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);		// a m-4
			code.add(LoadI); 		// a i
			
			Type elementType = CompoundType.makeChildType((CompoundType)node.child(0).getType());
			code.add(PushI, elementType.getSize());
			code.add(Multiply);
			code.add(Add);
			code.add(PushI, 16);
			code.add(Add);
		}
		private void visitNormalBinaryOperatorNode(BinaryOperatorNode node) {	// +  -  *  /  &&  ||
			newValueCode(node);
			Lextant operator = node.getOperator();
			ASMCodeFragment arg1 = removeValueCode(node.child(0));
			ASMCodeFragment arg2 = removeValueCode(node.child(1));
			code.append(arg1);		// a, b
			code.append(arg2); 		// a, b, c, d
			if (node.child(0).getPromotedType() == PrimitiveType.RATIONAL) {		
				// check 1st operand for promotion
				if (node.child(0).getType() != PrimitiveType.RATIONAL) {
					code.add(Memtop);
					if (node.child(1).getType() != PrimitiveType.RATIONAL) {
						code.add(PushI, node.child(1).getType().getSize());
						code.add(Subtract);
						code.add(Exchange);
						code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
						code.add(PushI, 1); 	// add denominator to 1st operand
						code.add(Memtop);
						code.add(PushI, node.child(1).getType().getSize());
						code.add(Subtract);
						turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
					}
					else {
						code.add(PushI, 4);
						code.add(Subtract);
						code.add(Exchange);
						code.add(StoreI); 		// store denominator
						code.add(Memtop);
						code.add(PushI, 8);
						code.add(Subtract);
						code.add(Exchange);
						code.add(StoreI); 		// store numerator
						code.add(PushI, 1); 	// add denominator to 1st operand
						code.add(Memtop);
						code.add(PushI, 8);
						code.add(Subtract);
						code.add(LoadI); 		// load numerator back
						code.add(Memtop);
						code.add(PushI, 4);
						code.add(Subtract);
						code.add(LoadI); 		// load denominator back
					}		
				}
				// check 2nd operand for promotion
				if (node.child(1).getType() != PrimitiveType.RATIONAL) {
					code.add(PushI, 1); 	// add denominator to 2nd operand
				}
				
				if (operator == Punctuator.ADD || operator == Punctuator.SUBTRACT) {
					// a/b + c/d = (ad+cb)/bd			
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
				// check 1st operand for promotion to float
				if (node.child(0).getType() != PrimitiveType.FLOATING && node.child(0).getPromotedType() == PrimitiveType.FLOATING) {
					code.add(Memtop);
					code.add(PushI, node.child(1).getType().getSize());
					code.add(Subtract);
					code.add(Exchange);
					code.add(opcodeForStore(node.child(1).getType()));	// store 2nd operand
					code.add(ConvertF); 	// convert 1st operand to floating
					code.add(Memtop);
					code.add(PushI, node.child(1).getType().getSize());
					code.add(Subtract);
					turnAddressIntoValue(code, node.child(1));	// load 2nd operand back
				}
				
				// check 2nd operand for promotion to float
				if (node.child(1).getType() != PrimitiveType.FLOATING && node.child(1).getPromotedType() == PrimitiveType.FLOATING) {
					code.add(ConvertF); 	// convert 2nd operand to floating
				}
				
				Type type1 = node.child(0).getPromotedType();
				Type type2 = node.child(1).getPromotedType();
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

		public void visitLeave(ExprListNode node) {		// DOES NOT WORK FOR RATIONALS OR PROMOTIONS RIGHT NOW
			// store record on heap
			// type id(4), status(4), subtype size(4), length(4), elements(subtype size * length)

			newAddressCode(node);
			Type subtype = node.child(0).getType();
			int subtypeSize = subtype.getSize();
			int length = node.nChildren();
			int numBytes = 16 + subtypeSize * length;
			code.add(PushI, numBytes);									// numBytes
			code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE); 		// address of space (refer to a)
			code.add(Duplicate); 										// a a
			code.add(PushI, 7); 										// a a 7
			code.add(StoreI);	// store type id 						// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 4); 										// a a 4
			code.add(Add); 												// a a+4
			code.add(PushI, 0); 										// a a+4 0
			code.add(StoreC); 	// store immutability status			// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 5); 										// a a 5
			code.add(Add); 												// a a+5
			if (subtype instanceof CompoundType) code.add(PushI, 1);	// a a+5 1									
			else code.add(PushI, 0);									// a a+5 0
			code.add(StoreC); 	// store subtype-is-reference status	// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 6); 										// a a 6
			code.add(Add); 												// a a+6
			code.add(PushI, 0); 										// a a+6 0
			code.add(StoreC); 	// store is-deleted status				// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 7); 										// a a 7
			code.add(Add); 												// a a+7
			code.add(PushI, 0); 										// a a+7 0
			code.add(StoreC); 	// store is-permanent status			// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 8); 										// a a 8
			code.add(Add); 												// a a+8
			code.add(PushI, subtypeSize); 								// a a+8 s
			code.add(StoreI); 	// store subtype size					// a
			code.add(Duplicate); 										// a a
			code.add(PushI, 12); 										// a a 12
			code.add(Add); 												// a a+12
			code.add(PushI, length); 									// a a+12 l
			code.add(StoreI);	// store array length					// a
			if (subtype != PrimitiveType.RATIONAL) {
				for (int i = 0; i < node.nChildren(); i++) {
					code.add(Duplicate);									// a a
					code.add(PushI, 16 + i * subtypeSize); 					// a a offset
					code.add(Add); 											// a a+offset
					code.append(removeValueCode(node.child(i)));			// a a+offset arr[i]
					code.add(opcodeForStore(subtype));						// a
				}
			}
			else {
				for (int i = 0; i < node.nChildren(); i++) {
					code.add(Duplicate);									// a a
					code.add(PushI, 16 + i * subtypeSize); 					// a a offset
					code.add(Add); 											// a a+offset
					code.append(removeValueCode(node.child(i)));			// a a+offset1 n d
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract); 									// a a+offset1 n d m-4
					code.add(Exchange); 									// a a+offset1 n m-4 d
					code.add(StoreI); 										// a a+offset1 n
					code.add(StoreI); 										// a
					code.add(Duplicate); 									// a a
					code.add(PushI, 16 + i * subtypeSize + 4); 				// a a offset2
					code.add(Add); 											// a a+offset2
					code.add(Memtop);
					code.add(PushI, 4);
					code.add(Subtract);										// a a+offset2 m-4
					code.add(LoadI); 										// a a+offset2 d
					code.add(StoreI); 										// a
				}
			}
		}
		
		public void visitLeave(NewArrayNode node) {
			// store record on heap
			// type id(4), status(4), subtype size(4), length(4), elements(subtype size * length)
			
			newAddressCode(node);
			code.append(removeValueCode(node.child(1)));				// l
			// if n is negative, issue an error
			code.add(Duplicate); 										// l l
			code.add(Duplicate); 										// l l l
			code.add(JumpNeg, RunTime.NEGATIVE_LENGTH_RUNTIME_ERROR); 	// l l
			
			Type subtype = CompoundType.makeChildType((CompoundType) node.getType());
			int subtypeSize = subtype.getSize();
			code.add(PushI, subtypeSize);								// l l s
			code.add(Multiply); 										// l l*s
			code.add(PushI, 16); 										// l l*s 16
			code.add(Add); 												// l #bytes
			code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE); 		// l address of space (refer to as a)
			code.add(Duplicate); 										// l a a
			code.add(PushI, 7); 										// l a a 7
			code.add(StoreI);	// store type id 						// l a
			code.add(Duplicate); 										// l a a
			code.add(PushI, 4); 										// l a a 4
			code.add(Add); 												// l a a+4
			code.add(PushI, 0); 										// l a a+4 0
			code.add(StoreC); 	// store immutability status			// l a
			code.add(Duplicate); 										// l a a
			code.add(PushI, 5); 										// l a a 5
			code.add(Add); 												// l a a+5
			if (subtype instanceof CompoundType) code.add(PushI, 1);	// l a a+5 1									
			else code.add(PushI, 0);									// l a a+5 0
			code.add(StoreC); 	// store subtype-is-reference status	// l a
			code.add(Duplicate); 										// l a a
			code.add(PushI, 6); 										// l a a 6
			code.add(Add); 												// l a a+6
			code.add(PushI, 0); 										// l a a+6 0
			code.add(StoreC); 	// store is-deleted status				// l a
			code.add(Duplicate); 										// l a a
			code.add(PushI, 7); 										// l a a 7
			code.add(Add); 												// l a a+7
			code.add(PushI, 0); 										// l a a+7 0
			code.add(StoreC); 	// store is-permanent status			// l a
			code.add(Duplicate); 										// l a a
			code.add(PushI, 8); 										// l a a 8
			code.add(Add); 												// l a a+8
			code.add(PushI, subtypeSize); 								// l a a+8 s
			code.add(StoreI); 	// store subtype size					// l a
			code.add(Duplicate); 										// l a a
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract); 										// l a a m-4
			code.add(Exchange); 										// l a m-4 a
			code.add(StoreI); 											// l a
			code.add(PushI, 12); 										// l a 12
			code.add(Add); 												// l a+12
			code.add(Exchange); 										// a+12 l
			code.add(StoreI); 											//
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);											// m-4
			code.add(LoadI); 											// a
		}
		
		public void visitLeave(CloneNode node) {	// b := clone a.
			newAddressCode(node);
			code.append(removeValueCode(node.child(0)));		// a
			code.add(Duplicate); 								// a a
			code.add(Duplicate); 								// a a a
			code.add(PushI, 8); 								// a a a 8
			code.add(Add); 										// a a a+8
			code.add(LoadI); 									// a a size
			code.add(Duplicate); 								// a a size size
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract); 								// a a size size m-4
			code.add(Exchange); 								// a a size m-4 size
			code.add(StoreI); 									// a a size					m-4: size
			code.add(Exchange); 								// a size a
			code.add(PushI, 12); 								// a size a 12
			code.add(Add); 										// a size a+12
			code.add(LoadI); 									// a size length
			code.add(Duplicate); 								// a size length length
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract);									// a size length length m-8
			code.add(Exchange); 								// a size length m-8 length
			code.add(StoreI); 									// a size length			m-8: length
			code.add(Multiply); 								// a size*length
			code.add(PushI, 16); 								// a size*length 16
			code.add(Add); 										// a #bytes
			// allocate memory for clone
			code.add(Call, MemoryManager.MEM_MANAGER_ALLOCATE);	// a c(address of clone)
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a c m-12
			code.add(Exchange); 								// a m-12 c
			code.add(StoreI); 									// a						m-12: c
			code.add(Duplicate); 								// a a
			code.add(LoadI); 									// a typeid
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a typeid m-12
			code.add(LoadI); 									// a typeid c
			code.add(Exchange); 								// a c typeid
			// copy type id
			code.add(StoreI); 									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 4); 								// a a 4
			code.add(Add); 										// a a+4
			code.add(LoadC); 									// a immutabilityStatus
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a immutabilityStatus m-12
			code.add(LoadI); 									// a immutabilityStatus c
			code.add(PushI, 4); 								// a immutabilityStatus c 4
			code.add(Add); 										// a immutabilityStatus c+4
			code.add(Exchange); 								// a c+4 immutabilityStatus
			// copy immutability status
			code.add(StoreC); 									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 5); 								// a a 5
			code.add(Add); 										// a a+5
			code.add(LoadC); 									// a subtypeIsReferenceStatus
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a subtypeIsReferenceStatus m-12
			code.add(LoadI); 									// a subtypeIsReferenceStatus c
			code.add(PushI, 5); 								// a subtypeIsReferenceStatus c 5
			code.add(Add); 										// a subtypeIsReferenceStatus c+5
			code.add(Exchange); 								// a c+5 subtypeIsReferenceStatus
			// copy subtype-is-reference status
			code.add(StoreC); 									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 6); 								// a a 6
			code.add(Add); 										// a a+6
			code.add(LoadC); 									// a isDeletedStatus
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a isDeletedStatus m-12
			code.add(LoadI); 									// a isDeletedStatus c
			code.add(PushI, 6); 								// a isDeletedStatus c 6
			code.add(Add); 										// a isDeletedStatus c+6
			code.add(Exchange); 								// a c+6 isDeletedStatus
			// copy is-deleted status
			code.add(StoreC);									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 7); 								// a a 7
			code.add(Add); 										// a a+7
			code.add(LoadC); 									// a isPermanentStatus
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// a isPermanentStatus m-12
			code.add(LoadI); 									// a isPermanentStatus c
			code.add(PushI, 7); 								// a isPermanentStatus c 7
			code.add(Add); 										// a isPermanentStatus c+7
			code.add(Exchange); 								// a c+7 isPermanentStatus
			// copy is-permanent status
			code.add(StoreC);									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 8); 								// a a 8
			code.add(Add); 										// a a+8
			code.add(LoadI); 									// a size
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract);									// a size m-12
			code.add(LoadI); 									// a size c
			code.add(PushI, 8); 								// a size c 8
			code.add(Add); 										// a size c+8
			code.add(Exchange); 								// a c+8 size
			// copy size
			code.add(StoreI); 									// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 12); 								// a a 12
			code.add(Add); 										// a a+12
			code.add(LoadI); 									// a length
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract);									// a length m-12
			code.add(LoadI); 									// a length c
			code.add(PushI, 12); 								// a length c 12
			code.add(Add); 										// a length c+12
			code.add(Exchange); 								// a c+12 length
			// copy length
			code.add(StoreI);									// a
			
			// copy elements
			Labeller labeller = new Labeller("clone");
			String startLabel = labeller.newLabel("start");
			String joinLabel = labeller.newLabel("join");
			Type subtype = CompoundType.makeChildType((CompoundType) node.child(0).getType());
			
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract); 								// a m-8
			code.add(LoadI); 									// a length
			code.add(PushI, 1); 								// a length 1
			code.add(Subtract); 								// a length-1
			code.add(Exchange); 								// length-1 a
			code.add(Memtop);
			code.add(PushI, 16);
			code.add(Subtract); 								// length-1 a m-16
			code.add(Exchange); 								// length-1 m-16 a
			code.add(StoreI); 									// length-1				m-16: a
			
			code.add(Label, startLabel);
			code.add(Duplicate); 								// i i
			code.add(Duplicate); 								// i i i
			code.add(JumpNeg, joinLabel);						// i i
			code.add(Memtop);
			code.add(PushI, 16);
			code.add(Subtract); 								// i i m-16
			code.add(LoadI); 									// i i a
			code.add(Exchange); 								// i a i			
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);  								// i a i m-4
			code.add(LoadI); 									// i a i size
			code.add(Multiply); 								// i a i*size
			code.add(PushI, 16); 								// i a i*size 16
			code.add(Add); 										// i a offset
			code.add(Duplicate); 								// i a offset offset
			code.add(Memtop);
			code.add(PushI, 20);
			code.add(Subtract);	 								// i a offset offset m-20
			code.add(Exchange); 								// i a offset m-20 offset
			code.add(StoreI); 									// i a offset				m-20: offset
			code.add(Add); 										// i a+offset
			if(subtype != PrimitiveType.RATIONAL) {
				code.add(opcodeForLoad(subtype));  				// i e(element)
				code.add(Memtop);
				code.add(PushI, 12);
				code.add(Subtract); 							// i e m-12
				code.add(LoadI); 								// i e c
				code.add(Memtop);
				code.add(PushI, 20);
				code.add(Subtract); 							// i e c m-20
				code.add(LoadI); 								// i e c offset
				code.add(Add); 									// i e c+offset
				code.add(Exchange); 							// i c+offset e
				code.add(opcodeForStore(subtype)); 				// i
			}
			else {
				code.add(Duplicate); 							// i a+offset a+offset
				code.add(LoadI); 								// i a+offset n
				code.add(Memtop);
				code.add(PushI, 12);
				code.add(Subtract); 							// i a+offset n m-12
				code.add(LoadI); 								// i a+offset n c
				code.add(Memtop);
				code.add(PushI, 20);
				code.add(Subtract); 							// i a+offset n c m-20
				code.add(LoadI); 								// i a+offset n c offset
				code.add(Add); 									// i a+offset n c+offset
				code.add(Exchange); 							// i a+offset c+offset n
				code.add(StoreI); 								// i a+offset
				code.add(PushI, 4); 							// i a+offset 4
				code.add(Add); 									// i a+offset+4
				code.add(LoadI); 								// i d
				code.add(Memtop);
				code.add(PushI, 12);
				code.add(Subtract); 							// i d m-12
				code.add(LoadI); 								// i d c
				code.add(Memtop);
				code.add(PushI, 20);
				code.add(Subtract); 							// i d c m-20
				code.add(LoadI);								// i d c offset
				code.add(Add); 									// i d c+offset
				code.add(PushI, 4); 							// i d c+offset 4
				code.add(Add); 									// i d c+offset+4
				code.add(Exchange); 							// i c+offset+4 d
				code.add(StoreI); 								// i
			}
			code.add(PushI, 1); 							// i 1
			code.add(Subtract); 							// i-1
			code.add(Jump, startLabel);
			
			code.add(Label, joinLabel); 						// -1 -1
			code.add(Pop);
			code.add(Pop);
			code.add(Memtop);
			code.add(PushI, 12);
			code.add(Subtract); 								// m-12
			code.add(LoadI); 									// c
		}
		
		public void visitLeave(LengthNode node) {
			newValueCode(node);
			code.append(removeValueCode(node.child(0)));		// a
			code.add(PushI, 12); 								// a 12
			code.add(Add); 										// a+12
			code.add(LoadI); 									// length
		}
		public void visitLeave(ReleaseNode node) {	// only works if the subtype is not a reference type
			Labeller labeller = new Labeller("release");
			String joinLabel = labeller.newLabel("join");
			
			newVoidCode(node);
			code.append(removeValueCode(node.child(0)));  		// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 6); 								// a a 6
			code.add(Add); 										// a a+6
			code.add(LoadC); 									// a is-deleted-status
			code.add(JumpTrue, joinLabel);						// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 7); 								// a a 7
			code.add(Add); 										// a a+7
			code.add(LoadC); 									// a is-permanent-status
			code.add(JumpTrue, joinLabel); 						// a
			code.add(Duplicate); 								// a a
			code.add(PushI, 6); 								// a a 6
			code.add(Add); 										// a a+6
			code.add(PushI, 1); 								// a a+6 1
			code.add(StoreC); 									// a
			code.add(Call, MemoryManager.MEM_MANAGER_DEALLOCATE);
			code.add(PushI, 1); // this is because pop is next
			
			code.add(Label, joinLabel); 						// a | 1
			code.add(Pop);
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
