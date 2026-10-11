package rose.era.v1_20_1.util;

/** 1.20.1 {@code net.minecraft.util.Tuple}, a mutable pair, gone by 1.21.11; mods use it in their own APIs (Curios). */
public class LegacyTuple<A, B> {
    private A a;
    private B b;

    public LegacyTuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }
}
