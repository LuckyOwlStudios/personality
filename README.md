# Personality

A Minecraft 1.21.1 mod (Fabric + NeoForge) that makes wildlife feel more alive.

## Sleep

- Animals sleep at night. Each one has its own bedtime and wake-up time (derived from its UUID), so a herd drifts off one by one.
- Nocturnal animals (cats, ocelots, armadillos) sleep through the day instead.
- Sleeping animals don't move, look around or make ambient sounds. They fold their legs and lie down, and their eyes close.
- They wake up when hurt, when interacted with, when a player sprints nearby (sneak or walk to let them be), when leashed, picked up by water, or when morning comes.
- Tamed pets that follow you stay up until you go to bed or leave them behind. Pets told to sit will doze off.

### Making other mobs sleep

Sleeping is data-driven, so datapacks and other mods can opt mobs in:

- `data/personality/tags/entity_type/sleeps.json`: mobs that follow a sleep schedule.
- `data/personality/tags/entity_type/nocturnal.json`: sleepers that sleep by day.

### Closed eyes

Eyelids are painted at runtime from a small definition per mob, at
`assets/<namespace>/personality/eyelids/<entity>.json`:

```json
{
  "texture_width": 64,
  "texture_height": 32,
  "eyes": [[6, 8, 2, 2], [12, 8, 2, 2]]
}
```

Each eye is `[x, y, width, height]` in texture pixels. The mod copies the skin colour from just above each eye
and adds a darker lash line along the bottom, so every texture variant (cat breeds, horse coats, resource packs,
HD textures) gets matching eyelids without extra art. Optional: `"lash_shade"` (default `0.6`).

The lying-down pose is generic too: any model whose leg parts follow vanilla naming (`left_front_leg`,
`right_hind_leg`, `left_leg`, ...) folds its legs and lowers onto the ground.

## Building

```
./gradlew build
```

Jars land in `fabric/build/libs` and `neoforge/build/libs`. Run the game with `./gradlew :fabric:runClient` or
`./gradlew :neoforge:runClient`.
