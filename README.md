# Personality

A Minecraft 1.21.1 mod (Fabric + NeoForge) that makes wildlife feel more alive.

## Sleep

- Animals sleep at night. Each one has its own bedtime and wake-up time (derived from its UUID), so a herd drifts off one by one.
- Nocturnal animals (cats, ocelots, armadillos) sleep through the day instead.
- Horses, donkeys and mules doze standing up, with their necks hanging low.
- Sleeping animals don't move, look around or make ambient sounds. They fold their legs and lie down, and their eyes close.
- They wake up when hurt, when interacted with, when a player sprints nearby (sneak or walk to let them be), when leashed, picked up by water, or when morning comes.
- Tamed pets that follow you stay up until you go to bed or leave them behind. Pets told to sit will doze off.

### Making other mobs sleep

Sleeping is data-driven, so datapacks and other mods can opt mobs in:

- `data/personality/tags/entity_type/sleeps.json`: mobs that follow a sleep schedule.
- `data/personality/tags/entity_type/nocturnal.json`: sleepers that sleep by day.
- `data/personality/tags/entity_type/sleeps_standing.json`: sleepers that stay on their feet instead of lying down.

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

## Config

Options live in `config/personality.json`, created on first launch with a comment above every option.
It is plain JSON with `//` comments and works the same on Fabric and NeoForge (no config library needed).
Edits apply the next time a world or server starts. Missing options are filled in with defaults and out-of-range
values are clamped.

- `sleep`: turn sleeping on or off, startle radius, how long animals stay awake after being hurt, fed or
  startled, whether pets wait for their owner, and whether sleepers are silent.
- `schedule`: bedtimes and wake-up times (as 24-hour clock hours) for day- and night-active animals, and how
  far each animal's own times may drift.
- `client`: whether sleeping animals lie down and close their eyes.

To add an option in code, declare it in `PersonalityConfig` with `bool`, `integer` or `decimal`, and read it with
`PersonalityConfig.MY_OPTION.get()`.

## Herd panic

- Hit an animal and the whole herd bolts. Every skittish animal within 12 blocks (the one you hit included) runs away
  from you for about 7 seconds, or until it's well clear of you. Arrows and other projectiles count too.
- Sleeping animals nearby wake up and run.
- Tamed pets, tamed horses, mounts with a rider and leashed animals stay put.
- Which animals are skittish is data-driven: `data/personality/tags/entity_type/skittish.json`.

## Fighting back

- Cows, mooshrooms and goats don't take it lying down. Hit one and it charges you and headbutts you (one heart, with a
  shove; goats shove harder) for about 10 seconds, or until you get 20 blocks away. Hitting it again keeps it angry.
- The rest of the herd still bolts; only the animal you hit fights back. Calves and kids run away instead.
- Players in creative or spectator mode are ignored, so the animal just panics as usual.
- Leashed animals don't fight back.
- Which animals fight back is data-driven: `data/personality/tags/entity_type/retaliates.json`.

## Rain

- When it rains, animals head for the nearest cover (under trees, roofs or overhangs), much like zombies and
  skeletons hide from the sun, and stay there until the rain stops.
- Animals can't fall asleep while rain is falling on them. One caught out in the rain at bedtime wakes up and
  looks for cover, then dozes off once it's dry. Animals with no cover nearby stay awake.
- Frogs, axolotls and turtles don't mind the rain. Tamed pets stick with their owner, and leashed or ridden
  animals go where they're taken.
- Data-driven via `data/personality/tags/entity_type/dislikes_rain.json`. Toggles live in `WeatherSettings`
  (`seekShelterInRain`, `noSleepingInRain`, `shelterSearchRadius`, `shelterSpeed`), ready for the config.

## Building

```
./gradlew build
```

Jars land in `fabric/build/libs` and `neoforge/build/libs`. Run the game with `./gradlew :fabric:runClient` or
`./gradlew :neoforge:runClient`.
