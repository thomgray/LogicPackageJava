package logic.deduction;
import java.util.HashMap;
import logic.Formula;
import logic.Logical;
import logic.Schema;
import logic.Sequence;
import logic.Set;
import logic.Syntax;
import static logic.deduction.InferencePL.*;
import static logic.deduction.InferenceSL.*;
import logic.formula.Term;

public class Deduction{        
    
    public Sequence<DedNode> ded = new Sequence();
    DedNode conclusion;
    DedNode[] premises;
    
    int logic;
    public boolean valid;
    int tier;
    
    Restrictions restricts = new Restrictions();
    Set<Term> arbitraries = new Set();
    
    public Deduction(Formula[] prems, Formula conc){        
        logic = conc.logic;
        for (int i = 0; i < prems.length; i++) {
            if (prems[i].logic!=logic) {
                throw new java.lang.IllegalArgumentException("Logic mis-match");
            }
        }
        tier=0;
        conclusion = new DedNode(conc);
        premises = new DedNode[prems.length];
        for (int i = 0; i < prems.length; i++) {
            premises[i] = new DedNode(prems[i]);
        }
        
        for (int i = 0; i < premises.length; i++) {
            add(PREM, premises[i], (DedNode[]) null);
        }        
        
        proveProcedure();
    }
    
    public Deduction(Formula ... args){
        this(tools.Arrays.subArray(args, 0, args.length-1), args[args.length-1]);
    }
    
    public Deduction(String ... args){
        this(Formula.stringsToFormulas(args));
    }
    
    private Deduction(){}
    
    static Deduction subProof(Deduction in, DedNode ass, String inf, DedNode conc){
        Deduction sp = new Deduction();
        sp.restricts = in.restricts.clone();
        
        sp.premises = in.premises.clone();   
        int firstline;
        
        if (in.ded.length>0) firstline = in.ded.getLast().line+1;
        else firstline = 1;
            
        sp.tier = in.tier+1;
        
        ass.inference = inf;
        ass.line = firstline;
        ass.dependencyNums = new int[]{firstline};
        sp.ded.add(ass);
        
        for (int i = 0; i < in.ded.length; i++) {
            DedNode re = in.ded.get(i).clone();
            re.line = sp.ded.getLast().line+1;
            re.inference = REIT;
            re.resetDependency(in.ded.get(i));  
            sp.ded.add(re);
        }
        sp.conclusion = conc;
        return sp;
        
    }
    
    private void proveProcedure() {
        prove(conclusion);
        
        
        valid = this.getValidity(conclusion);
        
        if (!valid){
            System.out.println("PROVING RAA RECURSIVE...");
            valid = InferenceSL.RAA_recursive(this, conclusion);
        }
                
        if (valid) {    
            closeDeduction();
            print();
            System.out.println("Proven: "+tools.Arrays.getString(premises)+" "+Logical.CDent+" "+conclusion.form);
        }else{
            print();
            System.out.println("Not Proven: "+tools.Arrays.getString(premises)+" "+Logical.CnDent+" "+conclusion.form);
        }
    }
  
    
    /**
     * <dl><dt><b>Recurs:<p>
     * <dt>&emsp&ensp Direct Invokes:<dd>{@link DedManager#directProof(logic.Deduction, logic.DedNode) directProof(ded)}
     * <dt>&emsp&ensp Indirect Invokes:<dd> {@link #prove(logic.DedNode) prove(conc.getParents(0))}
     * <dd> {@link #prove(logic.DedNode) prove(conc.getParents(1))}
     * </dl>
     * @param conc
     * @return 
     */
    boolean prove(DedNode conc){
        System.out.println("Proving "+conc.form+", tier "+tier);
        if (inferComposites(conc)) return true;
        
        switch (conc.connective.symbol) {
            case Syntax.conjunction:
                if (this.prove(conc.getChild(0)))
                    if (this.prove(conc.getChild(1)))
                        if (InferenceSL.CI_prime(this, conc)) return true;
                break;
            case Syntax.conditional:
                if (InferenceSL.CP_recursive(this, conc)) return true;
                break;
            case Syntax.biconditional:
                if (this.prove(conc.getChild(0))) 
                    if (this.prove(conc.getChild(1)))
                        if (InferenceSL.BI_prime(this, conc)) return true; 
                break;        
            case Syntax.disjunction:
                if (this.prove(conc.getChild(0)))
                    if (InferenceSL.DI_prime(this, conc)) return true;
                else if (this.prove(conc.getChild(1))) 
                    if (InferenceSL.DI_prime(this, conc)) return true;                  
                break; 
            case Syntax.negation:
                if (conc.getChild(0).connective.isNegation()) {
                    if (this.prove(DedNode.DNE(conc))){
                        InferenceSL.DNI_prime(this, conc);
                        return true;
                    }
                }
                break;
            default:                
                break;
        }         

        int c;
        do {
            c = ded.length;
            
        if (InferencePL.EI_recursive(this, conc)) return true;
        if (InferencePL.UI_recursive(this, conc)) return true;
        
        InferenceSL.MP_recursive(this);
        InferenceSL.MT_recursive(this);
        if (InferenceSL.RAA_cascading(this, conc)) return true;
        if (InferenceSL.DE_recursive(this, conc)) return true;
            
        } while (c>ded.length);
        
        return false;        
    }
    
