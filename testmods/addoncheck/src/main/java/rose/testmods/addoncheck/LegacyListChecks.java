package rose.testmods.addoncheck;

import static rose.testmods.addoncheck.AddonCheck.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import rose.era.v1_20_1.client.gui.LegacyObjectSelectionList;

/**
 * 1.20.1 selection lists (Nature's Compass' biome list) keep their 1.20.1 geometry and input on 26.3: rows at
 * itemHeight steps below y0, clicks with 1.20.1 button numbers, scrolling clamped to the rows.
 */
final class LegacyListChecks {
    private LegacyListChecks() {}

    static void register() {
        test("legacy_selection_list_rows_and_clicks", LegacyListChecks::rowsAndClicks);
    }

    private static final class Row extends LegacyObjectSelectionList.Entry<Row> {
        final List<Integer> clicks = new ArrayList<>();

        @Override
        public void renderContent(GuiGraphicsExtractor graphics, int index, int top, int left, int width, int height,
                                  int mouseX, int mouseY, boolean hovered, float partialTick) {
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            clicks.add(button);
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }
    }

    private static final class Rows extends LegacyObjectSelectionList<Row> {
        Rows() {
            // 1.20.1 (minecraft, width, height, y0, y1, itemHeight); a list on its own needs no Minecraft
            super(null, 300, 200, 40, 140, 25);
            setLeftPos(100);
        }

        Row add() {
            Row row = new Row();
            addEntry(row);
            return row;
        }

        Row at(double x, double y) {
            return getEntryAtPosition(x, y);
        }
    }

    private static void rowsAndClicks(GameTestHelper helper) {
        Rows list = new Rows();
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) rows.add(list.add());
        // rows start 4 below y0, 25 apart; x0 = 100, rows centred in the 300 wide list
        helper.assertTrue(list.at(250, 45) == rows.get(0) && list.at(250, 70) == rows.get(1), "rows aren't where 1.20.1 put them");
        helper.assertTrue(list.getLeft() == 100 && list.getRight() == 400 && list.getTop() == 40 && list.getBottom() == 140,
                "Forge's edge getters are " + list.getLeft() + "," + list.getTop() + "," + list.getRight() + "," + list.getBottom());
        // a 26.3 left click (button 1) reaches the row as 1.20.1's button 0, a right click (3) as 1
        list.mouseClicked(new MouseButtonEvent(250, 45, new MouseButtonInfo(1, 0)), false);
        list.mouseClicked(new MouseButtonEvent(250, 45, new MouseButtonInfo(3, 0)), false);
        helper.assertTrue(rows.get(0).clicks.equals(List.of(0, 1)), "the row got buttons " + rows.get(0).clicks);
        // 10 rows of 25 in 100 - 4 visible: scrolling stops at 250 - 96
        list.setScrollAmount(1000);
        helper.assertTrue(list.getScrollAmount() == 154 && list.getMaxScroll() == 154, "scrolled to " + list.getScrollAmount());
        list.mouseScrolled(250, 60, 0, 1);
        helper.assertTrue(list.getScrollAmount() == 154 - 12.5, "a scroll step moved to " + list.getScrollAmount());
        helper.assertTrue(rows.get(0).isMouseOver(250, 45) == (list.at(250, 45) == rows.get(0)), "rows know where they are");
        helper.succeed();
    }
}
