package logic;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;

/**Implements a  dendritic collection: e.g. {@code Tree<Integer>}<br>
 * <br>&emsp &emsp 1 - 2 - 3 - 4<br> &emsp &emsp &ensp |&emsp |&emsp <sup>|</sup>-5<br> &emsp &ensp &emsp | &emsp<sup>|</sup>- 6<br> &emsp &emsp &ensp |- 7<br> &emsp &emsp &ensp <sup>|</sup>-8<br>
 * @author thomdikdave
 * @param <T> 
 */
public class Tree<T> implements Iterable<T> {
    
    ArrayList<Node<T>> foundation = new ArrayList();
    
    public Tree(){}
    
    public Tree(T ... in){
        for (int i = 0; i < in.length; i++) {
            Node<T> nod = new Node(in[i]);
            nod.tier=0;
            nod.home = foundation;
            nod.parent=null;
            foundation.add(nod);
        }
    }
    
    public T get(int ... directions){
        return getNode(directions).item;
    }
    
    private Node<T> getNode(int[] ad){
        Node<T> out = foundation.get(ad[0]);
        for (int i = 1; i < ad.length; i++) {
            out = out.getChild(ad[i]);
        }
        return out;
    }
    
    public int[] getAddress(T in){
        return getAddress(getNode(in));
    }
    
    private int[] getAddress(Node<T> in){
        if (in==null) return null;
        int[] out = new int[]{in.getHomeIndex()};
        while (in.parent!=null) {
            in = in.parent;
            out = tools.Arrays.addItem(out, in.getHomeIndex());
        }
        tools.Arrays.reverse(out);
        return out;
    }
    
    public boolean contains(T in){
        Iterator<T> it = this.iterator();
        while (it.hasNext()) {
            T node = it.next();
            if (node.equals(in)) return true;
        }
        return false;
    }
    

    public void add(T in){
        Node<T> n = new Node(in, foundation);
        n.parent=null;
        n.tier=0;
        foundation.add(n);
    }
    
    public void placeAbove(T anchor, T item){
        Node<T> n = getNode(anchor);
        if (n==null) throw new java.lang.IllegalArgumentException();
        
        Node<T> newnode = new Node(item);
        this.setParent(newnode, n);
        n.children.add(newnode);
    }
    
    public void placeAbove(T anchor, Tree<T> item){
        Node<T> anch = this.getNode(anchor);
        if (anch==null) throw new java.lang.IllegalArgumentException();
        
        for (int i = 0; i < item.foundation.size(); i++) {
            Node<T> n = item.foundation.get(i);
            n.incrementTiers(anch.tier+1);
            anch.children.add(n);
            
            n.home = anch.children;
            n.parent = anch;
        }
    }
    
    public void placeAbove(T anchor, T ... args){
        Node<T> anch = getNode(anchor);
        if (anch==null) throw new java.lang.IllegalArgumentException();
        
        for (int i = 0; i < args.length; i++) {
            Node<T> n = new Node(args[i]);
            this.setParent(n, anch);
            anch.children.add(n);
        }
    }
    
    public void placeBelow(T anchor, T item){
        Node<T> anch = getNode(anchor);
        if (anch==null) throw new java.lang.IllegalArgumentException();
        
        Node<T> nod = new Node(item);
        this.setParent(nod, anch.parent);
        nod.children.add(anch);
        int ind = anch.home.indexOf(anch);
        nod.home.set(ind, nod);
        
        //this.setParent(anch, nod);
        anch.parent = nod;
        anch.home = nod.children;
        anch.incrementTiers();        
    }
    
    public void augment(Tree<T> in){
        augment(in,null);
    }
    
    public void augment(Tree<T> in, T above){
        in = in.getClone();
        if (above!=null){
            Node<T> par = getNode(above);
            for (Node<T> nod : in.foundation) {
                par.children.add(nod);
                nod.incrementTiers(par.tier+1);
                nod.parent = par;
                nod.home = par.children;
            }
        }else{
            for (Node<T> nod : in.foundation) {
                foundation.add(nod);
                nod.home = foundation;
            }
        }
    }
    
    /**
     * Replaces the specified item with the specified new item. The new item does not inherit descendents of the old item.<p>
     * e.g. a {@code Tree<Integer>}<br>&emsp &emsp 1 - 2 - 3 - 4<br> &emsp &emsp &ensp |&emsp |&emsp <sup>|</sup>-5<br> &emsp &ensp &emsp | &emsp<sup>|</sup>- 6<br> &emsp &emsp &ensp |- 7<br> &emsp &emsp &ensp <sup>|</sup>-8<br>
     * The method {@link #replace(java.lang.Object, java.lang.Object) replace(2,9)} yields: <br>&emsp &emsp 1 - 9<br> &emsp &emsp &ensp|- 7<br> &emsp &emsp &ensp<sup>|</sup>-8<br>
     * @see #overWrite(java.lang.Object, java.lang.Object) overWrite(oldt, newt)
     * @param oldt
     * @param newt 
     */
    public void replace(T oldt, T newt){
        Node<T> oldn = getNode(oldt);
        Node<T> newn = new Node(newt);
        ArrayList<Node<T>> hom = oldn.home;
        hom.set(hom.indexOf(oldn), newn);
        newn.home = hom;
        newn.parent = oldn.parent;
        newn.tier = oldn.tier;
    }
    
