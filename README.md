# Villager Brain Config

Villager Brain Config is a lightweight Minecraft mod that gives server owners, modpack creators, and technical players full control over villager movement penalties (maluses). Fully configure how villagers perceive different terrain types, influence pathfinding, and make villages feel smarter (or dumber) with a config file.

---

## Planned Features

- **Custom block malus values**: Assign movement penalties to any block (e.g., water = 10.0, path = 0.0, soul sand = 4.0)
- **Profession-aware configs**: Create different malus behaviors for different villager professions
- **Cross-mod compatibility**: The mod should work with any modded or vanilla villager professions
- **Simple JSON config system**: Easy to edit, reload, and distribute with modpacks
- **Server-side only**: No need for clients to install it

## Maybe Features

- **Block malus radius**: Assign a malus and a radius to a block, profession specific (eg. Nitwits avoid walking within 4 blocks of a bookshelf or a job site)

---

## Configuration

After first run, the mod will generate a `villager-brain-config.json` file in the `config` folder. You can just create the json without running the mod if you feel you don't need the example. Here's a sample:

```json
{
  "default": {
    "minecraft:water": 10.0,
    "minecraft:soul_sand": 4.0,
    "minecraft:grass_path": 0.0
  },
  "minecraft:farmer": {
    "minecraft:farmland": 0.0,
    "minecraft:composter": 0.5
  }
}
```

- Top-level keys are **namespace:profession** (`default`, `minecraft:farmer`, `librarian`, etc.)
- Inside each object, use block IDs mapped to float malus values
- Higher values mean villagers avoid it more
- Negative values means villagers will not even consider walking on it
- `default` applies to all villagers unless overridden

---

## Compatibility

- Minecraft 1.21.1 planned (more versions down the road)
- Fabric and NeoForge support
- Supports modded villager professions and any modded blocks or tags

---
