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
import java.util.Optional;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
    public TemplateBuilder(ITileForTemplateBuilder tile) {
        super(tile);
    }

    private Template.BuildingInfo getBuildingInfo() {
        return tile.getTemplateBuildingInfo();
    }

    @Override
    protected List<BlockPos> getToBreak() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo -> buildingInfo.toBreak)
                .orElse(Collections.emptyList());
    }

    @Override
    protected List<BlockPos> getToPlace() {
        return Optional.ofNullable(getBuildingInfo())
                .map(buildingInfo -> buildingInfo.toPlace)
                .orElse(Collections.emptyList());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return tile.getWorldBC().isAirBlock(blockPos);
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return Collections.singletonList(tile.getInvResources().extract(null, 1, 1, false));
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        FakePlayerBC fakePlayer = FakePlayerUtil.INSTANCE.getFakePlayer(
                (WorldServer) tile.getWorldBC(),
                tile.getBuilderPos(),
                tile.getOwner()
        );
        fakePlayer.setHeldItem(fakePlayer.getActiveHand(), placeTask.items.get(0));
        EnumActionResult result = placeTask.items.get(0).onItemUse(
                fakePlayer,
                tile.getWorldBC(),
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
        return getBuildingInfo().toPlace.contains(blockPos) && !tile.getWorldBC().isAirBlock(blockPos);
    }

    @Override
    public Box getBox() {
        return Optional.ofNullable(getBuildingInfo())
                .map(Template.BuildingInfo::getBox)
                .orElse(null);
    }

    @Override
    protected boolean isDone() {
        return getBuildingInfo() != null &&
                getBuildingInfo().toBreak.stream().allMatch(tile.getWorldBC()::isAirBlock) &&
                getBuildingInfo().toPlace.stream().allMatch(this::isBlockCorrect);
    }

}