    public void replace(T oldt, Tree<T> newtree){
        Node<T> oldn = getNode(oldt);
        ArrayList<Node<T>> hom = oldn.home;
        int index = hom.indexOf(oldn);
        newtree=newtree.getClone();
        for (Node<T> nod : newtree.foundation) {
            hom.add(index, nod);
            nod.parent = oldn.parent;
            nod.home = hom;
            nod.incrementTiers(oldn.tier);
            index++;
        }
        hom.remove(oldn);
    }
    
    /**
     * Over-writes the specified item with the specified new item. The new item inherits the descendents of the old item.<p>
     * e.g. a {@code Tree<Integer>}<br>&emsp &emsp 1 - 2 - 3 - 4<br> &emsp &emsp &ensp |&emsp |&emsp <sup>|</sup>-5<br> &emsp &ensp &emsp | &emsp<sup>|</sup>- 6<br> &emsp &emsp &ensp |- 7<br> &emsp &emsp &ensp <sup>|</sup>-8<br>
     * The method {@link #overWrite(java.lang.Object, java.lang.Object) overWrite(2,9)} yields: <br>&emsp &emsp 1 - 9 - 3 - 4<br> &emsp &emsp &ensp |&emsp |&emsp <sup>|</sup>-5<br> &emsp &ensp &emsp | &emsp<sup>|</sup>- 6<br> &emsp &emsp &ensp |- 7<br> &emsp &emsp &ensp <sup>|</sup>-8<br>
     * @see #replace(java.lang.Object, java.lang.Object) replace(oldt, newt)
     * @param oldt
     * @param newt 
     */
    public void overWrite(T oldt, T newt){
        Node<T> oldn = getNode(oldt);
        Node<T> newn = new Node(newt);
        newn.parent = oldn.parent;
        newn.children = oldn.children;
        newn.home = oldn.home;
        newn.tier = oldn.tier;
        newn.home.set(newn.home.indexOf(oldn), newn);
    }
    
    public void remove(T in){
        Node<T> node = getNode(in);
        node.home.remove(node);
    }
    
    
    
    /**
     * Sets the parent of the first parameter as the second parameter:
     * <ol><li>{@code Node<T>} child  
     * <li>{@code Node<T>} parent
     * </ol>Also sets:
     * <ul><li>child.home = parent.children <li>child.tier = parent.tier+1;
     * </ul>
     * 
     * @param child
     * @param parent 
     */
    private void setParent(Node<T> child, Node<T> parent){
        child.parent = parent;
        if (parent==null) {
            child.home = foundation;
            child.tier=0;
        }
        else {
            child.home = parent.children;
            child.tier = parent.tier+1;
        }
    }
    
    private Node<T> getNode(T in){
        Node<T> out;
        Iterator<Node<T>> it = foundation.iterator();        
        for (Node<T> n : foundation) {
            out = n.getNode(in);
            if (out!=null) return out;
        }
        return null;
    }
    
    
    public T getChild(T parent, int n){
        Node<T> parn = getNode(parent);
        if (parn==null) throw new java.lang.IllegalArgumentException();
        Node<T> childn = parn.children.get(n);
        return childn.item;
    }
    
    
    public T getChild(T parent){
        return getChild(parent,0);
    }
    
    public int numberOfChildren(T parent){
        Node<T> parn = getNode(parent);
        return parn.children.size();
    }
    
    /**
     * Returns the unique parent to the argument parameter. <p> e.g. For a {@code Tree<Integer>}:
     * <br>&emsp &emsp 1 - 2 - 3 - 4<br> &emsp &emsp &ensp |&emsp |&emsp |-5<br> &emsp &ensp &emsp | &emsp|- 6<br> &emsp &emsp &ensp |- 7<br> &emsp &emsp &ensp |-8<br>
     * @param child
     * @return 
     */
    public T getParent(T child){
        Node<T> chiln = getNode(child);
        if (chiln.parent!=null) return chiln.parent.item;
        else return null;
    }
    
    
    public Set<T> toSet(){
        Set<T> out = new Set();
        for (T t : this) {
            out.add(t);
        }
        return out;
    }
    
    public Sequence<T> toSequence(){
        Sequence<T> out = new Sequence();
        for (T t : this) {
            out.add(t);
        }
        out.reverseOrder();
        return out;
    }
    
