package net.minecraftforge.common.capabilities;

import net.minecraftforge.common.util.LazyOptional;

/** A named kind of attachable behaviour (e.g. "item handler"). */
public final class Capability<T> {
    private final String name;

    public Capability(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isRegistered() {
        return true;
    }

    public <R> LazyOptional<R> orEmpty(Capability<R> toCheck, LazyOptional<T> inst) {
        return this == toCheck ? inst.cast() : LazyOptional.empty();
    }

    @Override
    public String toString() {
        return "Capability[" + name + "]";
    }
}
