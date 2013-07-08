package buildcraft.transport;

import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;

import com.google.common.base.Throwables;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FallbackWrapper implements ITrigger {

    private ITrigger brokenInstance;

    private Method iconMethod;
    private Method activeMethod;

    public FallbackWrapper(ITrigger wrap) {
        try {
            iconMethod = wrap.getClass().getDeclaredMethod("getTextureIcon");
        } catch (Exception e) {
        }
        try {
            activeMethod = wrap.getClass().getDeclaredMethod("isTriggerActive", TileEntity.class, ITriggerParameter.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        brokenInstance = wrap;
    }
    @Override
    public int getId()
    {
        return brokenInstance.getId();
    }

    @SideOnly(Side.CLIENT)
    public Icon getIcon()
    {
        try {
            return (Icon) iconMethod.invoke(brokenInstance);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
    @Override
    public int getIconIndex()
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider()
    {
        return brokenInstance.getIconProvider();
    }

    @Override
    public boolean hasParameter()
    {
        return brokenInstance.hasParameter();
    }

    @Override
    public String getDescription()
    {
        return brokenInstance.getDescription();
    }

    @Override
    public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter)
    {
        try {
            return (Boolean)activeMethod.invoke(brokenInstance, tile, parameter);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public ITriggerParameter createParameter()
    {
        return brokenInstance.createParameter();
    }

    @Override
    public boolean equals(Object obj)
    {
        return brokenInstance.equals(obj);
    }
    @Override
    public int hashCode()
    {
        return brokenInstance.hashCode();
    }
}
