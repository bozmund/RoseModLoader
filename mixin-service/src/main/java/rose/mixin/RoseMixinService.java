package rose.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterConsole;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IFeatureValidator;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.ReEntranceLock;
import rose.loader.RoseClassLoader;

/**
 * Connects SpongePowered Mixin to Rose's class loader. Mixin finds this class through
 * {@code META-INF/services/org.spongepowered.asm.service.IMixinService}.
 *
 * <p>Mixin reads class bytes (including the mixin classes themselves, which must never be loaded as normal
 * classes) through this service, and Rose runs Mixin's transformer from {@link RoseMixin}.
 */
public final class RoseMixinService
        implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {

    private final ReEntranceLock lock = new ReEntranceLock(1);
    private final Map<String, ILogger> loggers = new ConcurrentHashMap<>();

    private RoseClassLoader loader() {
        RoseClassLoader loader = RoseMixin.loader();
        if (loader == null) throw new IllegalStateException("Mixin used before RoseMixin.bootstrap()");
        return loader;
    }

    // --- IMixinService ---

    @Override public String getName() { return "Rose"; }

    @Override public boolean isValid() { return RoseMixin.loader() != null; }

    @Override public void prepare() {}

    @Override public MixinEnvironment.Phase getInitialPhase() { return MixinEnvironment.Phase.PREINIT; }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) RoseMixin.setTransformerFactory(factory);
    }

    @Override public void init() {}

    @Override public void beginPhase() {}

    @Override public void checkEnv(Object bootSource) {}

    @Override public ReEntranceLock getReEntranceLock() { return lock; }

    @Override public IClassProvider getClassProvider() { return this; }

    @Override public IClassBytecodeProvider getBytecodeProvider() { return this; }

    @Override public ITransformerProvider getTransformerProvider() { return this; }

    @Override public IClassTracker getClassTracker() { return this; }

    @Override public IMixinAuditTrail getAuditTrail() { return null; }

    @Override public IFeatureValidator getFeatureValidator() { return null; }

    @Override public IAdviceProvider getAdviceProvider() { return null; }

    @Override public Collection<String> getPlatformAgents() {
        return List.of("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override public IContainerHandle getPrimaryContainer() { return new ContainerHandleVirtual("rose"); }

    @Override public Collection<IContainerHandle> getMixinContainers() { return List.of(); }

    @Override public InputStream getResourceAsStream(String name) { return loader().getResourceAsStream(name); }

    @Override public String getSideName() { return RoseMixin.side(); }

    @Override public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_25;
    }

    @Override public ILogger getLogger(String name) {
        return loggers.computeIfAbsent(name, LoggerAdapterConsole::new);
    }

    // --- IClassProvider ---

    @Override @Deprecated public URL[] getClassPath() { return new URL[0]; }

    @Override public Class<?> findClass(String name) throws ClassNotFoundException {
        return loader().loadClass(name);
    }

    @Override public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, loader());
    }

    @Override public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, RoseMixinService.class.getClassLoader());
    }

    // --- IClassBytecodeProvider ---

    @Override public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true, 0);
    }

    @Override public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags)
            throws ClassNotFoundException, IOException {
        byte[] bytes = loader().getClassBytes(name.replace('.', '/'), runTransformers);
        if (bytes == null) throw new ClassNotFoundException(name);
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, readerFlags);
        return node;
    }

    // --- ITransformerProvider (Rose has no legacy transformers to report) ---

    @Override public Collection<ITransformer> getTransformers() { return List.of(); }

    @Override public Collection<ITransformer> getDelegatedTransformers() { return List.of(); }

    @Override public void addTransformerExclusion(String name) {}

    // --- IClassTracker ---

    @Override public void registerInvalidClass(String name) {}

    @Override public boolean isClassLoaded(String name) { return loader().isClassLoaded(name); }

    @Override public String getClassRestrictions(String name) { return ""; }
}
