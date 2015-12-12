package logic;

public interface Logical extends Sortable{
    
    public static final int 
            FORMAT_PLAIN = 1, FORMAT_COMPACT = 2, FORMAT_EXPANDED = 3;            
            ;
    /**
     * connectives
     */
    public static final char
            biconditional = '⟷', conditional = '⟶', conjunction = '∧', disjunction = '∨', negation = '¬',
            qUniversal = '∀', qExistential = '∃',
            necessarily = '□', possibly = '◇'
            ;
    
    /**
     * element types
     */
    public static final char
            sentence = 's', connect = 'c', relation = 'r', function = 'f', variable = 'v', quantifier = 'q', equals = '=',
            atomic = 'a', term = 't'
            ;
    
    /**
     * logic constant signatures
     */
    public static final int 
            SENTENTIAL_LOGIC = 1, 
            PREDICATE_LOGIC_FO = 2,
            PREDICATE_LOGIC_SO = 3,
            SET_LOGIC = 4,
            MODAL_LOGIC = 5;
    
    public static final char NATURALS = 'ℕ', REALS = 'ℝ', INTEGERS = 'ℤ' ;
    public static final char CDent = '⊢', CnDent = '⊬', CSent = '⊨', CsSent = '⊭';
    public static final String ALEPH_0="ℵ⁰", ALEPH_1= "ℵ¹";
    
    public abstract Logical getDuplicate();
    
    public static String literal(int in){
        switch (in) {
            case SENTENTIAL_LOGIC:
                return "sentential logic";
            case PREDICATE_LOGIC_FO:
                return "first order predicate logic";
            case PREDICATE_LOGIC_SO:
                return "second order predicate logic";
            case SET_LOGIC:
                return "set theoretic logic";
            case MODAL_LOGIC:
                return "modal logic";
            default:
                throw new AssertionError();
        }
    }           
    
    
}
