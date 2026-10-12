package rose.era.v1_20_1.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** 1.20.1's {@code ObjectSelectionList} (see LegacySelectionList): rows that are selected and narrated one by one. */
public abstract class LegacyObjectSelectionList<E extends LegacyObjectSelectionList.Entry<E>> extends LegacySelectionList<E> {
    private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

    public LegacyObjectSelectionList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (getItemCount() == 0) return null;
        if (isFocused() && event instanceof FocusNavigationEvent.ArrowNavigation arrow) {
            E next = nextEntry(arrow.direction());
            return next != null ? ComponentPath.path(this, ComponentPath.leaf(next)) : null;
        }
        if (isFocused()) return null;
        E entry = getSelected();
        if (entry == null) entry = nextEntry(event.getVerticalDirectionForInitialFocus());
        return entry == null ? null : ComponentPath.path(this, ComponentPath.leaf(entry));
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        E entry = getHovered() != null ? getHovered() : getSelected();
        if (entry != null) {
            narrateListElementPosition(output.nest(), entry);
            entry.updateNarration(output);
        }
        if (isFocused()) output.add(NarratedElementType.USAGE, USAGE_NARRATION);
    }

    /** 1.20.1's {@code ObjectSelectionList.Entry}: narrated by its {@link #getNarration()}. */
    public abstract static class Entry<E extends Entry<E>> extends LegacySelectionList.Entry<E> implements NarrationSupplier {
        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, getNarration());
        }
    }
}
