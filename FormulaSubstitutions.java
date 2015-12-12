package logic;

import java.util.Iterator;
import logic.formula.Element;
import logic.formula.Equality;
import logic.formula.PrimeFormula;
import logic.formula.Quantifier;

public class FormulaSubstitutions{
    
    public static Formula prenexNormalForm(Formula in){
        in = FormulaSubstitutions.eliminateBiconditionals(in);
        ConstructionTree cons = in.getConstruction();
        while (!cons.get(0).isPrenex()) {
            for (Formula f : cons) {
                Formula prenexf;
                prenexf = prenexConditionalSchema(f);;
                prenexf = prenexConnectiveSchema1(prenexf);
                prenexf = prenexConnectiveSchema2(prenexf);
                prenexf = prenexNegationSchema(prenexf);
                cons.replace(f, prenexf);
                //in.construction.replace(f, prenexf);
            }
            //in = in.construction.get(0);
        }
        
        return cons.get(0);
    }
    
    public static Formula alphabeticVariant(Formula in, char newx){
        if (!in.connective.isQuantifier()) return in;
        return Formula.getQuantification(in.getChild(0), in.connective.castToQuantifier().symbol, newx, in.connective.castToQuantifier().boundVariable.symbol);
    }

    /**Returns a Formula according to the following substitution axioms
     *<ul><li>(Qxφ∧ψ) ⟹ Qx(φ∧ψ)</>
     * <li>(Qxφ∨ψ) ⟹ Qx(φ∨ψ)</ul>
     * @return
     */
    public static Formula prenexConnectiveSchema1(Formula in) {
        if (!(in.connective.isConjunction() || in.connective.isDisjunction()) || !in.getChild(0).connective.isQuantifier()) return in;
        
        Formula ant = in.getChild(0).getChild(0), cons = in.getChild(1);
        Formula Q = in.getChild(0);
        char v = Q.connective.castToQuantifier().boundVariable.symbol;
        char q = Q.connective.symbol;
        
        if (cons.containsVariableFree(v)) {
            v = Syntax.getFreeVariable(in);
            Q = alphabeticVariant(Q,v);
            ant = Q.getChild(0);
        }
        
        if (in.connective.isConjunction()) {
            return Formula.getQuantification(Formula.getConjunction(ant, cons), q, v);
        }else if (in.connective.isDisjunction()) {
            return Formula.getQuantification(Formula.getDisjunction(ant, cons), q, v);
        }else{
            throw new java.lang.RuntimeException();
        }
    }

    /**Returns a Formula according to the following substitution axioms
     *<ul><li>(φ∧Qxψ) ⟹ Qx(φ∧ψ)</li>
     * <li>(φ∨Qxψ) ⟹ Qx(φ∨ψ)</li>
     * <li>(φ⟶Qxψ) ⟹ Qx(φ⟶ψ)</li></ul>
     * @return
     */
    public static Formula prenexConnectiveSchema2(Formula in) {
        if (!(in.connective.isConjunction()||in.connective.isDisjunction()||in.connective.isConditional()) 
                || !in.getChild(1).connective.isQuantifier()) return in;
        Formula Q = in.getChild(1);
        char v = Q.connective.castToQuantifier().boundVariable.symbol;
        char q = Q.connective.symbol;
        Formula ant = in.getChild(0), cons = Q.getChild(0);
        
        if (ant.containsVariableFree(v)) {
            v = Syntax.getFreeVariable(in);
            Q = alphabeticVariant(Q,v);
            cons = Q.getChild(0);
        }
        if (in.connective.isConditional()) {
            return Formula.getQuantification(Formula.getConditional(ant, cons), q, v);
        }else if (in.connective.isConjunction()) {
            return Formula.getQuantification(Formula.getConjunction(ant, cons), q, v);
        }else if (in.connective.isDisjunction()) {
            return Formula.getQuantification(Formula.getDisjunction(ant, cons), q, v);
        }else{
            throw new java.lang.RuntimeException();
        }
    }

    /**Returns a Formula according to the following substitution axiom
     * <li>(Qxφ⟶ψ) ⟹ Q<sup>i</sup>x(φ⟶ψ)</li></ul>
     * @return
     */
    public static Formula prenexConditionalSchema(Formula in) {
        if (!in.connective.isConditional() || !in.getChild(0).connective.isQuantifier()) {
            return in;
        }
        Formula ant = in.getChild(0).getChild(0), cons = in.getChild(1);
        Formula quant = in.getChild(0);
        char Q = in.getChild(0).connective.castToQuantifier().symbol;
        Q = Syntax.getComplementConnective(Q);
        char v = in.getChild(0).connective.castToQuantifier().boundVariable.symbol;
        
        if (cons.containsVariableFree(v)) {
            v = Syntax.getFreeVariable(in);
            quant = alphabeticVariant(quant,v);
            ant = quant.getChild(0);
        }
        
        return Formula.getQuantification(Formula.getConditional(ant, cons), Q, v);
    }

    /**Returns a Formula according to the following substitution axiom
     *<ul><li>(¬Qxφ) ⟹ Q<sup>I</sup>x(¬φ)</li></ul>
     * @return
     */
    public static Formula prenexNegationSchema(Formula in) {
        if (!in.connective.isNegation() || !in.getChild(0).connective.isQuantifier()) return in;
        Formula q = in.getChild(0);
        char Q = Syntax.getComplementConnective(q.connective.castToQuantifier().symbol);
        return Formula.getQuantification(Formula.getNegation(q.getChild(0)), Q, q.connective.castToQuantifier().boundVariable.symbol);
    }

