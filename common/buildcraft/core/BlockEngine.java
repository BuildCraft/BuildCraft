package buildcraft.core;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.lib.engines.BlockEngineBase;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.utils.IModelRegister;
import buildcraft.core.lib.utils.ModelHelper;

public class BlockEngine extends BlockEngineBase implements IModelRegister {
    private final Class<? extends TileEngineBase>[] engineTiles;
    private final String[] names;

    public BlockEngine() {
        super();
        setUnlocalizedName("engineBlock");

        engineTiles = new Class[16];
        names = new String[16];
    }

    @Override
    public String getUnlocalizedName(int metadata) {
        return names[metadata] != null ? names[metadata] : "unknown";
    }

    public void registerTile(Class<? extends TileEngineBase> engineTile, int meta, String name) {
        if (BCRegistry.INSTANCE.isEnabled("engines", name)) {
            engineTiles[meta] = engineTile;
            names[meta] = name;
        }
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (!hasEngine(metadata)) {
            return null;
        }
        try {
            return engineTiles[metadata].newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List itemList) {
        int i = 0;
        for (String name : names) {
            if (name != null) {
                itemList.add(new ItemStack(this, 1, i));
            }
            i++;
        }
    }

	@Override
    public boolean hasEngine(int meta) {
        return engineTiles[meta] != null;
    }

    @Override
    public void registerModels() {
        Item item = ItemBlock.getItemFromBlock(this);
        ModelHelper.registerItemModel(item, 0, "_wood");
        ModelHelper.registerItemModel(item, 1, "_stone");
        ModelHelper.registerItemModel(item, 2, "_iron");
        ModelHelper.registerItemModel(item, 3, "_creative");
    }

    @Override
    public double getBreathingCoefficent() {
        return 1;
    }
}
