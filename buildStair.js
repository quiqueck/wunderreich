const path = require("path")
const fs = require("fs")

const colors = [
    'white', 'light_gray', 'gray', 'black', 
    'brown', 
    'red', 'orange', 'yellow', 'lime', 'green', 'cyan', 'light_blue', 'blue', 'purple', 'magenta', 'pink']

const colorsEN = [
        'White', 'Light gray', 'Gray', 'Black', 
        'Brown', 
        'Red', 'Orange', 'Yellow', 'Lime', 'Green', 'Cyan', 'Light blue', 'Blue', 'Purple', 'Magenta', 'Pink']
const colorsDE = [
    'Weiße', 'Hellgraue', 'Graue', 'Schwarze', 
    'Braune', 
    'Rote', 'Orange', 'Gelbe', 'Hellgrüne', 'Grüne', 'Türkise', 'Hellblaue', 'Blaue', 'Violette', 'Magenta', 'Pinke']
const type = "wool"
const nameEN = "Wool Stairs"
const nameDE = "Wolltreppe"
colors.forEach(color => {
    const blockName = color+'_'+type
    const basePath = "./src/main/resources/assets/wunderreich";

    const blockState = `{
  "variants": {
    "facing=east,half=bottom,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 270,
        "uvlock": true
    },
    "facing=east,half=bottom,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner"
    },
    "facing=east,half=bottom,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 270,
        "uvlock": true
    },
    "facing=east,half=bottom,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer"
    },
    "facing=east,half=bottom,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs"
    },
    "facing=east,half=top,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "uvlock": true
    },
    "facing=east,half=top,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 90,
        "uvlock": true
    },
    "facing=east,half=top,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "uvlock": true
    },
    "facing=east,half=top,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 90,
        "uvlock": true
    },
    "facing=east,half=top,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "x": 180,
        "uvlock": true
    },
    "facing=north,half=bottom,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 180,
        "uvlock": true
    },
    "facing=north,half=bottom,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 270,
        "uvlock": true
    },
    "facing=north,half=bottom,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 180,
        "uvlock": true
    },
    "facing=north,half=bottom,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 270,
        "uvlock": true
    },
    "facing=north,half=bottom,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "y": 270,
        "uvlock": true
    },
    "facing=north,half=top,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 270,
        "uvlock": true
    },
    "facing=north,half=top,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "uvlock": true
    },
    "facing=north,half=top,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 270,
        "uvlock": true
    },
    "facing=north,half=top,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "uvlock": true
    },
    "facing=north,half=top,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "x": 180,
        "y": 270,
        "uvlock": true
    },
    "facing=south,half=bottom,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner"
    },
    "facing=south,half=bottom,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 90,
        "uvlock": true
    },
    "facing=south,half=bottom,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer"
    },
    "facing=south,half=bottom,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 90,
        "uvlock": true
    },
    "facing=south,half=bottom,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "y": 90,
        "uvlock": true
    },
    "facing=south,half=top,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 90,
        "uvlock": true
    },
    "facing=south,half=top,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 180,
        "uvlock": true
    },
    "facing=south,half=top,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 90,
        "uvlock": true
    },
    "facing=south,half=top,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 180,
        "uvlock": true
    },
    "facing=south,half=top,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "x": 180,
        "y": 90,
        "uvlock": true
    },
    "facing=west,half=bottom,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 90,
        "uvlock": true
    },
    "facing=west,half=bottom,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "y": 180,
        "uvlock": true
    },
    "facing=west,half=bottom,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 90,
        "uvlock": true
    },
    "facing=west,half=bottom,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "y": 180,
        "uvlock": true
    },
    "facing=west,half=bottom,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "y": 180,
        "uvlock": true
    },
    "facing=west,half=top,shape=inner_left": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 180,
        "uvlock": true
    },
    "facing=west,half=top,shape=inner_right": {
        "model": "wunderreich:block/${blockName}_stairs_inner",
        "x": 180,
        "y": 270,
        "uvlock": true
    },
    "facing=west,half=top,shape=outer_left": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 180,
        "uvlock": true
    },
    "facing=west,half=top,shape=outer_right": {
        "model": "wunderreich:block/${blockName}_stairs_outer",
        "x": 180,
        "y": 270,
        "uvlock": true
    },
    "facing=west,half=top,shape=straight": {
        "model": "wunderreich:block/${blockName}_stairs",
        "x": 180,
        "y": 180,
        "uvlock": true
    }
  }
}`

    const modelBase = `{
    "parent": "minecraft:block/stairs",
    "textures": {
        "bottom": "minecraft:block/${blockName}",
        "top": "minecraft:block/${blockName}",
        "side": "minecraft:block/${blockName}"
    }
}`

    const modelInner = `{
    "parent": "minecraft:block/inner_stairs",
    "textures": {
        "bottom": "minecraft:block/${blockName}",
        "top": "minecraft:block/${blockName}",
        "side": "minecraft:block/${blockName}"
    }
}`

    const modelOuter = `{
    "parent": "minecraft:block/outer_stairs",
    "textures": {
        "bottom": "minecraft:block/${blockName}",
        "top": "minecraft:block/${blockName}",
        "side": "minecraft:block/${blockName}"
    }
}`

    const item = `{
        "parent": "wunderreich:block/${blockName}_stairs"
    }`
    const BLOCK_NAME = blockName.toUpperCase();
    const register = `public static final Block ${BLOCK_NAME}_STAIRS = registerStairs("${blockName}_stairs",
    Blocks.${BLOCK_NAME},
    StairBlock::new,
    Configs.MAIN.addStairs.get());`

    fs.writeFileSync(path.join(basePath, "blockstates", blockName+"_stairs.json"), blockState);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_stairs_inner.json"), modelInner);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_stairs_outer.json"), modelOuter);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_stairs.json"), modelBase);
    fs.writeFileSync(path.join(basePath, "models/item", blockName+"_stairs.json"), item);

    console.log(register);
})

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_stairs": "${colorsEN[i]} ${nameEN}",`)
}

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_stairs": "${colorsDE[i]} ${nameDE}",`)
}