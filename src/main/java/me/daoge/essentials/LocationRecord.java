package me.daoge.essentials;

import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.server.Server;
import org.allaymc.api.world.Dimension;
import org.allaymc.api.world.World;

/**
 * Serializable location data holder for homes and warps.
 *
 * @param name        location name
 * @param worldName   world display name
 * @param dimensionId dimension id within the world
 * @param x           x coordinate
 * @param y           y coordinate
 * @param z           z coordinate
 * @param pitch       pitch
 * @param yaw         yaw
 */
public record LocationRecord(
        String name,
        String worldName,
        int dimensionId,
        double x,
        double y,
        double z,
        double pitch,
        double yaw
) {
    public static LocationRecord from(String name, Location3dc location) {
        Dimension dimension = location.dimension();
        if (dimension == null) {
            throw new IllegalArgumentException("Location dimension is null");
        }
        World world = dimension.getWorld();
        return new LocationRecord(
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
}
