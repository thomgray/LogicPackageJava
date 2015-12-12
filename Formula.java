package logic;

import java.util.Iterator;
import java.util.Objects;
import logic.Tree.TreeIterator;
import logic.formula.*;

public class Formula extends Sequence<Element> implements Comparable<Formula>{

    public enum logic{
        SENTENTIAL, PREDICATE_1, PREDICATE_2, SET, MODAL 
    }
    
    //public static final logic log;
    public final String form;
    public final String atomicForm;
    public final int logic;
    public Connective connective;
    private ConstructionTree construction;
    
    public Formula(String in, int log){
        logic = log;
        form = Syntax.getForm(in);
        
        parseForm();
        
        String af="";
        for (Element el : this) af+=el.atomicForm;
        atomicForm = af;
        construction = getConstruction();
    }
    
    public Formula(String in){
        this(in,Syntax.getLogic(in));
    }
    
    public Formula(PrimeFormula in){
        in = (PrimeFormula)in.clone();
        if (in.isSentence()) logic = Logical.SENTENTIAL_LOGIC;
        else logic = Logical.PREDICATE_LOGIC_FO;
        
        this.add(in);
        form = in.form;
        atomicForm = in.atomicForm;
        construction = new ConstructionTree(this);
    }
    
    private void parseForm() {
        for (int i = 0; i < form.length(); i++) {
            char c = form.charAt(i);
            if (Syntax.isConnective(c)) {
                this.add(new Connective(form,i));
            }else if (Syntax.isQuantifier(c)) {
                this.add(new Quantifier(form,i));
                i++;
            }else if (Syntax.isRelation(c) && logic!=Logical.SENTENTIAL_LOGIC) {
                this.add(new Relation(form,i));
            }else if(c=='=' && logic!=Logical.SENTENTIAL_LOGIC){
                this.add(new Equality(form,i));
            }else if (Syntax.isSentence(c)&& logic==Logical.SENTENTIAL_LOGIC) {
                this.add(new Sentence(form,i));
            }else if (c=='('||c==')') {
                this.add(new Parenthesis(form,i));
            }else if (Syntax.isVariable(c) && logic!=Logical.SENTENTIAL_LOGIC) {
            }else if (Syntax.isFunction(c) && logic!=Logical.SENTENTIAL_LOGIC) {
                int lr=0;
                if (i==form.length()-1) ;
                else if(form.charAt(i+1)=='('){
                    do {
                        i++;
                        if (form.charAt(i)=='(')lr++;
                        else if (form.charAt(i)==')')lr--;
                    } while (lr>0);
                }
            }else{
                throw new MalFormedException(form,logic);
            }
            
        }
    }
    
    private String[] deconstruct(){
        if (this.length==1) {
            this.connective = Connective.NULL;
            return new String[]{};
        }else if (this.get(0).isQuantifier()){
            this.connective= (Quantifier)this.get(0);
            Sequence<Element> s = this.subSequence(1);
            String ss ="";
            for(Element e:s) ss+=e.form;
            return new String[]{ss};
        }else if (this.get(0).symbol=='('&& this.get(1).symbol==Logical.negation) {
            this.connective = (Connective) this.get(1);
            Sequence<Element> s = this.subSequence(2, length-1);
            String s1="";
            for (Element e: s) s1+=e.form;
            return new String[]{s1};
        }else if (this.get(0).symbol=='('){
            int lr=0, x=1;
            boolean repeat;
            do {
                repeat=false;
                if (this.get(x).symbol=='(') lr++;
                else if (this.get(x).symbol==')') lr--;
                if (this.get(x).isQuantifier()) repeat=true;
                x++;
            } while (lr>0 || repeat);
            //x== position of connective
            connective = (Connective)this.get(x);
            Sequence<Element> c1 = this.subSequence(1, x);
            Sequence<Element> c2 = this.subSequence(x+1, this.length-1);
            
            String c1s = "", c2s ="";
            for(Element e: c1) c1s+=e.form;
            for(Element e: c2) c2s+=e.form;
            return new String[]{c1s,c2s};
        }else{
            throw new MalFormedException(form,logic);
        }
    }
    
    
    public Formula getChild(int i){
        return this.getConstruction().getChild(this, i);
    }
    
    public static Formula getConjunction(Formula f1, Formula f2){
        if (f1.logic!=f2.logic) throw new java.lang.IllegalArgumentException();
        String s = Syntax.conjunction(f1.form, f2.form);
        return new Formula(s,f1.logic);
    }
    public static Formula getDisjunction(Formula f1, Formula f2){
        if (f1.logic!=f2.logic) throw new java.lang.IllegalArgumentException();
        String s = Syntax.disjunction(f1.form, f2.form);
        return new Formula(s,f1.logic);
    }
    public static Formula getConditional(Formula ant, Formula cons){
        if (ant.logic!=cons.logic) throw new java.lang.IllegalArgumentException();
        String s = Syntax.conditional(ant.form, cons.form);
        return new Formula(s,ant.logic);
    }
    public static Formula getBiconditional(Formula ant, Formula cons){
        if (ant.logic!=cons.logic) throw new java.lang.IllegalArgumentException();
        String s = Syntax.biconditional(ant.form, cons.form);
        return new Formula(s,ant.logic);
    }
    public static Formula getNegation(Formula f){
        String s = Syntax.negation(f.form);
        return new Formula(s,f.logic);
    }
    public static Formula getNegationStrict(Formula f){
        String s = Syntax.negationStrict(f.form);
        return new Formula(s,f.logic);
    }
    public static Formula getQuantification(Formula f, char Q, char v){
        if (f.logic==Logical.SENTENTIAL_LOGIC) throw new java.lang.IllegalArgumentException();
        String s = Syntax.quantification(Q, v, f.form);
        return new Formula(s,f.logic);
    }
    public static Formula getQuantification(Formula f, char Q, char v, char a){
        if (f.logic==Logical.SENTENTIAL_LOGIC) throw new java.lang.IllegalArgumentException();
        String s = Syntax.quantification(Q, v, a, f.form);
        return new Formula(s,f.logic);
    }
    public static Formula[] stringsToFormulas(String[] in) {
        int log = Syntax.getLogic(in);
        Formula[] out = new Formula[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = new Formula(in[i], log);
        }
        return out;
    }
    
