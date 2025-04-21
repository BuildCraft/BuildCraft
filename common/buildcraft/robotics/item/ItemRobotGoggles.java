package buildcraft.robotics.item;

import buildcraft.lib.item.IItemBuildCraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public class ItemRobotGoggles extends ArmorItem implements IItemBuildCraft {
    private final String idBC;

    public ItemRobotGoggles(String idBC, Properties properties) {
        // super(ArmorMaterial.CHAIN, 0, 0);
        super(ArmorMaterial.CHAIN, EquipmentSlotType.HEAD, properties);
        // setCreativeTab(BCCreativeTab.get("main"));
        this.idBC = idBC;
        init();
    }

//    @Override
//    public ArmorProperties getProperties(LivingEntity player, ItemStack armor, DamageSource source, double damage, int slot) {
//        return new ArmorProperties(0, 0, 0);
//    }

//    @Override
//    public int getArmorDisplay(Player player, ItemStack armor, int slot) {
//        return 0;
//    }

    @Override
    // public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot)
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        // Never damaged
        return 0;
    }

//    @Override
//    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
//        return null;// TODO!
//    }

    @Override
    public String getIdBC() {
        return idBC;
    }

    // Calen
    private String unlocalizedName;

    @Override
    public void setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.unlocalizedName;
    }
}
