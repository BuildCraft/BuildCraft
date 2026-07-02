package ct.buildcraft.robotics;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ct.buildcraft.api.boards.IRedstoneBoard;
import ct.buildcraft.api.boards.RedstoneBoardNBT;
import ct.buildcraft.api.boards.RedstoneBoardRegistry;
import ct.buildcraft.api.boards.RedstoneBoardRobot;
import ct.buildcraft.api.boards.RedstoneBoardRobotNBT;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.robotics.boards.BoardRobotCarrier;
import ct.buildcraft.robotics.boards.BoardRobotDelivery;
import ct.buildcraft.robotics.boards.BoardRobotFluidCarrier;
import ct.buildcraft.robotics.boards.BoardRobotHarvester;
import ct.buildcraft.robotics.boards.BoardRobotBomber;
import ct.buildcraft.robotics.boards.BoardRobotBuilder;
import ct.buildcraft.robotics.boards.BoardRobotButcher;
import ct.buildcraft.robotics.boards.BoardRobotFarmer;
import ct.buildcraft.robotics.boards.BoardRobotLeaveCutter;
import ct.buildcraft.robotics.boards.BoardRobotKnight;
import ct.buildcraft.robotics.boards.BoardRobotLumberjack;
import ct.buildcraft.robotics.boards.BoardRobotMiner;
import ct.buildcraft.robotics.boards.BoardRobotPicker;
import ct.buildcraft.robotics.boards.BoardRobotPlanter;
import ct.buildcraft.robotics.boards.BoardRobotPump;
import ct.buildcraft.robotics.boards.BoardRobotShovelman;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class BCRoboticsBoards {
    public static final int MODEL_EMPTY = 0;

    private static final Map<String, BoardEntry> ENTRIES_BY_ID = new LinkedHashMap<>();
    private static final Map<String, BoardEntry> ENTRIES_BY_KEY = new LinkedHashMap<>();
    private static boolean initialized;

    public static final BoardEntry EMPTY = board("buildcraft:boardRobotEmpty", "empty", "clean", "robot_base", 0);

    static {
        // Order copied from BuildCraft 7.1.x BuildCraftRobotics.preInit(). This order is also used by the creative tab.
        board("buildcraft:boardRobotPicker", "picker", "green", "robot_picker", 8000);
        board("buildcraft:boardRobotCarrier", "carrier", "green", "robot_carrier", 8000);
        board("buildcraft:boardRobotFluidCarrier", "fluid_carrier", "green", "robot_fluid_carrier", 8000);

        board("buildcraft:boardRobotLumberjack", "lumberjack", "blue", "robot_lumberjack", 32000);
        board("buildcraft:boardRobotHarvester", "harvester", "blue", "robot_harvester", 32000);
        board("buildcraft:miner", "miner", "blue", "robot_miner", 32000);
        board("buildcraft:boardRobotPlanter", "planter", "blue", "robot_planter", 32000);
        board("buildcraft:boardRobotFarmer", "farmer", "blue", "robot_farmer", 32000);
        board("buildcraft:leave_cutter", "leave_cutter", "blue", "robot_leave_cutter", 32000);
        board("buildcraft:boardRobotButcher", "butcher", "blue", "robot_butcher", 32000);
        board("buildcraft:shovelman", "shovelman", "blue", "robot_shovelman", 32000);
        board("buildcraft:boardRobotPump", "pump", "blue", "robot_pump", 32000);

        board("buildcraft:boardRobotDelivery", "delivery", "green", "robot_delivery", 128000);
        board("buildcraft:boardRobotKnight", "knight", "red", "robot_knight", 128000);
        board("buildcraft:boardRobotBomber", "bomber", "red", "robot_bomber", 128000);
        board("buildcraft:boardRobotStripes", "stripes", "yellow", "robot_stripes", 128000);

        board("buildcraft:boardRobotBuilder", "builder", "yellow", "robot_builder", 512000);
    }

    private BCRoboticsBoards() {
    }

    private static BoardEntry board(String legacyId, String key, String boardColor, String robotTexture, int energyCost) {
        int modelIndex = ENTRIES_BY_ID.size();
        BoardEntry entry = new BoardEntry(legacyId, key, boardColor, robotTexture, energyCost, modelIndex, new BasicBoardNBT(legacyId, key, boardColor, robotTexture));
        ENTRIES_BY_ID.put(legacyId, entry);
        ENTRIES_BY_KEY.put(key, entry);
        return entry;
    }

    public static void init() {
        if (initialized && RedstoneBoardRegistry.instance != null) {
            return;
        }
        SimpleRedstoneBoardRegistry registry = new SimpleRedstoneBoardRegistry();
        registry.setEmptyRobotBoard(EMPTY.nbt());
        for (BoardEntry entry : ENTRIES_BY_ID.values()) {
            if (entry != EMPTY) {
                registry.registerBoardType(entry.nbt(), entry.energyCost());
            }
        }
        RedstoneBoardRegistry.instance = registry;
        initialized = true;
    }

    public static Collection<BoardEntry> entriesWithEmpty() {
        return Collections.unmodifiableCollection(ENTRIES_BY_ID.values());
    }

    public static Collection<BoardEntry> robotEntries() {
        return ENTRIES_BY_ID.values().stream().filter(entry -> entry != EMPTY).toList();
    }

    public static BoardEntry getById(String id) {
        BoardEntry entry = ENTRIES_BY_ID.get(id);
        return entry == null ? EMPTY : entry;
    }

    public static BoardEntry getByKey(String key) {
        BoardEntry entry = ENTRIES_BY_KEY.get(key);
        return entry == null ? EMPTY : entry;
    }

    public static BoardEntry getBoard(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return EMPTY;
        }
        if (tag.contains("id")) {
            return getById(tag.getString("id"));
        }
        if (tag.contains("board")) {
            return getById(tag.getCompound("board").getString("id"));
        }
        return EMPTY;
    }

    public static BoardEntry getRobotBoard(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("board")) {
            return EMPTY;
        }
        return getById(tag.getCompound("board").getString("id"));
    }

    public static float getRobotModelValue(ItemStack stack) {
        return getRobotBoard(stack).modelValue();
    }

    public static float getBoardModelValue(ItemStack stack) {
        return getBoard(stack).boardColorValue();
    }

    public record BoardEntry(String id, String key, String boardColor, String robotTexture, int energyCost, int modelIndex,
                             BasicBoardNBT nbt) {
        public Component displayName() {
            return Component.translatable("buildcraft.boardRobot." + key);
        }

        public Component legacyDisplayName() {
            return Component.translatable("buildcraft." + legacyLangKey());
        }

        public String legacyLangKey() {
            return switch (key) {
                case "empty" -> "boardRobotClean";
                case "fluid_carrier" -> "boardRobotFluidCarrier";
                case "leave_cutter" -> "boardRobotLeaveCutter";
                default -> "boardRobot" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            };
        }

        public float modelValue() {
            return modelIndex / 100.0F;
        }

        public float boardColorValue() {
            return switch (boardColor) {
                case "green" -> 0.10F;
                case "blue" -> 0.20F;
                case "red" -> 0.30F;
                case "yellow" -> 0.40F;
                default -> 0.0F;
            };
        }

        public ResourceLocation robotTextureLocation() {
            return new ResourceLocation(BCRobotics.MODID, "textures/entities/" + robotTexture + ".png");
        }

        public boolean isInDev() {
            return "stripes".equals(key);
        }
    }

    public static class BasicBoardNBT extends RedstoneBoardRobotNBT {
        private final String id;
        private final String key;
        private final String boardColor;
        private final ResourceLocation robotTexture;

        BasicBoardNBT(String id, String key, String boardColor, String robotTexture) {
            this.id = id;
            this.key = key;
            this.boardColor = boardColor;
            this.robotTexture = new ResourceLocation(BCRobotics.MODID, "textures/entities/" + robotTexture + ".png");
        }

        @Override
        public String getID() {
            return id;
        }

        public String getKey() {
            return key;
        }

        public String getBoardColor() {
            return boardColor;
        }

        @Override
        public void addInformation(ItemStack stack, Player player, List<Component> list, boolean advanced) {
            BoardEntry entry = getById(id);
            if (entry.energyCost() > 0) {
                list.add(Component.translatable("tooltip.buildcraftrobotics.board.energy", entry.energyCost()));
            }
        }

        @Override
        public RedstoneBoardRobot create(EntityRobotBase robot) {
            if ("picker".equals(key)) {
                return new BoardRobotPicker(robot);
            }
            if ("carrier".equals(key)) {
                return new BoardRobotCarrier(robot);
            }
            if ("fluid_carrier".equals(key)) {
                return new BoardRobotFluidCarrier(robot);
            }
            if ("lumberjack".equals(key)) {
                return new BoardRobotLumberjack(robot);
            }
            if ("harvester".equals(key)) {
                return new BoardRobotHarvester(robot);
            }
            if ("miner".equals(key)) {
                return new BoardRobotMiner(robot);
            }
            if ("planter".equals(key)) {
                return new BoardRobotPlanter(robot);
            }
            if ("farmer".equals(key)) {
                return new BoardRobotFarmer(robot);
            }
            if ("leave_cutter".equals(key)) {
                return new BoardRobotLeaveCutter(robot);
            }
            if ("butcher".equals(key)) {
                return new BoardRobotButcher(robot);
            }
            if ("shovelman".equals(key)) {
                return new BoardRobotShovelman(robot);
            }
            if ("pump".equals(key)) {
                return new BoardRobotPump(robot);
            }
            if ("delivery".equals(key)) {
                return new BoardRobotDelivery(robot);
            }
            if ("knight".equals(key)) {
                return new BoardRobotKnight(robot);
            }
            if ("bomber".equals(key)) {
                return new BoardRobotBomber(robot);
            }
            if ("builder".equals(key)) {
                return new BoardRobotBuilder(robot);
            }
            return new BasicRobotBoard(robot, this);
        }

        @Override
        public ResourceLocation getRobotTexture() {
            return robotTexture;
        }
    }

    private static class BasicRobotBoard extends RedstoneBoardRobot {
        private final BasicBoardNBT nbt;

        BasicRobotBoard(EntityRobotBase robot, BasicBoardNBT nbt) {
            super(robot);
            this.nbt = nbt;
        }

        @Override
        public RedstoneBoardRobotNBT getNBTHandler() {
            return nbt;
        }
    }

    private static class SimpleRedstoneBoardRegistry extends RedstoneBoardRegistry {
        private final Map<String, RedstoneBoardNBT<?>> boards = new LinkedHashMap<>();
        private final Map<String, Integer> energyCosts = new LinkedHashMap<>();
        private RedstoneBoardRobotNBT emptyRobotBoard;

        @Override
        public void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, int energyCost) {
            boards.put(redstoneBoardNBT.getID(), redstoneBoardNBT);
            energyCosts.put(redstoneBoardNBT.getID(), energyCost);
        }

        @Override
        public void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability) {
            registerBoardType(redstoneBoardNBT, Math.round(160000 / probability));
        }

        @Override
        public void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT) {
            emptyRobotBoard = redstoneBoardNBT;
        }

        @Override
        public RedstoneBoardRobotNBT getEmptyRobotBoard() {
            return emptyRobotBoard;
        }

        @Override
        public RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag nbt) {
            return getRedstoneBoard(nbt.getString("id"));
        }

        @Override
        public RedstoneBoardNBT<?> getRedstoneBoard(String id) {
            return boards.getOrDefault(id, emptyRobotBoard);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void registerSprites(Consumer<ResourceLocation> spriteRegistrar) {
            for (BoardEntry entry : ENTRIES_BY_ID.values()) {
                spriteRegistrar.accept(new ResourceLocation(BCRobotics.MODID, "items/board/" + entry.boardColor()));
            }
        }

        @Override
        public Collection<RedstoneBoardNBT<?>> getAllBoardNBTs() {
            return boards.values();
        }

        @Override
        public int getEnergyCost(RedstoneBoardNBT<?> board) {
            return board == null ? 0 : energyCosts.getOrDefault(board.getID(), 0);
        }
    }
}
