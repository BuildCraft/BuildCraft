package buildcraft.core.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.StringUtils;

/** Created by asie on 3/7/15. */
@Deprecated
public class ItemDebugger extends ItemBuildCraft {
    public ItemDebugger() {
        super();

        setFull3D();
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        // TileEntity tile = world.getTileEntity(pos);
        // if (tile instanceof IDebuggable) {
        // ArrayList<String> info = new ArrayList<String>();
        // String effSide = FMLCommonHandler.instance().getEffectiveSide().name().substring(0, 1) + ":";
        // ((IDebuggable) tile).getDebugInfo(info, side);
        // for (String s : info) {
        // player.addChatComponentMessage(new ChatComponentText(effSide + s));
        // }
        // return true;
        // }
        return false;
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean debug) {
        list.add(StringUtils.localize("item.debugger.warning"));
    }
}
