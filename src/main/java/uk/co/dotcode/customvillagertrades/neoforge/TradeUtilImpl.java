package uk.co.dotcode.customvillagertrades.neoforge;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;

import uk.co.dotcode.customvillagertrades.CVT;
import uk.co.dotcode.customvillagertrades.TradeUtil;

public class TradeUtilImpl {

	private final RegistryAccess registryAccess;

	// ✅ Constructor takes RegistryAccess
	public TradeUtilImpl(RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	public RegistryAccess getRegistryAccess() {
		return registryAccess;
	}


	public List<VillagerProfession> getAllProfessions() {
		return BuiltInRegistries.VILLAGER_PROFESSION.stream().collect(Collectors.toList());
	}

	public VillagerProfession getProfessionFromKey(String professionKey) {
		String actualKey = professionKey.contains(":") ? professionKey : "minecraft:" + professionKey;
		return BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(actualKey));
	}

	public String getKeyFromProfession(VillagerProfession profession) {
		return BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString();
	}


	public Item getItemFromKey(String itemKey) {
		return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemKey));
	}

	public ResourceLocation getRegistryNameItem(Item item) {
		return BuiltInRegistries.ITEM.getKey(item);
	}


	public static Enchantment getEnchantmentFromKey(String key) {
		if (CVT.TRADE_UTIL == null) return null;

		RegistryAccess access = CVT.TRADE_UTIL.getRegistryAccess();

		return access.registryOrThrow(Registries.ENCHANTMENT)
				.get(ResourceLocation.tryParse(key));
	}

	public static List<Enchantment> getRegisteredEnchantments() {
		if (CVT.TRADE_UTIL == null) return List.of();

		RegistryAccess access = CVT.TRADE_UTIL.getRegistryAccess();

		return access.registryOrThrow(Registries.ENCHANTMENT)
				.holders()
				.map(Holder::value)
				.toList();
	}

	public static ResourceLocation getRegistryNameEnchantment(Enchantment ench) {

		if (CVT.TRADE_UTIL == null) return null;

		RegistryAccess access = CVT.TRADE_UTIL.getRegistryAccess();

		return access.registryOrThrow(Registries.ENCHANTMENT)
				.getKey(ench);
	}




	public static Potion getPotionFromKey(String key) {
		if (CVT.TRADE_UTIL == null) return null;
		return TradeUtilImpl.getPotionFromKey(key);
	}

	public List<Potion> getRegisteredPotions() {
		return BuiltInRegistries.POTION.stream().collect(Collectors.toList());
	}

	public ResourceLocation getPotionKey(Potion potion) {
		return BuiltInRegistries.POTION.getKey(potion);
	}


	public static MobEffect getEffectFromKey(String key) {
		if (CVT.TRADE_UTIL == null) return null;
		return TradeUtilImpl.getEffectFromKey(key);
	}

	public List<MobEffect> getRegisteredMobEffects() {
		return BuiltInRegistries.MOB_EFFECT.stream().collect(Collectors.toList());
	}

	public ResourceLocation getRegistryNameEffect(MobEffect effect) {
		return BuiltInRegistries.MOB_EFFECT.getKey(effect);
	}
}