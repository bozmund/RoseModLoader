package net.minecraftforge.fml.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reflection by SRG name. The translator already rewrote SRG string constants to 26.3 names (see
 * {@code RosettaRemapper.mapValue}), so the names that arrive here are real ones.
 */
public class ObfuscationReflectionHelper {
    public static String remapName(Object domain, String name) {
        return name;
    }

    @SuppressWarnings("unchecked")
    public static <T, E> T getPrivateValue(Class<? super E> classToAccess, E instance, String fieldName) {
        try {
            return (T) findField(classToAccess, fieldName).get(instance);
        } catch (IllegalAccessException e) {
            throw new UnableToAccessFieldException(e);
        }
    }

    public static <T, E> void setPrivateValue(Class<? super T> classToAccess, T instance, E value, String fieldName) {
        try {
            findField(classToAccess, fieldName).set(instance, value);
        } catch (IllegalAccessException e) {
            throw new UnableToAccessFieldException(e);
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new UnableToFindMethodException(e);
        }
    }

    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> c = clazz.getDeclaredConstructor(parameterTypes);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException e) {
            throw new UnknownConstructorException("Could not find constructor of " + clazz.getName(), e);
        }
    }

    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new UnableToFindFieldException(e);
        }
    }

    public static class UnableToAccessFieldException extends RuntimeException {
        UnableToAccessFieldException(Exception e) {
            super(e);
        }
    }

    public static class UnableToFindFieldException extends RuntimeException {
        UnableToFindFieldException(Exception e) {
            super(e);
        }
    }

    public static class UnableToFindMethodException extends RuntimeException {
        public UnableToFindMethodException(Throwable failed) {
            super(failed);
        }
    }

    public static class UnknownConstructorException extends RuntimeException {
        public UnknownConstructorException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
