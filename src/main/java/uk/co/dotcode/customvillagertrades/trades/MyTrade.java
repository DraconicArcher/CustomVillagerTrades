package uk.co.dotcode.customvillagertrades.trades;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import uk.co.dotcode.customvillagertrades.ModLogger;
import uk.co.dotcode.customvillagertrades.TradeUtil;

import java.util.List;
import java.util.Optional;

public class MyTrade {



	public TradeItem offer;
	public List<TradeItem> multiOffer;

	public TradeItem request;
	public List<TradeItem> multiRequest;

	public TradeItem additionalRequest;
	public List<TradeItem> additionalMultiRequest;



	public Integer tradeExp;
	public Integer maxUses;
	public Float priceMultiplier = 0.05f;
	public Integer demand = 0;
	public Integer tradeLevel;

	// =========================
	// ID
	// =========================

	public String UTID;

	public void assignUTID(String id) {
		this.UTID = id;
	}



	public MerchantOffer createTrade(Entity entity, RegistryAccess access) {

		TradeItem offerItem = pickOffer();
		TradeItem requestItem = pickRequest();
		TradeItem extraItem = pickExtraRequest();

		if (offerItem == null || requestItem == null) return null;

		ItemStack requestStack = requestItem.createItemStack(entity);
		ItemStack resultStack = offerItem.createItemStack(entity);
		ItemStack extraStack = extraItem != null
				? extraItem.createItemStack(entity)
				: ItemStack.EMPTY;

		return new MerchantOffer(
				new net.minecraft.world.item.trading.ItemCost(
						requestStack.getItem(),
						requestStack.getCount()
				),
				extraStack.isEmpty()
						? Optional.empty()
						: Optional.of(new net.minecraft.world.item.trading.ItemCost(
						extraStack.getItem(),
						extraStack.getCount()
				)),
				resultStack,
				0,
				maxUses == null ? 1 : maxUses,
				tradeExp == null ? 1 : tradeExp,
				priceMultiplier,
				demand == null ? 0 : demand
		);
	}



	private TradeItem pickOffer() {
		if (multiOffer != null && !multiOffer.isEmpty()) {
			return multiOffer.get(TradeUtil.random.nextInt(multiOffer.size()));
		}
		return offer;
	}

	private TradeItem pickRequest() {
		if (multiRequest != null && !multiRequest.isEmpty()) {
			return multiRequest.get(TradeUtil.random.nextInt(multiRequest.size()));
		}
		return request;
	}

	private TradeItem pickExtraRequest() {
		if (additionalMultiRequest != null && !additionalMultiRequest.isEmpty()) {
			return additionalMultiRequest.get(
					TradeUtil.random.nextInt(additionalMultiRequest.size())
			);
		}
		return additionalRequest;
	}



	public boolean validate(String profession, int index) {

		if (pickOffer() == null) {
			ModLogger.warn("Missing offer: " + profession + " entry " + index);
			return false;
		}

		if (pickRequest() == null) {
			ModLogger.warn("Missing request: " + profession + " entry " + index);
			return false;
		}

		if (tradeLevel == null) {
			ModLogger.warn("Missing tradeLevel: " + profession + " entry " + index);
			return false;
		}

		return true;
	}
}