    public static Formula eliminateDisjunctions(Formula f) {
        if (!Syntax.containsSymbol(f.form, Logical.disjunction)) return f.clone();
        Formula out = f.clone();
        ConstructionTree ct = out.getConstruction();
        for (Formula con : ct) {
            ct.replace(con, disjunctionConversionSchema(con));
        }
        out = ct.get(0);
        out = eliminateDoubleNegations(out);
        return out;
    }
    
    public static Formula disjunctionConversionSchema(Formula in){
        if (!in.connective.isDisjunction()) return in;
        Formula dj1 = in.getChild(0), dj2 = in.getChild(1);
        dj1 = Formula.getNegation(dj1); dj2 = Formula.getNegation(dj2);
        return Formula.getNegation(Formula.getConjunction(dj1, dj2));
    }

    public static Formula eliminateBiconditionals(Formula f) {
        if (!Syntax.containsSymbol(f.form, Logical.biconditional)) return f.clone();
        Formula out = f.clone();
        ConstructionTree ct = out.getConstruction();
        for (Formula cons : ct) {
           ct.replace(cons, biconditionalConversionSchema(cons));
        }
        return ct.get(0);
    }
    
    public static Formula biconditionalConversionSchema(Formula in){
        if (!in.connective.isBiconditional()) return in;
        Formula ant = in.getChild(0), cons = in.getChild(1);
        return Formula.getConjunction(Formula.getConditional(ant, cons), Formula.getConditional(cons, ant));
    }

    
    public static Formula eliminateConditionals(Formula in){
        Formula out = eliminateBiconditionals(in);
        if (!Syntax.containsSymbol(out.form, Logical.conditional)) return out;
        ConstructionTree ct = out.getConstruction();
        for (Formula construction : ct) {
            ct.replace(construction, conditionalConversionSchema(construction));
        }
        return ct.get(0);
    }
    
    public static Formula conditionalConversionSchema(Formula in){
        if (!in.connective.isConditional()) return in;
        Formula ant = in.getChild(0), cons = in.getChild(1);
        return Formula.getDisjunction(Formula.getNegation(ant), cons);
    }

    public static Formula eliminateConjunctions(Formula f) {
        if (!Syntax.containsSymbol(f.form, Logical.conjunction)) return f.clone();
        Formula out = f.clone();
        ConstructionTree ct = out.getConstruction();
        for (Formula con : ct) {
            ct.replace(con, conjunctionConversionSchema(con));
        }
        out = ct.get(0);
        out = eliminateDoubleNegations(out);
        return out;
        
    }
    
    public static Formula conjunctionConversionSchema(Formula in){
        if (!in.connective.isConjunction()) return in;
        Formula cj1 = in.getChild(0), cj2 = in.getChild(1);
        return Formula.getNegation(Formula.getDisjunction(Formula.getNegation(cj1), Formula.getNegation(cj2)));
    }

    public static Formula eliminateQuantifiers(Formula f, char Q) {
        Q = Syntax.getForm(Q);
        if (!Syntax.containsSymbol(f.form, Q)) return f.clone();
        Formula out = f.clone();
        ConstructionTree ct = out.getConstruction();
        for (Formula con : ct) {
            ct.replace(con, quantifierConversionSchema(con,Q));
        }
        out = ct.get(0);
        out = eliminateDoubleNegations(out);
        return out;
    }
    
    public static Formula quantifierConversionSchema(Formula in, char q){
        if (!in.connective.isQuantifier(q)) return in;
        char v = in.connective.castToQuantifier().boundVariable.symbol;
        char newq = Syntax.getComplementConnective(q);
        Formula f = in.getChild(0);
        return Formula.getNegation(Formula.getQuantification(Formula.getNegation(f), newq, v));
    }
    
    public static Formula eliminateDoubleNegations(Formula in){
        if (!in.form.contains("(¬(¬")) return in.clone();
        Formula out = in.clone();
        ConstructionTree ct = out.getConstruction();
        for (Formula con : ct) {
            ct.replace(con, DNESchema(con));
        }
        return ct.get(0);
    }
    
    public static Formula DNESchema(Formula in){
        if (!in.connective.isNegation() || !in.getChild(0).connective.isNegation()) return in;
        return in.getChild(0).getChild(0);
    }
    
    public static Formula contraPositive(Formula in){
        if (!in.connective.isConditional()) return in;
        
        Formula ant = in.getChild(0), cons = in.getChild(1);
        ant = Formula.getNegation(ant);
        cons = Formula.getNegation(cons);
        Formula out = Formula.getConditional(cons, ant);
        return out;
    }
    
    public static Formula apprachingEquivalence(Formula in, Formula master, Equality eq){
        if (!in.isTermVariant(master)) return null;
        in = in.clone();
        for (int i = 0; i < in.length; i++) {
            if (in.get(i).equals(master.get(i))) continue;
            PrimeFormula tp = (PrimeFormula)in.get(i), mp = (PrimeFormula)master.get(i);
            for (int j = 0; j < tp.arguments.length; j++) {
                tp.replaceTerm(tp.arguments[j], tp.arguments[j].getApproachingTermThroughEquivalence(mp.arguments[j], eq));
            }
        }
        String s="";
        for (Element e : in) s+=e.form;
        return new Formula(s,in.logic);
    }
    
    
}