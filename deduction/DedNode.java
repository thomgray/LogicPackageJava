package logic.deduction;

import logic.Formula;
import logic.Logical;
import logic.Sequence;
import logic.Syntax;
import logic.formula.Element;
import logic.formula.PrimeFormula;
import logic.formula.Term;

public class DedNode extends Formula{    
    
    Deduction subProof;
    DedNode[] dependencies = new DedNode[0];
    
    int line;
    String inference;
    
    int[] inferenceNums = new int[0];
    int[] dependencyNums = new int[0];
    
    /**
     * inference keys
     */
//    static final String 
//            PREM = "Premise", ACP = "Assumption (CP)", ARED = "Assumption (RAA)", ADE = "Assumption (∨E)", REIT = "Reiteration",
//            CI = "Conjunction Introduction", CE = "Conjunction Elimination", DI = "Disjunction Introduction", 
//            DE = "Disjunction Elimination", DNE = "Double-Negation Elimination", DNI = "Double-Negation Introduction",
//            MP = "Modus Ponens", MT = "Modus Tollens", CP = "Conditional Proof", CPDE = "Conditional Proof (∨E)", RAA= "Reductio Ad Absurdum", 
//            BI = "Biconditional Introduction", BE = "Biconditional Elimination"
//            ;
    
    public DedNode(String input, int i){
        super(input,i);
    }
    
    public DedNode(Formula in){
        super(in.form, in.logic);
    }
    
    public DedNode(PrimeFormula in){
        super(in);
    }
    
    @Override
    public DedNode getChild(int i){
        Formula cf = super.getChild(i);
        return new DedNode(cf);
    }
    
    @Override
    public DedNode getTermVariant(Term oldt, Term newt){
        DedNode out = this.clone();
        for (Element e : out) {
            if (e instanceof PrimeFormula){
                ((PrimeFormula)e).replaceTerm(oldt, newt);
            }
        }
        String s = "";
        for(Element e:out)s+=e.form;
        return new DedNode(s,logic);
    }
    
    public void setDependency(DedNode in){
        dependencies = tools.Arrays.add(dependencies, in);  
        inferenceNums = tools.Arrays.addItem(inferenceNums, in.line);
    }
    
    public void resetDependency(DedNode in){
        dependencies = new DedNode[]{in};
        inferenceNums = new int[]{in.line};
    }
    
    public void setSubProof(Deduction in){
        subProof = in;
    }
    
    public static DedNode getConjunction(DedNode A, DedNode B){
        String s = Syntax.conjunction(A.form, B.form);
        return new DedNode(s,A.logic);
    }
    
    public static DedNode getDisjunction(DedNode A, DedNode B){
        String s = Syntax.disjunction(A.form, B.form);
        return new DedNode(s, A.logic);
    }    
    public static DedNode getConditional(DedNode A, DedNode B){
        String s = Syntax.conditional(A.form, B.form);
        return new DedNode(s, A.logic);
    }    
    public static DedNode getBiconditional(DedNode A, DedNode B){
        String s = Syntax.biconditional(A.form, B.form);
        return new DedNode(s, A.logic);
    }
    public static DedNode getNegation(DedNode A){
        String s = Syntax.negation(A.form);
        return new DedNode(s, A.logic);
    }
    public static DedNode getNegationStrict(DedNode A){
        String s = Syntax.negationStrict(A.form);
        return new DedNode(s, A.logic);
    }
    public static DedNode DNE(DedNode A){
        if (A.connective.isNegation()&&A.getChild(0).connective.isNegation());
        else return null;        
        String s = A.getChild(0).getChild(0).form;
        return new DedNode(s, A.logic);        
    }
    public static DedNode DNI(DedNode A){
        String s = Syntax.negationStrict(A.form);
        s= Syntax.negationStrict(s);
        return new DedNode(s, A.logic);
    }
    
    /**
     * returns a NEW DedNode, never the Object itself
     * @return 
     */
    public DedNode getNegationEquivalent(){
        DedNode out;
        if (this.connective.isNegation()){
            if (this.getChild(0).connective.isNegation()) {
                out = this.getChild(0).getChild(0).getNegationEquivalent();
                return out;
            }
        }
        return this.clone();
    }

    
    
    public void dischargeDependency(DedNode in){
        int x = in.line;    
        this.dependencyNums = tools.Arrays.removeAll(this.dependencyNums, x);    
    }
       
    
    /**<ul><li>0 if equivalent<br>
     * <li>1 if this is nested negation of argument
     * <li>-1 if argument is nested negation of this <br>
     * <li> 5 otherwise (not equivalent);
     * </ul>
     * @param in DedNode comparison
     * @return 0, 1, -1 or 5;
     */
    public int isNestedNegation(DedNode in){
        if (this.equals(in)) {
            return 0;
        }
        Sequence<Formula> thisseq = this.getConstruction().toSequence();
        Sequence<Formula> inseq = in.getConstruction().toSequence();
        
        int i=0,j=0; //indicates the number of nested negations, and the index of the first non-neg;
        while(thisseq.get(i).connective.isNegation()) i++;
        while (inseq.get(j).connective.isNegation()) j++;
        
        if (thisseq.get(i).equals(inseq.get(j))) {
            if (i%2==j%2) {
                if (i<j) {
                    return -1;
                }else if (j<i) {
                    return 1;
                }
            }
        }
        return 5;
    }
    
    
    /** 
     * Clone does not carry or copy the parent's subproof
     * @return 
     */
    @Override
    public DedNode clone() {
        DedNode out = new DedNode(this.form, this.logic);
        out.dependencies = this.dependencies.clone();
        out.dependencyNums = this.dependencyNums.clone();
        out.inference = this.inference;
        out.inferenceNums = this.inferenceNums.clone();
        out.line = this.line;
        return out;
    }

}