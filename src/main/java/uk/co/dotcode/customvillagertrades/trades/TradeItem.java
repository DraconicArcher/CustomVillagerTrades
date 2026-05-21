package uk.co.dotcode.customvillagertrades.trades;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;


import net.minecraft.world.level.block.SuspiciousEffectHolder;
import uk.co.dotcode.customvillagertrades.ModLogger;
import uk.co.dotcode.customvillagertrades.TradeUtil;
import uk.co.dotcode.customvillagertrades.neoforge.TradeUtilImpl;

import java.util.*;

public class TradeItem {

    public String itemKey;
    public int amount = 1;
    public Integer priceModifier;
    public String name;

    public String advancedNBTData;

    private Holder<Item> itemHolder;

    private Integer amountRange;
    private Integer priceModifierAdditional;

    private List<EnchantmentEntry> enchantments = new ArrayList<>();
    private List<String> blacklist = new ArrayList<>();

    private Object metadata;
    private Integer r, g, b;

    private static final Random RANDOM = new Random();

    private record EffectData(
            Holder<MobEffect> effect,
            int duration,
            int amplifier,
            boolean visible
    ) {}



    public void resolve(RegistryAccess access) {
        if (itemHolder != null || itemKey == null) return;

        ResourceLocation id = ResourceLocation.tryParse(itemKey);
        if (id == null) return;

        itemHolder = access.registryOrThrow(Registries.ITEM)
                .getHolder(id)
                .orElse(null);
    }


    private List<EffectData> normalizeEffects(Entity entity) {

        if (effects == null || effects.isEmpty()) return List.of();

        RegistryAccess access = entity.level().registryAccess();
        var registry = access.registryOrThrow(Registries.MOB_EFFECT);

        List<EffectData> out = new ArrayList<>();

        for (MyTradeEffect e : effects) {

            String key = e.effectKey;

            // RANDOM SUPPORT
            if ("random".equalsIgnoreCase(key)) {

                List<Holder.Reference<MobEffect>> all = registry.holders().toList();
                if (all.isEmpty()) continue;

                Holder<MobEffect> random =
                        all.get(RANDOM.nextInt(all.size()));

                key = registry.getKey(random.value()).toString();
            }

            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null) continue;

            Holder<MobEffect> holder =
                    registry.getHolder(id).orElse(null);

            if (holder == null) continue;

            int duration = e.duration == null ? 100 : e.duration;

            int amplifier = Math.max(0,
                    (e.level == null ? 1 : e.level) - 1
            );

            boolean visible = e.isVisible == null || e.isVisible;

            out.add(new EffectData(holder, duration, amplifier, visible));
        }

        return out;
    }

    private void applyPotion(ItemStack stack, List<EffectData> effects) {

        List<MobEffectInstance> list = new ArrayList<>();

        for (EffectData e : effects) {
            list.add(new MobEffectInstance(
                    e.effect(),
                    e.duration(),
                    e.amplifier(),
                    false,
                    e.visible(),
                    e.visible()
            ));
        }


        stack.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.empty(), list));
    }





    private void applyStew(ItemStack stack, List<EffectData> effects) {

        List<SuspiciousStewEffects.Entry> list = new ArrayList<>();

        for (EffectData e : effects) {

            list.add(new SuspiciousStewEffects.Entry(
                    e.effect(),
                    e.duration()
            ));
        }

        stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS,
                new SuspiciousStewEffects(list));
    }





    public ItemStack createItemStack(Entity entity){

        if (itemHolder == null && itemKey != null) {

            ResourceLocation id = ResourceLocation.parse(itemKey);

            itemHolder = BuiltInRegistries.ITEM.getHolder(id).orElse(null);

            if (itemHolder == null) {

                ModLogger.error("Failed to resolve item: " + itemKey);

                return ItemStack.EMPTY;
            }
        }

        if (itemHolder == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(itemHolder.value(), getAmount());

        if (advancedNBTData != null && !advancedNBTData.isBlank()) {
            try {

                CompoundTag fullTag = TagParser.parseTag(advancedNBTData);


                if (fullTag.contains("BlockEntityTag")) {

                    CompoundTag blockEntityTag =
                            fullTag.getCompound("BlockEntityTag");

                    stack.set(
                            DataComponents.BLOCK_ENTITY_DATA,
                            CustomData.of(blockEntityTag)
                    );

                } else {


                    stack.set(
                            DataComponents.CUSTOM_DATA,
                            CustomData.of(fullTag)
                    );
                }

            } catch (Exception e) {
                ModLogger.error("Invalid advancedNBTData: " + advancedNBTData);
                e.printStackTrace();
            }
        }

        if (metadata instanceof String nbt && !nbt.isBlank()) {
            try {
                CompoundTag tag = TagParser.parseTag(nbt);


                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            } catch (Exception e) {
                ModLogger.error("Invalid advancedNBTData: " + nbt);
                e.printStackTrace();
            }
        }



        RegistryAccess access = entity.level().registryAccess();

        List<EffectData> normalized = normalizeEffects(entity);

        if (!normalized.isEmpty()) {

            if (stack.is(Items.POTION)
                    || stack.is(Items.SPLASH_POTION)
                    || stack.is(Items.LINGERING_POTION)
                    || stack.is(Items.TIPPED_ARROW)) {

                applyPotion(stack, normalized);
            }

            else if (stack.is(Items.SUSPICIOUS_STEW)) {

                applyStew(stack, normalized);
            }
        }







        if (!enchantments.isEmpty()) {

            // Pick ONE enchant entry
            EnchantmentEntry entry =
                    enchantments.get(RANDOM.nextInt(enchantments.size()));

            EnchantmentResolver.EnchResult result =
                    EnchantmentResolver.resolve(entry, itemKey, blacklist);

            if (result != null) {

                Enchantment ench = result.enchantment();
                int level = result.level();

                ResourceLocation enchId =
                        TradeUtilImpl.getRegistryNameEnchantment(ench);

                if (enchId != null) {

                    var holder = access.registryOrThrow(Registries.ENCHANTMENT)
                            .getHolder(enchId)
                            .orElse(null);

                    if (holder != null) {
                        stack.enchant(holder, level);
                    }
                }
            }
        }


        if (name != null && !name.isBlank()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }


        if (r != null && g != null && b != null) {
            int color = (r << 16) | (g << 8) | b;
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        }

        return stack;
    }


    private List<MyTradeEffect> effects = new ArrayList<>();



    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPriceModifier(Integer priceModifier) {
        this.priceModifier = priceModifier;
    }

    public void setEnchantments(List<EnchantmentEntry> enchantments) {
        this.enchantments = enchantments;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public void setEffects(List<MyTradeEffect> effects) {
        this.effects = effects;
    }


    public int getAmount() {
        if (amountRange != null && amountRange > 0) {
            return amount + RANDOM.nextInt(amountRange + 1);
        }
        return amount;
    }


    public boolean validate(String prof, int i) {
        if (itemHolder == null && itemKey == null) {
            ModLogger.error("Missing item: " + prof + " index " + i);
            return false;
        }
        return true;
    }
}