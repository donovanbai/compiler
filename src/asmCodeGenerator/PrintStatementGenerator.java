package asmCodeGenerator;

import static asmCodeGenerator.codeStorage.ASMOpcode.*;

import parseTree.ParseNode;
import parseTree.nodeTypes.BinaryOperatorNode;
import parseTree.nodeTypes.IdentifierNode;
import parseTree.nodeTypes.NewlineNode;
import parseTree.nodeTypes.PrintStatementNode;
import parseTree.nodeTypes.SpaceNode;
import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import asmCodeGenerator.ASMCodeGenerator.CodeVisitor;
import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.runtime.RunTime;

public class PrintStatementGenerator {
	ASMCodeFragment code;
	ASMCodeGenerator.CodeVisitor visitor;
	
	
	public PrintStatementGenerator(ASMCodeFragment code, CodeVisitor visitor) {
		super();
		this.code = code;
		this.visitor = visitor;
	}

	public void generate(PrintStatementNode node) {
		for(ParseNode child : node.getChildren()) {
			if(child instanceof NewlineNode || child instanceof SpaceNode) {
				ASMCodeFragment childCode = visitor.removeVoidCode(child);
				code.append(childCode);
			}
			else {
				appendPrintCode(child);
			}
		}
	}

	private void appendPrintCode(ParseNode node) {
		if (node instanceof IdentifierNode && ((IdentifierNode) node).getBinding().getType() == PrimitiveType.STRING) {
			String format = printFormat(node.getType());
			
			code.add(PushD, node.getToken().getLexeme());
			code.add(PushD, format);
			code.add(Printf);
		}
		else if (node instanceof IdentifierNode && ((IdentifierNode) node).getBinding().getType() == PrimitiveType.RATIONAL) {	
			Labeller labeller = new Labeller("compare");
			String trueLabel = labeller.newLabel("true");
			String falseLabel  = labeller.newLabel("false");
			String false2Label  = labeller.newLabel("false2");			
			String joinLabel  = labeller.newLabel("join");
			String join2Label  = labeller.newLabel("join2");
			String posLabel = labeller.newLabel("pos");
			String join3Label  = labeller.newLabel("join3");
			String negLabel = labeller.newLabel("neg");
			
			String intFormat = printFormat(PrimitiveType.INTEGER);
			String charFormat = printFormat(PrimitiveType.CHARACTER);
			
			visitor.appendAddressOfNumerator(code, (IdentifierNode)node);
			code.add(LoadI);
			visitor.appendAddressOfDenominator(code, (IdentifierNode)node);
			code.add(LoadI);
			code.add(Divide);		// result is integer part
			code.add(Duplicate);
			code.add(JumpFalse, falseLabel);	// skip printing if integer part is 0
			code.add(PushD, intFormat);
			code.add(Printf);				// print integer part
			code.add(Jump, joinLabel);
			
			code.add(Label, falseLabel);
			code.add(Pop);					// pop leftover "0"
			visitor.appendAddressOfNumerator(code, (IdentifierNode)node);
			code.add(LoadI);
			visitor.appendAddressOfDenominator(code, (IdentifierNode)node);
			code.add(LoadI);
			code.add(Multiply);
			code.add(JumpNeg, negLabel);	// print negative sign in front of fractional part if necessary
			code.add(Jump, joinLabel);
			
			code.add(Label, negLabel);
			code.add(PushI, '-');
			code.add(PushD, charFormat);
			code.add(Printf);	
			
			code.add(Label, joinLabel);
			visitor.appendAddressOfNumerator(code, (IdentifierNode)node);
			code.add(LoadI);		// load numerator
			visitor.appendAddressOfDenominator(code, (IdentifierNode)node); 
			code.add(LoadI);		// load denominator
			code.add(Remainder);	// result is fractional part
			code.add(Duplicate);
			code.add(JumpFalse, false2Label);	// skip printing if fractional part is 0
			code.add(PushI, '_');
			code.add(PushD, charFormat);
			code.add(Printf);				// print '_'
			code.add(Duplicate);
			code.add(JumpNeg, trueLabel);	// negate fractional part if it is negative
			code.add(Jump, join2Label);
			
			code.add(Label, trueLabel);
			code.add(Negate);
			
			code.add(Label, join2Label);
			code.add(PushD, intFormat);
			code.add(Printf);				// print fractional part
			code.add(PushI, '/');
			code.add(PushD, charFormat);
			code.add(Printf);
			visitor.appendAddressOfDenominator(code, (IdentifierNode)node); 
			code.add(LoadI); 			// load denominator from memory. negate it if it's negative
			code.add(Duplicate);
			code.add(JumpPos, posLabel);
			code.add(Negate);
			
			code.add(Label, posLabel);
			code.add(PushD, intFormat);
			code.add(Printf);				// print denominator
			code.add(Jump, join3Label);
			
			code.add(Label, false2Label);
			code.add(Pop);
			
			code.add(Label, join3Label);
		}
		else if (node.getType() == PrimitiveType.RATIONAL){
			Labeller labeller = new Labeller("compare");
			String trueLabel = labeller.newLabel("true");
			String false2Label  = labeller.newLabel("false2");	
			String false3Label  = labeller.newLabel("false3");	
			String joinLabel  = labeller.newLabel("join");
			String join2Label  = labeller.newLabel("join2");
			String pos2Label = labeller.newLabel("pos2");
			String join3Label  = labeller.newLabel("join3");
			String negLabel = labeller.newLabel("neg");
			
			String intFormat = printFormat(PrimitiveType.INTEGER);
			String charFormat = printFormat(PrimitiveType.CHARACTER);		
			
			if (node instanceof BinaryOperatorNode) {
				code.append(visitor.removeAddressCode(node));	// a
				code.add(Duplicate); 							// a, a
				code.add(LoadI); 								// a, n
				code.add(Exchange); 							// n, a
				code.add(PushI, 4); 							// n, a, 4
				code.add(Add); 									// n, a+4
				code.add(LoadI); 								// n, d
			}
			else code.append(visitor.removeValueCode(node));	// n, d
			code.add(Duplicate);		// n, d, d
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI); 			// n, d				m-4: d
			code.add(Exchange);
			code.add(Duplicate);
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract);
			code.add(Exchange);
			code.add(StoreI); 			// d, n				m-8: n
			code.add(Exchange);
			code.add(Divide);			// result is integer part
			code.add(Duplicate);
			code.add(JumpFalse, false2Label);	// skip printing if integer part is 0
			code.add(PushD, intFormat);
			code.add(Printf);			// print integer part
			code.add(Jump, joinLabel);
			
