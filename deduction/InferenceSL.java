package logic.deduction;

import java.util.Iterator;
import logic.Set;

/**
 * Implements methods for proof operations. Three general kinds of procedure:
 * 
 * <ol><li><b>PRIME METHODS </b><ul><li>no recursion</ul>
 * <li><b>CASCADING METHODS</b><ul><li>procedure passes to other methods (cascading or prime)
 * <li>Cascading methods are indexed. 0-cascades bottom out on next level (prime calls only)</ul>
 * <li><b>RECURSIVE METHODS</b><ul><li>procedure passes to Deduction</ul>
 * </ol>
 * Furthermore, methods may be:
 * <ol>
 * <li><b>GENERIC METHODS </b><ul><li>do not take conclusion arguments; prove blindly</ul>
 * <li><b>LOCAL METHODS</b><ul><li>take conclusion arguments; prove intentionally</ul>
 * </ol>
 * @author thomdikdave
 */
 class InferenceSL{
     
     static final String 
            PREM = "Premise", ACP = "Assumption (CP)", ARED = "Assumption (RAA)", ADE = "Assumption (∨E)", REIT = "Reiteration",
            CI = "Conjunction Introduction", CE = "Conjunction Elimination", DI = "Disjunction Introduction", 
            DE = "Disjunction Elimination", DNE = "Double-Negation Elimination", DNI = "Double-Negation Introduction",
            MP = "Modus Ponens", MT = "Modus Tollens", CP = "Conditional Proof", CPDE = "Conditional Proof (∨E)", RAA= "Reductio Ad Absurdum", 
            BI = "Biconditional Introduction", BE = "Biconditional Elimination"
            ;
     
     static Iterator<DedNode> it;

    /**
     * <i>Conjunction Elimination</i><p>
     * <b>PRIME, GENERIC</b><p> 
     * <i>Any</i> formula A is added to the deduction if:
     * <ul><li>(A∧B) or (B∧A) occurs in the deduction<i></i><br>&emsp<i>and</i>
     * <li> A is informative</ul>
     * Method is <i>closed</i> under this operation
     * @param 
     * @return true iff deduction is augmented 
     */
    public static boolean CE_prime(Deduction in){
        boolean out = false;        
        
        it = in.ded.iterator();
        while (it.hasNext()) {
            DedNode dn = it.next();
            if (dn.connective.isConjunction()) {
                DedNode c1 = dn.getChild(0);
                DedNode c2 = dn.getChild(1);
                if (in.isInformative(c1)) {
                    in.add(CE, c1, dn);
                    out = true;
                }
                if (in.isInformative(c2)) {
                    in.add(CE, c2, dn);
                    out=true;
                }
            }
        }

        return out;      
    }

    
    /**
     * <i>Modus Ponens</i><p>
     * <b>PRIME, GENERIC</b><p> 
     * <i>Any</i> formula B is added to the deduction if:
     * <ul><li>(A⟶B) occurs in the deduction<br> &emsp <i>and
     * <li>A occurs in the deduction<br> &emsp <i> and
     * <li>B is informative
     * </ul>
     * Method is <i>closed</i> under this operation
     * @param 
     * @return true iff deduction is successfully augmented
     */
    public static boolean MP_prime(Deduction in){
        boolean out = false;
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).connective.isConditional()) {
                DedNode ant = in.ded.get(i).getChild(0), cons = in.ded.get(i).getChild(1);
                if (in.isInformative(cons)) {
                    int m = in.getIndex(ant);
                    if (m>=0) {
                        in.add(MP, cons, in.ded.get(i), in.ded.get(m));
                        out = true;
                    }
                }
            }
        }
        return out;
    }
    
    /**
     * <i>Modus Ponens</i><p>
     * <b>CASCADING, GENERIC</b><p>
     * 
     *
     * <b>Procedure</b>:
     * <ul>
     * <li>scans the deduction for all formulas in the form (A⟶B)
     * <li>if A is informative, try to prove A invoking {@link #inferPrimes(logic.Deduction, logic.DedNode) inferPrimes(in,A)}
     * <li>if true is returned, B is added to the deduction
     * </ul>
     * @return true iff the deduction is augmented
     * 
     */
    public static boolean MP_cascading(Deduction in){
        boolean out = false;
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).connective.isConditional()) {                
                DedNode ant = in.ded.get(i).getChild(0), cons = in.ded.get(i).getChild(1);
                if (in.isInformative(cons)) {
                    if (in.inferPrimes(ant)) {
                       ant = in.getInDed(ant);
                       in.add(MP, cons, in.ded.get(i), ant);  
                       out= true;
                       InferenceSL.DNE_prime(in);
                    }
                }                
            }
        }
        return out;
    }
    
    public static boolean MP_recursive(Deduction in){
        boolean out = false;
        for (DedNode ded : in.ded) {
            if (!ded.connective.isConditional() || !in.restricts.allows(ded, null, MP)) continue;
            DedNode ant = ded.getChild(0), cons = ded.getChild(1);
            if (!in.isInformative(cons)) continue;
            in.restricts.imposeRestriction(ded, null, MP);
            if (in.prove(ant)){
                System.out.println(ant.form);
                System.out.println(in.getInDed(ant).form);
                in.add(MP, cons, ded,in.getInDed(ant));
                out=true;
            }
            in.restricts.liftRestriction(ded, null, MP);
        }
        return out;
    }

    
    /**
     * <i>Double Negation Elimination</i><p>
     * <b>PRIME, GENERIC</b><p> 
     * <i>Any</i> formula A is added to the deduction if:
     * <ul><li>(¬(¬A)) occurs in the deduction <br> &emsp <i> and
     * <li> A is informative
     * </ul>
     * Method is <i>closed</i> under this operation
     * @param 
     * @return true iff deduction is augmented 
     * @see #inferGenerics(logic.Deduction) inferGenerics(in)
     * @see #inferPrimes(logic.Deduction, logic.DedNode) inferPrimes(in,conc)
     */
    public static boolean DNE_prime(Deduction in){
        boolean out = false;
        it = in.ded.iterator();
        while (it.hasNext()) {
            DedNode dn = it.next();
            if (dn.connective.isNegation()&&dn.getChild(0).connective.isNegation()) {
                DedNode conc = dn.getChild(0).getChild(0);
                if (in.isInformative(conc)) {
                    in.add(DNE, conc, dn);
                    out=true;
                }
            }
        }
        return out;
    }
    

    /**
     * <i>Double Negation Introduction</i><p>
     * <b>PRIME, LOCAL</b><p>
     * {@code conc} parameter is added to the deduction if:
     * <ul>
     * <li>{@code conc} is in the form (¬(¬A))<br>&emsp <i> and
     * <li> A occurs in the deduction
     * <br>&emsp <i> and<li>{@code conc} is informative </ul>
     * @param 
     * @param 
     * @return true iff:<br> conclusion is added to the deduction <br>&emsp<i>or</i><br>conclusion is already in the deduction 
     */
    public static boolean DNI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) {return true;}
        if (!conc.connective.isNegation()) return false;
        else if (!conc.getChild(0).connective.isNegation()) return false;
            
        
                
        it = in.ded.iterator();
        while (it.hasNext()) {
            DedNode dn = it.next();
            if (dn.isNestedNegation(conc)==-1) {
                DedNode t = dn;
                while (!t.equals(conc)) {
                    DedNode t2 = DedNode.DNI(t);
                    if (in.isInformative(t2)) in.add(DNI, t2, t);                                      
                    t=t2;
                }
                return true;
            }
            
        }
        return false;
    }

    /**
     * <i>Modus Tollens </i><p>
     * <b>PRIME, GENERIC</b><p> 
     * <i>Any</i> formula (¬A) is added to the deduction if:
     * <ul><li>(A⟶B) occurs in the deduction<br>&emsp <i>and</i>
     * <li>(¬B) occurs in the deduction<br>&emsp <i>and</i>
     * <li> (¬A) is informative
     * </ul>    
     * Method is <i>closed</i> under this operation
     * @param 
     * @return true iff deduction is augmented
     */
    public static boolean MT_prime(Deduction in){
        boolean out = false;
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).connective.isConditional()) {
                DedNode conc = DedNode.getNegation(in.ded.get(i).getChild(0));
                if (in.isInformative(conc)) {
                    DedNode d = DedNode.getNegationStrict(in.ded.get(i).getChild(1));
                    int x = in.getIndex(d);
                    if (x>=0) {
                        conc = DedNode.getNegationStrict(in.ded.get(i).getChild(0));
                        in.add(MT, conc, in.ded.get(i), in.ded.get(x));
                        out=true;
                    }                 
                }
            }
        }
        return out;
    }
    
    /**
     * <i>Modus Tollens</i><p>
     * <b>CASCADING, GENERIC</b><p>
     * 
     *Any Formula (¬A) is added to the deduction if:
     * <ul>
     * <li>There is a formula (A⟶B) in the deduction<br>&emsp <i> and
     * <li> (¬B) is in the deduction following {@link #inferPrimes(logic.Deduction, logic.DedNode) inferPrimes(in,(¬B))}
     * <br>&emsp <i> and<li>(¬A) is informative </ul>
     * 
     * <dl><dt><b>Recurs:<dt>&emsp&ensp Direct Invokes:<dd>{@link #inferPrimes(logic.Deduction, logic.DedNode) inferPrimes(in,(¬B))}
     * <dt>&emsp&ensp Indirect Invokes:
     * <dd>{@link #CE_prime(logic.Deduction) CE_prime(in)}
     * <dd>{@link #BE_prime(logic.Deduction) BE_prime(in)}
     * <dd>{@link #DNE_prime(logic.Deduction) DNE_prime(in)}
     * <dd>{@link #MP_prime(logic.Deduction) MP_prime(in)}
     * <dd>{@link #MT_prime(logic.Deduction) MT_prime(in)}
     * <dd>{@link #BI_prime(logic.Deduction, logic.DedNode) BI_prime(in,conc)}
     * <dd>{@link #CI_prime(logic.Deduction, logic.DedNode) CI_prime(in,conc)}
     * <dd>{@link #DI_prime(logic.Deduction, logic.DedNode) DI_prime(in,conc)}
     * <dd>{@link #DNI_prime(logic.Deduction, logic.DedNode) DNI_prime(in,conc)}
     * </dl>
     * 
     * @return true iff the deduction is augmented
     * 
     * 
     */
    public static boolean MT_cascading(Deduction in){        
        boolean out=false;    
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).connective.isConditional()) {
                DedNode ant = in.ded.get(i).getChild(0), cons = in.ded.get(i).getChild(1);  
                cons = DedNode.getNegationStrict(cons);
                if (in.isInformative(DedNode.getNegation(ant))) {
                    if (in.inferPrimes(cons)) {
                        cons = in.getInDed(cons);
                        ant = DedNode.getNegationStrict(ant);
                        in.add(MT, ant, in.ded.get(i), cons);
                        out=true;
                    } 
                }                
            }
        }
        return out;        
    }
    
    public static boolean MT_recursive(Deduction in){
        boolean out = false;
        for (DedNode ded : in.ded) {
            if (!ded.connective.isConditional() || !in.restricts.allows(ded, null, MT)) continue;
            DedNode ant = ded.getChild(0), cons = ded.getChild(1);
            if (!in.isInformative(DedNode.getNegation(ant))||!in.isInformative(DedNode.getNegationStrict(ant)))continue;
            in.restricts.imposeRestriction(ded, null, MT);
            DedNode nant = DedNode.getNegationStrict(ant), ncons = DedNode.getNegationStrict(cons);
            if (in.prove(ncons)){
                in.add(MT, nant, ded,in.getInDed(ncons));
                out=true;
            }
            in.restricts.liftRestriction(ded, null, MT);
        }
        return out;
    }

    
    /**
     * <i>Conjunction Introduction</i><p>
     * <b>PRIME, LOCAL</b><p>
     * {@code conc} parameter is added to the deduction if:
     * <ul>
     * <li>{@code conc} is in the form (A∧B)<br>&emsp <i>and
     * <li> A and B are in the deduction <br> &emsp <i>and
     * <li> {@code conc} is informative
     * </ul>
     * @param 
     * @param 
     * @return true iff:<br> conclusion is added to the deduction <br>&emsp<i>or</i><br>conclusion is already in the deduction 
     */
    public static boolean CI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) {return true;}
        if (!conc.connective.isConjunction()) {return false;}
        
        int x=in.getIndex(conc.getChild(0)), y = in.getIndex(conc.getChild(1));
        if (x>=0&&y>=0) {
            in.add(CI, conc, in.ded.get(x), in.ded.get(y));
            return true;
        }
        return false;
    }
    
    /**
     * <i>Biconditional Introduction</i><p>
     * <b>PRIME, LOCAL</b><p>
     * {@code conc} parameter is added to the deduction iff:
     * <ul>
     * <li>{@code conc} is in the form (A⟷B)<br> &emsp <i>and
     * <li>Either:<ol>
     * <li>((A⟶B)∧(B⟶A)) or ((B⟶A)∧(A⟶B)) occurs in the deduction<br>&emsp <i>or
     * <li>(A⟶B) and (B⟶A) occur in the deduction
     * </ol>&emsp <i> and<li>{@code conc} is informative 
     * </ul>
     * Further, if (2)
     * <ul><li>((A⟶B)∧(B⟶A)) is added to the deduction prior to {@code conc}
     * </ul>
     * @param 
     * @param 
     * @return true iff:<br> conclusion is added to the deduction <br>&emsp<i>or</i><br>conclusion is already in the deduction 
     */
    public static boolean BI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isBiconditional()) return false;
        
        DedNode d1 = DedNode.getConditional(conc.getChild(0), conc.getChild(1));
        DedNode d2 = DedNode.getConditional(conc.getChild(1), conc.getChild(0));        
        DedNode cj = DedNode.getConjunction(d1, d2), cj2 = DedNode.getConjunction(d2, d1);
        int z= in.getIndex(cj);
        if (z==-1) z=in.getIndex(cj2);
        if (z>=0) {
            in.add(BI, conc, in.ded.get(z));
            return true;
        }
        
        int x = in.getIndex(d1), y=in.getIndex(d2);
        if (x>=0&&y>=0) {
            in.add(CI, cj, in.ded.get(x), in.ded.get(y));
            in.add(BI, conc, cj);
            return true;
        }                              
        return false;
    }
    
    /**
     * <i>Disjunction Introduction</i><p>
     * <b>PRIME, LOCAL</b><p>
     * {@code conc} parameter is added to the deduction if:
     * <ul>
     * <li>{@code conc} is in the form (A∨B)<br>&emsp<i> and
     * <li>A or B occur in the deduction<br>
     * &emsp <i> and<li>{@code conc} is informative 
     * </ul>
     * @param 
     * @param 
     * @return true iff:<br> conclusion is added to the deduction <br>&emsp<i>or</i><br>conclusion is already in the deduction 
     */
    public static boolean DI_prime(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) {return true;}
        if (!conc.connective.isDisjunction()) return false;
        DedNode dj1 = conc.getChild(0), dj2 = conc.getChild(1);
        
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).equals(dj1)||in.ded.get(i).equals(dj2)) {
                in.add(DI, conc, in.ded.get(i));
                return true;
            }
        }
        return false;        
    }
    
    /**
     * <i>Biconditional Elimination</i><p>
     * <b>PRIME, GENERIC</b> <p> 
     * <i>Any</i> formula ((A⟶B)∧(B⟶A)) is added to the deduction if:
     * <ul?<li>(A⟷B) <i>or</i> (B⟷A) occur in the deduction<br> &emsp <i> and
     * <li>((A⟶B)∧(B⟶A)) is informative
     * </ul>
     * Method is <i>closed</i> under this operation
     * @param 
     * @return true iff deduction is augmented 
     */
    public static boolean BE_prime(Deduction in){
        boolean out = false;
        for (int i = 0; i < in.ded.length; i++) {
            if (in.ded.get(i).connective.isBiconditional()) {
                DedNode cj1 = DedNode.getConditional(in.ded.get(i).getChild(0), in.ded.get(i).getChild(1));
                DedNode cj2 = DedNode.getConditional(in.ded.get(i).getChild(1), in.ded.get(i).getChild(0));
                DedNode cj = DedNode.getConjunction(cj1, cj2);
                if (in.isInformative(cj)) {
                    in.add(BE, cj, in.ded.get(i));
                    out=true;
                    
                    int x = in.getIndex(cj);
                    if (x>=0) {
                        if (in.isInformative(cj1)) {
                            in.add(CE, cj1, in.ded.get(x));
                            out=true;
                        }
                        if (in.isInformative(cj2)) {
                            in.add(CE, cj2, in.ded.get(x));
                            out=true;
                        }
                    }                    
                }                                
            }
        }
        return out;
    }
    
    
    /**
     * <dl><dt><b>Recurs<dd>{@link Deduction#prove(logic.DedNode) (Deduction)in.prove(conc)}
     * </dl>
     * @param in
     * @param conc
     * @param force
     * @return 
     */
    static boolean CP_recursive(Deduction in, DedNode conc){
        
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isConditional()) return false;
        
        DedNode ass = conc.getChild(0);
        DedNode cons = conc.getChild(1);
        Deduction sp = Deduction.subProof(in, ass, ACP, conc);
        if (sp.prove(cons)){
            cons = sp.getInDed(cons, ass);      
            sp.closeDeduction(cons);  //Must keep this! Can't trust the add method to retain the proper consequent!     
            conc.setSubProof(sp); //must set sp before adding to ded!!!!
            in.add(CP, conc, ass, cons);
            return true;
        }
        return false;
    }
    /**
     * <dl><dt><b>Recurs<dd>{@link Deduction#prove(logic.DedNode) (Deduction)in.prove(conc)}
     * </dl>
     * @param in
     * @param conc
     * @param force
     * @return 
     */
    static boolean CP_cascading(Deduction in, DedNode conc){
        
        if (!in.isInformative(conc)) return true;
        if (!conc.connective.isConditional()) return false;
        
        DedNode ass = conc.getChild(0);
        DedNode cons = conc.getChild(1);
        //Deduction sp = new Deduction(in, cons);  
        Deduction sp = Deduction.subProof(in, ass, ACP, conc);
        //sp.add(ACP, ass, (DedNode[]) null);
        if (sp.inferComposites(cons)){
            System.out.println("EASY!");
            cons = sp.getInDed(cons, ass);                   
            sp.closeDeduction(cons);  //Must keep this! Can't trust the add method to retain the proper consequent!     
            conc.setSubProof(sp); //must set sp before adding to ded!!!!
            in.add(CP, conc, ass, cons);
            return true;
        }
        return false;
    }
 
    
    /**
     * <dl><dt><b>Recurs<dd>{@link logic.Deduction#prove(logic.DedNode)  (Deduction)in.prove(conc)}
     * </dl>
     * @param in
     * @param ant
     * @param cons
     * @param dj
     * @return 
     */
    public static DedNode CP_forDE(Deduction in, DedNode ant, DedNode cons, DedNode dj){
        DedNode conc = DedNode.getConditional(ant, cons);
        if (!in.isInformative(conc)) return in.getInDed(conc);
        
        Deduction sp = Deduction.subProof(in, ant, ADE, cons);
        //sp.restricts.imposeRestriction(dj, conc, DE);
        if (sp.prove(cons)) {                        
            cons = sp.getInDed(cons, ant);
            sp.closeDeduction(cons);  //Must keep this! Can't trust the add method to retain the proper consequent!     
            conc.setSubProof(sp); //must set sp before adding to ded!!!!
            in.add(CPDE, conc, ant, cons);
            return conc;
        }
        return null;
    }
    
    
    /**
     * <dl><dt><b>Recurs:<dd>{@link #CP_forDE(logic.Deduction, logic.DedNode, logic.DedNode, logic.DedNode) CP_forDE(in,ant,cons,dj)}<p>
     * <dd> {@link Deduction#prove(logic.DedNode) (Deduction)in.prove(conc)}
     * </dl>
     * 
     */
    public static boolean DE_recursive(Deduction in, DedNode conc){  
        if (!in.isInformative(conc)) return true;
        it = in.ded.iterator();
        while (it.hasNext()) {
            DedNode dj = it.next();
            if (dj.connective.isDisjunction() && in.restricts.allows(dj, conc, DE)){
                in.restricts.imposeRestriction(dj, conc, DE);
                DedNode dj1 = dj.getChild(0);
                DedNode c1 = InferenceSL.CP_forDE(in, dj1, conc.clone(), dj);
                if (c1!=null) {
                    DedNode dj2 = dj.getChild(1);
                    DedNode c2 = InferenceSL.CP_forDE(in, dj2, conc.clone(), dj);
                    if (c2!=null) {
                        in.add(DE, conc, dj,c1,c2);
                        in.restricts.liftRestriction(dj, conc, DE);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * <i>Reductio Ad Absurdum</i><p>
     * <b>CASCADING</b><p>
     * (¬A) / (¬(¬A)), where A is the {@code conc} parameter is added to the deduction if:
     * <ul>
     * <li>(¬A) / A yields a non-null return from {@link #inferContradiciton_Indirect(logic.Deduction, logic.DedNode) inferContradiciton_Indirect(in,ass)}
     * <br>&emsp Note<br><li> {@link #DNE_prime(logic.Deduction) DNE_prime()} is invoked following a successful return
     * </ul>
     * <dl><dt><b>Recurs:<dd> {@link #inferCascades(logic.Deduction, logic.DedNode) inferCascades(in,A)}
     * </dl>
     * @param in
     * @param conc
     * @return 
     */
    public static boolean RAA_cascading(Deduction in, DedNode conc){
        if (!in.isInformative(conc)) return true;
        
        DedNode ass = DedNode.getNegation(conc);
        Deduction sp = Deduction.subProof(in, ass, ARED, null);
        
        DedNode contra = InferenceSL.inferContradiciton_Direct(sp, ass);
        if (contra==null) contra = InferenceSL.inferContradiciton_Indirect(sp, ass);
        if (contra!=null) {
            DedNode raa = DedNode.getNegationStrict(ass);
            raa.setSubProof(sp);
            in.add(RAA, raa, ass, contra);
            InferenceSL.DNE_prime(in);
            in.print();
            return true;
        }
        return false;
    }
    /**
     * <i>Reductio Ad Absurdum</i><p>
     * <b>CASCADING, GENRIC</b><p>
     * 
     * (¬A) is added to the deduction for some A if:
     * <ul><li>A occurs as a construct in the deduction as in {@link Deduction#getRAAFodder() in.getRAAFodder()}
     * <br>&emsp<i>and</i><br>Neither A nor (¬A) occur in the deduction
     * <li>The method {@link #inferContradiciton_Indirect(logic.deduction.Deduction, logic.deduction.DedNode) inferContradiction_Indirect(sp)} returns a non-null, where sp is a subproof with A as an assumption
     * </ul>
     * <dl><dt><b>Recurs:<dd> {@link #inferCascades(logic.deduction.Deduction, logic.deduction.DedNode) inferCascades(in,A)}
     * </dl>
     * @param conc
     * @return 
     */
    public static boolean RAA_genericCascading(Deduction in){
        boolean out = false;
        Set<DedNode> ants = in.getRAAFodder();
                
        for (DedNode ant : ants) {
            DedNode dn = DedNode.getNegation(ant);
            ants.add(dn); 
        }
        ants.sortAscending();
        
        for (DedNode d : ants) {
            DedNode nd = DedNode.getNegation(d);
            if(in.getInDed(d)==null && in.getInDed(nd)==null){
                Deduction sp = Deduction.subProof(in, d, ARED, null);
                DedNode c1 = InferenceSL.inferContradiciton_Indirect(sp, d);
                if (c1!=null){
                    DedNode conc = DedNode.getNegationStrict(d);
                    in.add(RAA,conc,c1);
                    InferenceSL.DNE_prime(in);
                    out=true;
                }
            }
        }
//        while (it.hasNext()) {
//            DedNode d = it.next(), nd = DedNode.getNegation(d);
//            if(in.getInDed(d)==null && in.getInDed(nd)==null){
//                //Deduction sp = new Deduction(in,null);
//                Deduction sp = Deduction.subProof(in, d, ARED, null);
//                //sp.add(ARED, d, (DedNode[]) null);
//                DedNode c1 = InferenceSL.inferContradiciton_Indirect(sp, d);
//                if (c1!=null){
//                    DedNode conc = DedNode.getNegationStrict(d);
//                    in.add(RAA,conc,c1);
//                    InferenceSL.DNE_prime(in);
//                    out=true;
//                }
//            }
//        }
        return out;
    }
    
    public static boolean RAA_recursive(Deduction in, DedNode conc){
        DedNode ass = DedNode.getNegation(conc);
        
        Deduction sp = Deduction.subProof(in, ass, ARED, null);
        
        DedNode contra = InferenceSL.recursiveInferContradictions(sp, ass);
        
        if (contra!=null) {
            DedNode sconc = DedNode.getNegationStrict(ass);
            sconc.setSubProof(sp);
            in.add(RAA, sconc, ass, contra);
            InferenceSL.DNE_prime(in);
            return true;            
        }
        return false;
    }
  
    /**
     * <b>CASCADING</b><p>
     * Returns (A∧(¬A)) for some formula A if:
     * <ul><li>A is in the deduction<br>&emsp  <i>or</i><br> (¬A) is in the deduction;<br><i>&emsp &emsp &emsp and
     * <li>A is provable by {@link #inferCascades(logic.Deduction, logic.DedNode) inferCascades(A)}<br><i>&emsp or
     * </i><br>(¬A) is provable by {@link #inferCascades(logic.Deduction, logic.DedNode) inferCascades(¬A)}
     * </ul>
     * If formula is returned, the deduction is augmented with (A∧(¬A)) and closed
     */
    public static DedNode inferContradiciton_Direct(Deduction in, DedNode ass){
        it = in.ded.iterator();
        
        while (it.hasNext()) {
            DedNode d = it.next();
            DedNode contra = DedNode.getNegation(d);
            if (in.inferComposites(contra)) {
                contra = in.getInDed(contra);
                DedNode conj = DedNode.getConjunction(d, contra);
                in.add(CI, conj, d,contra);
                conj = in.getInDed(conj,ass);
                in.closeDeduction(conj);
                return conj;
            }            
        }
        return null;
    }
    /**
     * <b>CASCADING</b><p>
     * Returns (A∧(¬A)) for some formula A if:
     * <ul><li>A occurs in the deduction<br>&emsp &emsp<i>or</i><br> There is a Formula in the deduction constructed of A, as in
     * {@link DedManager#getRAAFodder(logic.Deduction) DedManager.getRAAFodder(in)}
     * <br><i>&emsp and
     * <li>A is provable by {@link #inferCascades(logic.Deduction, logic.DedNode) inferCascades(A)}<br><i>&emsp and
     * <li> (¬A) is provable by {@link #inferCascades(logic.Deduction, logic.DedNode) inferCascades(¬A)}
     * </ul>
     * If formula is returned, the deduction is augmented with (A∧(¬A)) and closed
     */
    public static DedNode inferContradiciton_Indirect(Deduction in, DedNode ass){
        it = in.ded.iterator();
        
        Set<DedNode> ants = in.getRAAFodder();
        Iterator<DedNode> antit = ants.iterator();
        
        while (antit.hasNext()) {
            DedNode c1 = antit.next();
            if (in.inferComposites(c1)) {
                DedNode c2 = DedNode.getNegation(c1);
                if (in.inferComposites(c2)) {
                    c1 = in.getInDed(c1); c2 = in.getInDed(c2);
                    DedNode conj = DedNode.getConjunction(c1, c2);
                    in.add(CI, conj, c1,c2);
                    conj = in.getInDed(conj,ass);
                    in.closeDeduction(conj);
                    return conj;
                }
            }
        }
        
        return null;
    }
    
    public static DedNode recursiveInferContradictions(Deduction in, DedNode ass){
        it = in.ded.iterator();
        
        Set<DedNode> ants = in.getRAAFodder();
        Iterator<DedNode> antit = ants.iterator();
        
        while (antit.hasNext()) {
            DedNode d = antit.next(), nd = DedNode.getNegation(d);
            if (in.prove(d)) {
                if (in.prove(nd)) {
                    d = in.getInDed(d, ass); nd = in.getInDed(nd, ass);
                    DedNode contra = DedNode.getConjunction(d, nd);
                    in.add(CI, contra, d,nd);
                    in.closeDeduction(contra);
                    return contra;
                }
            }
        }
        return null;   
    }
    
}