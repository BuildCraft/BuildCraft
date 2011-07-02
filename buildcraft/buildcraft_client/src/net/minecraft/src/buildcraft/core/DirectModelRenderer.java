// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.PositionTextureVertex;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TexturedQuad;

import org.lwjgl.opengl.GL11;

// Referenced classes of package net.minecraft.src:
//            PositionTextureVertex, TexturedQuad, GLAllocation, Tessellator

public class DirectModelRenderer
{

    public DirectModelRenderer(int i, int j)
    {
        mirror = false;
        showModel = true;
        field_1402_i = false;
        textureOffsetX = i;
        textureOffsetY = j;
    }

    public void addBox(float f, float f1, float f2, int i, int j, int k)
    {
        addBox(f, f1, f2, i, j, k, 0.0F);
    }

    public void addBox(float f, float f1, float f2, int i, int j, int k, float f3)
    {
        corners = new PositionTextureVertex[8];
        faces = new TexturedQuad[6];
        float f4 = f + (float)i;
        float f5 = f1 + (float)j;
        float f6 = f2 + (float)k;
        f -= f3;
        f1 -= f3;
        f2 -= f3;
        f4 += f3;
        f5 += f3;
        f6 += f3;
        if(mirror)
        {
            float f7 = f4;
            f4 = f;
            f = f7;
        }
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, f1, f2, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f1, f2, 0.0F, 8F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(f4, f5, f2, 8F, 8F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(f, f5, f2, 8F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, f1, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f1, f6, 0.0F, 8F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(f4, f5, f6, 8F, 8F);
        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(f, f5, f6, 8F, 0.0F);
        corners[0] = positiontexturevertex;
        corners[1] = positiontexturevertex1;
        corners[2] = positiontexturevertex2;
        corners[3] = positiontexturevertex3;
        corners[4] = positiontexturevertex4;
        corners[5] = positiontexturevertex5;
        corners[6] = positiontexturevertex6;
        corners[7] = positiontexturevertex7;
        faces[0] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex5, positiontexturevertex1, positiontexturevertex2, positiontexturevertex6
        }, textureOffsetX + k + i, textureOffsetY + k, textureOffsetX + k + i + k, textureOffsetY + k + j);
        faces[1] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex, positiontexturevertex4, positiontexturevertex7, positiontexturevertex3
        }, textureOffsetX + 0, textureOffsetY + k, textureOffsetX + k, textureOffsetY + k + j);
        faces[2] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex5, positiontexturevertex4, positiontexturevertex, positiontexturevertex1
        }, textureOffsetX + k, textureOffsetY + 0, textureOffsetX + k + i, textureOffsetY + k);
        faces[3] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex2, positiontexturevertex3, positiontexturevertex7, positiontexturevertex6
        }, textureOffsetX + k + i, textureOffsetY + 0, textureOffsetX + k + i + i, textureOffsetY + k);
        faces[4] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex1, positiontexturevertex, positiontexturevertex3, positiontexturevertex2
        }, textureOffsetX + k, textureOffsetY + k, textureOffsetX + k + i, textureOffsetY + k + j);
        faces[5] = new TexturedQuad(new PositionTextureVertex[] {
            positiontexturevertex4, positiontexturevertex5, positiontexturevertex6, positiontexturevertex7
        }, textureOffsetX + k + i + k, textureOffsetY + k, textureOffsetX + k + i + k + i, textureOffsetY + k + j);
        if(mirror)
        {
            for(int l = 0; l < faces.length; l++)
            {
                faces[l].flipFace();
            }

        }
    }

    public void setRotationPoint(float f, float f1, float f2)
    {
        rotationPointX = f;
        rotationPointY = f1;
        rotationPointZ = f2;
    }

    public void render(float f)
    {
        if(field_1402_i)
        {
            return;
        }
        if(!showModel)
        {
            return;
        }

        if(rotateAngleX != 0.0F || rotateAngleY != 0.0F || rotateAngleZ != 0.0F)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
            if(rotateAngleZ != 0.0F)
            {
                GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
            }
            if(rotateAngleY != 0.0F)
            {
                GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
            }
            if(rotateAngleX != 0.0F)
            {
                GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
            }
            display (f);
            GL11.glPopMatrix();
        } else
        if(rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F)
        {
            GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
            display (f);
            GL11.glTranslatef(-rotationPointX * f, -rotationPointY * f, -rotationPointZ * f);
        } else
        {
        	display (f);
        }
    }

    public void renderWithRotation(float f)
    {
        if(field_1402_i)
        {
            return;
        }
        if(!showModel)
        {
            return;
        }
        
        GL11.glPushMatrix();
        GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
        if(rotateAngleY != 0.0F)
        {
            GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
        }
        if(rotateAngleX != 0.0F)
        {
            GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
        }
        if(rotateAngleZ != 0.0F)
        {
            GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
        }
        display (f);
        
        GL11.glPopMatrix();
    }

    public void postRender(float f)
    {
        if(field_1402_i)
        {
            return;
        }
        if(!showModel)
        {
            return;
        }
//        
//        display(f);
        
        if(rotateAngleX != 0.0F || rotateAngleY != 0.0F || rotateAngleZ != 0.0F)
        {
            GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
            if(rotateAngleZ != 0.0F)
            {
                GL11.glRotatef(rotateAngleZ * 57.29578F, 0.0F, 0.0F, 1.0F);
            }
            if(rotateAngleY != 0.0F)
            {
                GL11.glRotatef(rotateAngleY * 57.29578F, 0.0F, 1.0F, 0.0F);
            }
            if(rotateAngleX != 0.0F)
            {
                GL11.glRotatef(rotateAngleX * 57.29578F, 1.0F, 0.0F, 0.0F);
            }
        } else
        if(rotationPointX != 0.0F || rotationPointY != 0.0F || rotationPointZ != 0.0F)
        {
            GL11.glTranslatef(rotationPointX * f, rotationPointY * f, rotationPointZ * f);
        }
    }

    private void display(float f)
    {
        Tessellator tessellator = Tessellator.instance;
        for(int i = 0; i < faces.length; i++)
        {
            faces[i].draw(tessellator, f);
        }
    }

    private PositionTextureVertex corners[];
    private TexturedQuad faces[];
    private int textureOffsetX;
    private int textureOffsetY;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean mirror;
    public boolean showModel;
    public boolean field_1402_i;
}
