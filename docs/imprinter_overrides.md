# Imprinter Overrides (datapack)

Wunderreich auto-generates a "whisper imprinter" recipe for every tradeable enchantment at runtime
(so enchantments from other mods are picked up automatically, with a best-effort cost table). This
auto-generation stays on by default and needs no configuration.

On top of it, a **datapack override layer** lets you patch individual auto-generated imprinters or
disable them entirely. When no override files are present, behaviour is unchanged.

## Folder layout

Override files are normal server datapack data, keyed by the **source enchantment's** id:

```
data/<enchantment_namespace>/wunderreich/imprinter_overrides/<enchantment_path>.json
```

The datapack namespace is the enchantment's namespace, giving a 1:1 enchantment → resource id
mapping. Multiple datapacks that target the same enchantment resolve via the normal datapack order
(last pack loaded wins) — there is no custom merge or priority.

Examples:

| Enchantment            | Override file                                                        |
|------------------------|---------------------------------------------------------------------|
| `minecraft:sharpness`  | `data/minecraft/wunderreich/imprinter_overrides/sharpness.json`      |
| `minecraft:mending`    | `data/minecraft/wunderreich/imprinter_overrides/mending.json`        |
| `somemod:frost`        | `data/somemod/wunderreich/imprinter_overrides/frost.json`            |

Overrides are (re)applied automatically on world load and on `/reload`.

## Setting up the data pack

Override files live in a normal data pack. If you don't already have one, create:

```
<world>/datapacks/<your_pack>/
  pack.mcmeta
  data/minecraft/wunderreich/imprinter_overrides/thorns.json
```

`pack.mcmeta` (the data pack format for Minecraft 26.1 is `101`):

```json
{
  "pack": {
    "description": "My imprinter overrides",
    "pack_format": 101,
    "min_format": 101,
    "max_format": 101
  }
}
```

After adding or editing files, run `/reload` (or re-enter the world). On success the log shows a
line like `Loaded N imprinter override(s)`.

## JSON format

Every field is optional. A field that is present replaces the auto-generated value; an absent field
keeps the auto-generated value (partial merge).

| Field      | Type       | Meaning                                                                       |
|------------|------------|-------------------------------------------------------------------------------|
| `input`    | ItemStack  | The item + count the player must supply — this is the imprint / trade **cost**.|
| `output`   | ItemStack  | The trained whisperer produced by the imprinter.                              |
| `baseXP`   | int        | Base experience granted / used for the resulting trade.                       |
| `icon`     | ItemStack  | The icon shown in the imprinter UI / recipe viewers (REI/EMI/wthit).          |
| `enabled`  | boolean    | `false` suppresses this auto-generated imprinter entirely.                    |
| `disabled` | boolean    | `true` also suppresses it (alias of `"enabled": false`).                      |

`ItemStack` values use the vanilla item-stack JSON form: `{ "id": "<item id>", "count": <n>, ... }`
(`count` defaults to 1; data components may be supplied under `"components"` as usual).

### Example — patch the cost, XP and icon of Sharpness

`data/minecraft/wunderreich/imprinter_overrides/sharpness.json`

```json
{
  "input": { "id": "minecraft:diamond", "count": 3 },
  "baseXP": 42,
  "icon": { "id": "minecraft:netherite_sword" }
}
```

### Example — disable the Mending imprinter

`data/minecraft/wunderreich/imprinter_overrides/mending.json`

```json
{
  "enabled": false
}
```

## Notes

- The override only affects the **auto-generated** imprinter for that enchantment; the rest of the
  auto-generation (all other enchantments) is untouched.
- Disabling is decided before the recipe is built, so a disabled imprinter never appears in the game
  or in recipe viewers, and is never synced to clients.
- Overridden `ItemStack`s are resolved late (when the value is first needed), so overriding an item
  from another mod works as long as that mod is present.