			code.add(Label, false2Label);
			code.add(Pop);				// pop leftover "0"
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI);
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract);
			code.add(LoadI);
			code.add(Multiply);
			code.add(JumpNeg, negLabel);	// print negative sign in front of fractional part if necessary
			code.add(Jump, joinLabel);
			
			code.add(Label, negLabel);
			code.add(PushI, '-');
			code.add(PushD, charFormat);
			code.add(Printf);
			
			code.add(Label, joinLabel);
			code.add(Memtop);
			code.add(PushI, 8);
			code.add(Subtract);
			code.add(LoadI); 			// load numerator from memory
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI); 			// load denominator from memory
			code.add(Remainder);		// result is fractional part
			code.add(Duplicate);
			code.add(JumpFalse, false3Label);	// skip printing if fractional part is 0
			code.add(PushI, '_');
			code.add(PushD, charFormat);
			code.add(Printf);				// print '_'
			code.add(Duplicate);
			code.add(JumpNeg, trueLabel);	// negate fractional part if it is negative
			code.add(Jump, join2Label);
			
			code.add(Label, trueLabel);
			code.add(Negate);
			
			code.add(Label, join2Label);
			code.add(PushD, intFormat);
			code.add(Printf);				// print fractional part
			code.add(PushI, '/');
			code.add(PushD, charFormat);
			code.add(Printf);
			code.add(Memtop);
			code.add(PushI, 4);
			code.add(Subtract);
			code.add(LoadI); 			// load denominator from memory. negate it if it's negative
			code.add(Duplicate);
			code.add(JumpPos, pos2Label);
			code.add(Negate);
			
			code.add(Label, pos2Label);
			code.add(PushD, intFormat);
			code.add(Printf);				// print denominator
			code.add(Jump, join3Label);
			
			code.add(Label, false3Label);
			code.add(Pop);
			
			code.add(Label, join3Label);
		}
		else {
			String format = printFormat(node.getType());
	
			code.append(visitor.removeValueCode(node));
			convertToStringIfBoolean(node);
			code.add(PushD, format);
			code.add(Printf);
		}
	}
	private void convertToStringIfBoolean(ParseNode node) {
		if(node.getType() != PrimitiveType.BOOLEAN) {
			return;
		}
		
		Labeller labeller = new Labeller("print-boolean");
		String trueLabel = labeller.newLabel("true");
		String endLabel = labeller.newLabel("join");

		code.add(JumpTrue, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_FALSE_STRING);
		code.add(Jump, endLabel);
		code.add(Label, trueLabel);
		code.add(PushD, RunTime.BOOLEAN_TRUE_STRING);
		code.add(Label, endLabel);
	}


	private static String printFormat(Type type) {
		assert type instanceof PrimitiveType;
		
		switch((PrimitiveType)type) {
		case INTEGER:	return RunTime.INTEGER_PRINT_FORMAT;
		case BOOLEAN:	return RunTime.BOOLEAN_PRINT_FORMAT;
		case FLOATING:	return RunTime.FLOATING_PRINT_FORMAT;
		case CHARACTER:	return RunTime.CHARACTER_PRINT_FORMAT;
		case STRING:	return RunTime.STRING_PRINT_FORMAT;
		default:		
			assert false : "Type " + type + " unimplemented in PrintStatementGenerator.printFormat()";
			return "";
		}
	}
}
