package uk.co.dotcode.customvillagertrades.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

import uk.co.dotcode.customvillagertrades.CVT;
import uk.co.dotcode.customvillagertrades.ConfigHandler;
import uk.co.dotcode.customvillagertrades.ModLogger;
import uk.co.dotcode.customvillagertrades.events.ModEvents;
import uk.co.dotcode.customvillagertrades.events.TradeRegistry;

import java.io.File;

@Mod(CVT.MOD_ID)
public class CVTNeoForge {

	public CVTNeoForge(IEventBus modEventBus) {

		// config paths
		ConfigHandler.folder = FMLPaths.CONFIGDIR.get().resolve("custom trades").toFile();
		ConfigHandler.folderWanderer = FMLPaths.CONFIGDIR.get().resolve("custom trades/wanderer").toFile();
		ConfigHandler.folderExports = FMLPaths.CONFIGDIR.get().resolve("custom trades/exports").toFile();
		ConfigHandler.globalConfig = new File(FMLPaths.CONFIGDIR.get().toFile(), "cvtGlobal.json");


		ConfigHandler.init();


		NeoForge.EVENT_BUS.register(new ModEvents());
		NeoForge.EVENT_BUS.register(TradeRegistry.class);
		NeoForge.EVENT_BUS.register(CVT.class);

		NeoForge.EVENT_BUS.register(CommandRegistryForge.class);

		modEventBus.addListener(this::onLoadComplete);
		modEventBus.addListener(this::commonSetup);
	}


	private void commonSetup(final FMLCommonSetupEvent event) {

		event.enqueueWork(() -> {


			for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {

				if (prof == VillagerProfession.NONE ||
						prof == VillagerProfession.NITWIT) {
					continue;
				}

				TradeRegistry.registerTrades(prof);
			}


			TradeRegistry.registerTradesAllCategory();


			TradeRegistry.registerWanderingTrades();

			ModLogger.info("Finished registering all custom trades");
		});
	}



	private void onLoadComplete(final net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent event) {
    }
}