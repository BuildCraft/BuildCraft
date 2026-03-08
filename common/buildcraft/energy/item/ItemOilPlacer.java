package buildcraft.energy.item;

import buildcraft.energy.generation.structure.OilGenerator;
import buildcraft.energy.generation.structure.OilGenerator.GenType;
import buildcraft.energy.generation.structure.OilPlacer;
import buildcraft.energy.generation.structure.OilStructure;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.TimeUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ItemOilPlacer extends ItemBC_Neptune {
    public ItemOilPlacer(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    private static final String TAG_TYPE = "type";

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if (level.isClientSide()) {
            return ActionResult.pass(player.getItemInHand(hand));
        }

        ServerWorld serverLevel = (ServerWorld) level;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        final ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            final CompoundNBT tag = itemStack.getOrCreateTag();

            if (tag.contains(TAG_TYPE)) {
                final byte mode = tag.getByte(TAG_TYPE);
                tag.putByte(TAG_TYPE, (byte) ((mode + 1) % GenType.values().length));
            } else {
                tag.putByte(TAG_TYPE, (byte) GenType.LARGE.ordinal());
            }

            GenType craterType = GenType.values()[tag.getByte(TAG_TYPE)];

            player.sendMessage(new StringTextComponent("TYPE = " + craterType.name()), Util.NIL_UUID);
        } else {

            this.place(serverLevel, serverPlayer, itemStack, player.blockPosition());
        }
        return ActionResult.success(itemStack);
    }

    private void place(ServerWorld level, ServerPlayerEntity player, ItemStack stack, BlockPos pos) {
        player.sendMessage(new StringTextComponent(TextFormatting.AQUA + ">>>>>>>>> " + TimeUtil.formatNow()), Util.NIL_UUID);
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_TYPE)) {
            tag.putByte(TAG_TYPE, (byte) GenType.LARGE.ordinal());
        }

        GenType genType = GenType.values()[tag.getByte(TAG_TYPE)];

        player.sendMessage(new StringTextComponent(
                TextFormatting.GOLD + "GenType = " +
                        TextFormatting.YELLOW + genType
        ), Util.NIL_UUID);
        player.sendMessage(new StringTextComponent(
                TextFormatting.GOLD + "BiomeCategory = " +
                        TextFormatting.YELLOW + level.getBiome(pos).getBiomeCategory()
        ), Util.NIL_UUID);
        player.sendMessage(new StringTextComponent(
                TextFormatting.GOLD + "BlockPos = " +
                        TextFormatting.YELLOW + "[X = " + pos.getX() + " Y = " + pos.getY() + " Z = " + pos.getZ() + "]"
        ), Util.NIL_UUID);

        // localize some const values
        int minBuildHeight = 0;
//        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        int radius = 16 * OilGenerator.MAX_CHUNK_RADIUS;
        // the Box required by StructurePiece
        BlockPos min = new BlockPos(pos.getX() - radius, minBuildHeight, pos.getZ() - radius);
        Box box = new Box(min, min.offset(2 * radius, maxBuildHeight - minBuildHeight, 2 * radius));
        player.sendMessage(new StringTextComponent(
                TextFormatting.GOLD + "Structure " +
                        TextFormatting.YELLOW + "Creating " +
                        TextFormatting.GOLD + TimeUtil.formatNow()
        ), Util.NIL_UUID);
        // Structure
        OilStructure structure = OilGenerator.createStructureByType(genType, level.random, player.blockPosition().getX(), player.blockPosition().getZ(), minBuildHeight, box);
        // Placer
        if (structure != null) {
            player.sendMessage(new StringTextComponent(
                    TextFormatting.GOLD + "Structure " +
                            TextFormatting.YELLOW + "Placing " +
                            TextFormatting.GOLD + TimeUtil.formatNow()
            ), Util.NIL_UUID);
            final OilPlacer placer = new OilPlacer(level, structure.pieces, box.getBB());
            placer.place();
            player.sendMessage(new StringTextComponent(TextFormatting.GREEN + ">>>>>>>>> " + TimeUtil.formatNow()), Util.NIL_UUID);
        } else {
            player.sendMessage(new StringTextComponent("None!"), Util.NIL_UUID);
        }
    }
}
