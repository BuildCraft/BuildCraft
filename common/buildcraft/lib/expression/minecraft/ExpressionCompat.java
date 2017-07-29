package buildcraft.lib.expression.minecraft;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.api.enums.EnumPowerStage;

import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.NodeType2;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.lib.gui.pos.PositionCallable;
import buildcraft.lib.misc.ColourUtil;

public class ExpressionCompat {

    // Minecraft Types
    public static final NodeType2<Axis> ENUM_AXIS;
    public static final NodeType2<EnumFacing> ENUM_FACING;
    public static final NodeType2<EnumDyeColor> ENUM_DYE_COLOUR;

    // BuildCraft API types
    public static final NodeType2<EnumPowerStage> ENUM_POWER_STAGE;

    // BuildCraft Lib types
    public static final NodeType2<IGuiPosition> GUI_POSITION;
    public static final NodeType2<IGuiArea> GUI_AREA;

    static {
        ENUM_AXIS = new NodeType2<>(Axis.X);
        NodeTypes.addType("Axis", ENUM_AXIS);
        for (Axis a : Axis.values()) {
            ENUM_AXIS.putConstant("" + a, a);
        }

        ENUM_FACING = new NodeType2<>(EnumFacing.UP);
        NodeTypes.addType("Facing", ENUM_FACING);
        ENUM_FACING.put_t_t("getOpposite", EnumFacing::getOpposite);
        ENUM_FACING.put_t_o("getAxis", Axis.class, EnumFacing::getAxis);
        for (EnumFacing f : EnumFacing.values()) {
            ENUM_FACING.putConstant("" + f, f);
        }

        ENUM_DYE_COLOUR = new NodeType2<>(EnumDyeColor.WHITE);
        NodeTypes.addType("DyeColor", ENUM_DYE_COLOUR);
        NodeTypes.addType("DyeColour", ENUM_DYE_COLOUR);
        ENUM_DYE_COLOUR.put_t_l("to_argb", c -> 0xFF_00_00_00 | ColourUtil.getLightHex(c));
        for (EnumDyeColor c : EnumDyeColor.values()) {
            ENUM_DYE_COLOUR.putConstant("" + c, c);
            ENUM_DYE_COLOUR.putConstant("" + c, c);
        }

        ENUM_POWER_STAGE = new NodeType2<>(EnumPowerStage.BLUE);
        NodeTypes.addType("EnginePowerStage", ENUM_POWER_STAGE);
        for (EnumPowerStage stage : EnumPowerStage.VALUES) {
            ENUM_POWER_STAGE.putConstant("" + stage, stage);
        }

        GUI_POSITION = new NodeType2<>(IGuiPosition.class, new PositionAbsolute(0, 0));
        GUI_AREA = new NodeType2<>(IGuiArea.class, new GuiRectangle(0, 0));
        NodeTypes.addType("GuiPosition", GUI_POSITION);
        NodeTypes.addType("GuiArea", GUI_AREA);

//        GUI_POSITION.put_ll_t("pos", PositionAbsolute::new);
        GUI_POSITION.put_oo_t("pos", INodeLong.class, INodeLong.class, PositionCallable::new);
    }

    public static void setup() {
        // Just to call the above static initializer
    }
}
