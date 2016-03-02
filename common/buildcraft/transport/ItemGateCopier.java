package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.transport.BlockGenericPipe.Part;
import buildcraft.transport.BlockGenericPipe.RaytraceResult;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GatePluggable;

public class ItemGateCopier extends ItemBuildCraft {
    // Item damages for what the gate copier holds
    private static final int META_EMPTY = 0;
    private static final int META_FULL = 1;

    public ItemGateCopier() {
        super();
        setMaxStackSize(1);
        setMaxDamage(META_FULL);
        setUnlocalizedName("gateCopier");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        boolean isCopying = !player.isSneaking();
        Block block = world.getBlockState(pos).getBlock();
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound data = NBTUtils.getItemData(stack);
        PipePluggable pluggable = null;
        Gate gate = null;

        if (tile == null || !(tile instanceof IPipeTile)) {
            isCopying = true;
        } else {
            if (tile instanceof TileGenericPipe && block instanceof BlockGenericPipe) {
                RaytraceResult rayTraceResult = ((BlockGenericPipe) block).doRayTrace(world, pos, player);
                if (rayTraceResult != null && rayTraceResult.boundingBox != null && rayTraceResult.hitPart == Part.Pluggable) {
                    pluggable = ((TileGenericPipe) tile).getPipePluggable(rayTraceResult.sideHit);
                }
            } else {
                pluggable = ((IPipeTile) tile).getPipePluggable(side);
            }
        }

        if (pluggable instanceof GatePluggable) {
            gate = ((GatePluggable) pluggable).realGate;
        }

        if (isCopying) {
            if (gate == null) {
                data = new NBTTagCompound();
                stack.setTagCompound(data);

                // Tell ItemModelMesher that this is NOT damageable, so it will use the meta for the icon
                data.setBoolean("Unbreakable", true);

                // Tell ItemStack.getToolTip() that we want to hide the resulting "Unbreakable" line that we just added
                data.setInteger("HideFlags", 4);

                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.clear"));
                return true;
            }

            data = new NBTTagCompound();
            stack.setTagCompound(data);

            gate.writeStatementsToNBT(data);
            data.setByte("material", (byte) gate.material.ordinal());
            data.setByte("logic", (byte) gate.logic.ordinal());
            player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.gateCopied"));

            // Tell ItemModelMesher that this is NOT damageable, so it will use the meta for the icon
            data.setBoolean("Unbreakable", true);

            // Tell ItemStack.getToolTip() that we want to hide the resulting "Unbreakable" line that we just added
            data.setInteger("HideFlags", 4);
        } else {
            if (!data.hasKey("logic")) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.noInformation"));
                return true;
            } else if (gate == null) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.noGate"));
                return true;
            }

            GateMaterial dataMaterial = GateMaterial.fromOrdinal(data.getByte("material"));
            GateMaterial gateMaterial = gate.material;

            if (gateMaterial.numSlots < dataMaterial.numSlots) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.slots"));
            }
            if (gateMaterial.numActionParameters < dataMaterial.numActionParameters) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.actionParameters"));
            }
            if (gateMaterial.numTriggerParameters < dataMaterial.numTriggerParameters) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.triggerParameters"));
            }
            if (data.getByte("logic") != gate.logic.ordinal()) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.logic"));
            }

            gate.readStatementsFromNBT(data);
            if (!gate.verifyGateStatements()) {
                player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.warning.load"));
            }

            if (tile instanceof TileGenericPipe) {
                ((TileGenericPipe) tile).sendNetworkUpdate();
            }
            player.addChatMessage(new ChatComponentTranslation("chat.gateCopier.gatePasted"));
            return true;
        }

        return true;

    }

    @Override
    public int getMetadata(ItemStack stack) {
        return stack.hasTagCompound() ? META_FULL : META_EMPTY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelHelper.registerItemModel(this, META_EMPTY, "_empty");
        ModelHelper.registerItemModel(this, META_FULL, "_full");
    }
}
