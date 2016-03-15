package buildcraft.transport.client.shader;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.client.render.FluidRenderer;
import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.core.lib.client.render.FluidRenderer.FluidType;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.PipeTransportFluids;

@SideOnly(Side.CLIENT)
public class FluidShaderRenderer {
    private static final EnumFacing[] allFaces = { null, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH,
        EnumFacing.SOUTH };

    public static class PipeFluidHistory {
        final Map<EnumFacing, PipeFluidPart> map = Maps.newHashMap();
        final PipeTransportFluids trans;

        public PipeFluidHistory(PipeTransportFluids fluids, long thisTick) {
            this.trans = fluids;
            for (EnumFacing face : allFaces) {
                int ordinal = face == null ? 6 : face.ordinal();
                PipeFluidPart part = new PipeFluidPart();
                part.lastTick = part.tick = thisTick;
                part.lastAmount = part.amount = fluids.renderCache.amount[ordinal];
                map.put(face, part);
            }
        }

        public void tick(long thisTick) {
            for (EnumFacing face : allFaces) {
                int ordinal = face == null ? 6 : face.ordinal();
                PipeFluidPart part = map.get(face);
                int newAmount = trans.renderCache.amount[ordinal];
                if (newAmount != part.amount) {
                    part.lastTick = part.tick;
                    part.tick = thisTick;

                    part.lastAmount = part.amount;
                    part.amount = newAmount;
                }
            }
        }
    }

    public static class PipeFluidPart {
        int amount, lastAmount;
        long tick, lastTick;
    }

    public final World world;

    private final Map<BlockPos, PipeTransportFluids> pipeMap = Maps.newHashMap();

    private final Multimap<BlockPos, FluidShaderData> shaderPosMap = HashMultimap.create();
    private final Map<BlockPos, PipeFluidHistory> pipeHistoryMap = Maps.newHashMap();

    private final List<FluidShaderData> shaderList = Lists.newArrayList();
    private final Set<BlockPos> dirtyPipes = Sets.newHashSet();

    public FluidShaderRenderer(World world) {
        this.world = world;
    }

    public void destroy() {

    }

    public void addFluidTransport(PipeTransportFluids trans) {
        BlockPos pos = trans.container.getPos();
        pipeMap.put(pos, trans);
        dirtyPipes.add(pos);
    }

    public void removeFluidTransport(PipeTransportFluids trans) {
        BlockPos pos = trans.container.getPos();
        pipeMap.remove(pos);
        dirtyPipes.add(pos);
    }

    public void clientTick() {
        long tick = world.getTotalWorldTime();
        // compute shader data
        for (int i = 0; i < shaderList.size(); i++) {
            FluidShaderData data = shaderList.get(i);
            if (!data.isValid(tick)) {
                shaderList.remove(i);
                i--;
            }
        }

        for (Entry<BlockPos, PipeTransportFluids> pair : pipeMap.entrySet()) {
            computePipeShaderData(tick, pair);
        }
    }

    private void computePipeShaderData(long tick, Entry<BlockPos, PipeTransportFluids> pair) {
        BlockPos pos = pair.getKey();
        PipeTransportFluids trans = pair.getValue();

        PipeFluidHistory hist = pipeHistoryMap.get(pos);
        if (hist == null) {
            hist = new PipeFluidHistory(trans, tick);
            pipeHistoryMap.put(pos, hist);
        }
        hist.tick(tick);

        FluidStack fluid = trans.fluidType;

        TextureAtlasSprite sprite = FluidRenderer.getFluidTexture(fluid, FluidType.STILL);

        for (EnumFacing face : EnumFacing.values()) {
            computeConnectionShaderData(tick, pos, trans, hist, sprite, face);
        }
    }

