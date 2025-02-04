package com.shanebeestudios.core.plugin.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.shanebeestudios.core.plugin.CorePlugin;
import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.StringArgument;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.block.CraftBiome;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class DumpRegistryCommand {

    public DumpRegistryCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("dumpregistry")
            .then(new StringArgument("registry")
                .executes(context -> {
                    String reg = context.args().getByClass("registry", String.class);
                    assert reg != null;
                    if (reg.equalsIgnoreCase("biome")) {
                        dumpBiomes();
                    }
                }));

        command.register();
    }

    @SuppressWarnings("deprecation")
    private void dumpBiomes() {
        Utils.log("Dumping Biomes to directory!");
        for (org.bukkit.block.Biome bukkitBiome : Registry.BIOME) {
            NamespacedKey namespacedKey = bukkitBiome.getKey();

            File file = new File(CorePlugin.getInstance().getDataFolder(), "biomes/" + namespacedKey.namespace() + "/" + namespacedKey.getKey() + ".json");
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                continue;
            }
            Biome biome = CraftBiome.bukkitToMinecraft(bukkitBiome);
            Utils.log("Creating json for biome '" + namespacedKey + "'");
            dumpBiome(Holder.direct(biome), file.toPath());
        }
        Utils.log("Done dumping Biomes to directory!");
    }

    private void dumpBiome(Holder<Biome> biomeHolder, Path path) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, MinecraftServer.getServer().registryAccess());
        DataResult<JsonElement> jsonData = Biome.CODEC.encodeStart(ops, biomeHolder);
        JsonElement jsonElement = jsonData.getOrThrow();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.writeString(path, gson.toJson(jsonElement));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
