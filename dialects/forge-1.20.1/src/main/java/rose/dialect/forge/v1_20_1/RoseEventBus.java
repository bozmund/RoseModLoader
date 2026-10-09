package rose.dialect.forge.v1_20_1;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import rose.loader.RoseLoader;

/**
 * Rose's implementation of Forge's event bus. Listeners run in priority order; a canceled event skips listeners that
 * didn't ask to receive canceled events.
 *
 * <p>{@link #register(Object)} finds {@code @SubscribeEvent} methods by reading the class file instead of
 * reflection: reflection resolves every method's types at once, so one listener for an event Rose doesn't have yet
 * would hide all the others. Such listeners are skipped and reported.
 */
public final class RoseEventBus implements IEventBus {
    private static final String SUBSCRIBE = "Lnet/minecraftforge/eventbus/api/SubscribeEvent;";

    private record Listener(EventPriority priority, boolean receiveCanceled, Class<?> type, Consumer<Event> handler,
                            Object owner, long order) {}

    private final String name;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private long counter;

    public RoseEventBus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RoseEventBus[" + name + "]";
    }

    @Override
    public void register(Object target) {
        boolean statics = target instanceof Class<?>;
        Class<?> type = statics ? (Class<?>) target : target.getClass();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Subscriber s : subscribers(c)) {
                if (s.isStatic != statics) continue;
                addSubscriber(c, s, statics ? null : target, target);
            }
            if (statics) break; // Forge registers a class's own static methods only
        }
    }

    private void addSubscriber(Class<?> owner, Subscriber s, Object instance, Object registrant) {
        String where = owner.getName() + "." + s.name;
        Class<?> eventType;
        try {
            eventType = Class.forName(s.eventType.getClassName(), false, owner.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            Unsupported.event(s.eventType.getClassName(), where);
            return;
        }
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
            MethodType methodType = MethodType.methodType(void.class, eventType);
            MethodHandle handle = s.isStatic
                    ? lookup.findStatic(owner, s.name, methodType)
                    : lookup.findVirtual(owner, s.name, methodType).bindTo(instance);
            MethodHandle generic = handle.asType(MethodType.methodType(void.class, Event.class));
            add(s.priority, s.receiveCanceled, eventType, event -> invoke(generic, event, where), registrant);
        } catch (ReflectiveOperationException | LinkageError e) {
            Unsupported.listener(where, e);
        }
    }

    private static void invoke(MethodHandle handle, Event event, String where) {
        try {
            handle.invokeExact(event);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException("Listener " + where + " failed", t);
        }
    }

    private record Subscriber(String name, Type eventType, boolean isStatic, EventPriority priority, boolean receiveCanceled) {}

    /** {@code @SubscribeEvent} methods declared by {@code type}, read from its class file. */
    private static List<Subscriber> subscribers(Class<?> type) {
        byte[] bytes;
        try {
            bytes = RoseLoader.get().classLoader().getClassBytes(type.getName().replace('.', '/'), false);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + type.getName(), e);
        }
        List<Subscriber> out = new ArrayList<>();
        if (bytes == null) return out;
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    EventPriority priority = EventPriority.NORMAL;
                    boolean receiveCanceled;
                    boolean subscribed;

                    @Override
                    public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        if (!annotation.equals(SUBSCRIBE)) return null;
                        subscribed = true;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String key, Object value) {
                                if (key.equals("receiveCanceled")) receiveCanceled = (Boolean) value;
                            }

                            @Override
                            public void visitEnum(String key, String enumDesc, String value) {
                                if (key.equals("priority")) priority = EventPriority.valueOf(value);
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                        Type[] args = Type.getArgumentTypes(desc);
                        if (subscribed && args.length == 1 && args[0].getSort() == Type.OBJECT) {
                            out.add(new Subscriber(name, args[0], Modifier.isStatic(access), priority, receiveCanceled));
                        }
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        return out;
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {
        addUntyped(EventPriority.NORMAL, false, consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
        addUntyped(priority, false, consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {
        addUntyped(priority, receiveCancelled, consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {
        addTyped(priority, receiveCancelled, eventType, consumer);
    }

    @Override
    public <T extends Event> void rose$addListener(Consumer<T> consumer, Class<T> eventType) {
        addTyped(EventPriority.NORMAL, false, eventType, consumer);
    }

    @Override
    public <T extends Event> void rose$addListener(EventPriority priority, Consumer<T> consumer, Class<T> eventType) {
        addTyped(priority, false, eventType, consumer);
    }

    @Override
    public <T extends Event> void rose$addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer, Class<T> eventType) {
        addTyped(priority, receiveCancelled, eventType, consumer);
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void addTyped(EventPriority priority, boolean receiveCanceled, Class<T> type, Consumer<T> consumer) {
        add(priority, receiveCanceled, type, (Consumer<Event>) consumer, consumer);
    }

    /**
     * The translator couldn't see the lambda's event type (it was stored in a variable first). The listener gets
     * every event and the ones it can't take are filtered out by the ClassCastException they cause.
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> void addUntyped(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
        Unsupported.untypedListener(consumer.getClass().getName());
        Consumer<Event> raw = (Consumer<Event>) consumer;
        add(priority, receiveCanceled, Event.class, event -> {
            try {
                raw.accept(event);
            } catch (ClassCastException ignored) {
                // not this listener's event type
            }
        }, consumer);
    }

    private synchronized void add(EventPriority priority, boolean receiveCanceled, Class<?> type, Consumer<Event> handler, Object owner) {
        listeners.add(new Listener(priority, receiveCanceled, type, handler, owner, counter++));
        List<Listener> sorted = new ArrayList<>(listeners);
        sorted.sort(Comparator.comparing(Listener::priority).thenComparingLong(Listener::order));
        listeners.clear();
        listeners.addAll(sorted);
    }

    @Override
    public void unregister(Object object) {
        listeners.removeIf(l -> l.owner == object);
    }

    @Override
    public boolean post(Event event) {
        for (Listener l : listeners) {
            if (!l.type.isInstance(event)) continue;
            if (event.isCanceled() && !l.receiveCanceled) continue;
            event.setPhase(l.priority);
            try {
                l.handler.accept(event);
            } catch (RuntimeException | LinkageError e) {
                // A translated listener hitting an API Rose doesn't bridge yet shouldn't take the game down.
                if (Unsupported.STRICT) throw e;
                Unsupported.listener(String.valueOf(l.owner), e);
            }
        }
        return event.isCanceled();
    }

    @Override
    public void start() {}
}
