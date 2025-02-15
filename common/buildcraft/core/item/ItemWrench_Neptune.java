package buildcraft.core.item;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ItemWrench_Neptune extends ItemBC_Neptune implements IToolWrench {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:wrenched");

    public ItemWrench_Neptune(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxStackSize(1);
    }

    @Override
    public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
//        player.swingArm(hand);
        player.swing(hand);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        // Calen: if here is false, player.isShiftKeyDown() in PipeBehaviourLapis#onPipeActivate will always return false
//        return false;
        return true;
    }

    /**
     * Calen: called before block#use
     *
     * <br> {@link Item#onItemUseFirst(ItemStack, UseOnContext)} // pipe_lapis color negative shift
     * <br> if({@link Player#isShiftKeyDown()} && {@link Item#doesSneakBypassUse(ItemStack, LevelReader, BlockPos, Player)}) {
     * <br>     result = {@link net.minecraft.world.level.block.Block#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)} // pipe_lapis color positive shift | chest open gui
     * <br> }
     * <br> if(!{@link InteractionResult#consumesAction()}) {
     * <br>     {@link Item#useOn(UseOnContext)} // rotate block
     * <br> }
     * <br> to enable following all:
     * <br> right click chest -> open
     * <br> shift + right click chest -> rotate
     * <br> right click pipe_lapis -> positive shift color
     * <br> shift + right click pipe_lapis -> negative shift color
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player.isShiftKeyDown()) {
            return useOn(ctx);
        } else {
            return super.onItemUseFirst(stack, ctx);
        }
    }

    @Override
////    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        InteractionHand hand = ctx.getHand();
        Direction side = ctx.getClickedFace();
        Vec3 vec3Pos = ctx.getClickLocation();
        double hitX = vec3Pos.x;
        double hitY = vec3Pos.y;
        double hitZ = vec3Pos.z;

        // FIXME: Disabled world check as it doesn't allow us to swing the player's arm!
        // if (world.isRemote) {
        // return EnumActionResult.PASS;
        // }
        BlockState state = world.getBlockState(pos);
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
        if (result == InteractionResult.SUCCESS) {
            wrenchUsed(player, hand, player.getItemInHand(hand), new BlockHitResult(new Vec3(hitX, hitY, hitZ), side, pos, false));
        }
        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack stack1, final ItemStack stack2) {
        return false;
    }
}