    public Tree<T> getClone(){
        Tree<T> out = new Tree();
        for (Node<T> n : foundation) {
            Node<T> nclone = n.getClone();
            out.foundation.add(nclone);
            nclone.home = out.foundation;
        }
        return out;
    }
    
    public void print(){
        System.out.print("  ");
        for (int i = 0; i < foundation.size(); i++) {            
            foundation.get(i).print(1);
        }
        System.out.println("");
    }
    
    public void prinfInfo(){
        for (T t : this) {
            Node<T> n = getNode(t);
            String s = "";
            for(Node<T> nod:n.children) s+=nod.item.toString()+" ";
            System.out.println(t.toString()+"\tAddress: "+tools.Arrays.getString(getAddress(t))+
                    "\tChildren: "+s+"\tParent: "+(n.parent==null? "no parent":n.parent.item.toString())+
                    "\tTier: "+n.tier
                    );
        }
    }

    
    @Override
    public Iterator<T> iterator() {
        return new TreeIterator<T>() {
            Node<T> node;
            int[] address;
            @Override
            void init() {
                address = null;
                if (foundation.size()>0) node = foundation.get(0);
                else return;
                while (node.hasChildren()) node = node.getChild(0);
                address = getAddress(node);
            }

            @Override
            public boolean hasNext() {
                return address != null;
            }

            @Override
            public T next() {
                node = getNode(address);
                T out= node.item;
                if (node.hasOlderSiblings()) {
                    node = node.getNextSibling();
                    while (node.hasChildren()) node = node.getChild(0);
                }else{
                    node = node.parent;
                }
                address = getAddress(node);
                return out;
            }
        };
    }
    
    public Iterator<T> iterator(Schema<T> in){
        return new TreeIterator<T>(in) {
            Node<T> nod;
            int[] ad;
            @Override
            void init() {
                if(foundation.isEmpty()) return;
                nod = foundation.get(0);
                while (nod.hasChildren()) nod = nod.getChild(0);
                
                while (!schema.condition(nod.item)) {
                    if (nod.hasOlderSiblings()) {
                        nod=nod.getNextSibling();
                        while (nod.hasChildren()) nod = nod.getChild(0);
                    }else if(nod.parent!=null){
                        nod = nod.parent;
                    }else{
                        ad=null; return;
                    }
                }
                ad = getAddress(nod);
            }

            @Override
            public boolean hasNext() {
                return ad!=null;
            }

            @Override
            public T next() {
                nod = getNode(ad);
                T out = nod.item;
                do {
                    if (nod.hasOlderSiblings()){
                        nod=nod.getNextSibling();
                        while (nod.hasChildren()) nod=nod.getChild(0);
                    }
                    else if (nod.parent!=null) {
                        nod=nod.parent;
                        
                    }else{
                        ad=null;
                        return out;
                    }
                } while (!schema.condition(nod.item));
                ad = getAddress(nod);
                return out;
            }
        };
    }
    
    public Iterator<T> iterateDescendents(T parent){
        return new TreeIterator<T>() {
            Node<T> node, parnode;
            @Override
            void init() {
                parnode = getNode(parent);
                if(parnode.hasChildren()) node = parnode.getChild(0);
                else return;
                while (node.hasChildren()) {
                    node = node.getChild(0);
                }
            }

            @Override
            public boolean hasNext() {
                return !(node==null || node==parnode);
            }

            @Override
            public T next() {
                T out = node.item;
                if (node.hasOlderSiblings()){
                    node = node.getNextSibling();
                    while (node.hasChildren()) node = node.getChild(0);
                }
                else node = node.parent;
                return out;
            }
        };
    }
    
    public Iterator<T> iterateForwardFrom(T parent){
        return new TreeIterator<T>() {
            Node<T> nod, parnode;
            int[] add, currentadd;
            @Override
            void init() {
                if (foundation.isEmpty()) nod = null;
                else if (parent == null){
                    nod = foundation.get(0);
                    parnode = null;
                }else{
                    nod= getNode(parent);
                    parnode= nod;
                }
                add = getAddress(nod);
            }

            @Override
            public boolean hasNext() {
                return add!=null;
            }

            @Override
            public T next() {
                nod = getNode(add);
                currentadd = add;
                T out = nod.item;
                
                here:
                if (nod.hasChildren()) nod=nod.getChild(0);
                else if (nod.hasOlderSiblings()) nod = nod.getNextSibling();
                else{
                    while (!nod.hasOlderSiblings()) {
                        nod=nod.parent;
                        if (nod==null || nod == parnode) break here;
                    }
                    nod = nod.getNextSibling();
                }
                
                if (nod == parnode) nod = null;
                add = getAddress(nod);
                return out;
            }
            
            @Override
            public void skipThisNode(){
                nod = getNode(currentadd);
                here:
                if (nod.hasOlderSiblings()) nod = nod.getNextSibling();
                else{
                    while (!nod.hasOlderSiblings()) {
                        nod=nod.parent;
                        if (nod==null || nod == parnode) break here;
                    }
                    nod = nod.getNextSibling();
                }
                if (nod == parnode) nod = null;
                add = getAddress(nod);
            }
        };
    }
    
