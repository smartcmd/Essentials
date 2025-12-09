package me.daoge.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.api.math.location.Location3dc;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage player-specific home locations stored under the plugin's data folder.
 */
public class HomeManager {

    private static final String HOME_FILE_NAME = "home.json";
    private static final Type HOME_DATA_TYPE = new TypeToken<Map<String, List<LocationRecord>>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path homeFile;
    private final Map<UUID, Map<String, LocationRecord>> homes = new ConcurrentHashMap<>();

    public HomeManager(Path dataFolder) {
        this.homeFile = dataFolder.resolve(HOME_FILE_NAME);
        load();
    }

    /**
     * @return immutable sorted list of a player's homes (case-insensitive by name)
     */
    public List<LocationRecord> getSortedHomes(UUID playerId) {
        Map<String, LocationRecord> map = homes.get(playerId);
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        return map.values().stream()
                .sorted(Comparator.comparing(LocationRecord::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<LocationRecord> getHome(UUID playerId, String name) {
        Map<String, LocationRecord> map = homes.get(playerId);
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(normalize(name)));
    }

    /**
     * Add a home for the player.
     *
     * @param playerId player uuid
     * @param name     home name
     * @param location location snapshot
     * @return true if added successfully, false if name exists or location invalid
     */
    public boolean addHome(UUID playerId, String name, Location3dc location) {
        if (location == null || location.dimension() == null) {
            return false;
        }
        Map<String, LocationRecord> map = homes.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>());
        String key = normalize(name);
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, LocationRecord.from(name, location));
        save();
        return true;
    }

    /**
     * Remove a home for the player.
     *
     * @param playerId player uuid
     * @param name     home name
     * @return true if removed
     */
    public boolean removeHome(UUID playerId, String name) {
        Map<String, LocationRecord> map = homes.get(playerId);
        if (map == null) {
            return false;
        }
        LocationRecord removed = map.remove(normalize(name));
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    private void load() {
        try {
            Files.createDirectories(homeFile.getParent());
            if (!Files.exists(homeFile)) {
                save();
                return;
            }
            String content = Files.readString(homeFile);
            if (content.isBlank()) {
                return;
            }
            Map<String, List<LocationRecord>> loaded = gson.fromJson(content, HOME_DATA_TYPE);
            if (loaded != null) {
                loaded.forEach((uuidStr, list) -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Map<String, LocationRecord> playerHomes = homes.computeIfAbsent(uuid, id -> new ConcurrentHashMap<>());
                        if (list != null) {
                            list.forEach(home -> playerHomes.put(normalize(home.name()), home));
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid UUID entries
                    }
                });
            }
        } catch (Exception e) {
            EssentialsPlugin.getInstance().getPluginLogger().error("Failed to load homes", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(homeFile.getParent());
            Map<String, List<LocationRecord>> serializable = new HashMap<>();
            homes.forEach((uuid, map) -> serializable.put(uuid.toString(), new ArrayList<>(map.values())));

            String json = gson.toJson(serializable, HOME_DATA_TYPE);
            Files.writeString(
                    homeFile,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            EssentialsPlugin.getInstance().getPluginLogger().error("Failed to save homes", e);
        }
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}


