package uk.co.dotcode.customvillagertrades.neoforge;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import uk.co.dotcode.customvillagertrades.events.CVTCommands;

public class CommandRegistryForge {

	@SubscribeEvent
	public static void registerCommands(final RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		CVTCommands.register(dispatcher, event.getBuildContext());
	}

}
