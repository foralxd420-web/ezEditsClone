package com.ezedits.clone;

import com.ezedits.clone.noise.NoiseGenerator;
import com.ezedits.clone.palettes.PaletteManager;
import com.ezedits.clone.spline.SplineCalculator;
import com.ezedits.clone.texture.TextureEngine;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EzEditsPlugin extends JavaPlugin implements CommandExecutor {

    private final PaletteManager paletteManager = new PaletteManager();
    private final Map<UUID, List<BlockVector3>> splinePoints = new HashMap<>();

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("ezpalette")).setExecutor(this);
        Objects.requireNonNull(getCommand("eznoise")).setExecutor(this);
        Objects.requireNonNull(getCommand("ezspline")).setExecutor(this);
        Objects.requireNonNull(getCommand("eztexture")).setExecutor(this);
        getLogger().info("ezEditsClone zostal pomyslnie wlaczony!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player bPlayer)) {
            sender.sendMessage("Ta komenda jest przeznaczona tylko dla graczy!");
            return true;
        }

        Player actor = BukkitAdapter.adapt(bPlayer);
        World world = BukkitAdapter.adapt(bPlayer.getWorld());

        // --- OBSŁUGA /EZPALETTE (/EZP) ---
        if (cmd.getName().equalsIgnoreCase("ezpalette")) {
            if (args.length < 2) {
                bPlayer.sendMessage(ChatColor.RED + "Uzycie: /ezp <create|add|apply> <nazwa_palety> [waga]");
                return true;
            }
            String sub = args[0].toLowerCase();
            String pName = args[1].toLowerCase();

            if (sub.equals("create")) {
                paletteManager.createPalette(pName);
                bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Stworzono palete: " + pName);
            } else if (sub.equals("add")) {
                double weight = args.length > 2 ? Double.parseDouble(args[2]) : 1.0;
                BlockState handBlock = BukkitAdapter.adapt(bPlayer.getInventory().getItemInMainHand().getType()).getDefaultState();
                if (paletteManager.addBlock(pName, handBlock, weight)) {
                    bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Dodano blok do palety " + pName);
                } else {
                    bPlayer.sendMessage(ChatColor.RED + "[ezEdits] Paleta nie istnieje!");
                }
            } else if (sub.equals("apply")) {
                applyPaletteToSelection(actor, world, pName, bPlayer);
            }
            return true;
        }

        // --- OBSŁUGA /EZNOISE ---
        if (cmd.getName().equalsIgnoreCase("eznoise")) {
            if (args.length < 2) {
                bPlayer.sendMessage(ChatColor.RED + "Uzycie: /eznoise <skala> <nazwa_palety>");
                return true;
            }
            double scale = Double.parseDouble(args[0]);
            String pName = args[1].toLowerCase();
            applyNoiseToSelection(actor, world, scale, pName, bPlayer);
            return true;
        }

        // --- OBSŁUGA /EZSPLINE ---
        if (cmd.getName().equalsIgnoreCase("ezspline")) {
            if (args.length < 1) {
                bPlayer.sendMessage(ChatColor.RED + "Uzycie: /ezspline <addpos|clear|render> [nazwa_palety]");
                return true;
            }
            String sub = args[0].toLowerCase();
            UUID id = bPlayer.getUniqueId();
            splinePoints.putIfAbsent(id, new ArrayList<>());

            if (sub.equals("addpos")) {
                BlockVector3 loc = BlockVector3.at(bPlayer.getLocation().getBlockX(), bPlayer.getLocation().getBlockY(), bPlayer.getLocation().getBlockZ());
                splinePoints.get(id).add(loc);
                bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Dodano punkt Spline (" + splinePoints.get(id).size() + ")");
            } else if (sub.equals("clear")) {
                splinePoints.get(id).clear();
                bPlayer.sendMessage(ChatColor.YELLOW + "[ezEdits] Wyczyszczono punkty Spline.");
            } else if (sub.equals("render")) {
                if (args.length < 2) {
                    bPlayer.sendMessage(ChatColor.RED + "Podaj nazwe palety do renderowania!");
                    return true;
                }
                renderSpline(world, splinePoints.get(id), args[1].toLowerCase(), bPlayer);
            }
            return true;
        }

        // --- OBSŁUGA /EZTEXTURE (/EZT) ---
        if (cmd.getName().equalsIgnoreCase("eztexture")) {
            if (args.length < 2) {
                bPlayer.sendMessage(ChatColor.RED + "Uzycie: /eztexture <sunlight|height> <nazwa_palety>");
                return true;
            }
            String sub = args[0].toLowerCase();
            String pName = args[1].toLowerCase();

            try {
                Region selection = WorldEdit.getInstance().getSessionManager().get(actor).getSelection(world);
                try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
                    int count = 0;
                    if (sub.equals("sunlight")) {
                        count = TextureEngine.applySunlightTexture(session, selection, world, paletteManager, pName);
                        bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Nałożono teksturę światła słonecznego na " + count + " bloków!");
                    } else if (sub.equals("height")) {
                        count = TextureEngine.applyHeightTexture(session, selection, paletteManager, pName);
                        bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Nałożono teksturę wysokościową na " + count + " bloków!");
                    }
                }
            } catch (Exception e) {
                bPlayer.sendMessage(ChatColor.RED + "Zaznacz najpierw obszar WorldEdit!");
            }
            return true;
        }

        return false;
    }

    private void applyPaletteToSelection(Player actor, World world, String pName, org.bukkit.entity.Player bPlayer) {
        try {
            Region selection = WorldEdit.getInstance().getSessionManager().get(actor).getSelection(world);
            try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
                for (BlockVector3 pt : selection) {
                    session.setBlock(pt, paletteManager.getRandomBlock(pName));
                }
                bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Zastosowano palete na zaznaczeniu!");
            }
        } catch (Exception e) {
            bPlayer.sendMessage(ChatColor.RED + "Bledne lub brakujace zaznaczenie WorldEdit!");
        }
    }

    private void applyNoiseToSelection(Player actor, World world, double scale, String pName, org.bukkit.entity.Player bPlayer) {
        try {
            Region selection = WorldEdit.getInstance().getSessionManager().get(actor).getSelection(world);
            try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
                for (BlockVector3 pt : selection) {
                    double noise = NoiseGenerator.getNoise3D(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), scale);
                    if (noise > 0.55) {
                        session.setBlock(pt, paletteManager.getRandomBlock(pName));
                    }
                }
                bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Wygenerowano strukture szumu!");
            }
        } catch (Exception e) {
            bPlayer.sendMessage(ChatColor.RED + "Bledne lub brakujace zaznaczenie WorldEdit!");
        }
    }

    private void renderSpline(World world, List<BlockVector3> points, String pName, org.bukkit.entity.Player bPlayer) {
        List<BlockVector3> line = SplineCalculator.calculateLine(points);
        if (line.isEmpty()) {
            bPlayer.sendMessage(ChatColor.RED + "Dodaj najpierw minimum 2 punkty za pomoca /ezspline addpos");
            return;
        }
        try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
            for (BlockVector3 pt : line) {
                session.setBlock(pt, paletteManager.getRandomBlock(pName));
            }
            bPlayer.sendMessage(ChatColor.GREEN + "[ezEdits] Wygenerowano linie Spline (" + line.size() + " blokow)!");
        } catch (Exception e) {
            bPlayer.sendMessage(ChatColor.RED + "Blad podczas generowania linii!");
        }
    }
}
