package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;

/* XXX: no support for comparators */
public class TreeBijection<A,B> extends MapBijection<A,B> implements Cloneable {
    public TreeBijection() {
        super(new TreeMap<A,B>(), new TreeMap<B,A>());
    }
    public TreeBijection(Bijection<? extends A,? extends B> b) {
        this();
        putAll(b);
    }
    public Object clone() {
        return new TreeBijection<A,B>(this);
    }
};
