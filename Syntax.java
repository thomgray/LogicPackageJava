package logic;

/**
 *Carries static methods for basic syntactic operations
 * @author thomdikdave
 */
public abstract class Syntax{
    
    /**
     * connectives
     */
    public static final char
            biconditional = '⟷', conditional = '⟶', 
            conjunction = '∧', disjunction = '∨', negation = '¬',
            qUniversal = '∀', qExistential = '∃',
            necessarily = '□', possibly = '◇'
            ;
    public static final String ALEPH_0="ℵ⁰", ALEPH_1= "ℵ¹";
    public static final char ℕ = 'ℕ', ℝ = 'ℝ', ℤ = 'ℤ' ;
    
    
    public static boolean isRelation(char in){
        switch (in) {
            case 'B':
            case 'C':
            case 'D':    
            case 'F':
            case 'G':
            case 'H':  
            case 'J':    
            case 'K':    
            case 'L':    
            case 'M':    
            case 'N':    
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':       
                return true;
            default:
                return false;
        }        
    }
    
    public static boolean isVariable(char in){
        switch (in) {
            case 'm':    
            case 'n':    
            case 'o':   
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isConnective(char in){
        switch (in) {
            case '¬':
            case '∧':
            case '∨':
            case '⟶':
            case '⟷':
                return true;                
            default:
                return false;
        }
    }
    
    public static boolean isQuantifier(char in){
        switch (in) {
            case '∀': case '∃':
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isFunction(char in){
        switch (in) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':    
            case 'e':
            case 'f':
            case 'g':
            case 'h':  
            case 'i':    
            case 'j':    
            case 'k':    
            case 'l':
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isSentence(char in){
        switch (in) {
            case 'A': case'B':case'C':case'D':case'E':case'F':case 'G': case'H':case'I':case'J':case'K':case'L':
            case 'M': case'N':case'O':case'P':case'Q':case'R':case 'S': case'T':case'U':case'V':case'W':case'X':
            case 'Y': case'Z':
                return true;
            default:
                return false;
        }
    }
    
    /**
     *Replaces input conventions for {@code FormulaDud} for proper symbols
     * @param in {@code String} 
     * @return {@code String}
     */
    public static String getForm(String in){ 
        
        in=in.replace((CharSequence)" ", (CharSequence)"");
        //1OL & SL:
        in=in.replace((CharSequence)"$A", (CharSequence)"∀");
        in=in.replace((CharSequence)"$E", (CharSequence)"∃");            
        in=in.replace((CharSequence)"$v'", (CharSequence)"∨");
        in=in.replace((CharSequence)"<>", (CharSequence)"⟷");
        in=in.replace((CharSequence)"->", (CharSequence)"⟶"); 
        //set theoretic:
        in=in.replace((CharSequence)"$U", (CharSequence)"∪");
        in=in.replace((CharSequence)"$I", (CharSequence)"∩");
        in=in.replace((CharSequence)"<=", (CharSequence)"⊆");
        in=in.replace((CharSequence)">=", (CharSequence)"⊇");
        in=in.replace((CharSequence)"$~e", (CharSequence)"∉");
        in=in.replace((CharSequence)"$e", (CharSequence)"∈");
        in=in.replace((CharSequence)"{}", "∅");
        
        in=in.replace("$N", "□");
        in=in.replace("$P", "◇");

        in = in.replace('&', '∧');
        in = in.replace('~', '¬');
        in = in.replace('>', '⊃');
        in = in.replace('<', '⊂');     
        in=in.replace('*', '∨');
        
        return in;        
    }
    
    public static char getForm(char in){
        switch (in) {
            case 'A':
                return '∀';
            case 'E':
                return '∃';
            case 'P':
                return '◇';
            case 'N':
                return '□';
            default:
                return in;
        }
    }
    
    
    public static int getLogic(String in){
        int logic = Logical.SENTENTIAL_LOGIC;
        in = Syntax.getForm(in);    
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (Syntax.isFunction(c)||Syntax.isVariable(c)||Syntax.isQuantifier(c)) {
                logic= Logical.PREDICATE_LOGIC_FO;
                break;
            }
        }    
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i)=='□'||in.charAt(i)=='◇') {
                logic = Logical.MODAL_LOGIC;
                break;
            }
        }
        return logic;
    }
    public static int getLogic(String[] in){
        int logic = Logical.SENTENTIAL_LOGIC;
        for (int i = 0; i < in.length; i++) {
            in[i] = Syntax.getForm(in[i]);    
        }
        
        for (int i = 0; i < in.length; i++) {
            int l = getLogic(in[i]);
            if (l>logic) logic= l;
        }
        return logic;
    }
    
    public static String negation(String in){
        if (in.length()>1) {
            if (in.charAt(1)=='¬') {
                return in.substring(2, in.length()-1);
            }
        }
        return "(¬"+in+")";        
    }
    public static String negationStrict(String in){        
        return "(¬"+in+")";
    }
    
    public static String conjunction(String a, String b){
        return "("+a+"∧"+b+")";
    }
    
    public static String disjunction(String a, String b){
        return "("+a+"∨"+b+")";
    }
    
    public static String conditional(String a, String b){
        return "("+a+"⟶"+b+")";
    }
    
    public static String biconditional(String a, String b){
        return "("+a+"⟷"+b+")";
    }
    
    public static String quantification(char Q, char v, char a, String in){
        in = in.replace(a, v);
        return quantification(Q,v,in);
    }
    
    public static String quantification(char Q, char v, String in){
        switch (Q) {
            case 'A': case '∀':
                Q = '∀';                
                break;
            case 'E': case '∃':
                Q='∃';
                break;
            default:
                throw new AssertionError();
        }
        return Q+""+v+in;
    }
    

    public static char getComplementConnective(char Q){
        switch (Q) {
            case '∀':
                return '∃';
            case '∃':
                return '∀';
            case '□':
                return '◇';
            case '◇':
                return '□';                                
            default:
                throw new AssertionError();
        }
    }
    
    
    /**
     * Return a {@code char} variable that does not occur <b> free</b> in input FormulaDud
     * @param in
     * @return 
     */
    public static char getFreeVariable(Formula in){
        char out = 'x';
        do {
            if (in.containsVariableFree(out)) {
                if (out<'x') {
                    out--;
                }else if (out=='z') {
                    out='w';
                }else{
                    out++;
                }
            }else{
                return out;
            }
        } while (true);                
    }
    
    
    /**
     * return a {@code char} variable that does not occur in input FormulaDud
     * @param in
     * @return 
     */
    public static char getNewVariable(Formula in){
        char out = 'x';        
        do{   
            if (in.containsVariable(out)) {
                if (out<'x') {
                    out--;
                }else if (out=='z') {
                    out='w';
                }else{
                    out++;
                }
            }else{
                return out;
            }
        }while(true);
    }
        
    public static boolean containsSymbol(String in, char c){        
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i)==c) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the String enclosed in the parentheses beginning at index x. If if the index of x
     * is not '(', then the String of the character at x is returned<p>
     * e.g: '(abcd(ef))hij(k)...' returns (abcd(ef))
     * 
     * @param in
     * @param x
     * @return 
     */
    public static String getEnclosed(String in, int x){
        if (in.charAt(x)!='(') return String.valueOf(in.charAt(x));
        
        int lr=0;
        int i = x;
        try{
            do {
                if (in.charAt(i)=='(') lr++;
                else if (in.charAt(i)==')') lr--;
                i++;
            } while (lr>0);
            
        }catch (java.lang.StringIndexOutOfBoundsException e){
            return null;
        }
        return in.substring(x, i);
    }
}