    public boolean inferPrimes(DedNode conc){
        boolean out;
        
        InferencePL.EQE_primeStep3(this, conc);
        
        do {
            out=false;
            out=out||InferenceSL.BE_prime(this);
            out=out||InferenceSL.CE_prime(this);
            out=out||InferenceSL.DNE_prime(this);
            out=out||InferenceSL.MP_prime(this);
            out=out||InferenceSL.MT_prime(this);  
            
            out=out||InferencePL.UE_prime(this);
            out=out||InferencePL.EE_prime(this);
            
            out=out||InferencePL.EQE_primeStep1(this, conc);
            out=out||InferencePL.EQE_primeStep2(this, conc);
            //out=out||InferencePL.EQE_generic(this);
            
            if(this.getInDed(conc)!=null) return true; 
        } while (out);
        
        if (InferenceSL.BI_prime(this, conc)) return true;
        if (InferenceSL.CI_prime(this, conc)) return true;
        if (InferenceSL.DNI_prime(this, conc)) return true;
        if (InferenceSL.DI_prime(this, conc)) return true;

        return false;
    }
    
    public boolean inferComposites(DedNode conc){
        boolean out;
        if (inferPrimes(conc)) return true;
        
        do {   
            out=false;
            out=out||InferenceSL.MP_cascading(this);
            out=out||InferenceSL.MT_cascading(this);
            
            if (InferenceSL.CP_cascading(this, conc)) return true;
            //out=out||InferenceSL.RAA_genericCascading(this);
            if (InferencePL.EI_recursive(this, conc)) return true;
            if (InferencePL.UI_recursive(this, conc)) return true;
            
            if(getInDed(conc)!=null) return true;    
        } while (out);
        
        return false;
    }
    
    void add(String inf, DedNode in, DedNode ... args){
        ded.add(in);
        if (ded.length==1) in.line=1;
        else in.line = ded.get(-2).line+1 ;    
        
        in.inference = inf;
        
        System.out.println("LINE ADDED: "+in.line+"\t"+in.form+"\t"+in.inference);
        if (in.line%100==0) this.print();
        if (in.line>500) System.exit(0);
        
        switch (inf) {
            case PREM: case ACP: case ADE: case ARED:
                if(args!=null)throw new java.lang.IllegalArgumentException(inf+" takes no dependencies");
                in.dependencyNums = new int[]{in.line};  
                System.out.println(inf+" "+in.form+" added");
                return;
            case EQI:
                if(args!=null)throw new java.lang.IllegalArgumentException(inf+" takes no dependencies");
                System.out.println(inf+" "+in.form+" added");
                return;
            case CP: case RAA: case CPDE:
                if (args.length!=2)throw new IllegalArgumentException(inf+" takes 2 dependencies");
                
                if (in.subProof==null) throw new IllegalArgumentException("Add the subproof before adding to the deduction! Node "+in.form);
                        
                in.line = in.subProof.ded.getLast().line+1;
                
                int x=-1;
                for (int i = 0; i < args.length; i++) {
                    in.setDependency(args[i]);
                    
                    if(args[i].inference==ACP||args[i].inference==ARED||args[i].inference==ADE) x=i;
                    else in.dependencyNums = tools.Arrays.union(in.dependencyNums, args[i].dependencyNums);                                            
                }
                if (x>=0) in.dischargeDependency(args[x]);                
                tools.Arrays.order(in.dependencyNums);  
                return;
            case CE: case DI: case DNI: case DNE: case BI: case BE: case EI: case UI:
                if (args.length!=1) throw new IllegalArgumentException(inf+" takes 1 dependency");
                break;
            case UE: case EE:
                if (args.length!=1) throw new IllegalArgumentException(inf+" takes 1 dependency");                
                break;
            case CI: case MP: case MT: case EQE:
                if (args.length!=2) throw new IllegalArgumentException(inf+" takes 2 dependencies");
                break;
            case DE:
                if (args.length!=3) throw new IllegalArgumentException(inf+" takes 3 dependencies");
                break;
            default:
                throw new AssertionError();
        }
        for (int i = 0; i < args.length; i++) {
            in.setDependency(args[i]);
            in.dependencyNums = tools.Arrays.union(in.dependencyNums, args[i].dependencyNums);
        }
        tools.Arrays.order(in.dependencyNums);
        for (int i = 0; i < in.dependencies.length; i++) {
            if (in.dependencies[i].line==0) {
                throw new java.lang.RuntimeException("Dependedncies must be in the deduction: "+in.form+" "+in.inference+" "+in.dependencies[i].form);
            }
        }
    }
    
