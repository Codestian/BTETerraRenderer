package com.mndk.bteterrarenderer.client.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.client.KeyMappings18;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.loader.ConfigLoaders;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.File;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        File gameConfigDirectory = new File(Minecraft.getInstance().gameDirectory, "config");
        ConfigLoaders.loadAll(gameConfigDirectory);

        KeyMappings18.registerKeys();
        BTRConfigConnector.load();
        BTETerraRendererConstants.LOGGER.info("Mod setup done");
    }
}
