/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.stripes;

import java.lang.reflect.Method;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.ReflectionHelper;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesPipe;

public class DispenserStripesHandler implements IStripesHandler {

    private static Method dispenseStack =
        ReflectionHelper.findMethod(BehaviorDefaultDispenseItem.class, null,
            new String[]{"func_82487_b", "dispenseStack"}, IBlockSource.class, ItemStack.class);

    private IBehaviorDispenseItem dispenser;

    public DispenserStripesHandler(IBehaviorDispenseItem dispenser) {
        this.dispenser = dispenser;
    }

    @Override
    public StripesBehavior behave(final IStripesPipe pipe, StripesAction act, ItemStack is) {
        Position pos = pipe.getPosition();
        IBlockSource source = new BlockSourceImpl(pipe.getWorld(), (int) pos.x, (int) pos.y, (int) pos.z){
            @Override
            public int getBlockMetadata() {
                return pipe.getOpenOrientation().ordinal();
            }
        };
        if (dispenser instanceof BehaviorDefaultDispenseItem) {
            try {
                dispenseStack.invoke(dispenser, source, is);
                return StripesBehavior.DROP;
            } catch (Exception e) {
                //NOOP
            }
        }
        dispenser.dispense(source, is);
        return StripesBehavior.DROP;
    }
}