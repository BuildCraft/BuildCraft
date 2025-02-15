package buildcraft.factory.loot;

import buildcraft.factory.BCFactory;
import buildcraft.factory.block.BlockWaterGel;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LootConditionSpreading implements LootItemCondition {
    public static LootItemConditionType TYPE;

    public static void reg() {
        TYPE = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(BCFactory.MODID, "spreading"), new LootItemConditionType(new LootConditionSpreading.ConditionSerializer()));
    }

    public LootConditionSpreading() {
    }

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        return context.getParam(LootContextParams.BLOCK_STATE).getValue(BlockWaterGel.PROP_STAGE).spreading;
    }

    public static LootItemCondition.Builder builder() {
        return () -> new LootConditionSpreading();
    }

    public static class ConditionSerializer implements Serializer<LootConditionSpreading> {
        @Override
        public void serialize(JsonObject json, LootConditionSpreading value, JsonSerializationContext context) {
        }

        @Override
        public LootConditionSpreading deserialize(JsonObject json, JsonDeserializationContext context) {
            return new LootConditionSpreading();
        }
    }
}
