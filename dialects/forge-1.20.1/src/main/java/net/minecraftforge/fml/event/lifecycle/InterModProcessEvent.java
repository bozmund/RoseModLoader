package net.minecraftforge.fml.event.lifecycle;

import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;

/** Mods read the InterModComms messages sent to them (after InterModEnqueueEvent). */
public class InterModProcessEvent extends ParallelDispatchEvent {
    public Stream<InterModComms.IMCMessage> getIMCStream() {
        return InterModComms.getMessages(ModLoadingContext.get().getActiveNamespace());
    }

    public Stream<InterModComms.IMCMessage> getIMCStream(Predicate<String> methodFilter) {
        return InterModComms.getMessages(ModLoadingContext.get().getActiveNamespace(), methodFilter);
    }
}
