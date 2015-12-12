package logic.formula;

import logic.Logical;

public class Connective extends Element{
    /**
     * Blank connective for prime formulas
     */
    public static final Connective NULL = new Connective(" ",0); 
    
    public Connective(String in,int i){
        super(in,i);
    }
    public Connective(char in){
        this(String.valueOf(in),0);
    }
    
    public boolean isConjunction(){
        return this.symbol==Logical.conjunction;
    }
    public boolean isDisjunction(){
        return this.symbol==Logical.disjunction;
    }
    public boolean isConditional(){
        return this.symbol==Logical.conditional;
    }
    public boolean isBiconditional(){
        return this.symbol==Logical.biconditional;
    }
    public boolean isNegation(){
        return this.symbol==Logical.negation;
    }
    public boolean isUniveralQuantifier(){
        return this.symbol==Logical.qUniversal;
    }
    public boolean isExistentialQuantifier(){
        return this.symbol==Logical.qExistential;
    }
    public boolean isNull(){
        return this==Connective.NULL;
    }
    
    public Quantifier castToQuantifier(){
        if (this.isQuantifier()) {
            return (Quantifier)this;
        }else{
            throw new java.lang.IllegalArgumentException();
        }
    }
    
    @Override
    public Connective clone(){
        if (this instanceof Quantifier){
            return ((Quantifier)this).clone();
        }
        return new Connective(symbol);
    }
}