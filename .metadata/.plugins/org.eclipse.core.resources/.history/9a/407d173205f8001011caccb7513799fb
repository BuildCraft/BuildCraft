// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src;

import java.util.Random;

// Referenced classes of package net.minecraft.src:
//            MapGenBase, MathHelper, Block, BlockGrass, 
//            World

public class MapGenCaves extends MapGenBase
{

    public MapGenCaves()
    {
    }

    protected void generateLargeCaveNode(long l, int i, int j, byte abyte0[], double d, 
            double d1, double d2)
    {
        generateCaveNode(l, i, j, abyte0, d, d1, d2, 1.0F + rand.nextFloat() * 6F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void generateCaveNode(long l, int i, int j, byte abyte0[], double d, 
            double d1, double d2, float f, float f1, float f2, 
            int k, int i1, double d3)
    {
        double d4 = i * 16 + 8;
        double d5 = j * 16 + 8;
        float f3 = 0.0F;
        float f4 = 0.0F;
        Random random = new Random(l);
        if(i1 <= 0)
        {
            int j1 = field_1306_a * 16 - 16;
            i1 = j1 - random.nextInt(j1 / 4);
        }
        boolean flag = false;
        if(k == -1)
        {
            k = i1 / 2;
            flag = true;
        }
        int k1 = random.nextInt(i1 / 2) + i1 / 4;
        boolean flag1 = random.nextInt(6) == 0;
        for(; k < i1; k++)
        {
            double d6 = 1.5D + (double)(MathHelper.sin(((float)k * 3.141593F) / (float)i1) * f * 1.0F);
            double d7 = d6 * d3;
            float f5 = MathHelper.cos(f2);
            float f6 = MathHelper.sin(f2);
            d += MathHelper.cos(f1) * f5;
            d1 += f6;
            d2 += MathHelper.sin(f1) * f5;
            if(flag1)
            {
                f2 *= 0.92F;
            } else
            {
                f2 *= 0.7F;
            }
            f2 += f4 * 0.1F;
            f1 += f3 * 0.1F;
            f4 *= 0.9F;
            f3 *= 0.75F;
            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4F;
            if(!flag && k == k1 && f > 1.0F && i1 > 0)
            {
                generateCaveNode(random.nextLong(), i, j, abyte0, d, d1, d2, random.nextFloat() * 0.5F + 0.5F, f1 - 1.570796F, f2 / 3F, k, i1, 1.0D);
                generateCaveNode(random.nextLong(), i, j, abyte0, d, d1, d2, random.nextFloat() * 0.5F + 0.5F, f1 + 1.570796F, f2 / 3F, k, i1, 1.0D);
                return;
            }
            if(!flag && random.nextInt(4) == 0)
            {
                continue;
            }
            double d8 = d - d4;
            double d9 = d2 - d5;
            double d10 = i1 - k;
            double d11 = f + 2.0F + 16F;
            if((d8 * d8 + d9 * d9) - d10 * d10 > d11 * d11)
            {
                return;
            }
            if(d < d4 - 16D - d6 * 2D || d2 < d5 - 16D - d6 * 2D || d > d4 + 16D + d6 * 2D || d2 > d5 + 16D + d6 * 2D)
            {
                continue;
            }
            d8 = MathHelper.floor_double(d - d6) - i * 16 - 1;
            int l1 = (MathHelper.floor_double(d + d6) - i * 16) + 1;
            d9 = MathHelper.floor_double(d1 - d7) - 1;
            int i2 = MathHelper.floor_double(d1 + d7) + 1;
            d10 = MathHelper.floor_double(d2 - d6) - j * 16 - 1;
            int j2 = (MathHelper.floor_double(d2 + d6) - j * 16) + 1;
            if(d8 < 0)
            {
                d8 = 0;
            }
            if(l1 > 16)
            {
                l1 = 16;
            }
            if(d9 < 1)
            {
                d9 = 1;
            }
            worldObj.getClass();
            if(i2 > 128 - 8)
            {
                worldObj.getClass();
                i2 = 128 - 8;
            }
            if(d10 < 0)
            {
                d10 = 0;
            }
            if(j2 > 16)
            {
                j2 = 16;
            }
            boolean flag2 = false;
            for(int k2 = (int) d8; !flag2 && k2 < l1; k2++)
            {
                for(int i3 = (int) d10; !flag2 && i3 < j2; i3++)
                {
                    for(int j3 = i2 + 1; !flag2 && j3 >= d9 - 1; j3--)
                    {
                        worldObj.getClass();
                        int k3 = (k2 * 16 + i3) * 128 + j3;
                        if(j3 < 0)
                        {
                            continue;
                        }
                        worldObj.getClass();
                        if(j3 >= 128)
                        {
                            continue;
                        }
                        if(abyte0[k3] == Block.waterMoving.blockID || abyte0[k3] == Block.waterStill.blockID)
                        {
                            flag2 = true;
                        }
                        if(j3 != d9 - 1 && k2 != d8 && k2 != l1 - 1 && i3 != d10 && i3 != j2 - 1)
                        {
                            j3 = (int) d9;
                        }
                    }

                }

            }

            if(flag2)
            {
                continue;
            }
            for(int l2 = (int) d8; l2 < l1; l2++)
            {
                double d12 = (((double)(l2 + i * 16) + 0.5D) - d) / d6;
label0:
                for(int l3 = (int) d10; l3 < j2; l3++)
                {
                    double d13 = (((double)(l3 + j * 16) + 0.5D) - d2) / d6;
                    worldObj.getClass();
                    int i4 = (l2 * 16 + l3) * 128 + i2;
                    boolean flag3 = false;
                    if(d12 * d12 + d13 * d13 >= 1.0D)
                    {
                        continue;
                    }
                    int j4 = i2 - 1;
                    do
                    {
                        if(j4 < d9)
                        {
                            continue label0;
                        }
                        double d14 = (((double)j4 + 0.5D) - d1) / d7;
                        if(d14 > -0.69999999999999996D && d12 * d12 + d14 * d14 + d13 * d13 < 1.0D)
                        {
                            byte byte0 = abyte0[i4];
                            if(byte0 == Block.grass.blockID)
                            {
                                flag3 = true;
                            }
                            if(byte0 == Block.stone.blockID || byte0 == Block.dirt.blockID || byte0 == Block.grass.blockID)
                            {
                                if(j4 < 10)
                                {
                                    abyte0[i4] = (byte)Block.lavaMoving.blockID;
                                } else
                                {
                                    abyte0[i4] = 0;
                                    if(flag3 && abyte0[i4 - 1] == Block.dirt.blockID)
                                    {
                                        abyte0[i4 - 1] = (byte)Block.grass.blockID;
                                    }
                                }
                            }
                        }
                        i4--;
                        j4--;
                    } while(true);
                }

            }

            if(flag)
            {
                break;
            }
        }

    }

    protected void recursiveGenerate(World world, int i, int j, int k, int l, byte abyte0[])
    {
        int i1 = rand.nextInt(rand.nextInt(rand.nextInt(40) + 1) + 1);
        if(rand.nextInt(15) != 0)
        {
            i1 = 0;
        }
        for(int j1 = 0; j1 < i1; j1++)
        {
            double d = i * 16 + rand.nextInt(16);
            world.getClass();
            double d1 = rand.nextInt(rand.nextInt(128 - 8) + 8);
            double d2 = j * 16 + rand.nextInt(16);
            int k1 = 1;
            if(rand.nextInt(4) == 0)
            {
                generateLargeCaveNode(rand.nextLong(), k, l, abyte0, d, d1, d2);
                k1 += rand.nextInt(4);
            }
            for(int l1 = 0; l1 < k1; l1++)
            {
                float f = rand.nextFloat() * 3.141593F * 2.0F;
                float f1 = ((rand.nextFloat() - 0.5F) * 2.0F) / 8F;
                float f2 = rand.nextFloat() * 2.0F + rand.nextFloat();
                if(rand.nextInt(10) == 0)
                {
                    f2 *= rand.nextFloat() * rand.nextFloat() * 3F + 1.0F;
                }
                generateCaveNode(rand.nextLong(), k, l, abyte0, d, d1, d2, f2, f, f1, 0, 0, 1.0D);
            }

        }

    }
}
