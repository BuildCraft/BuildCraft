package buildcraft.builders.snapshot;

import buildcraft.lib.fake.FakePlayerBC;
import buildcraft.lib.misc.FakePlayerUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
    public TemplateBuilder(ITileForTemplateBuilder tile) {
        super(tile);
    }

    private Template.BuildingInfo getBuildingInfo() {
        return tile.getTemplateBuildingInfo();
    }

    @Override
    protected List<BlockPos> getToBreak() {
        return getBuildingInfo() == null ? Collections.emptyList() : getBuildingInfo().toBreak;
    }

    @Override
    protected List<BlockPos> getToPlace() {
        return getBuildingInfo() == null ? Collections.emptyList() : getBuildingInfo().toPlace;
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Collections.singletonList(tile.getInvResources().extract(null, 1, 1, false));
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        FakePlayerBC fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer(
                (WorldServer) tile.getWorld(),
                tile.getBuilderPos(),
                tile.getOwner()
        );
        fakePlayer.setHeldItem(fakePlayer.getActiveHand(), placeTask.items.get(0));
        EnumActionResult result = placeTask.items.get(0).onItemUse(
                fakePlayer,
                tile.getWorld(),
                placeTask.pos,
                fakePlayer.getActiveHand(),
                EnumFacing.UP,
                0.5F,
                0.0F,
                0.5F
        );
        return result == EnumActionResult.SUCCESS;
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        tile.getInvResources().insert(placeTask.items.get(0), false, false);
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        return getBuildingInfo().toPlace.contains(blockPos) && !tile.getWorld().isAirBlock(blockPos);
    }

    @Override
    protected Box getBox() {
        return tile.getTemplateBuildingInfo().getBox();
    }
}
