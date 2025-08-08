package com.jewey.rosia.common.items;

import com.jewey.rosia.common.container.ModContainerProviders;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ProspectingKitItem extends Item {
    public static final int DEPTH = -30;
    public static final int SHORT_DEPTH = DEPTH / 2;
    public static final int COOLDOWN = 10;
    public int[] grid = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public Item[] ore = new Item[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};

    public ProspectingKitItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        final ItemStack stack = player.getItemInHand(hand);
        final BlockPos pos = player.getOnPos();
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer)
        {
            Helpers.damageItem(player.getItemInHand(hand), 1);
            player.getCooldowns().addCooldown(this, COOLDOWN);
            if (!player.isShiftKeyDown())
            {
                checkArea(level, pos, DEPTH, TFCTags.Blocks.PROSPECTABLE);
            }
            else
            {
                checkArea(level, pos, SHORT_DEPTH, TFCTags.Blocks.PROSPECTABLE);
            }
            ModContainerProviders.PROSPECTING_KIT.openScreen(serverPlayer, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Check a 5x5 area around the player for ore
    public void checkArea(Level level, BlockPos pos, int depth, TagKey<Block> tag)
    {
        BlockPos startPos = pos.offset(-2, 0, -2);
        for(int x = 0; x <= 4; x++)
        {
            for(int z = 0; z <= 4; z++)
            {
                for(int y = 1; y >= depth; y--)
                {
                    Block block = level.getBlockState(startPos.offset(x, y, z)).getBlock();
                    if(Helpers.isBlock(block, tag))
                    {
                        grid[(x + (5 * z))] = 1;
                        ore[(x + (5 * z))] = getItem(block);
                        break;
                    }
                    grid[(x + (5 * z))] = 0;
                    ore[(x + (5 * z))] = null;
                }
            }
        }
    }

    public Item getItem(Block block)
    {
        // graded ore
        if(block.toString().contains("native_copper")) {return TFCBlocks.SMALL_ORES.get(Ore.NATIVE_COPPER).get().asItem();}
        if(block.toString().contains("native_gold")) {return TFCBlocks.SMALL_ORES.get(Ore.NATIVE_GOLD).get().asItem();}
        if(block.toString().contains("hematite")) {return TFCBlocks.SMALL_ORES.get(Ore.HEMATITE).get().asItem();}
        if(block.toString().contains("native_silver")) {return TFCBlocks.SMALL_ORES.get(Ore.NATIVE_SILVER).get().asItem();}
        if(block.toString().contains("cassiterite")) {return TFCBlocks.SMALL_ORES.get(Ore.CASSITERITE).get().asItem();}
        if(block.toString().contains("bismuthinite")) {return TFCBlocks.SMALL_ORES.get(Ore.BISMUTHINITE).get().asItem();}
        if(block.toString().contains("garnierite")) {return TFCBlocks.SMALL_ORES.get(Ore.GARNIERITE).get().asItem();}
        if(block.toString().contains("malachite")) {return TFCBlocks.SMALL_ORES.get(Ore.MALACHITE).get().asItem();}
        if(block.toString().contains("magnetite")) {return TFCBlocks.SMALL_ORES.get(Ore.MAGNETITE).get().asItem();}
        if(block.toString().contains("limonite")) {return TFCBlocks.SMALL_ORES.get(Ore.LIMONITE).get().asItem();}
        if(block.toString().contains("sphalerite")) {return TFCBlocks.SMALL_ORES.get(Ore.SPHALERITE).get().asItem();}
        if(block.toString().contains("tetrahedrite")) {return TFCBlocks.SMALL_ORES.get(Ore.TETRAHEDRITE).get().asItem();}
        // ore
        if(block.toString().contains("bituminous_coal")) {return TFCItems.ORES.get(Ore.BITUMINOUS_COAL).get();}
        if(block.toString().contains("lignite")) {return TFCItems.ORES.get(Ore.LIGNITE).get();}
        if(block.toString().contains("gypsum")) {return TFCItems.ORES.get(Ore.GYPSUM).get();}
        if(block.toString().contains("graphite")) {return TFCItems.ORES.get(Ore.GRAPHITE).get();}
        if(block.toString().contains("sulfur")) {return TFCItems.ORES.get(Ore.SULFUR).get();}
        if(block.toString().contains("cinnabar")) {return TFCItems.ORES.get(Ore.CINNABAR).get();}
        if(block.toString().contains("cryolite")) {return TFCItems.ORES.get(Ore.CRYOLITE).get();}
        if(block.toString().contains("saltpeter")) {return TFCItems.ORES.get(Ore.SALTPETER).get();}
        if(block.toString().contains("sylvite")) {return TFCItems.ORES.get(Ore.SYLVITE).get();}
        if(block.toString().contains("borax")) {return TFCItems.ORES.get(Ore.BORAX).get();}
        if(block.toString().contains("halite")) {return TFCItems.ORES.get(Ore.HALITE).get();}
        // gems
        if(block.toString().contains("amethyst")) {return TFCItems.ORES.get(Ore.AMETHYST).get();}
        if(block.toString().contains("diamond")) {return TFCItems.ORES.get(Ore.DIAMOND).get();}
        if(block.toString().contains("emerald")) {return TFCItems.ORES.get(Ore.EMERALD).get();}
        if(block.toString().contains("lapis_lazuli")) {return TFCItems.ORES.get(Ore.LAPIS_LAZULI).get();}
        if(block.toString().contains("opal")) {return TFCItems.ORES.get(Ore.OPAL).get();}
        if(block.toString().contains("pyrite")) {return TFCItems.ORES.get(Ore.PYRITE).get();}
        if(block.toString().contains("ruby")) {return TFCItems.ORES.get(Ore.RUBY).get();}
        if(block.toString().contains("sapphire")) {return TFCItems.ORES.get(Ore.SAPPHIRE).get();}
        if(block.toString().contains("topaz")) {return TFCItems.ORES.get(Ore.TOPAZ).get();}

        return block.asItem();
    }
}
