package logic.deduction;

import java.util.Iterator;
import logic.Formula;
import logic.FormulaSubstitutions;
import logic.Set;
import static logic.Logical.*;
import logic.Schema;
import logic.Syntax;
import logic.formula.Element;
import logic.formula.Equality;
import logic.formula.Function;
import logic.formula.Term;
import logic.formula.Variable;

public class InferencePL extends InferenceSL{
    public static Iterator<DedNode> dit;
    public static final Schema<Term> 
            saturated = new Schema<Term>() {
        @Override
        public boolean condition(Term x) {
            for (Term t : x.composition) {
                if (t.isVariable()) return false;
            }
            return true;
        }
    },
            names = new Schema<Term>() {
        @Override
        public boolean condition(Term x) {
            return (x.isFunction() && x.form.length()==1);
        }
    };
    public static final Schema<DedNode>
            equivalence = new Schema<DedNode>() {
        @Override
        public boolean condition(DedNode x) {
            return (x.isAtomic() && x.getPrimeFormula().isEquality());
        }
    };
    
    public static final String             
            UE ="Universal Instantiation", UI = "Universal Introduction", EI = "Existential Generalisation", EE = "Existential Elimination",
            EQI = "Equality Introduction", EQE = "Equality Elimination"
            
                ;
    
    public static boolean UE_prime(Deduction in){
        boolean out = false;
        Set<Term> terms = in.getTerms().subSet(saturated);
        terms.union(in.arbitraries);
        for (DedNode d : in.ded) {
            if (d.connective.isUniveralQuantifier()) {
                char var = d.connective.castToQuantifier().boundVariable.symbol;
                String el = d.getChild(0).form;
                for (Term t : terms) {
                    String s = el.replace(String.valueOf(var), t.form);
                    DedNode conc = new DedNode(s,in.logic);
                    if (in.isInformative(conc)) {
                        in.add(UE, conc, d);
                        out=true;
                    }
                }
            }
        }
        return out;
    }

    
    public static boolean UI_recursive(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isUniveralQuantifier()) return false;
        if (!in.restricts.allows(null, conc, UI)) return false;
        
