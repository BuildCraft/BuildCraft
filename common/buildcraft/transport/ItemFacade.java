/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.PipeWire;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.world.FakeBlockAccessSingleBlock;
import buildcraft.core.proxy.CoreProxy;

public class ItemFacade extends ItemBuildCraft implements IFacadeItem, IPipePluggableItem {
    public static class FacadeState {
        public final IBlockState state;
        public final boolean transparent;
        public final boolean hollow;
        public final PipeWire wire;

        public FacadeState(IBlockState state, PipeWire wire) {
            this.state = state;
            this.wire = wire;
            this.transparent = false;
            this.hollow = false;
        }

        public FacadeState(IBlockState state, PipeWire wire, boolean hollow) {
            this.state = state;
            this.wire = wire;
            this.transparent = false;
            this.hollow = hollow;
        }

        public FacadeState(NBTTagCompound nbt) {
            String key = nbt.getString("block");
            Block block = (Block) Block.blockRegistry.getObject(new ResourceLocation(key));
            if (block == null) throw new NullPointerException("Could not load a block from the key \"" + key + "\"");
            int metadata = nbt.getByte("metadata");
            state = block.getStateFromMeta(metadata);
            this.wire = nbt.hasKey("wire") ? PipeWire.fromOrdinal(nbt.getByte("wire")) : null;
            this.transparent = nbt.hasKey("transparent") && nbt.getBoolean("transparent");
            this.hollow = nbt.hasKey("hollow") && nbt.getBoolean("hollow");
        }

        private FacadeState(PipeWire wire) {
            state = Blocks.bedrock.getDefaultState();
            this.wire = wire;
            this.transparent = true;
            this.hollow = false;
        }

        public static FacadeState create(IBlockState state) {
            return create(state, null);
        }

        public static FacadeState create(IBlockState state, PipeWire wire) {
            return new FacadeState(state, wire);
        }

        public static FacadeState createTransparent(PipeWire wire) {
            return new FacadeState(wire);
        }

        public void writeToNBT(NBTTagCompound nbt) {
            if (state != null) {
                nbt.setString("block", Utils.getNameForBlock(state.getBlock()));
                nbt.setByte("metadata", (byte) state.getBlock().getMetaFromState(state));
            }
            if (wire != null) {
                nbt.setByte("wire", (byte) wire.ordinal());
            }
            nbt.setBoolean("transparent", transparent);
            nbt.setBoolean("hollow", hollow);
        }

        public static NBTTagList writeArray(FacadeState[] states) {
            if (states == null) {
                return null;
            }
            NBTTagList list = new NBTTagList();
            for (FacadeState state : states) {
                NBTTagCompound stateNBT = new NBTTagCompound();
                state.writeToNBT(stateNBT);
                list.appendTag(stateNBT);
            }
            return list;
        }

        public static FacadeState[] readArray(NBTTagList list) {
            if (list == null) {
                return new FacadeState[0];
            }
            final int length = list.tagCount();
            FacadeState[] states = new FacadeState[length];
            for (int i = 0; i < length; i++) {
                states[i] = new FacadeState(list.getCompoundTagAt(i));
            }
            return states;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(hollow).append(transparent).append(state).append(wire).build();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            FacadeState other = (FacadeState) obj;
            if (hollow != other.hollow) return false;
            if (state == null) {
                if (other.state != null) return false;
            } else if (!state.equals(other.state)) return false;
            if (transparent != other.transparent) return false;
            if (wire != other.wire) return false;
            return true;
        }
    }

    public static final ArrayList<ItemStack> allStacks = new ArrayList<>();

    private static final HashSet<IBlockState> blacklistedFacades = new HashSet<>();
    private static final HashSet<IBlockState> whitelistedFacades = new HashSet<>();
    private static final Block NULL_BLOCK = null;
    private static final ItemStack NO_MATCH = new ItemStack(NULL_BLOCK, 0, 0);
    private static final Block[] PREVIEW_FACADES = new Block[] { Blocks.planks, Blocks.stonebrick, Blocks.glass };

    private static ArrayList<ItemStack> previewStacks;

