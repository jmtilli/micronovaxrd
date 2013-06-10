package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
public abstract class MapBijection<A,B> implements Bijection<A,B> {
    private Map<A,B> keyToValue;
    private Map<B,A> valueToKey;

    protected MapBijection(Map<A,B> keyToValue, Map<B,A> valueToKey) {
        this.keyToValue = keyToValue;
        this.valueToKey = valueToKey;
    }

    public static class Entry<A,B> implements Map.Entry<A,B> {
        private final A key;
        private final B value;
        public Entry(A key, B value) {
            this.key = key;
            this.value = value;
        }
        public boolean equals(Object o) {
            if(o == null)
                return false;
            try {
                Map.Entry<?,?> e2 = (Map.Entry<?,?>)o;
                return (key == null ? e2.getKey() == null : key.equals(e2.getKey()))
                    && (value == null ? e2.getValue() == null : value.equals(e2.getValue()));
            }
            catch(ClassCastException ex) {
                return false;
            }
        }
        public A getKey() {
            return key;
        }
        public B getValue() {
            return value;
        }
        public int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
        public B setValue(B b2) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        public A setKey(A a2) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
    public void clear() {
        keyToValue.clear();
        valueToKey.clear();
    }



    public boolean containsKey(A key) {
        boolean result = keyToValue.containsKey(key);
        if(result) {
            B value = keyToValue.get(key);
            assert(valueToKey.containsKey(value));
            assert(valueToKey.get(value).equals(key));
        }
        return result;
    }
    public boolean containsValue(B value) {
        boolean result = valueToKey.containsKey(value);
        if(result) {
            A key = valueToKey.get(value);
            assert(keyToValue.containsKey(key));
            assert(keyToValue.get(key).equals(value));
        }
        return result;
    }


    public Set<Map.Entry<A,B>> entrySet() {
        return Collections.unmodifiableSet(keyToValue.entrySet());
    }
    public boolean equals(Object o) {
        if(this == o)
            return true;
        try {
            Bijection<?,?> b2 = (Bijection<?,?>)o;
            return entrySet().equals(b2.entrySet());
        }
        catch(ClassCastException ex) {
            return false;
        }
    }
    public B getByKey(A key) {
        B value = keyToValue.get(key);
        if(keyToValue.containsKey(key))
            assert(valueToKey.containsKey(value) && valueToKey.get(value).equals(key));
        else
            assert(value == null);
        return value;
    }
    public A getByValue(B value) {
        A key = valueToKey.get(value);
        if(valueToKey.containsKey(value))
            assert(keyToValue.containsKey(key) && keyToValue.get(key).equals(value));
        else
            assert(key == null);
        return key;
    }
    public int hashCode() {
        return keyToValue.hashCode();
    }
    public boolean isEmpty() {
        assert(keyToValue.isEmpty() == valueToKey.isEmpty());
        return keyToValue.isEmpty();
    }

    /* these could be made modifiable if needed */
    public Set<A> keySet() {
        return Collections.unmodifiableSet(keyToValue.keySet());
    }
    public Set<B> valueSet() {
        return Collections.unmodifiableSet(valueToKey.keySet());
    }
    public void put(A key, B value) {
        if(containsKey(key)) {
            removeKey(key);
        }
        if(containsValue(value)) {
            removeValue(value);
        }
        keyToValue.put(key,value);
        valueToKey.put(value,key);
    }
    public void putAll(Bijection<? extends A,? extends B> t) {
        for(Map.Entry<? extends A,? extends B> e: t.entrySet()) {
            put(e.getKey(),e.getValue());
        }
    }
    public B removeKey(A key) {
        if(keyToValue.containsKey(key)) {
            B value = keyToValue.remove(key);
            assert(valueToKey.containsKey(value));
            A key2 = valueToKey.remove(value);
            assert(key2.equals(key));
            return value;
        } else {
            return null;
        }
    }
    public A removeValue(B value) {
        if(valueToKey.containsKey(value)) {
            A key = valueToKey.remove(value);
            assert(keyToValue.containsKey(key));
            B value2 = keyToValue.remove(key);
            assert(value2.equals(value));
            return key;
        } else {
            return null;
        }
    }
    public int size() {
        assert(keyToValue.size() == valueToKey.size());
        return keyToValue.size();
    }
};
