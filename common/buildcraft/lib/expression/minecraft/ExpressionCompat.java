package buildcraft.lib.expression.minecraft;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.NodeType2;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.PositionAbsolute;
import buildcraft.lib.misc.ColourUtil;

public class ExpressionCompat {

    public static final FunctionContext CONTEXT = new FunctionContext();

    public static final NodeType2<Axis> ENUM_AXIS;
    public static final NodeType2<EnumFacing> ENUM_FACING;
    public static final NodeType2<EnumDyeColor> ENUM_DYE_COLOUR;

    public static final NodeType2<IGuiPosition> GUI_POSITION;
    public static final NodeType2<IGuiArea> GUI_AREA;

    static {
        ENUM_AXIS = new NodeType2<>(Axis.X);
        NodeTypes.addType("axis", ENUM_AXIS);
        for (Axis a : Axis.values()) {
            CONTEXT.putConstant("Axis." + a, Axis.class, a);
        }

        ENUM_FACING = new NodeType2<>(EnumFacing.UP);
        NodeTypes.addType("facing", ENUM_FACING);
        ENUM_FACING.put_t_t("getOpposite", EnumFacing::getOpposite);
        ENUM_FACING.put_t_o("getAxis", Axis.class, EnumFacing::getAxis);
        for (EnumFacing f : EnumFacing.values()) {
            CONTEXT.putConstant("Facing." + f, EnumFacing.class, f);
        }

        ENUM_DYE_COLOUR = new NodeType2<>(EnumDyeColor.WHITE);
        NodeTypes.addType("dye_color", ENUM_DYE_COLOUR);
        NodeTypes.addType("dye_colour", ENUM_DYE_COLOUR);
        ENUM_DYE_COLOUR.put_t_l("to_argb", c -> 0xFF_00_00_00 | ColourUtil.getLightHex(c));
        for (EnumDyeColor c : EnumDyeColor.values()) {
            CONTEXT.putConstant("DyeColor." + c, EnumDyeColor.class, c);
            CONTEXT.putConstant("DyeColour." + c, EnumDyeColor.class, c);
        }

        GUI_POSITION = new NodeType2<>(IGuiPosition.class, new PositionAbsolute(0, 0));
        GUI_AREA = new NodeType2<>(IGuiArea.class, new GuiRectangle(0, 0));
        NodeTypes.addType("gui_position", GUI_POSITION);
        NodeTypes.addType("gui_area", GUI_AREA);

        CONTEXT.put_ll_o();
    }

    public static void setup() {
        // Just to call the above static initializer
    }
}
