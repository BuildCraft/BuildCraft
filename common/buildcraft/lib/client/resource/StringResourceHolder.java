/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Charsets;

import net.minecraft.util.ResourceLocation;

public class StringResourceHolder extends ResourceHolder {
    private static final String REGEX_LINE_END = "\\R";
    private List<String> lines = new ArrayList<>();

    public StringResourceHolder(ResourceLocation location) {
        super(location);
    }

    public List<String> getLines() {
        return lines;
    }

    @Override
    protected final void onLoad(byte[] data) {
        String fullData = new String(data, Charsets.UTF_8);
        List<String> newLines = new ArrayList<>();
        Collections.addAll(newLines, fullData.split(REGEX_LINE_END));
        lines = newLines;
        onStringChange();
    }

    public void onStringChange() {}
}