        Variable v = conc.connective.castToQuantifier().boundVariable;
        Term at = getArbitraryTerm(in);
        in.arbitraries.add(at);
        DedNode mconc = conc.getChild(0).getTermVariant(v, at);
        in.restricts.imposeRestriction(null, conc, UI);
        if (in.prove(mconc)){
            in.add(UI, conc, in.getInDed(mconc));
            in.arbitraries.removeElement(at);
            return true;
        }
        in.arbitraries.removeElement(at);
        return false;
    }
    
    /**
     * Augments the deduction with the conclusion iff:
     * <ul><li>The conclusion's main connective is an existential quantification <br>&emsp &emsp <i> and</i>
     * <li>There is a formula F in the deduction such that {@link Formula#isTermVariantTforX(logic.Formula, logic.formula.Term, logic.formula.Term) F.istermVariantTforX(G,null,x)}, where G is the 
     * enclosed formula of the conclusion, and x is the bound variable of the conclusion. i.e:
     * <ul><li>F is a term variant of G<br> &emsp &emsp <i>and
     * <li>There is a term t in F such that replacing t for x in G yields F
     * </ul></ul>
     * Returns true iff the deduction is augmented
     * @param in
     * @param conc
     * @return 
     */
    public static boolean EI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isExistentialQuantifier()) return false;
        DedNode d = conc.getChild(0);
        Variable v= conc.connective.castToQuantifier().boundVariable;
        
        for (DedNode ded : in.ded) {
            if (!ded.isTermVariant(d)) continue;
            if (ded.isTermVariantTforX(d, null, v)){
                in.add(EI, conc, ded);
                return true;
            }
        }
        return false;
    }
    
    
    
    public static boolean EI_recursive(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isExistentialQuantifier()) return false;
        Variable v = conc.connective.castToQuantifier().boundVariable;
        Set<Term> terms = in.getTerms().subSet(saturated);
        if (terms.cardinality==0){
            Term t = getArbitraryTerm(in);
            Equality eq = new Equality(t,t);
            DedNode d = new DedNode(new Formula(eq));
            in.add(EQI, d, (DedNode[]) null);
            terms = in.getTerms().subSet(saturated);
        }
        
        for (Term t : terms) {
            Formula f = conc.getChild(0);
            f=f.getTermVariant(v, t);
            DedNode mconc = new DedNode(f);
            if (in.prove(mconc)){
                mconc = in.getInDed(mconc);
                in.add(EI, conc, mconc);
                return true;
            }
        }
        return false;
    }
    
    public static boolean EE_prime(Deduction in){
        boolean out = false;
        for (DedNode ded : in.ded) {
            if(!ded.connective.isExistentialQuantifier()) continue;
            if (!in.restricts.allows(ded, null, EE)) continue;
            in.restricts.imposeRestriction(ded, null, EE);
            Variable v = ded.connective.castToQuantifier().boundVariable;
            Term t = getArbitraryTerm(in);
            DedNode d = new DedNode(ded.getChild(0).getTermVariant(v, t));
            in.add(EE, d, ded);
            out=true;
        }
        return out;
    }
    
    public static boolean EQE_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.isAtomic() || !conc.getPrimeFormula().isEquality()) return false;
        
        Term t1 = conc.getPrimeFormula().castToEquality().getLeftArgument();
        Term finalTerm = conc.getPrimeFormula().castToEquality().getRightArgument();
        
        Equality e = new Equality(t1,t1);
        DedNode step1 = new DedNode(e);
        EQI_prime(in,step1);
        DedNode step = step1;
        for (dit=in.ded.iterator(equivalence);dit.hasNext();){
            DedNode d = dit.next();
            Term rs = step.getPrimeFormula().castToEquality().getRightArgument();
            Term rs1 = rs.getApproachingTermThroughEquivalence(finalTerm, d.getPrimeFormula().castToEquality());
            System.out.println(d.form);
            System.out.println(rs.form+" "+rs1.form);
            if (rs.equals(rs1))continue;
            e = new Equality(t1,rs1);
            DedNode newstep = new DedNode(e);
            System.out.println(newstep.form);
            if (!in.isInformative(newstep)){
                newstep = in.getInDed(newstep);
            }else in.add(EQE, newstep, step,d);
            if (newstep.equals(conc)) return true;
            step = newstep;
        }
        
        return false;
        
    }  
    
    /**
     * Augments the deduction with DedNode parameter {@code conc} 'a=b' iff for any name c:
     * <ul><li> 'a=c' is in the deduction<br> &emsp &emsp <i>and</i>
     * <li> 'c=b' or 'b=c' is in the deduction
     * </ul>Alternatively if c=a is in the deduction instead of a=c, the deduction is also augmented by:
     * <ol><li> 'a=a' (=I) <li> 'a=c' (=E)<br> prior to: <li> 'a=b' (=E)
     * </ol>
     * @param in
     * @param conc
     * @return 
     */
    public static boolean EQE_primeStep1(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.isAtomic() || !conc.getPrimeFormula().isEquality()) return false;
        
        Equality concEq = conc.getPrimeFormula().castToEquality();
        Term cta = concEq.getLeftArgument(), ctb = concEq.getRightArgument();
        for(it = in.ded.iterator(equivalence);it.hasNext();){
            DedNode e = it.next();
            Equality ee = e.getPrimeFormula().castToEquality();
            if (ee.equates(cta) && !ee.equates(cta, cta)){
                Term c;
                if (ee.getLeftArgument().equals(cta)) c=ee.getRightArgument();
                else c = ee.getLeftArgument();
                for(Iterator<DedNode> it2 = in.ded.iterator(equivalence);it2.hasNext();){
                    DedNode e2 = it2.next();
                    Equality ee2 = e2.getPrimeFormula().castToEquality();
                    if (ee2.equates(c, ctb)){
                        if (!ee.arguments[0].equals(cta)){
                            Equality aisa = new Equality(cta,cta);
                            DedNode aisaNode = new DedNode(aisa);
                            in.add(EQI, aisaNode, (DedNode[]) null);
                            DedNode aiscNode = new DedNode(new Equality(cta,c));
                            in.add(EQE, aiscNode, aisaNode,e);
                            e=aiscNode;
                        }
                        in.add(EQE, conc, e,e2);
                        return true;
                    }
                }
            }
        }
        return false;
    }  
    
    /**
     * Augments the deduction with the conc parameter 'a=b' equivalence iff:
     * <ul><li>'a=c' or 'c=a' is in the deduction<br> &emsp &emsp <i>and</i><li> equivalences 't<sub>1</sub>=s<sub>1</sub>' ... 't<sub>n</sub>=s<sub>n</sub>' are in the deduction 
     * where c and b are equivalents through eq<sub>1</sub> ... eq<sub>n</sub>
     * <li> in the case of 'c=a', the deduction is first augmented as per the {@link #EQE_forceLeftTerm(logic.deduction.Deduction, logic.deduction.DedNode, logic.formula.Term) forceLeftTerm()} method
     * </ul>Alternatively, is 'a=c' or 'c=a' is not in the deduction for ant term 'c', the conc parameter is added to the ddeuction iff:
     * <ul><li> equivalences 't<sub>1</sub>=s<sub>1</sub>' ... 't<sub>n</sub>=s<sub>n</sub>' are in the deduction where a and b are equivalents through eq<sub>1</sub> ... eq<sub>n</sub> 
     * </ul>Possible (non-null) augmentations include:
     * <ol>( 'a=c' )<br>...<li>'a=b'
     * </ol>
     * <ol>( 'c=a' )<br> <li>'a=a'<li>'a=c'<br>...<li>'a=b'
     * </ol>
     * <ol> <li>'a=a'<br>...<li>'a=b'
     * </ol>
     * @param in
     * @param conc
     * @return 
     */
    public static boolean EQE_primeStep2(Deduction in, DedNode conc){
        if (!in.isInformative(conc))return true;
        if (!conc.isAtomic() || !conc.getPrimeFormula().isEquality()) return false;
        
        Equality conceq = conc.getPrimeFormula().castToEquality();
        Term concleft = conceq.getLeftArgument();
        Term finalterm = conceq.getRightArgument();
        
        for(it = in.ded.iterator(equivalence);it.hasNext();){
            DedNode d = it.next();
            Equality de = d.getPrimeFormula().castToEquality();
            if (!de.equates(concleft)) continue;
            d = EQE_forceLeftTerm(in,d,concleft);
            DedNode step = d;
            for(Iterator<DedNode> it2 = in.ded.iterator(equivalence);it2.hasNext();){
                DedNode eq = it2.next();
                Equality eqq = eq.getPrimeFormula().castToEquality();
                DedNode stepn = new DedNode(new Equality(concleft,step.getPrimeFormula().castToEquality().getRightArgument().getApproachingTermThroughEquivalence(finalterm, eqq)));
                if (stepn.equals(step)) continue;
                if (!in.isInformative(stepn)) stepn = in.getInDed(stepn);
                else{
                    if (stepn.equals(conc)) stepn=conc;
                    in.add(EQE, stepn, step,eq);
                }
                step = stepn;
                if (step.equals(conc)) return true;
            }
        }
        
        DedNode taut= new DedNode(new Formula(new Equality(concleft,concleft)));
        in.add(EQI, taut, (DedNode[]) null);
        DedNode step = taut;
        for(it = in.ded.iterator(equivalence);it.hasNext();){
            DedNode eq = it.next();
            Equality eqq = eq.getPrimeFormula().castToEquality();
            Equality eq2 = new Equality(concleft,step.getPrimeFormula().castToEquality().getRightArgument().getApproachingTermThroughEquivalence(finalterm, eqq));
            DedNode step2 = new DedNode(eq2);
            if (step2.equals(step)) continue;
            if (!in.isInformative(step2)) step2 = in.getInDed(step2);
            else{
                if (step2.equals(conc)) step2=conc;
                in.add(EQE, step2, step,eq);
            }
            step = step2;
            if (step.equals(conc)) return true;
        }
        return false;
    }
    
    public static boolean EQE_primeStep3(Deduction in, DedNode conc){
        for (DedNode d : in.ded) {
            if (!d.isTermVariant(conc))continue;
            for (Iterator<DedNode> it = in.ded.iterator(equivalence);it.hasNext();) {
                DedNode ded = it.next();
                Equality eq = ded.getPrimeFormula().castToEquality();
                DedNode mconc = new DedNode(FormulaSubstitutions.apprachingEquivalence(d, conc, eq));
                if (in.isInformative(mconc)){
                    in.add(EQE, mconc, d,ded);
                    if (mconc.equals(conc)) return true;
                }
            } 
        }
        return false;
    }
        
    /**
     * Augments the deduction with any formula a=b iff:
     * <ul><li>'a=c` is in the deduction for any c <br>&emsp &emsp <i> and
     * <li>'b=c` or 'c=b` is in the deduction
     * </ul>
     * @param in 
     */
    public static boolean EQE_generic(Deduction in){
        System.out.println("EQE generic");
        boolean out = false;
        for (DedNode ded : in.ded) {
            if (!ded.isAtomic() || !ded.getPrimeFormula().isEquality()) continue;
            Equality eq = ded.getPrimeFormula().castToEquality();
            Term leftarg = eq.getLeftArgument(), rightarg = eq.getRightArgument();
            for (Iterator<DedNode> it1 = in.ded.iterator(equivalence);it1.hasNext();){
                DedNode ded2 = it1.next();
                Equality eq1 = ded2.getPrimeFormula().castToEquality();
                if (eq1.equals(eq)) continue;
                if (eq1.equates(rightarg)){
                    Term newterm = eq1.getCorrespondingArgument(rightarg);
                    if (newterm.equals(leftarg))continue;
                    Equality neweq = new Equality(leftarg,newterm);
                    DedNode conc = new DedNode(new Formula(neweq));
                    if (in.isInformative(conc)){
                        in.add(EQE, conc, ded,ded2);
                        out=true;
                    }
                }
            }
        }
        return out;
    }
    
    /**
     * For a (equivalency) DedNode conc parameter 'a=b' and Term parameter t:
     * <ul><li>returns 'a=b' in the deduction iff t is equivalent to 'a'<br>
     * &emsp &emsp <i>or</i><li>if t is equivalent to 'b', returns 'a=b' after augmenting the deduction with:
     * <ol><li>'a=a'<li>'a=b'</ol>
     * </ul>
     * Returns null if:
     * <ul><li> conc parameter is not an equivalence<li>t is not equivalent to 'a' or 'b'
     * </ul>Will trigger an exception if the node parameter is not in the deduction;
     * @param in
     * @param node
     * @param leftTerm
     * @return 
     */
    private static DedNode EQE_forceLeftTerm(Deduction in, DedNode node, Term leftTerm){
        if (!node.isAtomic() || !node.getPrimeFormula().isEquality()) return null;
        Equality eq = node.getPrimeFormula().castToEquality();
        if (!eq.equates(leftTerm)) return null;
        if (eq.getLeftArgument().equals(leftTerm)) return node;
        Equality taut = new Equality(leftTerm,leftTerm);
        DedNode tautn = new DedNode(taut);
        
        if (!in.isInformative(tautn))tautn = in.getInDed(tautn);
        else in.add(EQI, tautn, (DedNode[]) null);
        
        DedNode conc = new DedNode(new Equality(leftTerm,eq.getLeftArgument()));
        in.add(EQE, conc, tautn,in.getInDed(node));
        return conc;
    }
    
    
    
    public static DedNode getEquivalence(Deduction in, Term a, Term b){
        for (DedNode ded : in.ded) {
            if (!ded.isAtomic() || !ded.get(0).isEquality()) continue;
            Equality eq = ded.getPrimeFormula().castToEquality();
            if (eq.getLeftArgument().equals(a) && eq.getRightArgument().equals(b)) return ded;
            else if (eq.getRightArgument().equals(a) && eq.getLeftArgument().equals(b)) return ded;
        }
        return null;
    }
    
    public static boolean EQI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (conc.length!=1 || !conc.get(0).isEquality()) return false;
        Equality eq = (Equality)conc.get(0);
        if (!eq.arguments[0].equals(eq.arguments[1])) return false;
        in.add(EQI, conc, (DedNode[]) null);
        return true;
    }
    
    
    public static Term getArbitraryTerm(Deduction in){
        char c = 'a';
        Set<Term> terms = in.getTerms();
        boolean bool;
        do {
            bool=true;
            for (Term t : terms) {
                if (Syntax.containsSymbol(t.form, c)){
                    bool = false;
                    break;
                }
            } 
            for (Term t : in.arbitraries) {
                if (Syntax.containsSymbol(t.form, c)){
                    bool=false;
                    break;
                }
            }
            if (!bool) c++;
        } while (!bool);
        Term a = new Function(String.valueOf(c));
        return a;
    }
    
}