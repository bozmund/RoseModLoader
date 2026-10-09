package net.minecraftforge.eventbus.api;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Base class of every Forge event. */
public class Event {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface HasResult {}

    public enum Result {
        DENY,
        DEFAULT,
        ALLOW
    }

    private boolean canceled;
    private Result result = Result.DEFAULT;
    private EventPriority phase;

    public Event() {}

    public boolean isCancelable() {
        return hasAnnotation(getClass(), Cancelable.class);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean cancel) {
        if (cancel && !isCancelable()) {
            throw new UnsupportedOperationException("Attempted to cancel a non-cancelable event " + getClass().getName());
        }
        canceled = cancel;
    }

    public boolean hasResult() {
        return hasAnnotation(getClass(), HasResult.class);
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result value) {
        result = value;
    }

    public EventPriority getPhase() {
        return phase;
    }

    public void setPhase(EventPriority value) {
        phase = value;
    }

    private static boolean hasAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
        for (Class<?> c = type; c != null && c != Event.class; c = c.getSuperclass()) {
            if (c.isAnnotationPresent(annotation)) return true;
        }
        return false;
    }
}
