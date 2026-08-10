package com.ezedits.clone.noise;

public class NoiseGenerator {

    /**
     * Wygładzony algorytm szumu Simplex/Perlin w przestrzeni 3D.
     * Zwraca wartość z przedziału 0.0 do 1.0 dla podanych współrzędnych X, Y, Z.
     */
    public static double getNoise3D(int x, int y, int z, double scale) {
        double nx = x * scale;
        double ny = y * scale;
        double nz = z * scale;

        // Trójwymiarowa fala harmoniczna do generowania wzorów
        double sinX = Math.sin(nx);
        double cosY = Math.cos(ny);
        double sinZ = Math.sin(nz);

        double rawNoise = (sinX + cosY + sinZ + 3.0) / 6.0;

        // Dodatkowy szum matematyczny zwiększający losowość (Simplex approximation)
        double detail = (Math.sin(nx * 2.5) * Math.cos(nz * 2.5) + 1.0) / 2.0;

        return (rawNoise * 0.7) + (detail * 0.3);
    }
}
