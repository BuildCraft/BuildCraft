/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.elem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import com.google.common.collect.ForwardingList;

import buildcraft.lib.misc.StringUtilBC;

public class ToolTip extends ForwardingList<String> implements RandomAccess {

    /* If the impl list class does not implement RandomAccess then the interface MUST be removed from this class */
    private final List<String> delegate = new ArrayList<>();
    private final long delay;
    private long mouseOverStart;

    /** Creates a {@link ToolTip} based off of an array of localisation keys. The localised strings can use "\n" to
     * split up into separate lines. */
    public static ToolTip createLocalized(String... localeKeys) {
        List<String> allLines = new ArrayList<>();
        for (String key : localeKeys) {
            String localized = StringUtilBC.localize(key);
            allLines.addAll(StringUtilBC.splitIntoLines(localized));
        }
        return new ToolTip(allLines);
    }

    public ToolTip(String... lines) {
        this.delay = 0;
        Collections.addAll(delegate, lines);
    }

    public ToolTip(int delay, String... lines) {
        this.delay = delay;
        Collections.addAll(delegate, lines);
    }

    private ToolTip(List<String> lines) {
        this.delay = 0;
        delegate.addAll(lines);
    }

    @Override
    protected final List<String> delegate() {
        return delegate;
    }

    public void onTick(boolean mouseOver) {
        if (delay == 0) {
            return;
        }
        if (mouseOver) {
            if (mouseOverStart == 0) {
                mouseOverStart = System.currentTimeMillis();
            }
        } else {
            mouseOverStart = 0;
        }
    }

    public boolean isReady() {
        if (delay == 0) {
            return true;
        }
        if (mouseOverStart == 0) {
            return false;
        }
        return System.currentTimeMillis() - mouseOverStart >= delay;
    }

    public void refresh() {}
}
