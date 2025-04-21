package buildcraft.core.item;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ItemWrench_Neptune extends ItemBC_Neptune implements IToolWrench {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:wrenched");

    public ItemWrench_Neptune(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxStackSize(1);
    }

    @Override
    public boolean canWrench(PlayerEntity player, Hand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(PlayerEntity player, Hand hand, ItemStack wrench, RayTraceResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
//        player.swingArm(hand);
        player.swing(hand);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        // Calen: if here is false, player.isShiftKeyDown() in PipeBehaviourLapis#onPipeActivate will always return false
//        return false;
        return true;
    }

    /**
     * Calen: called before block#use
     *
     * <br> {@link Item#onItemUseFirst(ItemStack, ItemUseContext)} // pipe_lapis color negative shift
     * <br> if({@link PlayerEntity#isShiftKeyDown()} && {@link Item#doesSneakBypassUse(ItemStack, IWorldReader, BlockPos, PlayerEntity)}) {
     * <br>     result = {@link net.minecraft.block.Block#use(BlockState, World, BlockPos, PlayerEntity, Hand, BlockRayTraceResult)} // pipe_lapis color positive shift | chest open gui
     * <br> }
     * <br> if(!{@link ActionResultType#consumesAction()}) {
     * <br>     {@link Item#useOn(ItemUseContext)} // rotate block
     * <br> }
     * <br> to enable following all:
     * <br> right click chest -> open
     * <br> shift + right click chest -> rotate
     * <br> right click pipe_lapis -> positive shift color
     * <br> shift + right click pipe_lapis -> negative shift color
     */
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        if (player.isShiftKeyDown()) {
            return useOn(ctx);
        } else {
            return super.onItemUseFirst(stack, ctx);
        }
    }

    @Override
//    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    public ActionResultType useOn(ItemUseContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        World world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Hand hand = ctx.getHand();
        Direction side = ctx.getClickedFace();
        Vector3d vec3Pos = ctx.getClickLocation();
        double hitX = vec3Pos.x;
        double hitY = vec3Pos.y;
        double hitZ = vec3Pos.z;

        // FIXME: Disabled world check as it doesn't allow us to swing the player's arm!
        // if (world.isRemote) {
        // return EnumActionResult.PASS;
        // }
        BlockState state = world.getBlockState(pos);
        ActionResultType result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
        if (result == ActionResultType.SUCCESS) {
            wrenchUsed(player, hand, player.getItemInHand(hand), new BlockRayTraceResult(new Vector3d(hitX, hitY, hitZ), side, pos, false));
        }
        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack stack1, final ItemStack stack2) {
        return false;
    }
}
