package me.daoge.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.api.math.location.Location3dc;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage warp points stored under the plugin's data folder.
 */
public class WarpManager {

    private static final String WARP_FILE_NAME = "warp.json";
    private static final Type WARP_LIST_TYPE = new TypeToken<List<LocationRecord>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path warpFile;
    private final Map<String, LocationRecord> warps = new ConcurrentHashMap<>();

    public WarpManager(Path dataFolder) {
        this.warpFile = dataFolder.resolve(WARP_FILE_NAME);
        load();
    }

    /**
     * @return immutable view of all warps
     */
    public Collection<LocationRecord> getWarps() {
        return warps.values();
    }

    /**
     * @return warp list sorted by name (case-insensitive)
     */
    public List<LocationRecord> getSortedWarps() {
        return warps.values().stream()
                .sorted(Comparator.comparing(LocationRecord::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<LocationRecord> getWarp(String name) {
        return Optional.ofNullable(warps.get(normalize(name)));
    }

    /**
     * Add a warp using the given location.
     *
     * @param name     warp name
     * @param location location snapshot
     * @return true if added, false if name exists or location invalid
     */
    public boolean addWarp(String name, Location3dc location) {
        if (location == null || location.dimension() == null) {
            return false;
        }
        String key = normalize(name);
        if (warps.containsKey(key)) {
            return false;
        }
        warps.put(key, LocationRecord.from(name, location));
        save();
        return true;
    }

    /**
     * Remove a warp by name.
     *
     * @param name warp name
     * @return true if removed
     */
    public boolean removeWarp(String name) {
        LocationRecord removed = warps.remove(normalize(name));
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    private void load() {
        try {
            Files.createDirectories(warpFile.getParent());
            if (!Files.exists(warpFile)) {
                save();
                return;
            }
            String content = Files.readString(warpFile);
            if (content.isBlank()) {
                return;
            }
            List<LocationRecord> loaded = gson.fromJson(content, WARP_LIST_TYPE);
            if (loaded != null) {
                loaded.forEach(warp -> warps.put(normalize(warp.name()), warp));
            }
        } catch (Exception e) {
            EssentialsPlugin.getInstance().getPluginLogger().error("Failed to load warps", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(warpFile.getParent());
            String json = gson.toJson(new ArrayList<>(warps.values()), WARP_LIST_TYPE);
            Files.writeString(
                    warpFile,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            EssentialsPlugin.getInstance().getPluginLogger().error("Failed to save warps", e);
        }
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}

