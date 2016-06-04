package buildcraft.lib.bpt.builder;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.bpt.BlueprintAPI;
import buildcraft.api.bpt.IBptAction;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.lib.permission.PlayerOwner;

public abstract class AbstractBuilder implements IBuilder, ITickable {
    private final PlayerOwner owner;
    private final World world;
    private final DelayedList<IBptAction> actions = new DelayedList<>();
    private final BuilderAnimationManager animationManager;

    public AbstractBuilder(PlayerOwner owner, World world, BuilderAnimationManager animationManager) {
        this.owner = owner;
        this.world = world;
        this.animationManager = animationManager;
    }

    public AbstractBuilder(PlayerOwner owner, World world, BuilderAnimationManager animation, NBTTagCompound nbt) {
        this(owner, world, animation);
        NBTTagList list = (NBTTagList) nbt.getTag("actions");
        for (int delay = 0; delay < list.tagCount(); delay++) {
            NBTTagList innerList = (NBTTagList) list.get(delay);
            for (int j = 0; j < innerList.tagCount(); j++) {
                NBTTagCompound tag = innerList.getCompoundTagAt(j);
                actions.add(delay, BlueprintAPI.deserializeAction(tag));
            }
        }
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        List<List<IBptAction>> actions = this.actions.getAllElements();
        NBTTagList list = new NBTTagList();
        for (List<IBptAction> innerActions : actions) {
            NBTTagList innerList = new NBTTagList();

            for (IBptAction action : innerActions) {
                innerList.appendTag(BlueprintAPI.serializeAction(action));
            }

            list.appendTag(innerList);
        }
        nbt.setTag("actions", list);

        return nbt;
    }

    @Override
    public void update() {
        for (IBptAction action : actions.advance()) {
            action.run(this);
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public PlayerOwner getOwner() {
        return owner;
    }

    @Override
    public int startBlockAnimation(Vec3d target, IBlockState state, int delay) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return 0;
    }

    @Override
    public int startItemStackAnimation(Vec3d target, ItemStack display, int delay) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return 0;
    }

    @Override
    public int[] startFluidAnimation(Vec3d target, FluidStack fluid, int delay) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public int[] startPowerAnimation(Vec3d target, int milliJoules, int delay) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public IRequestedStack requestStack(IStackFilter filter, int amunt) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public IRequestedFluid requestFluid(IFluidFilter filter, int amount) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
        return null;
    }

    @Override
    public void addAction(IBptAction action, int delay) {
        actions.add(delay, action);
    }
}
