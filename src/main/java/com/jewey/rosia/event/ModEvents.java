package com.jewey.rosia.event;

import com.jewey.rosia.Rosia;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Path;



public class ModEvents {

    public static void init()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(ModEvents::onPackFinder);
    }

    /*
    MIT License
    Copyright (c) 2022 eerussianguy
    https://github.com/eerussianguy/firmalife/blob/1.20.x/src/main/java/com/eerussianguy/firmalife/common/FLEvents.java
     */
    public static void onPackFinder(AddPackFindersEvent event)
    {
        try
        {
            if (event.getPackType() == PackType.CLIENT_RESOURCES)
            {
                final IModFile modFile = ModList.get().getModFileById(Rosia.MOD_ID).getFile();
                final Path resourcePath = modFile.getFilePath();
                try (PathPackResources pack = new PathPackResources(modFile.getFileName() + ":overload", resourcePath, true))
                {
                    final PackMetadataSection metadata = pack.getMetadataSection(PackMetadataSection.TYPE);
                    if (metadata != null)
                    {
                        Rosia.LOGGER.info("Injecting Rosia override pack");
                        event.addRepositorySource(consumer ->
                                consumer.accept(Pack.readMetaAndCreate("rosia_data", Component.literal("Rosia Resources"), true, id -> pack, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN))
                        );
                    }
                }

            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