    private void computeConnectionShaderData(long tick, BlockPos pos, PipeTransportFluids trans, PipeFluidHistory hist, TextureAtlasSprite sprite,
            EnumFacing face) {
        PipeFluidPart part = hist.map.get(face);
        if (part.amount == 0 || part.lastAmount == 0) {
            return;
        }

        PipeFluidPart centre = hist.map.get(null);
        PipeFluidHistory otherHist = pipeHistoryMap.get(pos.offset(face));
        PipeFluidPart other = otherHist == null ? null : otherHist.map.get(face.getOpposite());

        int ordinal = face.ordinal();
        Vec3 pipeEnd = Utils.convertMiddle(pos).add(Utils.convert(face, trans.container.renderState.customConnections[ordinal] + 0.5));

        /* Not connected to another pipe and the fluid is not in the centre */
        if (centre.amount == 0 && other == null) {
            // Collection<FluidShaderData> datas = shaderPosMap.get(pos);
            FluidShaderData toUse = null;
            for (FluidShaderData data : shaderList) {
                // Point A (The first point) was this block, so this is the right shader data
                if (Utils.convertFloor(data.positionA.point) == pos) {
                    if (data.positionA.point.distanceTo(pipeEnd) < 0.5) {
                        toUse = data;
                        break;
                    }
                }
                if (Utils.convertFloor(data.positionB.point) == pos) {
                    if (data.positionB.point.distanceTo(pipeEnd) < 0.5) {
                        toUse = data;
                        break;
                    }
                }
            }

            /* We didn't find a suitable shader data, create a new one with the assumption that it is flowing INTO the
             * pipe */
            if (toUse == null) {
                FluidShaderDataBuilder builder = new FluidShaderDataBuilder();
                builder.setSprite(sprite);
                builder.setExpires(tick + 110);

                Vec3 axisBasedOffset = new Vec3(0, face.getAxis() == Axis.Y ? 0 : 0.125, 0);

                FluidPositionInfoBuilder posBuilder = new FluidPositionInfoBuilder();
                posBuilder.setMin(pipeEnd.subtract(Utils.convertExcept(face, 0.25)));
                posBuilder.setMax(pipeEnd.add(Utils.convertExcept(face, 0.25)));
                posBuilder.setPoint(pipeEnd.subtract(axisBasedOffset));
                posBuilder.setDirection(Utils.convert(face.getOpposite()));
                posBuilder.setTextureIndex(0.2f);
                posBuilder.setMoves(false);
                builder.setPositionA(posBuilder.build());

                Vec3 pipeMiddle = Utils.convertMiddle(pos).add(Utils.convert(face, 0.25));
                posBuilder.setMin(pipeMiddle.subtract(Utils.convertExcept(face, 0.25)));
                posBuilder.setMax(pipeMiddle.add(Utils.convertExcept(face, 0.25)));
                posBuilder.setPoint(Utils.convertMiddle(pos).add(Utils.convert(face, 0.125)).subtract(axisBasedOffset));
                posBuilder.setDirection(Utils.convert(face.getOpposite()));
                posBuilder.setTextureIndex(0.8f);
                posBuilder.setMoves(true);
                posBuilder.setStartMoving(tick);
                posBuilder.setEndMoving(tick + 90);
                builder.setPositionB(posBuilder.build());

                FluidShaderData fsd = new FluidShaderData(builder);
                // shaderPosMap.put(pos, fsd);
                shaderList.add(fsd);
            }
            // Re-use the existing one, extending it if need be.
            else {

            }
        }
        // Not connected to another pipe and the fluid has reached the centre
        else if (other == null) {

        }
        // Connected to another pipe but has not reached the centre
        else if (centre.amount == 0) {

        }
        // Connected to another pipe and has reached a centre
        else {

        }
    }

    public void renderAll(float partialTicks) {
        long tick = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        EntityPlayerSP clientPlayer = Minecraft.getMinecraft().thePlayer;
        Vec3 pos = Utils.getInterpolatedVec(clientPlayer, partialTicks);

        GL11.glPushMatrix();
        RenderUtils.translate(Utils.multiply(pos, -1));

        // FluidShaderManager.INSTANCE.getShader().useShader();
        // temp
        GL11.glPointSize(8);
        GL11.glLineWidth(4);

        for (FluidShaderData fluidShaderData : shaderList) {
            renderShader(tick, partialTicks, fluidShaderData);
        }

        // FluidShaderManager.INSTANCE.getShader().endShader();

        GL11.glPopMatrix();
    }

    private void renderShader(long tick, float partialTicks, FluidShaderData data) {
        FluidPositionInfo a = data.interpolateA(tick, partialTicks);
        FluidPositionInfo b = data.interpolateB(tick, partialTicks);
        for (FluidPositionInfo fpi : new FluidPositionInfo[] { a, b }) {
            // if (fpi.visible) {
            GL11.glTexCoord2f(data.sprite.getInterpolatedU(fpi.textureIndex * 16), data.sprite.getMinV());

            GL11.glBegin(GL11.GL_POINTS);
            RenderUtils.vertex3f(fpi.min);
            RenderUtils.vertex3f(fpi.max);
            RenderUtils.vertex3f(fpi.point);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINES);
            RenderUtils.vertex3f(fpi.point);
            RenderUtils.vertex3f(fpi.point.add(Utils.multiply(fpi.direction.normalize(), 0.3)));
            GL11.glEnd();

            // }
        }
    }
}
