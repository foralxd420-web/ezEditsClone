package com.ezedits.clone.texture;

import com.ezedits.clone.palettes.PaletteManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;

public class TextureEngine {

    /**
     * Teksturowanie światłem słonecznym (Sunlight)
     * Zmienia tylko bloki, które mają bezpośredni dostęp do nieba (brak bloków nad nimi).
     */
    public static int applySunlightTexture(EditSession session, Region region, World world, PaletteManager paletteManager, String paletteName) {
        int changed = 0;
        for (BlockVector3 pt : region) {
            // Sprawdzamy czy blok wyżej to powietrze/brak przeszkody
            BlockVector3 above = pt.add(0, 1, 0);
            if (world.getBlock(above).getBlockType().equals(BlockTypes.AIR)) {
                session.setBlock(pt, paletteManager.getRandomBlock(paletteName));
                changed++;
            }
        }
        return changed;
    }

    /**
     * Teksturowanie wysokościowe (Height/Gradient)
     * Zmienia teksturę w zależności od poziomu Y w zaznaczeniu.
     */
    public static int applyHeightTexture(EditSession session, Region region, PaletteManager paletteManager, String paletteName) {
        int changed = 0;
        int minY = region.getMinimumPoint().getBlockY();
        int maxY = region.getMaximumPoint().getBlockY();
        int heightRange = Math.max(1, maxY - minY);

        for (BlockVector3 pt : region) {
            double relativeHeight = (double) (pt.getBlockY() - minY) / heightRange;
            
            // Górna połowa zaznaczenia otrzymuje teksturę z palety
            if (relativeHeight > 0.5) {
                session.setBlock(pt, paletteManager.getRandomBlock(paletteName));
                changed++;
            }
        }
        return changed;
    }
}