    public boolean contains(Element in){
        for (Element e : this) {
            if (in.equals(this)) return true;
        }
        return false;
    }
    
    public boolean containsVariableFree(char x){
        if (connective.isQuantifier() && connective.castToQuantifier().boundVariable.symbolIs(x)) {
            return false;
        }
        ConstructionTree ct = this.getConstruction();
        Iterator<Formula> it = ct.iterateChildren(this);
        while (it.hasNext()) {
            if (it.next().containsVariableFree(x)) return true;
        }
        return Syntax.containsSymbol(form, x);
    }
    
    public boolean containsVariable(char x){
        return Syntax.containsSymbol(form, x);
    }
    
    public Set<Term> getTerms(){
        Set<Term> out = new Set<>();
        for (Element thi : this) {
            if (thi instanceof Relation || thi instanceof Equality) {
                Term[] ts = ((PrimeFormula)thi).arguments;
                for (Term t : ts) {
                    out.union(t.composition.toSet());
                }
            }
        }
        return out;
    }
    
    public boolean isTermVariant(Formula in){
        for (int i = 0; i < this.length; i++) {
            if (this.get(i).symbol!=in.get(i).symbol) return false;
        }
        return true;
    }
    
    public boolean isTermVariantTforX(Formula in, Term t, Term x){
        if (!this.isTermVariant(in)) return false;
        if (t==null){
            here:
            for (int i = 0; i < this.length; i++) {
                Element e = this.get(i), me = in.get(i);
                if (e instanceof PrimeFormula){
                    for (int j = 0; j < ((PrimeFormula)e).arguments.length; j++) {
                        Term tt = ((PrimeFormula)e).arguments[j];
                        Term mt = ((PrimeFormula)me).arguments[j];
                        for(TreeIterator it = (TreeIterator)tt.composition.iterateForward();it.hasNext();){
                            Term tt1 = (Term)it.next();
                            int[] ad = tt.composition.getAddress(tt1);
                            Term mt1 = (Term)mt.composition.get(ad);
                            if (!mt1.equals(tt1) && mt1.equals(x)){
                                t = tt1;
                                break here;
                            }else if(tt1.equals(mt1)){
                                it.skipThisNode();
                            }else if(mt1.symbol==tt1.symbol){
                                continue;
                            }else{
                               return false;
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < this.length; i++) {
            Element e = this.get(i), me = in.get(i);
            if (e instanceof PrimeFormula){
                for (int j = 0; j < ((PrimeFormula)e).arguments.length; j++) {
                    Term tt = ((PrimeFormula)e).arguments[j];
                    Term mt = ((PrimeFormula)me).arguments[j];
                    if (!tt.isEquivalentToThisReplacingTforX(mt, t, x)) return false;
                }
            }
        }
        return true;
    }
    
    public Formula getTermVariant(Term oldt, Term newt){
        Formula out = this.clone();
        for (Element e : out) {
            if (e instanceof PrimeFormula){
                ((PrimeFormula)e).replaceTerm(oldt, newt);
            }
        }
        String s = "";
        for(Element e:out)s+=e.form;
        return new Formula(s,logic);
    }
    
    public boolean isPrenex(){
        boolean bool = true;
        ConstructionTree c = this.getConstruction();
        for (Formula f : c) {
            if (f.connective.isQuantifier()) {
                bool = false;
            }
            if (!bool && !f.connective.isQuantifier()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isAtomic(){
        return this.length==1;
    }
    public PrimeFormula getPrimeFormula(){
        if (this.length==1) return (PrimeFormula)this.get(0);
        else return null;
    }
    
    public ConstructionTree getConstruction(){
        if (construction!=null) return construction;
        construction =  new ConstructionTree(this);
        try{
            String[] parents = deconstruct();            
            for (String par : parents) {
                Formula parf = new Formula(par,logic);
                construction.placeAbove(this, parf.getConstruction());
            }
        }catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            throw new MalFormedException(form,logic);
        }
        
        return construction;
    }
    
    
    @Override
    public Formula clone(){
        return new Formula(this.form,this.logic);
    }
    
    @Override
    public String toString(){
        Object o;
        return form;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Formula){
            return ((Formula)obj).form.equals(this.form);
        }else if (obj instanceof PrimeFormula) {
            return this.form.equals(((PrimeFormula)obj).form);
        }else return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.form);
        hash = 31 * hash + Objects.hashCode(this.atomicForm);
        hash = 31 * hash + this.logic;
        return hash;
    }

    /**
     * this.equals(that) returns 0;
     * Otherwise, order is based on the following criteria in descending order of priority:
     * <ol><li> length <li>form.length <li> lexical order of the forms
     * </ol>
     * @param f
     * @return 
     */
    @Override
    public int compareTo(Formula f) {
        if (this.equals(f)) return 0;
        if (this.length>f.length) return -1;
        if (this.length<f.length) return 1;
        if (this.form.length()>f.form.length()) return -1;
        if (this.form.length()<f.form.length()) return 1;
        return (this.form.compareTo(f.form));
    }
}