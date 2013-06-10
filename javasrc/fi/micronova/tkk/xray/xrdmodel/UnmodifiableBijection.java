package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
public class UnmodifiableBijection<A,B> implements Bijection<A,B> {
    private Bijection<A,B> b;

    public UnmodifiableBijection(Bijection<A,B> b) {
        this.b = b;
    }
    public void clear() {
        throw new UnsupportedOperationException("unmodifiable bijection");
    }
    public boolean containsKey(A key) {
        return b.containsKey(key);
    }
    public boolean containsValue(B value) {
        return b.containsValue(value);
    }
    public boolean equals(Object o) {
        return b.equals(o);
    }
    public B getByKey(A key) {
        return b.getByKey(key);
    }
    public A getByValue(B value) {
        return b.getByValue(value);
    }
    public int hashCode() {
        return b.hashCode();
    }
    public void put(A key, B value) {
        throw new UnsupportedOperationException("unmodifiable bijection");
    }
    public void putAll(Bijection<? extends A,? extends B> t) {
        throw new UnsupportedOperationException("unmodifiable bijection");
    }
    public B removeKey(A key) {
        throw new UnsupportedOperationException("unmodifiable bijection");
    }
    public A removeValue(B value) {
        throw new UnsupportedOperationException("unmodifiable bijection");
    }
    public boolean isEmpty() {
        return b.isEmpty();
    }
    public int size() {
        return b.size();
    }

    public Set<Map.Entry<A,B>> entrySet() {
        return Collections.unmodifiableSet(b.entrySet());
    }
    public Set<A> keySet() {
        return Collections.unmodifiableSet(b.keySet());
    }
    public Set<B> valueSet() {
        return Collections.unmodifiableSet(b.valueSet());
    }
};
