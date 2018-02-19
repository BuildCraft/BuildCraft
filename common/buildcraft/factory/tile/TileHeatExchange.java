package buildcraft.factory.tile;

import buildcraft.api.BCBlocks;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.cap.CapabilityHelper;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.misc.*;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TileHeatExchange extends TileBC_Neptune implements ITickable, IDebuggable {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("HeatExchanger");
    public static final int NET_ID_CHANGE_SECTION = IDS.allocId("CHANGE_SECTION");
    public static final int NET_ID_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_ID_TANK_OUT = IDS.allocId("TANK_OUT");
    public static final int NET_ID_STATE = IDS.allocId("STATE");

    /** Fluid amount multipliers -- this is the maximum amount of fluid that can be transfered per tick. All numbers
     * need to be divisors of 1000 */
    private static final int[] FLUID_MULT = { 10, 16, 20 };

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    protected ExchangeSection section;
    private boolean checkNeighbours;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        NBTTagCompound nbtSection = nbt.getCompoundTag("section");
        if (!nbtSection.hasNoTags()) {
            if (nbtSection.getBoolean("start")) {
                section = new ExchangeSectionStart(this, nbtSection);
            } else {
                section = new ExchangeSectionEnd(this, nbtSection);
            }
        }
        checkNeighbours = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (section != null) {
            nbt.setTag("section", section.writeToNbt());
        }
        return nbt;
    }

    @Override
    public void update() {
        if (checkNeighbours) {
            checkNeighbours = false;
            Deque<TileHeatExchange> exchangers = findAdjacentExchangers();
            if (world.isRemote) {
                // Find the start + end sections and link them up
                if (exchangers.size() > 2) {
                    TileHeatExchange start = exchangers.getFirst();
                    TileHeatExchange end = exchangers.getLast();
                    if (start.isStart() && end.isEnd()) {
                        ((ExchangeSectionStart) start.section).endSection = (ExchangeSectionEnd) end.section;
                    }
                }
            } else {
                if (exchangers.size() < 3) {
                    // TODO: Remove all exchangers sections
                } else if (exchangers.size() > 5) {
                    // TODO: Remove all exchangers sections
                } else {
                    ExchangeSectionStart sectionStart = null;
                    ExchangeSectionEnd sectionEnd = null;
                    for (TileHeatExchange exchange : exchangers) {
                        // For efficiency, only run this check once.
                        exchange.checkNeighbours = false;
                        if (exchange.section instanceof ExchangeSectionStart) {
                            if (sectionStart == null) {
                                sectionStart = (ExchangeSectionStart) exchange.section;
                            } else {
                                // TODO: Attempt to merge sections together!
                            }
                        } else if (exchange.section instanceof ExchangeSectionEnd) {
                            if (sectionEnd == null) {
                                sectionEnd = (ExchangeSectionEnd) exchange.section;
                            } else {
                                // TODO: Attempt to merge sections together!
                            }
                        }
                        exchange.section = null;
                    }
                    if (sectionStart == null) {
                        sectionStart = new ExchangeSectionStart(exchangers.getFirst());
                    }
                    if (sectionEnd == null) {
                        sectionEnd = new ExchangeSectionEnd(exchangers.getLast());
                    }
                    sectionStart.endSection = sectionEnd;
                    sectionStart.middleCount = exchangers.size() - 2;
                    exchangers.getFirst().setSection(sectionStart);
                    exchangers.getLast().setSection(sectionEnd);
                    for (TileHeatExchange exchange : exchangers) {
                        exchange.sendNetworkUpdate(NET_ID_CHANGE_SECTION);
                    }
                }
            }
        }
        if (section != null) {
            section.tick();
        }
    }

    private Deque<TileHeatExchange> findAdjacentExchangers() {
        EnumFacing thisFacing = getFacing();
        EnumFacing dirToStart = thisFacing.rotateY();
        EnumFacing dirToEnd = thisFacing.rotateYCCW();
        Deque<TileHeatExchange> exchangers = new ArrayDeque<>();
        exchangers.add(this);
        for (int i = 1; i < 6; i++) {
            TileEntity neighbour = getLocalTile(pos.offset(dirToStart, i));
            if (neighbour instanceof TileHeatExchange) {
                TileHeatExchange other = (TileHeatExchange) neighbour;
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addFirst(other);
            } else {
                break;
            }
        }
        for (int i = 1; i < 6; i++) {
            TileEntity neighbour = getLocalTile(pos.offset(dirToEnd, i));
            if (neighbour instanceof TileHeatExchange) {
                TileHeatExchange other = (TileHeatExchange) neighbour;
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addLast(other);
            } else {
                break;
            }
        }
        return exchangers;
    }

    private void setSection(ExchangeSection section) {
        if (this.section != section) {
            this.section = section;
            section.tile = this;
            sendNetworkUpdate(NET_ID_CHANGE_SECTION);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_ID_CHANGE_SECTION, buffer, side, ctx);
            } else if (id == NET_ID_CHANGE_SECTION) {
                if (buffer.readBoolean()) {
                    boolean start = buffer.readBoolean();
                    if (start) {
                        section = new ExchangeSectionStart(this);
                    } else {
                        section = new ExchangeSectionEnd(this);
                    }
                    section.readPayload(NET_ID_CHANGE_SECTION, buffer, side, ctx);
                } else {
                    section = null;
                }
                checkNeighbours = true;
            } else if (section != null) {
                section.readPayload(id, buffer, side, ctx);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_ID_CHANGE_SECTION, buffer, side);
            } else if (id == NET_ID_CHANGE_SECTION) {
                if (section == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeBoolean(section instanceof ExchangeSectionStart);
                    section.writePayload(id, buffer, side);
                }
            } else if (section != null) {
                section.writePayload(id, buffer, side);
            }
        }
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (section instanceof ExchangeSectionStart) {
            // Temp
            return BoundingBoxUtil.makeAround(VecUtil.convertCenter(getPos()), 10);
        }
        return super.getRenderBoundingBox();
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (section != null) {
            return section.caps.getCapability(capability, facing);
        }
        return null;
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
        float hitZ) {
        return section != null && section.tankManager.onActivated(player, pos, hand);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (section instanceof ExchangeSectionStart) {
            ((ExchangeSectionStart) section).endSection = null;
        }
    }

    @Override
    public void validate() {
        super.validate();
        checkNeighbours = true;
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos nehighbour) {
        if (nehighbour.getY() != pos.getY()) {
            // Heat exchange tiles can only be horizontally adjacent
            return;
        }
        checkNeighbours = true;
    }

    @Override
    public void addDrops(List<ItemStack> toDrop, int fortune) {
        super.addDrops(toDrop, fortune);
        if (section != null) {
            section.tankManager.addDrops(toDrop);
        }
    }

    public boolean isStart() {
        return section instanceof ExchangeSectionStart;
    }

    public boolean isEnd() {
        return section instanceof ExchangeSectionEnd;
    }

    public ExchangeSection getSection() {
        return section;
    }

    @Nullable
    EnumFacing getFacing() {
        IBlockState state = getCurrentStateForBlock(BCBlocks.Factory.HEAT_EXCHANGE);
        if (state == null) {
            return null;
        }
        return state.getValue(BlockBCBase_Neptune.PROP_FACING);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        if (section == null) {
            left.add("section = null");
        } else {
            left.add("section = " + (section instanceof ExchangeSectionStart ? "start" : "end"));
            section.getDebugInfo(left, right, side);
        }
    }

    static abstract class ExchangeSection {
        final Tank tankInput, tankOutput;
        final TankManager tankManager;
        public final FluidSmoother smoothedTankInput, smoothedTankOutput;
        public final CapabilityHelper caps = new CapabilityHelper();

        public TileHeatExchange tile;

        ExchangeSection(TileHeatExchange tile) {
            this.tile = tile;
            tankInput = new Tank("input", 2 * Fluid.BUCKET_VOLUME, tile);
            tankOutput = new Tank("output", 2 * Fluid.BUCKET_VOLUME, tile);
            tankOutput.setCanFill(false);
            tankManager = new TankManager(tankOutput, tankInput);
            smoothedTankInput = createFluidSmoother(tankInput, NET_ID_TANK_IN);
            smoothedTankOutput = createFluidSmoother(tankOutput, NET_ID_TANK_OUT);
        }

        ExchangeSection(TileHeatExchange tile, NBTTagCompound nbt) {
            this(tile);
            tankInput.readFromNBT(nbt.getCompoundTag("input"));
            tankOutput.readFromNBT(nbt.getCompoundTag("output"));
        }

        FluidSmoother createFluidSmoother(Tank tank, int netId) {
            return new FluidSmoother(w -> tile.createAndSendMessage(netId, w), tank);
        }

        NBTTagCompound writeToNbt() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("input", tankInput.serializeNBT());
            nbt.setTag("output", tankOutput.serializeNBT());
            return nbt;
        }

        void tick() {
            World world = tile.world;
            smoothedTankInput.tick(world);
            smoothedTankOutput.tick(world);
        }

        void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
            if (side == Side.CLIENT) {
                if (id == NET_ID_CHANGE_SECTION) {
                    readPayload(NET_ID_TANK_IN, buffer, side, ctx);
                    readPayload(NET_ID_TANK_OUT, buffer, side, ctx);
                    smoothedTankInput.resetSmoothing(tile.world);
                    smoothedTankOutput.resetSmoothing(tile.world);
                } else if (id == NET_ID_TANK_IN) {
                    smoothedTankInput.handleMessage(tile.world, buffer);
                } else if (id == NET_ID_TANK_OUT) {
                    smoothedTankOutput.handleMessage(tile.world, buffer);
                }
            } else if (side == Side.SERVER) {

            }
        }

        void writePayload(int id, PacketBufferBC buffer, Side side) {
            if (side == Side.SERVER) {
                if (id == NET_ID_CHANGE_SECTION) {
                    writePayload(NET_ID_TANK_IN, buffer, side);
                    writePayload(NET_ID_TANK_OUT, buffer, side);
                } else if (id == NET_ID_TANK_IN) {
                    smoothedTankInput.writeInit(buffer);
                } else if (id == NET_ID_TANK_OUT) {
                    smoothedTankOutput.writeInit(buffer);
                }
            } else if (side == Side.CLIENT) {

            }
        }

        void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
            left.add("tank_input = " + tankInput.getDebugString());
            left.add("tank_output = " + tankOutput.getDebugString());
            left.add("smoothed_input: ");
            smoothedTankInput.getDebugInfo(left, right, side);
            left.add("smoothed_output: ");
            smoothedTankOutput.getDebugInfo(left, right, side);
        }
    }

    public static class ExchangeSectionStart extends ExchangeSection {

        private ExchangeSectionEnd endSection;
        public int middleCount;
        private int progress = 0;
        private int progressLast = 0;
        private EnumProgressState progressState = EnumProgressState.OFF;
        private EnumProgressState lastSentState = EnumProgressState.OFF;
        private int inputCoolantAmountCharge = 0;
        private int inputHeatantAmountCharge = 0;

        {
            tankInput.setFilter(this::isHeatant);
            caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankInput, EnumPipePart.DOWN);
            caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);
        }

        ExchangeSectionStart(TileHeatExchange tile) {
            super(tile);
        }

        ExchangeSectionStart(TileHeatExchange tile, NBTTagCompound nbt) {
            super(tile, nbt);
            inputCoolantAmountCharge = nbt.getInteger("coolantCharge");
            inputHeatantAmountCharge = nbt.getInteger("heatantCharge");
        }

        @Override
        NBTTagCompound writeToNbt() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("start", true);
            nbt.setInteger("coolantCharge", inputCoolantAmountCharge);
            nbt.setInteger("heatantCharge", inputHeatantAmountCharge);
            return nbt;
        }

        @Override
        void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
            super.readPayload(id, buffer, side, ctx);
            if (side == Side.CLIENT) {
                if (id == NET_ID_CHANGE_SECTION) {
                    middleCount = buffer.readUnsignedByte();
                } else if (id == NET_ID_STATE) {
                    progressState = buffer.readEnumValue(EnumProgressState.class);
                }
            }
        }

        @Override
        void writePayload(int id, PacketBufferBC buffer, Side side) {
            super.writePayload(id, buffer, side);
            if (side == Side.SERVER) {
                if (id == NET_ID_CHANGE_SECTION) {
                    buffer.writeByte(middleCount);
                } else if (id == NET_ID_STATE) {
                    buffer.writeEnumValue(progressState);
                }
            }
        }

        public ExchangeSectionEnd getEndSection() {
            return endSection;
        }

        public EnumProgressState getProgressState() {
            return progressState;
        }

        public double getProgress(float partialTicks) {
            return MathUtil.interp(partialTicks, progressLast, progress) / 120.0;
        }

        private boolean isHeatant(FluidStack fluid) {
            return BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getRecipeForInput(fluid) != null;
        }

        private IFluidHandler getTankForSide(EnumFacing side) {
            EnumFacing thisFacing = tile.getFacing();
            if (thisFacing == null || side != thisFacing.rotateY()) {
                return null;
            }
            return tankOutput;
        }

        @Override
        void tick() {
            super.tick();

            updateProgress();
            if (tile.world.isRemote) {
                spawnParticles();
                return;
            }
            if (endSection != null) {
                craft();
            } else if (progressState != EnumProgressState.OFF) {
                progressState = EnumProgressState.STOPPING;
            }
            output();
            if (progressState != lastSentState) {
                lastSentState = progressState;
                tile.sendNetworkUpdate(NET_ID_STATE);
            }
        }

        private void updateProgress() {
            progressLast = progress;
            switch (progressState) {
                case STOPPING: {
                    progress--;
                    if (progress <= 0) {
                        progress = 0;
                        progressState = EnumProgressState.OFF;
                    }
                    return;
                }
                case PREPARING:
                case RUNNING: {
                    int lag = 120;
                    progress++;
                    if (progress >= lag) {
                        progress = lag;
                        progressState = EnumProgressState.RUNNING;
                    }
                    return;
                }
                default: {
                }
            }
        }

        private void craft() {
            Tank c_in = endSection.tankInput;
            Tank c_out = tankOutput;
            Tank h_in = tankInput;
            Tank h_out = endSection.tankOutput;
            IRefineryRecipeManager reg = BuildcraftRecipeRegistry.refineryRecipes;
            ICoolableRecipe c_recipe = reg.getCoolableRegistry().getRecipeForInput(c_in.getFluid());
            IHeatableRecipe h_recipe = reg.getHeatableRegistry().getRecipeForInput(h_in.getFluid());
            if (h_recipe == null || c_recipe == null) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
                BCLog.logger.warn("Invalid heat values!");
                progressState = EnumProgressState.STOPPING;
                return;
            }
            int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
            int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
            if (h_diff < 1 || c_diff < 1) {
                throw new IllegalStateException("Invalid recipe " + c_recipe + ", " + h_recipe);
            }

            FluidStack c_in_f_raw = c_recipe.in();
            FluidStack c_out_f_raw = c_recipe.out();
            FluidStack h_in_f_raw = h_recipe.in();
            FluidStack h_out_f_raw = h_recipe.out();

            // TODO: Use "charge" to add mb to the charge
            // Ok, so how is the API meant to work? It looks like we just drop the relative amounts...
            // TODO: Make mult the *maximum* multiplier, not the exact one.
            int max = FLUID_MULT[middleCount - 1];
            boolean needs_c = true;// heatProvided <= 0;
            boolean needs_h = true;// coolingProvided <= 0;

            FluidStack c_in_f = setAmount(c_recipe.in(), max);
            FluidStack c_out_f = setAmount(c_recipe.out(), max);
            FluidStack h_in_f = setAmount(h_recipe.in(), max);
            FluidStack h_out_f = setAmount(h_recipe.out(), max);
            if (canFill(c_out, c_out_f) && canFill(h_out, h_out_f) && canDrain(c_in, c_in_f)
                && canDrain(h_in, h_in_f)) {
                if (progressState == EnumProgressState.OFF) {
                    progressState = EnumProgressState.PREPARING;
                } else if (progressState == EnumProgressState.RUNNING) {
                    // heatProvided--;
                    // coolingProvided--;
                    if (needs_c) {
                        // heatProvided += c_diff;
                        fill(c_out, c_out_f);
                        drain(c_in, c_in_f);
                    }

                    if (needs_h) {
                        // coolingProvided += h_diff;
                        fill(h_out, h_out_f);
                        drain(h_in, h_in_f);
                    }
                }
            } else {
                progressState = EnumProgressState.STOPPING;
            }
        }

        private void spawnParticles() {
            if (progressState == EnumProgressState.RUNNING) {
                ExchangeSectionEnd end = endSection;
                if (end == null) {
                    return;
                }
                Vec3d from = VecUtil.convertCenter(tile.getPos());
                FluidStack c_in_f = end.smoothedTankInput.getFluidForRender();
                if (c_in_f != null && c_in_f.getFluid() == FluidRegistry.LAVA) {
                    EnumFacing facing = tile.getFacing();
                    if (facing != null) {
                        spewForth(from, facing.rotateY(), EnumParticleTypes.SMOKE_LARGE);
                    }
                }

                FluidStack h_in_f = smoothedTankInput.getFluidForRender();
                from = VecUtil.convertCenter(end.tile.getPos());
                if (h_in_f != null && h_in_f.getFluid() == FluidRegistry.WATER) {
                    EnumFacing dir = EnumFacing.UP;
                    spewForth(from, dir, EnumParticleTypes.CLOUD);
                }
            }
        }

        private void spewForth(Vec3d from, EnumFacing dir, EnumParticleTypes particle) {
            Vec3d vecDir = new Vec3d(dir.getDirectionVec());
            from = from.add(vecDir);

            double x = from.xCoord;
            double y = from.yCoord;
            double z = from.zCoord;

            Vec3d motion = VecUtil.scale(vecDir, 0.4);
            int particleCount = Minecraft.getMinecraft().gameSettings.particleSetting;
            World w = tile.getWorld();
            if (particleCount == 2 || w == null) {
                return;
            }
            particleCount = particleCount == 0 ? 5 : 2;
            for (int i = 0; i < particleCount; i++) {
                double dx = motion.xCoord + (Math.random() - 0.5) * 0.1;
                double dy = motion.yCoord + (Math.random() - 0.5) * 0.1;
                double dz = motion.zCoord + (Math.random() - 0.5) * 0.1;
                double interp = i / (double) particleCount;
                x -= dx * interp;
                y -= dy * interp;
                z -= dz * interp;

                w.spawnParticle(particle, x, y, z, dx, dy, dz);
            }
        }

        private void output() {
            IFluidHandler thisOut = getFluidAutoOutputTarget();
            FluidUtilBC.move(tankOutput, thisOut, 1000);

            if (endSection != null) {
                IFluidHandler endOut = endSection.getFluidAutoOutputTarget();
                FluidUtilBC.move(endSection.tankOutput, endOut, 1000);
            }
        }

        private static FluidStack setAmount(FluidStack fluid, int mult) {
            if (fluid == null) {
                return null;
            }
            return new FluidStack(fluid, mult);
        }

        private static boolean canFill(Tank t, FluidStack fluid) {
            return fluid == null || t.fillInternal(fluid, false) == fluid.amount;
        }

        private static boolean canDrain(Tank t, FluidStack fluid) {
            FluidStack f2 = t.drainInternal(fluid, false);
            return f2 != null && f2.amount == fluid.amount;
        }

        private static void fill(Tank t, FluidStack fluid) {
            if (fluid == null) {
                return;
            }
            int a = t.fillInternal(fluid, true);
            if (a != fluid.amount) {
                String err = "Buggy transition! Failed to fill " + fluid.getFluid();
                throw new IllegalStateException(err + " x " + fluid.amount + " into " + t);
            }
        }

        private static void drain(Tank t, FluidStack fluid) {
            FluidStack f2 = t.drainInternal(fluid, true);
            if (f2 == null || f2.amount != fluid.amount) {
                String err = "Buggy transition! Failed to drain " + fluid.getFluid();
                throw new IllegalStateException(err + " x " + fluid.amount + " from " + t);
            }
        }

        @Nullable
        private IFluidHandler getFluidAutoOutputTarget() {
            EnumFacing facing = tile.getFacing();
            if (facing == null) {
                return null;
            }
            TileEntity neighbour = tile.getNeighbourTile(facing.rotateY());
            if (neighbour == null) {
                return null;
            }
            return neighbour.getCapability(CapUtil.CAP_FLUIDS, facing.rotateYCCW());
        }

        @Override
        void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
            super.getDebugInfo(left, right, side);
            left.add("progress = " + progress);
            left.add("state = " + progressState);
            left.add("has_end = " + (endSection != null));
            // left.add("heatProvided = " + heatProvided);
            // left.add("coolingProvided = " + coolingProvided);
        }
    }

    public static class ExchangeSectionEnd extends ExchangeSection {

        {
            tankInput.setFilter(this::isCoolant);
            caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankOutput, EnumPipePart.UP);
            caps.addCapability(CapUtil.CAP_FLUIDS, this::getTankForSide, EnumPipePart.HORIZONTALS);
        }

        ExchangeSectionEnd(TileHeatExchange tile) {
            super(tile);
        }

        ExchangeSectionEnd(TileHeatExchange tile, NBTTagCompound nbt) {
            super(tile, nbt);
        }

        private boolean isCoolant(FluidStack fluid) {
            return BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getRecipeForInput(fluid) != null;
        }

        private IFluidHandler getTankForSide(EnumFacing side) {
            EnumFacing thisFacing = tile.getFacing();
            if (thisFacing == null || side != thisFacing.rotateYCCW()) {
                return null;
            }
            return tankInput;
        }

        @Override
        NBTTagCompound writeToNbt() {
            NBTTagCompound nbt = super.writeToNbt();
            nbt.setBoolean("start", false);
            return nbt;
        }

        @Nullable
        IFluidHandler getFluidAutoOutputTarget() {
            TileEntity neighbour = tile.getNeighbourTile(EnumFacing.UP);
            if (neighbour == null) {
                return null;
            }
            return neighbour.getCapability(CapUtil.CAP_FLUIDS, EnumFacing.DOWN);
        }
    }

    public enum EnumProgressState {
        /** Progress is at 0, not moving. */
        OFF,
        /** Progress is increasing from 0 to max */
        PREPARING,
        /** progress stays at max */
        RUNNING,
        /** Progress is decreasing from max to 0. */
        STOPPING
    }
}
