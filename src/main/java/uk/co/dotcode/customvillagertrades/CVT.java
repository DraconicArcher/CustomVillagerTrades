package uk.co.dotcode.customvillagertrades;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import uk.co.dotcode.customvillagertrades.events.TradeRegistry;
import uk.co.dotcode.customvillagertrades.neoforge.TradeUtilImpl;

import java.io.File;

public class CVT {

	public static final String MOD_ID = "customvillagertrades";

	public static GlobalConfig globalConfig = new GlobalConfig();
	public static TradeUtilImpl TRADE_UTIL;

	private static boolean initialized = false;



	public static void init() {


		ConfigHandler.registeredCustomTrades.clear();

		ConfigHandler.customTrades.forEach((key, collection) -> {
			TradeRegistry.registerCollection(key, collection, false);
		});

		TradeRegistry.registerTradesAllCategory();

		System.out.println("CVT INIT COMPLETE");
		System.out.println("Loaded trades: " + ConfigHandler.customTrades.keySet());
		System.out.println("Registered trades: " + ConfigHandler.registeredCustomTrades.keySet());
	}

	public static void reload() {
		ConfigHandler.init();

		if (ConfigHandler.ACCESS == null) {
			System.out.println("CVT reload: config reloaded (server not ready for trade rebuild yet)");
			return;
		}

		ConfigHandler.finalizeTrades();
		init();

		System.out.println("CVT reload complete");
	}





	@SubscribeEvent
	public static void onServerStarting(ServerStartingEvent event) {

		ConfigHandler.ACCESS = event.getServer().registryAccess();
		TRADE_UTIL = new TradeUtilImpl(ConfigHandler.ACCESS);

		// IMPORTANT: finalize FIRST, then init conversion
		ConfigHandler.finalizeTrades();
		init();

		initialized = true;
	}

	public static void sendConfigIssues(Player player) {
		if (!ModLogger.getConfigIssues().isEmpty()) {
			for (String s : ModLogger.getConfigIssues()) {
				player.sendSystemMessage(Component.literal("[Custom Villager Trades] " + s));
			}
		}
	}
}