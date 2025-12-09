package me.daoge.essentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.api.world.World;

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
    private static final Type HOME_DATA_TYPE = new TypeToken<Map<String, List<HomePoint>>>() {
    }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path homeFile;
    private final Map<UUID, Map<String, HomePoint>> homes = new ConcurrentHashMap<>();

    public HomeManager(Path dataFolder) {
        this.homeFile = dataFolder.resolve(HOME_FILE_NAME);
        load();
    }

    /**
     * @return immutable sorted list of a player's homes (case-insensitive by name)
     */
    public List<HomePoint> getSortedHomes(UUID playerId) {
        Map<String, HomePoint> map = homes.get(playerId);
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        return map.values().stream()
                .sorted(Comparator.comparing(HomePoint::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<HomePoint> getHome(UUID playerId, String name) {
        Map<String, HomePoint> map = homes.get(playerId);
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
        Map<String, HomePoint> map = homes.computeIfAbsent(playerId, id -> new ConcurrentHashMap<>());
        String key = normalize(name);
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, HomePoint.from(name, location));
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
        Map<String, HomePoint> map = homes.get(playerId);
        if (map == null) {
            return false;
        }
        HomePoint removed = map.remove(normalize(name));
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
            Map<String, List<HomePoint>> loaded = gson.fromJson(content, HOME_DATA_TYPE);
            if (loaded != null) {
                loaded.forEach((uuidStr, list) -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Map<String, HomePoint> playerHomes = homes.computeIfAbsent(uuid, id -> new ConcurrentHashMap<>());
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
            Map<String, List<HomePoint>> serializable = new HashMap<>();
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

    /**
     * Serializable home data holder.
     *
     * @param name        home name
     * @param worldName   world display name
     * @param dimensionId dimension id within the world
     * @param x           x coordinate
     * @param y           y coordinate
     * @param z           z coordinate
     * @param pitch       pitch
     * @param yaw         yaw
     */
    public record HomePoint(
            String name,
            String worldName,
            int dimensionId,
            double x,
            double y,
            double z,
            double pitch,
            double yaw
    ) {
        public Location3d toLocation() {
            World world = Server.getInstance().getWorldPool().getWorld(worldName);
            if (world == null) {
                return null;
            }
            Dimension dimension = world.getDimension(dimensionId);
            if (dimension == null) {
                return null;
            }
            return new Location3d(x, y, z, pitch, yaw, dimension);
        }

        public static HomePoint from(String name, Location3dc location) {
            Dimension dimension = location.dimension();
            if (dimension == null) {
                throw new IllegalArgumentException("Location dimension is null");
            }
            World world = dimension.getWorld();
            return new HomePoint(
                    name,
                    world.getWorldData().getDisplayName(),
                    dimension.getDimensionInfo().dimensionId(),
                    location.x(),
                    location.y(),
                    location.z(),
                    location.pitch(),
                    location.yaw()
            );
        }
    }
}


