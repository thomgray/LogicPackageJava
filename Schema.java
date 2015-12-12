package logic;

import logic.formula.Element;

public abstract class Schema<T>{
    
    public static final Schema<Integer> 
            EVEN_NUMBERS = new Schema<Integer>() {
        @Override
        public boolean condition(Integer x) {
            return x%2==0;
        }
    },
            ODD_NUMBERS = new Schema<Integer>() {
        @Override
        public boolean condition(Integer x) {
            return x%2==1;
        }
    },            
            PRIME_NUMBERS = new Schema<Integer>() {
        @Override
        public boolean condition(Integer x) {
            if (x<2) return false;
            for (int i = 2; i < x; i++) if (x%i==0) return false;
            return true;
        }
    }
            ;
    
    private Object[] info;
    
    public Schema(){}
    public Schema(Object ... in){info = in;}
    
    /**
     * Implement the condition under which element x is in the set
     * @param <T> the object-type over which the schema presides
     * @param x the element to be evaluated
     * @return 
     */
    public abstract boolean condition(T x);
    
    public static <T> Schema<T> disjunctiveSchema(Schema<T> ... in){
        return new Schema<T>() {
            @Override
            public boolean condition(T x) {
                for (int i = 0; i < in.length; i++) {
                    if (in[i].condition(x)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    public static <T> Schema<T> conjunctiveSchema(Schema<T> ... in){
        return new Schema<T>() {
            @Override
            public boolean condition(T x) {
                for (int i = 0; i < in.length; i++) {
                    if (!in[i].condition(x)) {
                        return false;                        
                    }
                }
                return true;
            }
        };
    }
    
    public static Schema<Integer> lessThan(int i){
        return new Schema<Integer>() {
            @Override
            public boolean condition(Integer x) {
                return x<i;
            }
        };
    }
    
    public static Schema<Integer> moreThan(int i){
        return new Schema<Integer>() {
            @Override
            public boolean condition(Integer x) {
                return x>i;
            }
        };
    }
    
    public static Schema<Formula> containsElement(Element in){
        return new Schema<Formula>() {
            @Override
            public boolean condition(Formula x) {
                for (Element el : x) {
                    if (el.equals(in)) return true;
                }
                return false;
            }
        };
    }
    
}