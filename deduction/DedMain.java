package logic.deduction;

import logic.Formula;
import logic.FormulaSubstitutions;
import logic.TruthTable;
import thom.swing.GUI;


public class DedMain{    
    
    
    
    public static void main(String[] args) {
//
        Formula f1 = new Formula("(P*(~Q))");
        Formula f3 = new Formula("Q"),
            //f4 = new Formula("(~P)"),
            f4 = new Formula("P")  ;  

                //f4 = FormulaSubstitutions.eliminateConditionals(f4);
                f1 = FormulaSubstitutions.eliminateDisjunctions(f1);
                ; 
                System.out.println(f3.form+" "+f4.form);
        
        Formula[] prems = new Formula[]{f3,f4};
        Deduction ded = new Deduction(
                
                f1,f3,f4
                
            );
        
        TruthTable tt = new TruthTable(f3,f4);
        tt.printTable();    
    }
    



         

}