    boolean isInformative(DedNode in){
        for (int i = 0; i < ded.length; i++) {
            if (ded.get(i).equals(in)) {
                return false;
            }
        }
        return true;
    }    
    
    /**
     * returns <ul><li>the dedIndex of the node equivalent to the parameter argument
     * <li>-1 if node isn't contained in the array</ul>
     * @param in
     * @return 
     */
    int getIndex(DedNode in){
        return ded.getIndex(in);        
    }
    
    DedNode getInDed(DedNode in){
        return ded.getEquivalent(in);        
    }
    
    DedNode getInDed(DedNode conc, DedNode ass){
        ass = this.getInDed(ass);
        for (int i = 0; i < ded.length; i++) {
            DedNode d = ded.get(i);
            if (d.equals(conc) && tools.Arrays.contains(d.dependencyNums, ass.line)) {
                return d;
            }
        }
        
        for (int i = 0; i < ded.length; i++) {
            DedNode d = ded.get(i);
            if (d.equals(conc)) {
                DedNode step1 = DedNode.getConjunction(ass, d);
                this.add(CI, step1, ass, d);
                DedNode step2 = step1.getChild(1);
                this.add(CE, step2, step1);
                return step2;
            }
        }
        return null;
    }

    boolean getValidity(DedNode conc) {
        int x = this.getIndex(conc);
        if (x>=0) {
            return true;
        }else{
            return false;
        }
    }
    
    Set<DedNode> getRAAFodder(){
        Schema<Formula> saturated = new Schema<Formula>() {
            @Override
            public boolean condition(Formula x) {
                for (int i = 0; i < x.form.length(); i++) {
                    if (Syntax.isVariable(x.form.charAt(i))) return false;
                }
                return true;
            }
        };
        Set<DedNode> out = new Set();
        for (int i = 0; i < ded.length; i++) {
            if (!out.contains(DedNode.getNegation(ded.get(i)))) {
                 out.add(ded.get(i));
            }                       
        }
        out.subSet(saturated);
        out.sortAscending();
        return out;
    }


    void closeDeduction(){        
        closeDeduction(ded.getEquivalent(conclusion));
    }
    void closeDeduction(DedNode conc){
        if (!getValidity(conc)) return;  
        
        System.out.println("CLOSING:");
        Sequence<DedNode> newded = new Sequence();
        int[] list = new int[0];
                
        HashMap<Integer,Integer> conversion1 = new HashMap();
        HashMap<Integer,Integer> conversion2 = new HashMap();
        
        int m=getFirstLine()*-1;
                
        for (int i = ded.length-1; i>=0; i--) {
            boolean include=false;
            
            if (ded.get(i)==conc) include=true;
            if (tools.Arrays.getIndex(list, ded.get(i).line)>=0) include=true;                            
            
            if (include){
                newded.addAtBottom(ded.get(i));
                list = tools.Arrays.union(list, ded.get(i).inferenceNums);
                if (ded.get(i).subProof!=null) {
                    list = ded.get(i).subProof.getSPInferenceNums(list);
                }
            }    
        }
        ded=newded;
        
        int x = conversion1.size();
        conversion1 = this.getConversion(conversion1, m);
        int d = conversion1.size()-x;
        m=m-d;

        for (int i = m+1; i < 0; i++) {
            conversion2.put(i, i*-1);
        }
        
        this.conversion1(conversion1);

        
        this.conversion2(conversion2);
        
        print();
    }
    
    
    private HashMap<Integer,Integer> getConversion(HashMap<Integer,Integer> map, int m){
        for (int i = 0; i < ded.length; i++) {
            if (ded.get(i).subProof!=null) {
                int x = map.size();
                map = ded.get(i).subProof.getConversion(map, m);
                int d = map.size()-x;
                m=m-d;
            }
            map.put(ded.get(i).line, m);
            m--;
        }
        return map;        
    }
    