    public Iterator<T> iterateForward(){
        return iterateForwardFrom(null);
    }
    
    public Iterator<T> iterateChildren(T parent){
        return new TreeIterator<T>() {
            Node<T> node, parnode;
            int[] ad;
            @Override
            void init() {
                parnode = getNode(parent);
                if (parnode.hasChildren()) {
                    node = parnode.getChild(0);
                }
                ad = getAddress(node);
            }

            @Override
            public boolean hasNext() {
                return ad!=null;
            }

            @Override
            public T next() {
                node = getNode(ad);
                T out = node.item;
                if (node.hasOlderSiblings()) {
                    node = node.getNextSibling();
                }else node=null;
                ad = getAddress(node);
                return out;
            }
        };
    }
    
    public Iterator<T> iterateAncestors(T from){
        return new TreeIterator<T>() {
            Node<T> node;
            int[] ad;
            @Override
            void init() {
                node = getNode(from);
                node = node.parent;
                ad = getAddress(node);
            }

            @Override
            public boolean hasNext() {
                return ad!=null;
            }

            @Override
            public T next() {
                node = getNode(ad);
                T out = node.item;
                if (node.hasOlderSiblings()) {
                    node = node.getNextSibling();
                }else node = node.parent;
                ad = getAddress(node);
                return out;
            }
        };
    }
    
    public Iterator<T> iterateDirectAncestors(T from){
        return new TreeIterator<T>() {
            Node<T> node;
            int[] ad;
            @Override
            void init() {
                node = getNode(from);
                node = node.parent;
                ad = getAddress(node);
            }

            @Override
            public boolean hasNext() {
                return ad!=null;
            }

            @Override
            public T next() {
                node = getNode(ad);
                T out = node.item;
                node = node.parent;
                ad = getAddress(node);
                return out;
            }
        };
        
    }
    
    

    public abstract class TreeIterator<T> implements Iterator<T>{
        
        TreeIterator(){
            init();
        }
        
        Schema<T> schema;
        TreeIterator(Schema<T> schem){
            schema = schem;
            init();
        }
        
        abstract void init();
        
        @Override
        public abstract boolean hasNext();

        @Override
        public abstract T next();
        
        public void skipThisNode(){}
        
        
    }
    
    private class Node<T>{

        T item;
        ArrayList<Node<T>> children = new ArrayList();
        Node<T> parent;
        int[] address;
        
        ArrayList<Node<T>> home;
        int tier;

        Node(T in, ArrayList<Node<T>> hom){
            item = in;
            home = hom;
        }               
        Node(T in){
            item = in;
        }            
        
        Node<T> getNode(T in){
            if (in==this.item) {
                return this;
            }else{
                Node<T> out;
                for (Node<T> ch : children) {
                    out = ch.getNode(in);
                    if (out!=null) return out;
                }
            }
            return null;
        }
        
        void print(int indent){
            String f = "%-"+indent+"s %-20s";
            if (this.home.indexOf(this)==0) {
                System.out.printf("%-20s", this.item.toString());
            }else{
                System.out.println("");
                System.out.printf(f, "",this.item.toString());
            }
            for (int i = 0; i < this.children.size(); i++) {
                this.children.get(i).print(indent+20);
            }
        }

        private void incrementTiers() {
            this.tier++;
            for (Node<T> child : children) {
                child.incrementTiers();
            }
        }
        private void incrementTiers(int x) {
            this.tier+=x;
            for (Node<T> child : children) {
                child.incrementTiers(x);
            }
        }
        int getHomeIndex(){
            return this.home.indexOf(this);
        } 
        boolean hasOlderSiblings(){
            return this.home.indexOf(this)<home.size()-1;
        }
        boolean hasChildren(){
            return this.children.size()>0;
        }
        
        Node<T> getNextSibling(){
            if (this.hasOlderSiblings()) return this.home.get(this.getHomeIndex()+1);
            else throw new java.lang.IllegalArgumentException();
        }
        Node<T> getChild(int i){
            if (this.hasChildren()) return this.children.get(i);
            else throw new java.lang.IllegalArgumentException();
        }
        Node<T> getClone(){
            Node<T> out = new Node(item);
            out.children = new ArrayList<>();
            for (Node<T> kid : children) {
                Node<T> kidclone = kid.getClone();
                out.children.add(kidclone);
                kidclone.home = out.children;
                kidclone.parent = out;
            }
            out.tier = tier;
            return out;
        }
    }
    
   
}

