package com.ezedits.clone.texture;

import com.ezedits.clone.palettes.PaletteManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TextureEngine {

    public static int applySunlightTexture(EditSession session, Region region, World world, PaletteManager paletteManager, String paletteName) {
        int changed = 0;
        for (BlockVector3 pt : region) {
            BlockVector3 above = pt.add(0, 1, 0);
            if (world.getBlock(above).getBlockType().equals(BlockTypes.AIR)) {
                session.setBlock(pt, paletteManager.getRandomBlock(paletteName));
                changed++;
            }
        }
        return changed;
    }

    public static int applyHeightTexture(EditSession session, Region region, PaletteManager paletteManager, String paletteName) {
        int changed = 0;
        int minY = region.getMinimumPoint().getBlockY();
        int maxY = region.getMaximumPoint().getBlockY();
        int heightRange = Math.max(1, maxY - minY);

        for (BlockVector3 pt : region) {
            double relativeHeight = (double) (pt.getBlockY() - minY) / heightRange;
            if (relativeHeight > 0.5) {
                session.setBlock(pt, paletteManager.getRandomBlock(paletteName));
                changed++;
            }
        }
        return changed;
    }

    /**
     * Teksturowanie widoku (View/Player-facing)
     * Nakłada teksturę tylko na bloki, do których można poprowadzić czystą linię wzroku od gracza.
     */
    public static int applyViewTexture(EditSession session, Region region, World weWorld, org.bukkit.World bukkitWorld, Player bPlayer, PaletteManager paletteManager, String paletteName) {
        int changed = 0;
        Location eyeLoc = bPlayer.getEyeLocation();
        Vector eyeVec = eyeLoc.toVector();

        for (BlockVector3 pt : region) {
            // Zamiana współrzędnych WorldEdita na środek bloku z Bukkita
            Vector blockVec = new Vector(pt.getBlockX() + 0.5, pt.getBlockY() + 0.5, pt.getBlockZ() + 0.5);
            Vector direction = blockVec.clone().subtract(eyeVec);
            double distance = direction.length();
            
            // Pomijamy bloki, w których stoi gracz (aby uniknąć błędów promienia)
            if (distance < 1.0) continue; 
            
            direction.normalize();

            // Rzucamy wirtualny promień od oczu gracza w stronę sprawdzanego bloku
            boolean hasLineOfSight = true;
            
            // Krokujemy co 0.5 bloku w stronę celu, by sprawdzić czy coś zasłania
            for (double d = 0; d < distance - 0.6; d += 0.5) {
                Vector checkVec = eyeVec.clone().add(direction.clone().multiply(d));
                org.bukkit.block.Block b = bukkitWorld.getBlockAt(checkVec.getBlockX(), checkVec.getBlockY(), checkVec.getBlockZ());
                
                // Jeśli natrafimy na solidny blok przed celem, to ten blok jest zasłonięty!
                if (b.getType().isSolid()) {
                    hasLineOfSight = false;
                    break;
                }
            }

            // Jeśli promień doleciał bez przeszkód, blok widzi gracza - nakładamy teksturę
            if (hasLineOfSight) {
                session.setBlock(pt, paletteManager.getRandomBlock(paletteName));
                changed++;
            }
        }
        return changed;
    }
}
