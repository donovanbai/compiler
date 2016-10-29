package semanticAnalyzer.signatures;

import java.util.List;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;

//immutable
public class FunctionSignature {
	private static final boolean ALL_TYPES_ACCEPT_ERROR_TYPES = true;
	private Type resultType;
	private Type[] paramTypes;
	Object whichVariant;
	
	
	///////////////////////////////////////////////////////////////
	// construction
	
	public FunctionSignature(Object whichVariant, Type ...types) { // paramTypes comes before  resultType
		assert(types.length >= 1);
		storeParamTypes(types);
		resultType = types[types.length-1];
		this.whichVariant = whichVariant;
	}
	private void storeParamTypes(Type[] types) {
		paramTypes = new Type[types.length-1];
		for(int i=0; i<types.length-1; i++) {
			paramTypes[i] = types[i];
		}
	}
	
	
	///////////////////////////////////////////////////////////////
	// accessors
	
	public Object getVariant() {
		return whichVariant;
	}
	public Type resultType() {
		return resultType;
	}
	public boolean isNull() {
		return false;
	}
	
	
	///////////////////////////////////////////////////////////////
	// main query

	public boolean accepts(List<Type> types) {
		if(types.size() != paramTypes.length) {
			return false;
		}
		
		for(int i=0; i<paramTypes.length; i++) {
			if(!assignableTo(paramTypes[i], types.get(i))) {
				return false;
			}
		}		
		return true;
	}
	private boolean assignableTo(Type variableType, Type valueType) {
		if(valueType == PrimitiveType.ERROR && ALL_TYPES_ACCEPT_ERROR_TYPES) {
			return true;
		}	
		return variableType.equals(valueType);
	}
	
	// Null object pattern
	private static FunctionSignature neverMatchedSignature = new FunctionSignature(1, PrimitiveType.ERROR) {
		public boolean accepts(List<Type> types) {
			return false;
		}
		public boolean isNull() {
			return true;
		}
	};
	public static FunctionSignature nullInstance() {
		return neverMatchedSignature;
	}
	
	// the switch here is ugly compared to polymorphism.  This should perhaps be a method on Lextant.
	public static FunctionSignature signatureOf(Lextant lextant, List<Type> operandTypes) {
		assert(lextant instanceof Punctuator);	
		Punctuator punctuator = (Punctuator)lextant;
		
		switch(punctuator) {
		case ADD:			return FunctionSignatures.signaturesOf(Punctuator.ADD).acceptingSignature(operandTypes);
		case SUBTRACT:		return FunctionSignatures.signaturesOf(Punctuator.SUBTRACT).acceptingSignature(operandTypes);
		case MULTIPLY:		return FunctionSignatures.signaturesOf(Punctuator.MULTIPLY).acceptingSignature(operandTypes);
		case DIVIDE:		return FunctionSignatures.signaturesOf(Punctuator.MULTIPLY).acceptingSignature(operandTypes);
		case GREATER:		return FunctionSignatures.signaturesOf(Punctuator.GREATER).acceptingSignature(operandTypes);
		case LESS:			return FunctionSignatures.signaturesOf(Punctuator.LESS).acceptingSignature(operandTypes);
		case GREATER_OR_EQ:	return FunctionSignatures.signaturesOf(Punctuator.GREATER_OR_EQ).acceptingSignature(operandTypes);
		case LESS_OR_EQ:	return FunctionSignatures.signaturesOf(Punctuator.LESS_OR_EQ).acceptingSignature(operandTypes);
		case EQUAL:			return FunctionSignatures.signaturesOf(Punctuator.EQUAL).acceptingSignature(operandTypes);
		case NOT_EQUAL:		return FunctionSignatures.signaturesOf(Punctuator.NOT_EQUAL).acceptingSignature(operandTypes);
		case PIPE:			return FunctionSignatures.signaturesOf(Punctuator.PIPE).acceptingSignature(operandTypes);
		case AND:			return FunctionSignatures.signaturesOf(Punctuator.AND).acceptingSignature(operandTypes);
		case OR:			return FunctionSignatures.signaturesOf(Punctuator.OR).acceptingSignature(operandTypes);
		case NOT:			return FunctionSignatures.signaturesOf(Punctuator.NOT).acceptingSignature(operandTypes);
		default:
			return neverMatchedSignature;
		}
	}

}