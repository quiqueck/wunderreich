const path = require("path")
const fs = require("fs")

// const colors = [
//     'white', 'light_gray', 'gray', 'black', 
//     'brown', 
//     'red', 'orange', 'yellow', 'lime', 'green', 'cyan', 'light_blue', 'blue', 'purple', 'magenta', 'pink']

// const colorsEN = [
//         'White', 'Light gray', 'Gray', 'Black', 
//         'Brown', 
//         'Red', 'Orange', 'Yellow', 'Lime', 'Green', 'Cyan', 'Light blue', 'Blue', 'Purple', 'Magenta', 'Pink']
// const colorsDE = [
//     'Weiße', 'Hellgraue', 'Graue', 'Schwarze', 
//     'Braune', 
//     'Rote', 'Orange', 'Gelbe', 'Hellgrüne', 'Grüne', 'Türkise', 'Hellblaue', 'Blaue', 'Violette', 'Magenta', 'Pinke']

    const colors = [
        'acacia', 'birch', 'oak', 'dark_oak', 
        'jungle', 
        'crimson', 'warped', 'spruce', 'mangrove']
    
    const colorsEN = [
            'Acacia', 'Birch', 'Oak', 'Dark Oak', 
            'Jungle Wood', 
            'Crimson', 'Warped', "Spruce", "Mangrove"]
    const colorsDE = [
        'Akazienholz', 'Birkenholz', 'Eichenholz', 'Schwarzeichenholz', 
        'Tropenholz', 
        'Karmesin', 'Wirr', "Fichtenholz", 'Mangrovenholz']
const type = "terracotta"
const nameEN = "Terrakotta Wall"
const nameDE = "Keramikmauer"
colors.forEach(color => {
    const blockName = type==''?color:(color+'_'+type)
    const basePath = "./src/main/resources/assets/wunderreich";

    const blockState = `{
        "multipart": [
          {
            "when": {
              "up": "true"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_post"
            }
          },
          {
            "when": {
              "north": "low"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side",
              "uvlock": true
            }
          },
          {
            "when": {
              "east": "low"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side",
              "y": 90,
              "uvlock": true
            }
          },
          {
            "when": {
              "south": "low"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side",
              "y": 180,
              "uvlock": true
            }
          },
          {
            "when": {
              "west": "low"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side",
              "y": 270,
              "uvlock": true
            }
          },
          {
            "when": {
              "north": "tall"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side_tall",
              "uvlock": true
            }
          },
          {
            "when": {
              "east": "tall"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side_tall",
              "y": 90,
              "uvlock": true
            }
          },
          {
            "when": {
              "south": "tall"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side_tall",
              "y": 180,
              "uvlock": true
            }
          },
          {
            "when": {
              "west": "tall"
            },
            "apply": {
              "model": "wunderreich:block/${blockName}_wall_side_tall",
              "y": 270,
              "uvlock": true
            }
          }
        ]
      }`

    const modelInventory = `{
  "parent": "minecraft:block/wall_inventory",
  "textures": {
    "wall": "minecraft:block/${blockName}"
  }
}`

    const modelPost = `{
    "parent": "minecraft:block/template_wall_post",
    "textures": {
        "wall": "minecraft:block/${blockName}"
    }
}`
    const modelSideTall = `{
    "parent": "minecraft:block/template_wall_side_tall",
    "textures": {
        "wall": "minecraft:block/${blockName}"
    }
}`
    const modelSide = `{
    "parent": "minecraft:block/template_wall_side",
    "textures": {
        "wall": "minecraft:block/${blockName}"
    }
}`

    

    const item = `{
        "parent": "wunderreich:block/${blockName}_wall_inventory"
    }`
    const BLOCK_NAME = blockName.toUpperCase();
    const register = `public static final Block ${BLOCK_NAME}_WALL = registerWall("${blockName}_wall",
    Blocks.${BLOCK_NAME},
    WallBlock::new,
    Configs.MAIN.addWalls.get());`

    fs.writeFileSync(path.join(basePath, "blockstates", blockName+"_wall.json"), blockState);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_wall_inventory.json"), modelInventory);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_wall_post.json"), modelPost);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_wall_side_tall.json"), modelSideTall);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_wall_side.json"), modelSide);
    fs.writeFileSync(path.join(basePath, "models/item", blockName+"_wall.json"), item);

    console.log(register);
})

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_wall": "${colorsEN[i]} ${nameEN}",`)
}

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_wall": "${colorsDE[i]} ${nameDE}",`)
}