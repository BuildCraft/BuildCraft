package buildcraft.energy.item;

import buildcraft.energy.generation.structure.OilGenerator;
import buildcraft.energy.generation.structure.OilGenerator.GenType;
import buildcraft.energy.generation.structure.OilPlacer;
import buildcraft.energy.generation.structure.OilStructure;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.TimeUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemOilPlacer extends ItemBC_Neptune {
    public ItemOilPlacer(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    private static final String TAG_TYPE = "type";

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        final ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            final CompoundTag tag = itemStack.getOrCreateTag();

            if (tag.contains(TAG_TYPE)) {
                final byte mode = tag.getByte(TAG_TYPE);
                tag.putByte(TAG_TYPE, (byte) ((mode + 1) % GenType.values().length));
            } else {
                tag.putByte(TAG_TYPE, (byte) GenType.LARGE.ordinal());
            }

            GenType craterType = GenType.values()[tag.getByte(TAG_TYPE)];

            ((ServerPlayer) player).sendSystemMessage(Component.literal("TYPE = " + craterType.name()));
        } else {

            this.place(serverLevel, serverPlayer, itemStack, player.blockPosition());
        }
        return InteractionResultHolder.success(itemStack);
    }

    private void place(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos pos) {
        player.sendSystemMessage(Component.literal(ChatFormatting.AQUA + ">>>>>>>>> " + TimeUtil.formatNow()));
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_TYPE)) {
            tag.putByte(TAG_TYPE, (byte) GenType.LARGE.ordinal());
        }

        GenType genType = GenType.values()[tag.getByte(TAG_TYPE)];

        player.sendSystemMessage(Component.literal(
                ChatFormatting.GOLD + "GenType = " +
                        ChatFormatting.YELLOW + genType
        ));
        player.sendSystemMessage(Component.literal(
                ChatFormatting.GOLD + "BlockPos = " +
                        ChatFormatting.YELLOW + "[X = " + pos.getX() + " Y = " + pos.getY() + " Z = " + pos.getZ() + "]"
        ));

        // localize some const values
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();
        int radius = 16 * OilGenerator.MAX_CHUNK_RADIUS;
        // the Box required by StructurePiece
        BlockPos min = new BlockPos(pos.getX() - radius, minBuildHeight, pos.getZ() - radius);
        Box box = new Box(min, min.offset(2 * radius, maxBuildHeight - minBuildHeight, 2 * radius));
        player.sendSystemMessage(Component.literal(
                ChatFormatting.GOLD + "Structure " +
                        ChatFormatting.YELLOW + "Creating " +
                        ChatFormatting.GOLD + TimeUtil.formatNow()
        ));
        // Structure
        OilStructure structure = OilGenerator.createStructureByType(genType, level.random, player.getBlockX(), player.getBlockZ(), minBuildHeight, maxBuildHeight, box);
        // Placer
        if (structure != null) {
            player.sendSystemMessage(Component.literal(
                    ChatFormatting.GOLD + "Structure " +
                            ChatFormatting.YELLOW + "Placing " +
                            ChatFormatting.GOLD + TimeUtil.formatNow()
            ));
            final OilPlacer placer = new OilPlacer(level, structure.pieces, box.getBB());
            placer.place();
            player.sendSystemMessage(Component.literal(ChatFormatting.GREEN + ">>>>>>>>> " + TimeUtil.formatNow()));
        } else {
            player.sendSystemMessage(Component.literal("None!"));
        }
    }
}
