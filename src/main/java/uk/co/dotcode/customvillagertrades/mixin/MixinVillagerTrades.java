package uk.co.dotcode.customvillagertrades.mixin;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import uk.co.dotcode.customvillagertrades.ConfigHandler;
import uk.co.dotcode.customvillagertrades.TradeUtil;
import uk.co.dotcode.customvillagertrades.trades.TradeCollection;

@Mixin(value = Villager.class, priority = 9999)
public abstract class MixinVillagerTrades {

	@Invoker("getVillagerData")
	public abstract VillagerData invokeGetVillagerData();

	@Redirect(
			method = "updateTrades",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/npc/Villager;addOffersFromItemListings(Lnet/minecraft/world/item/trading/MerchantOffers;[Lnet/minecraft/world/entity/npc/VillagerTrades$ItemListing;I)V"
			)
	)
	private void redirectAddOffersFromItemListings(
			Villager villager,
			MerchantOffers merchantOffers,
			VillagerTrades.ItemListing[] itemListings,
			int i
	) {
		VillagerData data = invokeGetVillagerData();
		VillagerProfession profession = data.getProfession();

		String key = TradeUtil.getKeyFromProfession(profession);

		TradeCollection coll = ConfigHandler.customTrades.get(key);

		int limit = 2;

		if (coll != null) {
			limit = Math.max(1, coll.maxTrades);

			if (coll.removeOtherTrades) {
				limit = Math.min(limit, itemListings.length);
			}
		}

		TradeUtil.addOffersFromItemListings(villager, itemListings, limit);
	}

	@ModifyVariable(method = "updateTrades()V", at = @At("STORE"), ordinal = 0)
	private VillagerTrades.ItemListing[] injected(VillagerTrades.ItemListing[] originalEntries) {

		VillagerData data = invokeGetVillagerData();
		VillagerProfession profession = data.getProfession();
		int level = data.getLevel();

		if (ConfigHandler.registeredCustomTrades.containsKey(TradeUtil.getKeyFromProfession(profession))) {
			boolean shouldRemoveAll = ConfigHandler.customTrades
					.get(TradeUtil.getKeyFromProfession(profession)).removeOtherTrades;

			VillagerTrades.ItemListing[] customTrades = getApplicableTrades(profession, level);

			if (shouldRemoveAll) {
				return customTrades;
			}

			VillagerTrades.ItemListing[] mergedTrades = ArrayUtils.addAll(originalEntries, customTrades);
			return mergedTrades;
		}

		return originalEntries;
	}

	@Unique
	private VillagerTrades.ItemListing[] getApplicableTrades(VillagerProfession profession, int level) {
		VillagerTrades.ItemListing[] addedEntries = ConfigHandler.registeredCustomTrades
                .get(TradeUtil.getKeyFromProfession(profession)).get(level).toArray(new VillagerTrades.ItemListing[0]);
		VillagerTrades.ItemListing[] allCustomEntries = addedEntries;

		if (ConfigHandler.registeredAllCategoryTrades != null) {
			if (ConfigHandler.registeredAllCategoryTrades.containsKey(level)) {
				VillagerTrades.ItemListing[] addedAllCategoryEntries = ConfigHandler.registeredAllCategoryTrades
                        .get(level).toArray(new VillagerTrades.ItemListing[0]);
				allCustomEntries = ArrayUtils.addAll(addedEntries, addedAllCategoryEntries);
			}
		}

		return allCustomEntries;
	}
}