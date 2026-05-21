package uk.co.dotcode.customvillagertrades.events;

import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.dotcode.customvillagertrades.CVT;
import uk.co.dotcode.customvillagertrades.ConfigHandler;
import uk.co.dotcode.customvillagertrades.ModLogger;
import uk.co.dotcode.customvillagertrades.TradeUtil;
import uk.co.dotcode.customvillagertrades.trades.MyWandererTrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModEvents {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public void onVillagerTrades(VillagerTradesEvent event) {

		String professionKey = TradeUtil.getKeyFromProfession(event.getType());

		if (ConfigHandler.registeredCustomTrades.containsKey(professionKey)) {

			Map<Integer, List<VillagerTrades.ItemListing>> trades =
					ConfigHandler.registeredCustomTrades.get(professionKey);

			if (trades == null) return;

			for (Map.Entry<Integer, List<VillagerTrades.ItemListing>> entry : trades.entrySet()) {

				Integer level = entry.getKey();
				List<VillagerTrades.ItemListing> listings = entry.getValue();

				if (level == null || listings == null) continue;

				var targetList = event.getTrades()
						.computeIfAbsent(level, k -> new java.util.ArrayList<>());

				for (VillagerTrades.ItemListing listing : listings) {
					if (listing != null) {
						targetList.add(listing);
					}
				}
			}
		}


		if (ConfigHandler.registeredAllCategoryTrades != null) {

			for (Map.Entry<Integer, List<VillagerTrades.ItemListing>> entry
					: ConfigHandler.registeredAllCategoryTrades.entrySet()) {

				Integer level = entry.getKey();
				List<VillagerTrades.ItemListing> listings = entry.getValue();

				if (level == null || listings == null) continue;

				var targetList = event.getTrades()
						.computeIfAbsent(level, k -> new java.util.ArrayList<>());

				for (VillagerTrades.ItemListing listing : listings) {
					if (listing != null) {
						targetList.add(listing);
					}
				}
			}
		}

		System.out.println("VillagerTradesEvent fired for: " + event.getType());
	}

	private void addLimited(
			List<VillagerTrades.ItemListing> target,
			List<VillagerTrades.ItemListing> source,
			int max
	) {
		if (source.isEmpty()) return;

		Collections.shuffle(source);

		for (int i = 0; i < Math.min(max, source.size()); i++) {
			target.add(source.get(i));
		}
	}



	@SubscribeEvent
	public void onWandererTrades(WandererTradesEvent event) {

		ModLogger.info("WandererTradesEvent fired");

		var converted = ConfigHandler.registeredCustomWandererTrades;

		if (converted == null || converted.isEmpty()) {
			ModLogger.info("No registered custom wanderer trades");
			return;
		}

		boolean removeVanilla = false;

		var original = ConfigHandler.loadWandererTrades("wanderer");

		if (original != null) {
			removeVanilla = original.removeOtherTrades;
		}

		if (removeVanilla) {
			event.getGenericTrades().clear();
			event.getRareTrades().clear();

			ModLogger.info("Removed vanilla wanderer trades");
		}

		var common = converted.get(1);

		if (common != null) {
			event.getGenericTrades().addAll(common);

			ModLogger.info("Added common wanderer trades: " + common.size());
		}

		var rare = converted.get(2);

		if (rare != null) {
			event.getRareTrades().addAll(rare);

			ModLogger.info("Added rare wanderer trades: " + rare.size());
		}
	}


}