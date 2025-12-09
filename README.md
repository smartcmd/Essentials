# Essentials ✨

A basic Essentials plugin built on top of Allay. 🧰

It provides common utility commands and simple event listeners to help with day-to-day server use.

## Features 🚀

- `ping`: view your current network latency 📡
- `back`: return to your last death location or position before a teleport 🧭
- `tpa`: send teleport requests between players and accept/deny them 🔗
- `warp`: manage named warp points (tp/add/remove/list) 🗺️
- `home`: manage personal homes (tp/add/remove) 🏠

> See "Commands & Permissions" below for details.

## Configuration ⚙️

The plugin uses a `config.yml` file located in the `plugins/Essentials/` directory. On first run, it will automatically create a default configuration file.

### config.yml Structure

```yaml
# Essentials Plugin Configuration
# Control which features are enabled

# Feature toggles - Set to false to disable a feature
features:
  # Enable /ping command - Shows player's network latency
  ping: true

  # Enable /back command - Teleport back to death location
  back: true

  # Enable /tpa command - Request to teleport to another player
  tpa: true

  # Enable /home commands - Manage personal home locations
  # Sub-commands: tp, add, remove, list
  home: true

  # Enable /warp commands - Manage server-wide warp points
  # Sub-commands: tp, add, remove, list
  warp: true
```

### How to Disable Features

To disable a feature, simply set its value to `false` in the config file:

```yaml
features:
  ping: false  # Disables the /ping command
  back: true   # Keeps /back enabled
```

After modifying the config, restart the server for changes to take effect.

## Commands & Permissions 🔐

- `ping` 📡
  - Description: Display your current latency.
  - Permission: `essentials.ping`

- `back` 🧭
  - Description: Return to your last key position (e.g., death point or the position before teleport).
  - Permission: `essentials.back`

- `tpa` 🔗
  - Description: Send a teleport request to another player, with accept/deny subcommands (if implemented).
  - Permission: `essentials.tpa`

- `warp` 🗺️
  - Description: Manage and travel to named warp points.
  - Subcommands:
    - `warp tp`: open a form to choose a warp to teleport to.
      - Permission: `essentials.command.warp.tp`
    - `warp add`: create a warp at your current location.
      - Permission: `essentials.command.warp.add`
    - `warp remove`: delete an existing warp via form selection.
      - Permission: `essentials.command.warp.remove`
    - `warp list`: list all available warp names. 📋
      - Permission: `essentials.command.warp.list`

- `home` 🏠
  - Description: Manage and travel to your own homes.
  - Subcommands:
    - `home tp`: open a form listing your homes to teleport to.
      - Permission: `essentials.command.home.tp`
    - `home add`: create a home at your current location.
      - Permission: `essentials.command.home.add`
    - `home remove`: delete one of your homes via form selection.
      - Permission: `essentials.command.home.remove`
    - `home list`: list your home names. 📋
      - Permission: `essentials.command.home.list`

## License 📜

This project is open-sourced under the MIT license. See [LICENSE](LICENSE) for details.