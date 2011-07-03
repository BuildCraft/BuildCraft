package net.minecraft.src.buildcraft.energy;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.BlockFluid;
import net.minecraft.src.World;
import net.minecraft.src.Material;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock; 

public class BlockOilFlowing extends BlockFluid implements ICustomTextureBlock
{

    public BlockOilFlowing(int i, Material material)
    {
        super(i, material);
        field_659_a = 0;
        field_658_b = new boolean[4];
        field_660_c = new int[4];
    }
	
    public int getRenderType() {
        return BuildCraftCore.oilModel;
    }
    
    @Override
	public String getTextureFile() {	
		return BuildCraftCore.customBuildCraftTexture;
	}

    private void func_30005_i(World world, int i, int j, int k)
    {
        int l = world.getBlockMetadata(i, j, k);
        world.setBlockAndMetadata(i, j, k, blockID + 1, l);
        world.markBlocksDirty(i, j, k, i, j, k);
        world.markBlockNeedsUpdate(i, j, k);
    }

    public void updateTick(World world, int i, int j, int k, Random random)
    {
        int l = func_301_g(world, i, j, k);
        byte byte0 = 1;
        boolean flag = true;
        if(l > 0)
        {
            int i1 = -100;
            field_659_a = 0;
            i1 = func_307_e(world, i - 1, j, k, i1);
            i1 = func_307_e(world, i + 1, j, k, i1);
            i1 = func_307_e(world, i, j, k - 1, i1);
            i1 = func_307_e(world, i, j, k + 1, i1);
            int j1 = i1 + byte0;
            if(j1 >= 8 || i1 < 0)
            {
                j1 = -1;
            }
            if(func_301_g(world, i, j + 1, k) >= 0)
            {
                int l1 = func_301_g(world, i, j + 1, k);
                if(l1 >= 8)
                {
                    j1 = l1;
                } else
                {
                    j1 = l1 + 8;
                }
            }
            if(j1 != l)
            {
                l = j1;
                if(l < 0)
                {
                    world.setBlockWithNotify(i, j, k, 0);
                } else
                {
                    world.setBlockMetadataWithNotify(i, j, k, l);
                    world.scheduleUpdateTick(i, j, k, blockID, tickRate());
                    world.notifyBlocksOfNeighborChange(i, j, k, blockID);
                }
            } else
            if(flag)
            {
                func_30005_i(world, i, j, k);
            }
        } else
        {
            func_30005_i(world, i, j, k);
        }
        if(func_312_l(world, i, j - 1, k))
        {
            if(l >= 8)
            {
                world.setBlockAndMetadataWithNotify(i, j - 1, k, blockID, l);
            } else
            {
                world.setBlockAndMetadataWithNotify(i, j - 1, k, blockID, l + 8);
            }
        } else
        if(l >= 0 && (l == 0 || func_309_k(world, i, j - 1, k)))
        {
            boolean aflag[] = func_4035_j(world, i, j, k);
            int k1 = l + byte0;
            if(l >= 8)
            {
                k1 = 1;
            }
            if(k1 >= 8)
            {
                return;
            }
            if(aflag[0])
            {
                func_311_f(world, i - 1, j, k, k1);
            }
            if(aflag[1])
            {
                func_311_f(world, i + 1, j, k, k1);
            }
            if(aflag[2])
            {
                func_311_f(world, i, j, k - 1, k1);
            }
            if(aflag[3])
            {
                func_311_f(world, i, j, k + 1, k1);
            }
        }
    }

    private void func_311_f(World world, int i, int j, int k, int l)
    {
        if(func_312_l(world, i, j, k))
        {
            int i1 = world.getBlockId(i, j, k);
            if(i1 > 0)
            {
                Block.blocksList[i1].dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k));
            }
            world.setBlockAndMetadataWithNotify(i, j, k, blockID, l);
        }
    }

    private int func_4034_a(World world, int i, int j, int k, int l, int i1)
    {
        int j1 = 1000;
        for(int k1 = 0; k1 < 4; k1++)
        {
            if(k1 == 0 && i1 == 1 || k1 == 1 && i1 == 0 || k1 == 2 && i1 == 3 || k1 == 3 && i1 == 2)
            {
                continue;
            }
            int l1 = i;
            int i2 = j;
            int j2 = k;
            if(k1 == 0)
            {
                l1--;
            }
            if(k1 == 1)
            {
                l1++;
            }
            if(k1 == 2)
            {
                j2--;
            }
            if(k1 == 3)
            {
                j2++;
            }
            if(func_309_k(world, l1, i2, j2) || world.getBlockMaterial(l1, i2, j2) == blockMaterial && world.getBlockMetadata(l1, i2, j2) == 0)
            {
                continue;
            }
            if(!func_309_k(world, l1, i2 - 1, j2))
            {
                return l;
            }
            if(l >= 4)
            {
                continue;
            }
            int k2 = func_4034_a(world, l1, i2, j2, l + 1, k1);
            if(k2 < j1)
            {
                j1 = k2;
            }
        }

        return j1;
    }

    private boolean[] func_4035_j(World world, int i, int j, int k)
    {
        for(int l = 0; l < 4; l++)
        {
            field_660_c[l] = 1000;
            int j1 = i;
            int i2 = j;
            int j2 = k;
            if(l == 0)
            {
                j1--;
            }
            if(l == 1)
            {
                j1++;
            }
            if(l == 2)
            {
                j2--;
            }
            if(l == 3)
            {
                j2++;
            }
            if(func_309_k(world, j1, i2, j2) || world.getBlockMaterial(j1, i2, j2) == blockMaterial && world.getBlockMetadata(j1, i2, j2) == 0)
            {
                continue;
            }
            if(!func_309_k(world, j1, i2 - 1, j2))
            {
                field_660_c[l] = 0;
            } else
            {
                field_660_c[l] = func_4034_a(world, j1, i2, j2, 1, l);
            }
        }

        int i1 = field_660_c[0];
        for(int k1 = 1; k1 < 4; k1++)
        {
            if(field_660_c[k1] < i1)
            {
                i1 = field_660_c[k1];
            }
        }

        for(int l1 = 0; l1 < 4; l1++)
        {
            field_658_b[l1] = field_660_c[l1] == i1;
        }

        return field_658_b;
    }

    private boolean func_309_k(World world, int i, int j, int k)
    {
        int l = world.getBlockId(i, j, k);
        if(l == Block.doorWood.blockID || l == Block.doorSteel.blockID || l == Block.signPost.blockID || l == Block.ladder.blockID || l == Block.reed.blockID)
        {
            return true;
        }
        if(l == 0)
        {
            return false;
        }
        Material material = Block.blocksList[l].blockMaterial;
        return material.getIsSolid();
    }

    protected int func_307_e(World world, int i, int j, int k, int l)
    {
        int i1 = func_301_g(world, i, j, k);
        if(i1 < 0)
        {
            return l;
        }
        if(i1 >= 8)
        {
            i1 = 0;
        }
        return l >= 0 && i1 >= l ? l : i1;
    }

    private boolean func_312_l(World world, int i, int j, int k)
    {
        Material material = world.getBlockMaterial(i, j, k);
        if(material == blockMaterial)
        {
            return false;
        } else
        {
            return !func_309_k(world, i, j, k);
        }
    }

    public void onBlockAdded(World world, int i, int j, int k)
    {
        super.onBlockAdded(world, i, j, k);
        if(world.getBlockId(i, j, k) == blockID)
        {
            world.scheduleUpdateTick(i, j, k, blockID, tickRate());
        }
    }

    int field_659_a;
    boolean field_658_b[];
    int field_660_c[];
}
