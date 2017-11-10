/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.schematics;

import java.util.HashMap;

import net.minecraft.item.Item;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class BptPipeExtension {

	private static final HashMap<Item, BptPipeExtension> bptPipeExtensionRegistry = new HashMap<Item, BptPipeExtension>();

	public BptPipeExtension(Item i) {
		bptPipeExtensionRegistry.put(i, this);
	}

	public void postProcessing(SchematicTile slot, IBuilderContext context) {

	}

	public void rotateLeft(SchematicTile slot, IBuilderContext context) {

	}

	public static boolean contains(Item i) {
		return bptPipeExtensionRegistry.containsKey(i);
	}

	public static BptPipeExtension get(Item i) {
		return bptPipeExtensionRegistry.get(i);
	}

}
