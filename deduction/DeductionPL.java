package logic.deduction;

import java.util.Iterator;
import logic.Formula;
import logic.FormulaSubstitutions;
import logic.Logical;
import logic.Set;
import static logic.deduction.DedNode.*;
import static logic.deduction.InferencePL.*;
import logic.formula.Term;

/**
 *
 * @author thomdikdave
 */
public class DeductionPL extends Deduction{
    
    public DeductionPL(Formula ... in){
        super(in);   
    }
    
    public static void main(String[] args) {
        Formula f = new Formula("a=d");
        Formula g = new Formula("a=b"), h = new Formula("f(c)=b"), i = new Formula("d=f(c)");
        Formula j = FormulaSubstitutions.eliminateConditionals(g);
        
        j=FormulaSubstitutions.eliminateDisjunctions(j);
        j=FormulaSubstitutions.eliminateConjunctions(j);
        DeductionPL ded = new DeductionPL(g,h,i,f);
    }
    
    void proveProcedure() {
        prove(conclusion);
                
        System.out.println(conclusion.connective.form);
        this.print();
        
        valid = this.getValidity(conclusion);
        
        //if (!valid) valid = InferenceSL.RAA_recursive(this, conclusion);
                
        if (valid) {    
            closeDeduction();
            print();
            System.out.println("Proven: "+tools.Arrays.getString(premises)+" "+Logical.CDent+" "+conclusion.form);
        }else{
            print();
            System.out.println("Not Proven: "+tools.Arrays.getString(premises)+" "+Logical.CnDent+" "+conclusion.form);
        }
    }
    
    @Override
    boolean prove(DedNode conc){
        int x;
        do {
            x = ded.length;
//            InferencePL.EE_prime(this);
            //InferencePL.inferPrimes(this, conc);
//            InferencePL.UE_prime(this);
//            InferencePL.EI_prime(this, conc);
//            InferencePL.EI_recursive(this, conc);
            //InferencePL.EQI_prime(this, conc);
            //InferencePL.EQE_primeStep1(this, conc);
            //InferencePL.EQE_primeStep2(this, conc);
            InferencePL.EQE_generic(this);
//            InferencePL.UI_recursive(this,conc);
            
        } while (ded.length>x);
        
        
        
        
        //super.prove(conc);
        return this.getValidity(conc);
    }
    
    
}

