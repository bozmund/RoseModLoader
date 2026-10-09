package net.minecraftforge.common.capabilities;

/** Names a capability by its type: {@code new CapabilityToken<IItemHandler>(){}}. */
public abstract class CapabilityToken<T> {
    protected final String getType() {
        var type = ((java.lang.reflect.ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return type.getTypeName();
    }
}
