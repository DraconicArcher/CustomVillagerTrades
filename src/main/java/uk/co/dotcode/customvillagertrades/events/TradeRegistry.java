package uk.co.dotcode.customvillagertrades.events;

import java.util.*;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import uk.co.dotcode.customvillagertrades.ConfigHandler;
import uk.co.dotcode.customvillagertrades.ModLogger;
import uk.co.dotcode.customvillagertrades.TradeUtil;
import uk.co.dotcode.customvillagertrades.trades.*;

public class TradeRegistry {

	public static final Map<String, String> usedUTIDs = new HashMap<>();


	private static boolean initialized = false;


	public static void registerWanderingTrades() {

		var data = ConfigHandler.loadWandererTrades("wanderer");
		if (data == null || !data.validate()) return;

		Map<Integer, List<VillagerTrades.ItemListing>> converted = new HashMap<>();

		for (MyWandererTrade t : data.trades) {

			int level = t.isRare ? 2 : 1;

			converted.computeIfAbsent(level, k -> new ArrayList<>())
					.add(new MyTradeConverted(t));
		}

		ConfigHandler.registeredCustomWandererTrades = converted;
	}



	@SubscribeEvent
	public static void onVillagerTrades(VillagerTradesEvent event) {

		String key = TradeUtil.getKeyFromProfession(event.getType());

		var collection = ConfigHandler.registeredCustomTrades.get(key);
		if (collection == null) return;


		if (initialized && event.getType() == VillagerProfession.NONE) return;

		var trades = event.getTrades();


		boolean removeVanilla = false;


		var original = ConfigHandler.customTrades.get(key);
		if (original != null) {
			removeVanilla = original.removeOtherTrades;
		}

		if (removeVanilla) {
			trades.clear();
		}


		for (var entry : collection.entrySet()) {

			int level = entry.getKey();
			List<VillagerTrades.ItemListing> listings = entry.getValue();

			if (listings == null || listings.isEmpty()) continue;

			List<VillagerTrades.ItemListing> target =
					trades.computeIfAbsent(level, k -> new ArrayList<>());

			target.addAll(listings);
		}

		initialized = true;
	}



	public static void registerTrades(VillagerProfession profession) {

		if (profession == VillagerProfession.NONE ||
				profession == VillagerProfession.NITWIT) {
			return;
		}

		String key = TradeUtil.getKeyFromProfession(profession);

		var data = ConfigHandler.loadTrades(key);
		registerCollection(key, data, false);
	}



	public static void registerCollection(String profession,
										  TradeCollection tradeCollection,
										  boolean reload) {

		if (tradeCollection == null || !tradeCollection.validate()) return;

		tradeCollection = manageUTIDs(tradeCollection);

		if (tradeCollection.shouldUpdateFile) {
			ConfigHandler.overwriteTradeCollection(tradeCollection);
		}

		if (reload) {
			ConfigHandler.init();
			tradeCollection = ConfigHandler.loadTrades(profession);
			if (tradeCollection == null) return;
		}

		Map<Integer, List<VillagerTrades.ItemListing>> converted = new HashMap<>();

		for (MyTrade t : tradeCollection.trades) {
			if (t == null || t.tradeLevel == null) continue;

			converted
					.computeIfAbsent(t.tradeLevel, k -> new ArrayList<>())
					.add(new MyTradeConverted(t));
		}

		ConfigHandler.registeredCustomTrades.put(profession, converted);
		ConfigHandler.customTrades.put(profession, tradeCollection);
	}



	public static void registerTradesAllCategory() {

		var data = ConfigHandler.loadTrades("all");
		if (data == null || !data.validate()) return;

		data = manageUTIDs(data);

		if (data.shouldUpdateFile) {
			ConfigHandler.overwriteTradeCollection(data);
		}

		Map<Integer, List<VillagerTrades.ItemListing>> converted = new HashMap<>();

		for (MyTrade t : data.trades) {
			if (t == null || t.tradeLevel == null) continue;

			converted
					.computeIfAbsent(t.tradeLevel, k -> new ArrayList<>())
					.add(new MyTradeConverted(t));
		}

		ConfigHandler.registeredAllCategoryTrades = converted;
	}


	public static VillagerTrades.ItemListing convert(MyWandererTrade trade) {
		if (trade == null) return null;
		return new MyTradeConverted(trade);
	}






	public static TradeCollection manageUTIDs(TradeCollection coll) {

		if (coll == null || coll.trades == null) return coll;

		TradeCollection out = new TradeCollection();
		out.profession = coll.profession;
		out.removeOtherTrades = coll.removeOtherTrades;
		out.shouldUpdateFile = coll.shouldUpdateFile;

		List<MyTrade> safeList = new ArrayList<>();

		for (MyTrade trade : coll.trades) {

			if (trade == null) {
				continue; // 🚨 prevents List.of crash / NPE
			}

			if (trade.UTID == null || trade.UTID.isEmpty()) {
				trade = TradeUtil.generateUTID(coll.profession, trade);
				out.shouldUpdateFile = true;
			}

			usedUTIDs.put(trade.UTID, coll.profession);
			safeList.add(trade);
		}

		out.trades = List.of(safeList.toArray(new MyTrade[0]));
		return out;
	}



	public static String addNewTrade(String profession, MyTrade trade) {

		if (trade.UTID == null || trade.UTID.isEmpty()) {
			trade = TradeUtil.generateUTID(profession, trade);
		}

		usedUTIDs.put(trade.UTID, profession);

		var coll = ConfigHandler.customTrades.getOrDefault(profession, new TradeCollection());
		coll.profession = profession;
		coll.removeOtherTrades = false;

		var list = new ArrayList<>(List.of(coll.trades == null ? new MyTrade[0] : coll.trades));
		list.add(trade);

		coll.trades = List.of(list.toArray(new MyTrade[0]));
		coll.shouldUpdateFile = true;

		registerCollection(profession, coll, true);

		return trade.UTID;
	}

	public static String removeTrade(String utid) {

		String prof = usedUTIDs.get(utid);
		if (prof == null) return "Failed to remove trade " + utid;

		var coll = ConfigHandler.customTrades.get(prof);
		if (coll == null) return "Failed to remove trade " + utid;

		List<MyTrade> list = new ArrayList<>();

		for (MyTrade t : coll.trades) {
			if (!utid.equals(t.UTID)) {
				list.add(t);
			}
		}

		coll.trades = List.of(list.toArray(new MyTrade[0]));
		coll.shouldUpdateFile = true;

		registerCollection(prof, coll, true);

		return "Removed trade " + utid;
	}
}