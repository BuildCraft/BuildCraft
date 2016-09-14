package buildcraft.silicon;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileLaser;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BCSiliconProxy implements IGuiHandler {
    @SidedProxy
    private static BCSiliconProxy proxy;

    public static BCSiliconProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
            if (tile instanceof TileAssemblyTable) {
                TileAssemblyTable laser = (TileAssemblyTable) tile;
                return new ContainerAssemblyTable(player, laser);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {}

    public void fmlInit() {
       AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(10000000, ImmutableSet.of(new ItemStack(Items.BAKED_POTATO)), new ItemStack(Items.APPLE)));
       AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(10000000, ImmutableSet.of(new ItemStack(Items.BAKED_POTATO)), new ItemStack(Items.GOLDEN_APPLE)));
       AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(10000000, ImmutableSet.of(new ItemStack(Items.BAKED_POTATO)), new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
    }

    public void fmlPostInit() {}

    @SideOnly(Side.SERVER)
    public static class ServerProxy extends BCSiliconProxy {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends BCSiliconProxy {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (ID == BCSiliconGuis.ASSEMBLY_TABLE.ordinal()) {
                if (tile instanceof TileAssemblyTable) {
                    TileAssemblyTable laser = (TileAssemblyTable) tile;
                    return new GuiAssemblyTable(new ContainerAssemblyTable(player, laser));
                }
            }
            return null;
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            OBJLoader.INSTANCE.addDomain("buildcraftsilicon");
            ClientRegistry.bindTileEntitySpecialRenderer(TileLaser.class, new RenderLaser());
        }
    }
}
