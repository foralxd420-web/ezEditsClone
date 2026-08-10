package com.ezedits.clone.palettes;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.Map;

public class PaletteManager {
    
    // Słownik przechowujący palety: Nazwa Palety -> (Typ Bloku -> Waga/Szansa)
    private final Map<String, Map<BlockState, Double>> palettes = new HashMap<>();

    // Tworzenie nowej pustej palety
    public void createPalette(String name) {
        palettes.put(name.toLowerCase(), new HashMap<>());
    }

    // Dodawanie bloku z wagą do palety
    public boolean addBlock(String name, BlockState block, double weight) {
        String key = name.toLowerCase();
        if (!palettes.containsKey(key)) {
            return false;
        }
        palettes.get(key).put(block, weight);
        return true;
    }

    // Pobieranie losowego bloku z palety na podstawie wyliczonej wagi
    public BlockState getRandomBlock(String name) {
        Map<BlockState, Double> palette = palettes.get(name.toLowerCase());
        
        // Jeśli paleta nie istnieje lub jest pusta, zwróć zwykły kamień
        if (palette == null || palette.isEmpty()) {
            return BlockTypes.STONE.getDefaultState();
        }

        double totalWeight = palette.values().stream().mapToDouble(Double::doubleValue).sum();
        double random = Math.random() * totalWeight;
        double countWeight = 0.0;

        for (Map.Entry<BlockState, Double> entry : palette.entrySet()) {
            countWeight += entry.getValue();
            if (random <= countWeight) {
                return entry.getKey();
            }
        }
        return palette.keySet().iterator().next();
    }

    public boolean hasPalette(String name) {
        return palettes.containsKey(name.toLowerCase());
    }

    public Map<String, Map<BlockState, Double>> getPalettes() {
        return palettes;
    }
}
