/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;
import com.google.common.collect.ForwardingList;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class ToolTip extends ForwardingList<Component> implements RandomAccess {

    /* If the impl list class does not implement RandomAccess then the interface MUST be removed from this class */
    private final List<Component> delegate = new ArrayList<>();
    private final long delay;
    private long mouseOverStart;

    /** Creates a {@link ToolTip} based off of an array of localisation keys. The localised strings can use "\n" to
     * split up into separate lines. */
    public static ToolTip createLocalized(String... localeKeys) {
        List<Component> allLines = new ArrayList<>();
        for (String key : localeKeys) {
            String localized = LocaleUtil.localize(key);
            allLines.addAll(StringUtilBC.splitIntoLines(localized));
        }
        return new ToolTip(allLines);
    }

    public ToolTip(Component... lines) {
        this.delay = 0;
        Collections.addAll(delegate, lines);
    }

    public ToolTip(int delay, Component... lines) {
        this.delay = delay;
        Collections.addAll(delegate, lines);
    }

    public ToolTip(List<Component> lines) {
        this.delay = 0;
        delegate.addAll(lines);
    }

    @Override
    protected final List<Component> delegate() {
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

    public void refresh() {
    }
}
