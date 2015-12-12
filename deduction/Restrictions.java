package logic.deduction;

import logic.Set;

class Restrictions{
    private final Set<Restriction> data;
    
    Restrictions(){
        data = new Set<>();
    }
    
    
    void imposeRestriction(DedNode d, DedNode conc, String inf){
        if (this.allows(d, conc, inf))
            data.add(new Restriction(d,conc,inf));
    }
    
    boolean allows(DedNode d, DedNode conc, String inf){
        for (Restriction res : data) {
            if (res.restricts(d, conc, inf)) return false;
        }
        return true;
    }
    
    void liftRestriction(DedNode d, DedNode conc, String inf){
        for (Restriction res : data) {
            if (res.restricts(d, conc, inf)){
                data.removeElement(res);
                return;
            }
        }
    }

    void print(){
        for (Restriction d : data) {
            System.out.println("Line: "+d.node+", Conc: "+d.conc+", Inference: "+d.inference);
        }
    }

    @Override
    public Restrictions clone() {
        Restrictions out = new Restrictions();
        for (Restriction r : data) {
            out.data.add(r);
        }
        return out;
    }
}



class Restriction{
    DedNode node;
    DedNode conc;
    String inference;
    Restriction(DedNode line, DedNode conclusion, String inf){
        node = line;
        conc = conclusion;
        this.inference = inf;
    }
    boolean restricts(DedNode d, DedNode c, String inf){
        if (conc==null) return (d.equals(node) && inference.equals(inf));
        else if (node==null) return (conc.equals(c) && inference.equals(inf));
        
        return (conc.equals(c) && node.equals(d) && inference.equals(inf));
    }
}