package buildcraft.factory.loot;

import buildcraft.factory.BCFactory;
import buildcraft.factory.block.BlockWaterGel;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class LootConditionSpreading implements ILootCondition {
    public static LootConditionType TYPE;

    public static void reg() {
        TYPE = Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(BCFactory.MODID, "spreading"), new LootConditionType(new ConditionSerializer()));
    }

    public LootConditionSpreading() {
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        return context.getParamOrNull(LootParameters.BLOCK_STATE).getValue(BlockWaterGel.PROP_STAGE).spreading;
    }

    public static IBuilder builder() {
        return () -> new LootConditionSpreading();
    }

    public static class ConditionSerializer implements ILootSerializer<LootConditionSpreading> {
        @Override
        public void serialize(JsonObject json, LootConditionSpreading value, JsonSerializationContext context) {
        }

        @Override
        public LootConditionSpreading deserialize(JsonObject json, JsonDeserializationContext context) {
            return new LootConditionSpreading();
        }
    }
}