    private void conversion1(HashMap<Integer,Integer> c1){
        for (int i = 0; i < ded.length; i++) {
            if (ded.get(i).subProof!=null) {
                ded.get(i).subProof.conversion1(c1);
            }            
            if (c1.containsKey(ded.get(i).line)) ded.get(i).line = c1.get(ded.get(i).line);
            for (int j = 0; j < ded.get(i).dependencyNums.length; j++) {
                if (c1.containsKey(ded.get(i).dependencyNums[j])) {
                    ded.get(i).dependencyNums[j] = c1.get(ded.get(i).dependencyNums[j]);
                }
            }
            for (int j = 0; j < ded.get(i).inferenceNums.length; j++) {
                if (c1.containsKey(ded.get(i).inferenceNums[j])) {
                    ded.get(i).inferenceNums[j] = c1.get(ded.get(i).inferenceNums[j]);
                }
            }
        }
    }
    
    private void conversion2(HashMap<Integer, Integer> c2){
        for (int i = 0; i < ded.length; i++) {
            if (ded.get(i).subProof!=null) {
                ded.get(i).subProof.conversion2(c2);
            }            
            if (c2.containsKey(ded.get(i).line)) ded.get(i).line = c2.get(ded.get(i).line);
            for (int j = 0; j < ded.get(i).dependencyNums.length; j++) {
                if (c2.containsKey(ded.get(i).dependencyNums[j])) {
                    ded.get(i).dependencyNums[j] = c2.get(ded.get(i).dependencyNums[j]);
                }
            }
            for (int j = 0; j < ded.get(i).inferenceNums.length; j++) {
                if (c2.containsKey(ded.get(i).inferenceNums[j])) {
                    ded.get(i).inferenceNums[j] = c2.get(ded.get(i).inferenceNums[j]);
                }
            }
        }
    }    
    
    int[] getSPInferenceNums(int[] in){
        for (int i = 0; i < ded.length; i++) {
            if (ded.get(i).subProof!=null) {
                in = ded.get(i).subProof.getSPInferenceNums(in);
            }
            in = tools.Arrays.union(in, ded.get(i).inferenceNums);
        }
        return in;
    }
    
    Set<Term> getTerms(){
        Set<Term> out = new Set();
        if (conclusion!=null) out.union(conclusion.getTerms());
        for (DedNode d : ded) {
            out.union(d.getTerms());
        }
        return out;
    }
    
    
    void print(){
        System.out.printf("%n%-5s %-10s %-30s %25s %n", "Line", "Dep#", "", "Inference");
        System.out.println("------------------------------------------------------------------------");
        printDed(this, 0);
    }
    
    private static void printDed(Deduction in, int off){
        for (int i = 0; i < in.ded.length; i++) {
            DedNode bit = in.ded.get(i);
            if (bit.subProof!=null) {
                printDed(bit.subProof, off+1);
            }
            String dn = tools.Arrays.getString(bit.dependencyNums), inn = tools.Arrays.getString(bit.inferenceNums);
            for(int j = 0; j<off; j++) System.out.print("   ");
            System.out.printf("%-5d %-10s %-20s %10s %-30s %n", bit.line, dn, bit.form, inn, bit.inference);
        }        
    }

     int getFirstLine() {
        if (ded.get(0).subProof!=null) {
            return ded.get(0).subProof.getFirstLine();
        }else{
            return ded.get(0).line;
        }
    }

    
    
 
}