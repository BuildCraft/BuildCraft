package ct.buildcraft.api.transport.pluggable;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

@AutoRegisterCapability
public abstract class PipePluggable {
	public final static PipePluggable EMPTY = new PipePluggable(null,null,null) {
		public VoxelShape getBoundingBox() {return Shapes.empty();}};
	
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public /*final*/ Direction side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    /** Writes the payload that will be passed into
     * {@link PluggableDefinition#loadFromBuffer(IPipeHolder, Direction, FriendlyByteBuf)} on the client. (This is called
     * on the server and sent to the client). Note that this will be called *instead* of write and read payload. */
    public void writeCreationPayload(FriendlyByteBuf buffer) {

    }

    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {

    }

    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {

    }

    public final void scheduleNetworkUpdate() {
        holder.scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.ordinal()]);
    }

    public void onTick() {}

    /** @return A bounding box that will be used for collisions and raytracing. */
    public abstract VoxelShape getBoundingBox();

    /** @return True if the pipe cannot connect outwards (it is blocked), or False if this does not block the pipe. */
    public boolean isBlocking() {
        return false;
    }

    /** Gets the value of a specified capability key, or null if the given capability is not supported at the call time.
     * This is effectively {@link ICapabilityProvider}, but where
     * {@link ICapabilityProvider#hasCapability(Capability, Direction)} will return true when this returns a non-null
     * value. */
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return LazyOptional.empty();
    }

    /** Gets the {@link Capability} that is accessible from the pipe that this is attached to.
     * 
     * @param cap
     * @return */
    public <T> LazyOptional<T> getInternalCapability(@Nonnull Capability<T> cap) {
        return LazyOptional.empty();
    }

    /** Called whenever this pluggable is removed from the pipe. */
    public void onRemove() {}

    /** @param toDrop A list containing all the items to drop (so you should add your items to this list) */
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        ItemStack stack = getPickStack();
        if (!stack.isEmpty()) {
            toDrop.add(stack);
        }
    }

    /** Called whenever this pluggable is picked by the player (similar to Block.getPickBlock)
     * 
     * @return The stack that should be picked, or ItemStack.EMPTY if no stack can be picked from this pluggable. */
    public ItemStack getPickStack() {
        return ItemStack.EMPTY;
    }

    public InteractionResult onPluggableActivate(Player player, BlockHitResult trace, Level level) {
        return InteractionResult.PASS;
    }

    @Nullable
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        return null;
    }

    /** Called if the {@link IPluggableStaticBaker} returns quads with tint indexes set to
     * <code>data * 6 + key.side.ordinal()</code>. <code>"data"</code> is passed in here as <code>"tintIndex"</code>.
     * 
     * @return The tint index to render the quad with, or -1 for default. */
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(int tintIndex) {
        return -1;
    }

    /** PipePluggable version of
     * {@link Block#canBeConnectedTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, Direction)}.
     *not exist in 1.19.2 anymore *///TODO CHeck
    @Deprecated
    public boolean canBeConnected() {
        return false;
    }

    /** PipePluggable version of
     * {@link net.minecraft.block.state.BlockState#isSideSolid(IBlockAccess, BlockPos, Direction)} */
    public boolean isSideSolid() {
        return false;
    }

    /** PipePluggable version of {@link Block#getExplosionResistance(World, BlockPos, Entity, Explosion)} */
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return 0;
    }

    public boolean canConnectToRedstone(@Nullable Direction to) {
        return false;
    }

    /** PipePluggable version of
     * {@link net.minecraft.block.state.BlockState#getBlockFaceShape(IBlockAccess, BlockPos, Direction)} */
/*    public BlockFaceShape getBlockFaceShape() {
        return BlockFaceShape.UNDEFINED;
        
    }
    //Minecraft code
    public enum BlockFaceShape
    {
        SOLID,
        BOWL,
        CENTER_SMALL,
        MIDDLE_POLE_THIN,
        CENTER,
        MIDDLE_POLE,
        CENTER_BIG,
        MIDDLE_POLE_THICK,
        UNDEFINED;
    }*/

    public void onPlacedBy(Player player) {

    }

	public void rotate(Rotation axis) {
		if(this != PipePluggable.EMPTY)
			this.side = axis.rotate(side);
	}

}
