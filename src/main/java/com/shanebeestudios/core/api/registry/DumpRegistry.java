package com.shanebeestudios.core.api.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.shanebeestudios.core.plugin.CorePlugin;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility to dump registry objects to file
 * <p>Will output to json Datapack format</p>
 *
 * @param <N> Registry type
 */
@SuppressWarnings({"unused"})
public class DumpRegistry<N> {

    private static final RegistryOps<JsonElement> REGISTRY_OPS = RegistryOps.create(JsonOps.INSTANCE, MinecraftServer.getServer().registryAccess());
    private static final File DATA_FOLDER = CorePlugin.getInstance().getDataFolder();
    private static final Map<String, DumpRegistry<?>> BY_NAME = new HashMap<>();

    static {
        register("biomes", Registries.BIOME, Biome.DIRECT_CODEC);
        register("enchantments", Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC);
    }

    private static <N> void register(String name, ResourceKey<Registry<N>> registry, Codec<N> codec) {
        DumpRegistry<N> reg = new DumpRegistry<>(registry, codec);
        BY_NAME.put(name, reg);
    }

    /**
     * Get a DumpRegistry by name
     *
     * @param name Name of DumpRegistry
     * @return DumpRegistry by name or null if none found
     */
    public static DumpRegistry<?> getByName(String name) {
        return BY_NAME.get(name);
    }

    /**
     * Get all DumpRegistry names
     *
     * @return All names
     */
    public static Set<String> getRegistryNames() {
        return BY_NAME.keySet();
    }

    private final ResourceKey<Registry<N>> registryKey;
    private final String registryPath;
    private final Codec<N> codec;

    private DumpRegistry(ResourceKey<Registry<N>> registryKey, Codec<N> codec) {
        this.registryKey = registryKey;
        this.registryPath = registryKey.identifier().getPath();
        this.codec = codec;
    }

    /**
     * Dump all objects from registry to file
     */
    public void dumpObjects() {
        Registry<N> registry = MinecraftServer.getServer().registryAccess().lookupOrThrow(registryKey);
        for (N registryValue : registry) {
            Identifier identifier = registry.getKey(registryValue);
            assert identifier != null;
            dump(registryValue, identifier);
        }
    }

    private void dump(N registryValue, Identifier location) {
        File file = new File(DATA_FOLDER, "data/" + location.getNamespace() + "/" + this.registryPath + "/" + location.getPath() + ".json");

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }
        DataResult<JsonElement> jsonData = this.codec.encodeStart(REGISTRY_OPS, registryValue);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.writeString(file.toPath(), gson.toJson(jsonData.getOrThrow()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
