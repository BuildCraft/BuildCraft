/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class StackRequest {
    public ItemStack stack;
    public int index;
    public TileEntity requester;
    public DockingStation station;
}
