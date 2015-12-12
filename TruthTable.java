package logic;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TruthTable{    
    int rows,columns;
    int np, ns;
    Formula[] premises;
    Formula conclusion;
    Formula[] sentences;
    
    HashMap<Formula,Boolean>[] vals;
    
    public boolean valid;
    public boolean[][] data;
    public HashMap<String,Boolean>[] valuations;
    
    private JTable ttable;
    
    public TruthTable(Formula[] prems, Formula conc){
        for (int i = 0; i < prems.length; i++) {
            if (prems[i].logic!=Logical.SENTENTIAL_LOGIC) {
                throw new java.lang.IllegalArgumentException("Truth Table requires Formulas in Sentential Logic");
            }
        }
        if (conc.logic!=Logical.SENTENTIAL_LOGIC) {
            throw new java.lang.IllegalArgumentException("Truth Table requires Formulas in Sentential Logic");
        }
        
        premises = prems; conclusion = conc;   
        np = premises.length;
        
        initialiseTable();
        evaluateFormulas();
        constructData();
        getRefinedValues();
        valid = getValidity();                
    }
    
    public TruthTable(Formula ... args){
        this(tools.Arrays.subArray(args,0,args.length-1),args[args.length-1]);
    }

    private void initialiseTable() {
        sentences = getSentenceSymbols(tools.Arrays.add(premises, conclusion));
        ns = sentences.length;
        columns = sentences.length+premises.length+1;
        rows = (int)Math.pow(2, ns);
        vals = new HashMap[rows];
        data = new boolean[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            vals[i] = new HashMap<>();
        }
                
        for (int i = 0; i < sentences.length; i++) {
            //for each ss...
            boolean val = true;
            int m = 0;            
            for (int j = 0; j < vals.length; j++) {
                //for each valuation...                
                if (m==(int)Math.pow(2, ns-i-1)) {
                    val=!val;
                    m=0;
                }
                vals[j].put(sentences[i], val);
                m++;
            }
         
            for (int j = 0; j < rows; j++) {
                data[j][i] = vals[j].get(sentences[i]);
            }
            
        }                
    }
    
    private static Formula[] getSentenceSymbols(Formula[] in){
        Formula[] out = new Formula[0];
        for (int i = 0; i < in.length; i++) {
            for (int j = 0; j < in[i].length; j++) {
                if (in[i].get(j).isSentence()) {
                    out=tools.Arrays.add(out, new Formula(in[i].get(j).form));
                }
            }
        }
        out = tools.Arrays.removeDuplicates(out);
        return out;
    }

    private void evaluateFormulas() {
        for (int i = 0; i < premises.length; i++) {
            for (int j = 0; j < vals.length; j++) {
                enterValuation(premises[i], vals[j]);
            }
        }
        for (int i = 0; i < vals.length; i++) {
            enterValuation(conclusion, vals[i]);
        }
    }

    private void enterValuation(Formula form, HashMap<Formula, Boolean> val) {
        ConstructionTree ct = form.getConstruction();
        Iterator<Formula> it = ct.iterateDescendents(form);
        while (it.hasNext()) {
            enterValuation(it.next(), val);
        }
        if (form.connective.isNull()) {
            for (int i = 0; i < sentences.length; i++) {
                if (sentences[i].equals(form)) {
                    val.put(form, val.get(sentences[i]));
                }
            }
        }else{
            boolean v;
            switch (form.connective.symbol) {
                case Logical.conjunction:
                    v= val.get(form.getChild(0)) && val.get(form.getChild(1));
                    break;
                case Logical.disjunction:
                    v= val.get(form.getChild(0)) ||  val.get(form.getChild(1));
                    break;
                case Logical.negation:
                    v = ! val.get(form.getChild(0));
                    break;
                case Logical.conditional:
                    boolean v1,v2;
                    v1=! val.get(form.getChild(0)); v2 = val.get(form.getChild(1));
                    v = v1||v2;
                    break;
                case Logical.biconditional:
                    boolean v3,v4;
                    v3= val.get(form.getChild(0)); v4= val.get(form.getChild(1));
                    v=v3==v4;
                    break;
                default:
                    throw new AssertionError();                        
            }
            val.put(form, v);
        }
    }

    private void constructData() {
        for (int i = 0; i < premises.length; i++) {
            for (int j = 0; j < rows; j++) {
                data[j][ns+i] = vals[j].get(premises[i]);
            }
        }
        for (int i = 0; i < rows; i++) {
            data[i][data[i].length-1] = vals[i].get(conclusion);
        }                
    }
    
    public void printTable(){
        for (Formula sentence : sentences) {
            System.out.print(sentence.toString()+"\t");
        }
        for (Formula premise : premises) {
            System.out.print(premise.toString()+"\t");
        }
        System.out.println(conclusion.toString());
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                System.out.print(data[i][j]+"\t");
            }
            System.out.println("");
        }
    }

    private boolean getValidity() {
        for (int i = 0; i < rows; i++) {
            boolean m=true;
            for (int j = 0; j < premises.length; j++) {
                if (!vals[i].get(premises[j])) {
                    m=false;
                    break;
                }
            }
            if (m) {
                if (!vals[i].get(conclusion)) {
                    return false;
                }
            }                        
        }
        return true;
    }

    private void getRefinedValues() {  
        valuations = new HashMap[rows];
        for (int i = 0; i < rows; i++) {
            valuations[i] = new HashMap<>();
            for (int j = 0; j < sentences.length; j++) {
                valuations[i].put(sentences[j].form, vals[i].get(sentences[j]));
            }
            for (int j = 0; j < premises.length; j++) {
                valuations[i].put(premises[j].form, vals[i].get(premises[j]));
            }
            valuations[i].put(conclusion.form, vals[i].get(conclusion));
        }
    }
    
    public JTable getJTable(){
        
        if (ttable!=null) {
            System.out.println("got it");
            return ttable;
        }

        ttable = new TJTable();        
        ttable.setDragEnabled(false);       
        ttable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        for (int i = 0; i < ttable.getModel().getColumnCount(); i++) {
            TableColumn c = ttable.getColumnModel().getColumn(i);
            if (i<sentences.length) {
                c.setPreferredWidth(50);
            }else{
                c.setPreferredWidth(100);
            }
        }
        return ttable;
    }
      
    class TJTable extends JTable{
        
    DefaultTableModel model;
    Object[][] modeldata;
    Object[] colnames;
    
    Color   SGREEN = new Color(100,1,1), SRED = new Color(1,100,1), SBACK = Color.WHITE,
            PGREEN = Color.GREEN, PRED = Color.RED, PBACK = Color.LIGHT_GRAY
            //CGREEN = Color
            ;

    
        public TJTable(){
            modeldata = new Object[rows][columns];
            for (int i = 0; i < modeldata.length; i++) {
                for (int j = 0; j < modeldata[i].length; j++) {
                    modeldata[i][j] = data[i][j];
                }
            }
            colnames = new Object[ns+np+1];
            for (int i = 0; i < sentences.length; i++) {
                colnames[i] = sentences[i].form;
            }
            for (int i = 0; i < premises.length; i++) {
                colnames[i+ns] = premises[i].form;
            }
            colnames[ns+np] = conclusion.form;            
            
            model = new DefaultTableModel(modeldata, colnames);
            this.setModel(model);
            
            TableCellRenderer rend = new TableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel c = new JLabel();
                    c.setText(String.valueOf(table.getValueAt(row, column)));
                    c.setBackground(Color.LIGHT_GRAY);
                    
                    if (!isSelected&&row%2==0) {
                        c.setBackground(Color.WHITE);
                    }
                    if (isSelected) {
                        c.setBackground(c.getBackground());
                    }
                    c.setOpaque(true);
                    return c;              
                    
                    
                }
            };    
            this.setDefaultRenderer(Object.class, rend);
        }
    
        @SuppressWarnings("unchecked")        
        public Component prepareRenderer(TableCellRenderer renderer, int row, int col){
            Component c = super.prepareRenderer(renderer, row, col);

            //if (!isRowSelected(row)) {
                c.setBackground(getBackground());
                int modelRow = convertRowIndexToModel(row);

                boolean[] premtvs = new boolean[premises.length];
                for (int i=0; i<premises.length;i++){
                    premtvs[i] = (boolean)model.getValueAt(modelRow, i+sentences.length);
                }
                boolean premsTrue = true;
                for(int i=0; i<premtvs.length; i++){
                    premsTrue = premsTrue && premtvs[i];
                }
                boolean concTrue = (boolean)model.getValueAt(modelRow, columns-1);

                if (concTrue && premsTrue) {
                    c.setBackground(PGREEN);
                }
                if (premsTrue && !concTrue){
                    c.setBackground(PRED);
                }
                
                int modelCol = this.convertColumnIndexToModel(col);
                if (modelCol<ns) {  
                    if (c.getBackground()==PGREEN) {
                        c.setBackground(SGREEN);
                    }else if (c.getBackground()==PRED) {
                        c.setBackground(SRED);
                    }else{
                        c.setBackground(SBACK);
                    }
                }
                if (modelCol==ns+np) {
                    //implement conc colours
                }

            //}
            //c.setVisible(true);
            return c;
        }
        
    }
    
}