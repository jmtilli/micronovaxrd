package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;

public class HashBijection<A,B> extends MapBijection<A,B> implements Cloneable {
    public HashBijection() {
        super(new HashMap<A,B>(), new HashMap<B,A>());
    }
    public HashBijection(Bijection<? extends A,? extends B> b) {
        this();
        putAll(b);
    }
    public Object clone() {
        return new HashBijection<A,B>(this);
    }
};
