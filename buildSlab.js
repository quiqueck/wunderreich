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
const type = "terracotta"
const nameEN = "Terracotta Slab"
const nameDE = "Keramikstufe"
colors.forEach(color => {
    const blockName = color+'_'+type
    const basePath = "./src/main/resources/assets/wunderreich";

    const blockState = `{
        "variants": {
            "type=bottom": {
                "model": "wunderreich:block/${blockName}_slab"
            },
            "type=double": {
                "model": "block/${blockName}"
            },
            "type=top": {
                "model": "wunderreich:block/${blockName}_top"
            }
        }
    }`

    const modelTop = `{
        "parent": "minecraft:block/slab_top",
        "textures": {
            "bottom": "minecraft:block/${blockName}",
            "top": "minecraft:block/${blockName}",
            "side": "minecraft:block/${blockName}"
        }
    }`

    const modelBottom = `{    
        "parent": "minecraft:block/slab",
        "textures": {
            "particle": "block/${blockName}",        
            "bottom": "minecraft:block/${blockName}",
            "top": "minecraft:block/${blockName}",
            "side": "minecraft:block/${blockName}"
        }    
    }`

    const item = `{
        "parent": "wunderreich:block/${blockName}_slab"
    }`
    const BLOCK_NAME = blockName.toUpperCase();
    const register = `public static final Block ${BLOCK_NAME}_SLAB = registerSlab("${blockName}_slab",
    Blocks.${BLOCK_NAME},
    ConcreteSlabBlock::new,
    Configs.MAIN.addDirtSlabs.get());`

    fs.writeFileSync(path.join(basePath, "blockstates", blockName+"_slab.json"), blockState);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_slab.json"), modelBottom);
    fs.writeFileSync(path.join(basePath, "models/block", blockName+"_slab_top.json"), modelTop);
    fs.writeFileSync(path.join(basePath, "textures/block", blockName+"_slab.json"), blockState);
    fs.writeFileSync(path.join(basePath, "models/item", blockName+"_slab.json"), item);

    console.log(register);
})

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_slab": "${colorsEN[i]} ${nameEN}",`)
}

console.log()
for (let i=0; i<colors.length; i++){
    let color = colors[i]
    const blockName = color+'_'+type
    console.log(`"block.wunderreich.${blockName}_slab": "${colorsDE[i]} ${nameDE}",`)
}