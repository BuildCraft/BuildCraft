package ct.buildcraft.core.item;

import ct.buildcraft.api.blocks.CustomRotationHelper;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.tools.IToolWrench;
import ct.buildcraft.builders.tile.TileBuilder;
import ct.buildcraft.core.BCCore;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.tile.TileTank;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLLoader;

public class ItemWrench extends Item implements IToolWrench{

	public ItemWrench() {
		super(new Item.Properties().stacksTo(1).tab(BCCore.BUILDCRAFT_TAB));
	}

	@Override
	public InteractionResult useOn(UseOnContext coc) {
        // FIXME: Disabled world check as it doesn't allow us to swing the player's arm!
        // if (world.isRemote) {
        // return EnumActionResult.PASS;
        // }
//		var s = FMLLoader.getGamePath().toAbsolutePath();
//		BCLog.logger.debug(s.toString());
		Level world = coc.getLevel();
		BlockPos pos = coc.getClickedPos();
		Direction side = coc.getClickedFace();
		Player player = coc.getPlayer();
		InteractionHand hand = coc.getHand();
		Vec3 c = coc.getClickLocation();
        BlockState state = world.getBlockState(pos);
		//DEBUG
        var f = world.getBlockEntity(pos);
      //  if(f!=null)
        BCLog.logger.info("isTile? :"+(f!=null));
		if(world.getBlockEntity(pos) instanceof TileBuilder tile) {
			ct.buildcraft.api.core.BCLog.logger.debug("ItemWrench :"+(tile.path != null));
			return InteractionResult.CONSUME;
		}//*/
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);

        if (result == InteractionResult.SUCCESS) {
            wrenchUsed(player, hand, player.getItemInHand(hand), BlockHitResult.miss(c, side, pos));
            //world.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        SoundUtil.playSlideSound(world, pos, state, result);

        return result;
	}

	@Override
	public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
//        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);/TODO
        player.swingingArm = hand;
	}

	

}