    public ItemFacade() {
        super(BuildCraftTransport.showAllFacadesCreative ? BCCreativeTab.get("facades") : BCCreativeTab.get("main"));

        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        switch (getFacadeType(itemstack)) {
            case Basic:
                FacadeState[] states = getFacadeStates(itemstack);
                String displayName = states.length > 0 ? getFacadeStateDisplayName(states[0]) : "CORRUPT";
                return super.getItemStackDisplayName(itemstack) + ": " + displayName;
            case Phased:
                return BCStringUtils.localize("item.FacadePhased.name");
            default:
                return "";
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        return "item.Facade";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean debug) {
        for (FacadeState state : getFacadeStates(stack)) {
            if (state != null && !state.transparent && state.state != null && Item.getItemFromBlock(state.state.getBlock()) != null) {
                Item.getItemFromBlock(state.state.getBlock()).addInformation(new ItemStack(state.state.getBlock(), 1, state.state.getBlock()
                        .getMetaFromState(state.state)), player, list, debug);
            }
        }
        if (getFacadeType(stack) == FacadeType.Phased) {
            String stateString = BCStringUtils.localize("item.FacadePhased.state");
            FacadeState defaultState = null;
            for (FacadeState state : getFacadeStates(stack)) {
                if (state.wire == null) {
                    defaultState = state;
                    continue;
                }
                list.add(String.format(stateString, state.wire.getColor(), getFacadeStateDisplayName(state)));
            }
            if (defaultState != null) {
                list.add(1, String.format(BCStringUtils.localize("item.FacadePhased.state_default"), getFacadeStateDisplayName(defaultState)));
            }
        }
    }

    public static String getFacadeStateDisplayName(FacadeState state) {
        if (state.state == null) {
            return BCStringUtils.localize("item.FacadePhased.state_transparent");
        }
        // if (state.state.getBlock().getRenderType() == 31) {
        // TODO: Find out what render type is 31... and what this now means
        // meta &= 0x3;
        // } else if (state.block.getRenderType() == 39 && meta > 2) {
        // meta = 2;
        // }
        String s = CoreProxy.proxy.getItemDisplayName(new ItemStack(state.state.getBlock(), 1, state.state.getBlock().getMetaFromState(state.state)));
        if (state.hollow) {
            s += " (" + BCStringUtils.localize("item.Facade.state_hollow") + ")";
        }
        return s;
    }

    public static List<IBlockState> getAllFacades() {
        List<IBlockState> stacks = new ArrayList<>();

        for (Block b : Block.blockRegistry) {
            for (int i = 0; i < 16; i++) {
                try {
                    IBlockState state = b.getStateFromMeta(i);
                    if (isValidFacade(state)) {
                        stacks.add(state);
                    }
                } catch (Exception e) {

                }
            }
        }

        return stacks;
    }

    public static boolean isValidFacade(IBlockState state) {
        if (blacklistedFacades.contains(state)) {
            return false;
        }

        if (whitelistedFacades.contains(state)) {
            return true;
        }

        Block block = state.getBlock();

        if (block instanceof IFluidBlock || block.hasTileEntity(state)) {
            return false;
        }

        block.setBlockBoundsBasedOnState(new FakeBlockAccessSingleBlock(state), BlockPos.ORIGIN);

        if (block.getBlockBoundsMinX() != 0.0D || block.getBlockBoundsMinY() != 0.0D || block.getBlockBoundsMinZ() != 0.0D) {
            return false;
        }

        if (block.getBlockBoundsMaxX() != 1.0D || block.getBlockBoundsMaxY() != 1.0D || block.getBlockBoundsMaxZ() != 1.0D) {
            return false;
        }

        return true;
    }

    public static boolean isTransparentFacade(IBlockState state) {
        Block block = state.getBlock();

        return !block.isVisuallyOpaque() && !block.isOpaqueCube();
    }

    private static void generateFacadeStacks() {
        HashSet<IBlockState> states = new HashSet<>();

        for (Block b : Block.blockRegistry) {
            for (int i = 0; i < 16; i++) {
                try {
                    Item item = Item.getItemFromBlock(b);
                    if (item != null) {
                        IBlockState state = b.getStateFromMeta(i);
                        if (!states.contains(state) && isValidFacade(state)) {
                            states.add(state);
                            allStacks.add(BuildCraftTransport.facadeItem.getFacadeForBlock(state));
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        if (BuildCraftTransport.showAllFacadesCreative) {
            previewStacks = allStacks;
        } else {
            previewStacks = new ArrayList<>();

            List<ItemStack> hollowFacades = new ArrayList<>();
            for (Block b : PREVIEW_FACADES) {
                if (isValidFacade(b.getDefaultState()) && !blacklistedFacades.contains(b.getDefaultState())) {
                    ItemStack facade = BuildCraftTransport.facadeItem.getFacadeForBlock(b.getDefaultState());
                    previewStacks.add(facade);
                    FacadeState state = getFacadeStates(facade)[0];
                    hollowFacades.add(getFacade(new FacadeState(state.state, state.wire, true)));
                }
            }
            previewStacks.addAll(hollowFacades);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List itemList) {
        itemList.addAll(previewStacks);
    }

    public void initialize() {
        for (String s : BuildCraftTransport.facadeBlacklist) {
            // TODO: Add state support?
            blacklistFacade(Block.getBlockFromName(s));
        }
    }

    public static FacadeState[] getFacadeStates(ItemStack stack) {
        if (!stack.hasTagCompound()) return new FacadeState[0];
        NBTTagCompound nbt = stack.getTagCompound();
        nbt = migrate(stack, nbt);
        if (!nbt.hasKey("states")) return new FacadeState[0];
        return FacadeState.readArray(nbt.getTagList("states", Constants.NBT.TAG_COMPOUND));
    }

    private static NBTTagCompound migrate(ItemStack stack, NBTTagCompound nbt) {
        Block block = null, blockAlt = null;
        int metadata = 0, metadataAlt;
        PipeWire wire = null;
        if (nbt.hasKey("id")) block = Block.blockRegistry.getObjectById(nbt.getInteger("id"));
        else if (nbt.hasKey("name")) block = Block.blockRegistry.getObject(new ResourceLocation(nbt.getString("name")));
        if (nbt.hasKey("name_alt")) blockAlt = Block.blockRegistry.getObject(new ResourceLocation(nbt.getString("name_alt")));
        if (nbt.hasKey("meta")) metadata = nbt.getInteger("meta");
        if (nbt.hasKey("meta_alt")) metadataAlt = nbt.getInteger("meta_alt");
        else metadataAlt = stack.getItemDamage() & 0x0000F;
        if (nbt.hasKey("wire")) wire = PipeWire.fromOrdinal(nbt.getInteger("wire"));
        if (block != null) {
            FacadeState[] states;
            FacadeState mainState = FacadeState.create(block.getStateFromMeta(metadata));
            if (blockAlt != null && wire != null) {
                FacadeState altState = FacadeState.create(blockAlt.getStateFromMeta(metadataAlt), wire);
                states = new FacadeState[] { mainState, altState };
            } else {
                states = new FacadeState[] { mainState };
            }
            NBTTagCompound newNbt = getFacade(states).getTagCompound();
            stack.setTagCompound(newNbt);
            return newNbt;
        }
        return nbt;
    }

    @Override
    public IBlockState[] getBlockStatesForFacade(ItemStack stack) {
        FacadeState[] states = getFacadeStates(stack);
        IBlockState[] blocks = new IBlockState[states.length];
        for (int i = 0; i < states.length; i++) {
            blocks[i] = states[i].state;
        }
        return blocks;
    }

    // GETTERS FOR FACADE DATA
    @Override
    public FacadeType getFacadeType(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return FacadeType.Basic;
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey("type")) {
            return FacadeType.Basic;
        }
        return FacadeType.fromOrdinal(nbt.getInteger("type"));
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        // Simply send shift click to the pipe / mod block.
        return true;
    }

    public void initializeRecipes() {
        generateFacadeStacks();

        Set<IBlockState> states = new HashSet<>();

        for (Block b : Block.blockRegistry) {
            for (int i = 0; i < 16; i++) {
                try {
                    IBlockState state = b.getStateFromMeta(i);
                    if (!states.contains(state) && isValidFacade(state)) {
                        states.add(state);

                        ItemStack itemStack = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));

                        ItemStack facade = getFacadeForBlock(state);
                        ItemStack facade6 = facade.copy();
                        facade6.stackSize = 6;

                        FacadeState fstate = getFacadeStates(facade6)[0];
                        ItemStack facadeHollow = getFacade(new FacadeState(fstate.state, fstate.wire, true));

                        ItemStack facade6Hollow = facadeHollow.copy();
                        facade6Hollow.stackSize = 6;

                        if (!Loader.isModLoaded("BuildCraft|Silicon") || BuildCraftTransport.facadeForceNonLaserRecipe) {
                            GameRegistry.addShapedRecipe(facade6, "t ", "ts", "t ", 't', itemStack, 's', BuildCraftTransport.pipeStructureCobblestone);
                            GameRegistry.addShapedRecipe(facade6Hollow, "t ", " s", "t ", 't', itemStack, 's', BuildCraftTransport.pipeStructureCobblestone);
                        } else {
                            // TODO: Make the Assembly Table variant a dynamic recipe handler to save RAM

                            String recipeId = "buildcraft:facade{" + Utils.getNameForBlock(state.getBlock()) + "#" + itemStack.getItemDamage() + "}";

                            BuildcraftRecipeRegistry.assemblyTable.addRecipe(recipeId, 8000, facade6, new ItemStack(BuildCraftTransport.pipeStructureCobblestone,
                                    3), itemStack);

                            BuildcraftRecipeRegistry.assemblyTable.addRecipe(recipeId + ":hollow", 8000, facade6Hollow, new ItemStack(
                                    BuildCraftTransport.pipeStructureCobblestone, 3), itemStack);

                            BuildcraftRecipeRegistry.assemblyTable.addRecipe(recipeId + ":toHollow", 160, facadeHollow, facade);
                            BuildcraftRecipeRegistry.assemblyTable.addRecipe(recipeId + ":fromHollow", 160, facade, facadeHollow);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public static void whitelistFacade(IBlockState state) {
         whitelistedFacades.add(state);
    }

    public static void whitelistFacade(Block block) {
        if (block != null) {
            for (IBlockState state : block.getBlockState().getValidStates()) {
                whitelistFacade(state);
            }
        }
    }

    public static void blacklistFacade(IBlockState state) {
        blacklistedFacades.add(state);
    }

    public static void blacklistFacade(Block block) {
        if (block != null) {
            for (IBlockState state : block.getBlockState().getValidStates()) {
                blacklistFacade(state);
            }
        }
    }

    public class FacadeRecipe implements IRecipe {

        @Override
        public boolean matches(InventoryCrafting inventorycrafting, World world) {
            Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);

            return facade != null && facade[0] != null && ((IBlockState[]) facade[0]).length == 1;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
            Object[] facade = getFacadeBlockFromCraftingGrid(inventorycrafting);
            if (facade == null || ((IBlockState[]) facade[0]).length != 1) return null;

            IBlockState block = ((IBlockState[]) facade[0])[0];
            ItemStack originalFacade = (ItemStack) facade[1];

            if (block == null) return null;

            return getNextFacadeItemStack(block, originalFacade);
        }

        private Object[] getFacadeBlockFromCraftingGrid(InventoryCrafting inventorycrafting) {
            ItemStack slotmatch = null;
            int countOfItems = 0;
            for (int i = 0; i < inventorycrafting.getSizeInventory(); i++) {
                ItemStack slot = inventorycrafting.getStackInSlot(i);

                if (slot != null && slot.getItem() == ItemFacade.this && slotmatch == null) {
                    slotmatch = slot;
                    countOfItems++;
                } else if (slot != null) slotmatch = NO_MATCH;

                if (countOfItems > 1) return null;
            }

            if (slotmatch != null && slotmatch != NO_MATCH) return new Object[] { getBlockStatesForFacade(slotmatch), slotmatch };

            return null;
        }

        private ItemStack getNextFacadeItemStack(IBlockState state, ItemStack originalFacade) {
            // TODO: Add an API for me!
            IBlockState newState = state;
            if (newState.getPropertyNames().contains(BlockQuartz.VARIANT)) {
                BlockQuartz.EnumType type = newState.getValue(BlockQuartz.VARIANT);
                if ("lines".equals(type.toString())) {
                    newState = newState.withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.byMetadata(((type.getMetadata() - 1) % 3) + 2));
                }
            } else {
                for (net.minecraft.block.properties.IProperty<?> prop : state.getProperties().keySet()) {
                    if (prop.getName().equals("axis")) {
                        newState = newState.cycleProperty(prop);
                    }
                }
            }

            return getFacadeForBlock(newState);
        }

        @Override
        public int getRecipeSize() {
            return 1;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return null;
        }

        @Override
        public ItemStack[] getRemainingItems(InventoryCrafting inv) {
            ItemStack[] itemStack = new ItemStack[inv.getSizeInventory()];

            for (int i = 0; i < itemStack.length; ++i) {
                ItemStack itemstack = inv.getStackInSlot(i);
                itemStack[i] = ForgeHooks.getContainerItem(itemstack);
            }

            return itemStack;
        }
    }

    @Override
    public ItemStack getFacadeForBlock(IBlockState state) {
        return getFacade(FacadeState.create(state));
    }

    public static ItemStack getAdvancedFacade(PipeWire wire, IBlockState state, IBlockState stateAlt) {
        return getFacade(FacadeState.create(state), FacadeState.create(stateAlt, wire));
    }

    public static ItemStack getFacade(FacadeState... states) {
        if (states == null || states.length == 0) return null;
        final boolean basic = states.length == 1 && states[0].wire == null;

        ItemStack stack = new ItemStack(BuildCraftTransport.facadeItem, 1, 0);

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("type", (byte) (basic ? FacadeType.Basic : FacadeType.Phased).ordinal());
        nbt.setTag("states", FacadeState.writeArray(states));

        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
        return new FacadePluggable(getFacadeStates(stack));
    }
}
