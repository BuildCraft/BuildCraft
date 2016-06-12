package buildcraft.builders.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.bpt.*;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.misc.NBTUtils;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemSchematicSingle extends ItemBuildCraft_BC8 {
    private static final String NBT_KEY_SCHEMATIC = "schematic";
    private static final int DAMAGE_CLEAN = 0;
    private static final int DAMAGE_STORED_SCHEMATIC = 1;

    public ItemSchematicSingle(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (stack.getItemDamage() == DAMAGE_CLEAN) {
            return 16;
        } else {
            return super.getItemStackLimit(stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, DAMAGE_CLEAN, "clean");
        addVariant(variants, DAMAGE_STORED_SCHEMATIC, "used");
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        if (player.isSneaking()) {
            NBTTagCompound itemData = NBTUtils.getItemData(stack);
            itemData.removeTag(NBT_KEY_SCHEMATIC);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return EnumActionResult.SUCCESS;
        }
        int damage = stack.getItemDamage();
        if (damage != DAMAGE_STORED_SCHEMATIC) {
            IBlockState state = world.getBlockState(pos);
            SchematicFactoryWorldBlock factory = BlueprintAPI.getWorldBlockSchematic(state.getBlock());
            if (factory != null) {
                try {
                    SchematicBlock schematic = factory.createFromWorld(world, pos);
                    NBTTagCompound schematicData = schematic.serializeNBT();
                    NBTTagCompound itemData = NBTUtils.getItemData(stack);
                    itemData.setTag(NBT_KEY_SCHEMATIC, schematicData);
                    stack.setItemDamage(DAMAGE_STORED_SCHEMATIC);
                    return EnumActionResult.SUCCESS;
                } catch (SchematicException e) {
                    e.printStackTrace();
                }
            }
            return EnumActionResult.FAIL;
        } else {
            NBTTagCompound schematicNBT = NBTUtils.getItemData(stack).getCompoundTag(NBT_KEY_SCHEMATIC);
            if (schematicNBT == null) {
                player.addChatMessage(new TextComponentString("No schematic data!"));
                return EnumActionResult.FAIL;
            }
            BlockPos place = pos.offset(side);
            if (!world.isAirBlock(place)) {
                player.addChatMessage(new TextComponentString("Not an air block @" + place));
                return EnumActionResult.FAIL;
            } else {
                world.setBlockToAir(place);
            }
            try {
                SchematicBlock schematic = BlueprintAPI.deserializeSchematicBlock(schematicNBT);
                IBuilderAccessor playerBuilder = new BuilderPlayer(player);
                boolean failed = false;
                for (IBptTask task : schematic.createTasks(playerBuilder, place)) {
                    task.receivePower(playerBuilder, Integer.MAX_VALUE);
                    if (!task.isDone(playerBuilder)) {
                        player.addChatMessage(new TextComponentString("A task failed!"));
                        failed = true;
                    }
                }
                if (failed) {
                    return EnumActionResult.FAIL;
                }
                return EnumActionResult.SUCCESS;
            } catch (SchematicException e) {
                e.printStackTrace();
                return EnumActionResult.FAIL;
            }
        }
    }
}
