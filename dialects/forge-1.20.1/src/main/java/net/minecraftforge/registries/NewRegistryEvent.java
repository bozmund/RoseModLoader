package net.minecraftforge.registries;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * Mods create custom registries (mod bus, before RegisterEvent). Rose posts it at the same point; creating a registry
 * isn't supported yet, so it only serves mods that use it as a "registries are about to fill" hook.
 */
public class NewRegistryEvent extends Event implements IModBusEvent {}
