package com.jewey.rosia.common.items;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCArmorMaterials;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.PhysicalDamageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ModArmorMaterials implements ArmorMaterial, PhysicalDamageType.Multiplier {

    PURPLE_STEEL(1004, 1140, 1130, 835, 4, 7, 9, 4, 24, 3f, 0.15f, 65, 65, 65);

    private final ResourceLocation serializedName;
    private final int feetDamage;
    private final int legDamage;
    private final int chestDamage;
    private final int headDamage;
    private final int feetReduction;
    private final int legReduction;
    private final int chestReduction;
    private final int headReduction;
    private final int enchantability;
    private final float toughness;
    private final float knockbackResistance;
    private final float crushingModifier;
    private final float piercingModifier;
    private final float slashingModifier;

    ModArmorMaterials(int feetDamage, int legDamage, int chestDamage, int headDamage, int feetReduction, int legReduction, int chestReduction, int headReduction, int enchantability, float toughness, float knockbackResistance, float crushingModifier, float piercingModifier, float slashingModifier)
    {
        this.serializedName = Helpers.identifier(name().toLowerCase(Locale.ROOT));
        this.feetDamage = feetDamage;
        this.legDamage = legDamage;
        this.chestDamage = chestDamage;
        this.headDamage = headDamage;
        this.feetReduction = feetReduction;
        this.legReduction = legReduction;
        this.chestReduction = chestReduction;
        this.headReduction = headReduction;
        this.enchantability = enchantability;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;

        // Since each slot is applied separately, the input values are values for a full set of armor of this type.
        this.crushingModifier = crushingModifier * 0.25f;
        this.piercingModifier = piercingModifier * 0.25f;
        this.slashingModifier = slashingModifier * 0.25f;
    }

    @Override
    public float crushing()
    {
        return crushingModifier;
    }

    @Override
    public float piercing()
    {
        return piercingModifier;
    }

    @Override
    public float slashing()
    {
        return slashingModifier;
    }


    @Override
    public int getDurabilityForType(ArmorItem.@NotNull Type pType) {
        if(pType == ArmorItem.Type.BOOTS) {return feetDamage;}
        else if (pType == ArmorItem.Type.LEGGINGS) {return legDamage;}
        else if (pType == ArmorItem.Type.CHESTPLATE) {return chestDamage;}
        else if (pType == ArmorItem.Type.HELMET) {return headDamage;}
        return 0;
    }

    @Override
    public int getDefenseForType(ArmorItem.@NotNull Type pType) {
        if(pType == ArmorItem.Type.BOOTS) {return feetReduction;}
        else if (pType == ArmorItem.Type.LEGGINGS) {return legReduction;}
        else if (pType == ArmorItem.Type.CHESTPLATE) {return chestReduction;}
        else if (pType == ArmorItem.Type.HELMET) {return headReduction;}
        return 0;
    }

    @Override
    public int getEnchantmentValue()
    {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound()
    {
        return TFCSounds.ARMOR_EQUIP.get(TFCArmorMaterials.BLACK_STEEL).get();
    }

    /**
     * Use {@link #getId()} because it doesn't have weird namespaced side effects.
     */
    @Override
    @Deprecated
    public String getName()
    {
        // Note that in HumanoidArmorLayer, the result of getName() is used directly in order to infer the armor texture location
        // So, this needs to be properly namespaced despite being a string.
        return serializedName.toString();
    }

    public ResourceLocation getId()
    {
        return serializedName;
    }

    @Override
    public float getToughness()
    {
        return toughness;
    }

    @Override
    public float getKnockbackResistance()
    {
        return knockbackResistance;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return Ingredient.EMPTY;
    }

}
