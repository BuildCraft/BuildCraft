package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCBoardNBT extends RedstoneBoardRobotNBT {
    public static final Map<String, BCBoardNBT> REGISTRY = new HashMap<String, BCBoardNBT>();
    // private final ResourceLocation texture;
    private final ResourceLocation robotTexture;
    // Calen 1.18.2
    private final ResourceLocation robotTextureFullLocation;
    private final ResourceLocation robotId;
    private final ResourceLocation id;
    private final String upperName, boardType;
    private final Constructor<? extends RedstoneBoardRobot> boardInit;

    @OnlyIn(Dist.CLIENT)
    private TextureAtlasSprite icon;

    public BCBoardNBT(String id, String name, Class<? extends RedstoneBoardRobot> board, String boardType) {
        this.id = new ResourceLocation(id);
        this.boardType = boardType;
        // this.upperName = name.substring(0, 1).toUpperCase() + name.substring(1);
        String upperName = name.substring(0, 1).toUpperCase() + name.substring(1);
        while (upperName.contains("_")) {
            int index = upperName.indexOf('_');
            if (index + 1 < upperName.length()) {
                upperName = upperName.substring(0, index) + upperName.substring(index + 1, index + 2).toUpperCase() + upperName.substring(index + 2);
            }
        }
        this.upperName = upperName;
        // this.texture = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/robot_" + name + ".png");
        this.robotTexture = new ResourceLocation("buildcraftrobotics:entities/robot_" + name);
        this.robotTextureFullLocation = new ResourceLocation("buildcraftrobotics:textures/entities/robot_" + name + ".png");
        this.robotId = new ResourceLocation("buildcraftrobotics:robot_" + name);

        Constructor<? extends RedstoneBoardRobot> boardInitLocal;
        try {
            boardInitLocal = board.getConstructor(EntityRobotBase.class);
        } catch (Exception e) {
            e.printStackTrace();
            boardInitLocal = null;
        }
        this.boardInit = boardInitLocal;

        REGISTRY.put(name, this);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
//    public void addInformation(ItemStack stack, Player player, List list, boolean advanced)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        // list.add(new TextComponent(ChatFormatting.BOLD.toString()).append(new TranslatableComponent("buildcraft.boardRobot" + this.upperName)));
        list.add(new TranslationTextComponent("buildcraft.boardRobot" + upperName + ".desc"));
    }

    @Override
    public RedstoneBoardRobot create(EntityRobotBase robot) {
        try {
            return boardInit.newInstance(robot);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResourceLocation getRobotTexture() {
        return robotTexture;
    }

    // Calen 1.18.2
    @Override
    public ResourceLocation getRobotTextureFullLocation() {
        return robotTextureFullLocation;
    }

    @Override
    public ResourceLocation getRobotId() {
        return robotId;
    }

    @Override
    // public String getItemModelLocation()
    public String getBoardTexture() {
        // return "buildcraftrobotics:board/" + boardType;
        return "buildcraftrobotics:items/board/" + boardType;
    }

//    @Override
//    public String getDisplayName() {
//        return LocaleUtil.localize("buildcraft.boardRobot" + upperName);
//    }

    @Override
    public ITextComponent getDisplayNameComponent() {
        return new TranslationTextComponent("buildcraft.boardRobot" + upperName);
    }
}
