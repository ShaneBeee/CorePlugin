package com.shanebeestudios.core.plugin.command;

import com.shanebeestudios.coreapi.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.Component;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.List;

public class BiomeSampleCommand {

    private final List<Entity> textDisplays = new ArrayList<>();

    public BiomeSampleCommand() {
        registerCommand();
    }

    private void registerCommand() {
        CommandTree command = new CommandTree("biomesample")
            .then(LiteralArgument.literal("sample")
                .executesPlayer(info -> {
                    Player player = info.sender();
                    Location loc = player.getLocation().getBlock().getLocation().add(0, 1, 0);
                    loc.setYaw(player.getLocation().getYaw() + 180);
                    createSample(loc);
                }))
            .then(LiteralArgument.literal("clear")
                .executes((sender, args) -> {
                    this.textDisplays.forEach(Entity::remove);
                    this.textDisplays.clear();
                }));

        command.register();
    }

    private void createSample(Location location) {
        String biome = location.getBlock().getBiome().getKey().toString();
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        ServerChunkCache serverchunkcache = serverLevel.getChunkSource();
        RandomState randomstate = serverchunkcache.randomState();

        Climate.Sampler sampler = randomstate.sampler();
        Climate.TargetPoint sample = sampler.sample(QuartPos.fromBlock(location.getBlockX()), QuartPos.fromBlock(location.getBlockY()), QuartPos.fromBlock(location.getBlockZ()));

        List<Component> lines = new ArrayList<>();
        lines.add(Utils.getMini("<grey>Biome: <aqua>" + biome));
        lines.add(getM("Continentalness", getCont(sample.continentalness())));
        lines.add(getM("Temperature", getTemp(sample.temperature())));
        lines.add(getM("Humidity", getHumidity(sample.humidity())));
        lines.add(getM("Erosion", getErosion(sample.erosion())));
        lines.add(getM("Weirdness", getWeirdness(sample.weirdness())));
        lines.add(getM("Depth", getDepth(sample.depth())));
        lines.add(getM("Peaks and Valleys", getPV(sample.weirdness())));

        Component text = Utils.getMini("<b><u>Biome Sampler:<reset> ");
        for (Component line : lines) {
            text = text.appendNewline();
            text = text.append(line);
        }
        Component finalText = text;

        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, textDisplay -> {
            textDisplay.text(finalText);
            textDisplay.setBackgroundColor(Color.fromARGB(200, 0, 0, 0));
            textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
            textDisplay.setLineWidth(1000);
        });

        this.textDisplays.add(display);
    }

    private Component getM(String name, String count) {
        return Utils.getMini("<grey>" + name + ": <aqua>" + count);
    }

    public String getCont(long value) {
        float continentalness = ((float) value) / 10000;
        String suffix;
        if (continentalness <= -1.05) suffix = "0 <reset>=<green> Mushroom Fields"; // Mushroom Fields
        else if (continentalness <= -0.455) suffix = "1 <reset>=<green> Deep Ocean"; // Deep ocean
        else if (continentalness <= -0.19) suffix = "2 <reset>=<green> Ocean"; // Ocean
        else if (continentalness <= -0.11) suffix = "3 <reset>=<green> Coast"; // Coast
        else if (continentalness <= 0.03) suffix = "4 <reset>=<green> Near-Inland";
        else if (continentalness <= 0.3) suffix = "5 <reset>=<green> Mid-Inland";
        else if (continentalness <= 1.0) suffix = "6 <reset>=<green> Far-Inland";
        else suffix = "7 <reset>=<green> Huh?";
        return String.format("%.2f <reset>=<aqua> %s", continentalness, suffix);
    }

    public String getTemp(long value) {
        double temperature = ((float) value) / 10000;
        String suffix;
        if (temperature <= -0.45) suffix = "0 <reset>=<green> Frozen";
        else if (temperature <= -0.15) suffix = "1 <reset>=<green> Cold";
        else if (temperature <= 0.2) suffix = "2 <reset>=<green> Mid";
        else if (temperature <= 0.55) suffix = "3 <reset>=<green> Warm";
        else if (temperature <= 1.0) suffix = "4 <reset>=<green> Hot";
        else suffix = "5 <reset>=<green> Huh?";
        return String.format("%.2f <reset>=<aqua> %s", temperature, suffix);
    }

    public String getHumidity(long value) {
        double humidity = ((float) value) / 10000;
        String suffix;
        if (humidity <= -0.35) suffix = "0";
        else if (humidity <= -0.1) suffix = "1";
        else if (humidity <= 0.1) suffix = "2";
        else if (humidity <= 0.3) suffix = "3";
        else if (humidity <= 1.0) suffix = "4";
        else suffix = "5";
        return String.format("%.2f <reset>=<aqua> %s", humidity, suffix);
    }

    public String getErosion(long value) {
        double erosion = ((float) value) / 10000;
        String suffix;
        if (erosion <= -0.78) suffix = "0";
        else if (erosion <= -0.375) suffix = "1";
        else if (erosion <= -0.2225) suffix = "2";
        else if (erosion <= 0.05) suffix = "3";
        else if (erosion <= 0.45) suffix = "4";
        else if (erosion <= 0.55) suffix = "5";
        else suffix = "6";
        return String.format("%.2f <reset>=<aqua> %s", erosion, suffix);
    }

    public String getWeirdness(long value) {
        double weirdness = ((float) value) / 10000;
        String suffix = weirdness <= 0 ? "0" : "1";
        return String.format("%.2f <reset>=<aqua> %s", weirdness, suffix);
    }

    public String getDepth(long value) {
        double depth = ((float) value) / 10000;
        String suffix = "" + (int) ((depth) * 128);
        return String.format("%.2f <reset>=<aqua> %s", depth, suffix);
    }

    public String getPV(long value) {
        double peaksAndValleys = -(Math.abs(Math.abs(((float) value) / 10000) - 0.6666667F) - 0.33333334F) * 3.0F;
        String suffix;
        if (peaksAndValleys <= -0.85) suffix = "0 <reset>=<green> Valleys";
        else if (peaksAndValleys <= -0.6) suffix = "1 <reset>=<green> Low";
        else if (peaksAndValleys <= 0.2) suffix = "2 <reset>=<green> Mid";
        else if (peaksAndValleys <= 0.7) suffix = "3 <reset>=<green> High";
        else if (peaksAndValleys <= 1.0) suffix = "4 <reset>=<green> Peaks";
        else return "5 = Huh?";
        return String.format("%.2f <reset>=<aqua> %s", peaksAndValleys, suffix);
    }

}
