package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
public interface Bijection<A,B> {
    /*
    public static interface Entry<A,B> {
        public boolean equals(Object o);
        public A getKey();
        public B getValue();
        public int hashCode();
    }
    */
    public void clear();
    public boolean containsKey(A key);
    public boolean containsValue(B value);
    public Set<Map.Entry<A,B>> entrySet();
    public boolean equals(Object o);
    public B getByKey(A key);
    public A getByValue(B value);
    public int hashCode();
    public boolean isEmpty();
    public Set<A> keySet();
    public void put(A key, B value);
    public void putAll(Bijection<? extends A,? extends B> t);
    public B removeKey(A key);
    public A removeValue(B value);
    public int size();
    public Set<B> valueSet();
};
