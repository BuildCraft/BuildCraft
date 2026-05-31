/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.gui.help;

import java.util.List;

import buildcraft.lib.gui.IHelpElement;

public class DummyHelpElement implements IHelpElement {
    public static final DummyHelpElement INSTANCE = new DummyHelpElement();
    @Override public void addHelpElements(List<ElementHelpInfo.HelpPosition> elements) {}
}
