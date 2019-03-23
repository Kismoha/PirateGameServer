/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import static Model.Game.MAP_HEIGHT;
import static Model.Game.MAP_WIDTH;
import Util.TileType;
import java.util.Random;

/**
 *
 * @author kismo
 */
public class MapGenerator {

    private final int rockCount = 20;
    private final int rockChanceLowerLimit = 0;
    private final int rockChanceUpperLimit = 10000;
    private final int rockChanceLoss = 2500;
    private final int rockChanceThreshold;
    private final int rockBaseChance;

    private final int currentCount = 10;
    private final int currentChanceLowerLimit = 0;
    private final int currentChanceUpperLimit = 10000;
    private final int currentChanceLoss = 100;
    private final int currentChanceThreshold;
    private final int currentBaseChance;

    private int[][] rockStartNodes;
    private TileType[][] map;

    public MapGenerator() {
        rockChanceThreshold
                = (rockChanceUpperLimit - rockChanceLowerLimit) / 8;
        rockBaseChance = rockChanceUpperLimit - rockChanceThreshold;

        currentChanceThreshold
                = (currentChanceUpperLimit - currentChanceLowerLimit) / 2;
        currentBaseChance = currentChanceUpperLimit - currentChanceThreshold;

        map = new TileType[MAP_HEIGHT][MAP_WIDTH];
        rockStartNodes = new int[rockCount][2];
    }

    public void genMap() {
        fillMapWithWater();
        generateRockNodes();
        populateMap();
    }

    private void fillMapWithWater() {
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                map[i][j] = TileType.WATER;
            }
        }
    }

    private void populateMap() {
        populateRocks();
        populateCurrents();
    }

    private void populateRocks() {
        for (int i = 0; i < rockCount; i++) {
            populateRockTile(rockStartNodes[i][0],
                    rockStartNodes[i][1], rockBaseChance);
        }
    }

    private void populateCurrents() {

    }

    private void generateRockNodes() {
        for (int i = 0; i < rockCount; i++) {
            rockStartNodes[i] = generateNodeCoords();
        }
    }

    private int[] generateNodeCoords() {
        Random rn = new Random();
        return new int[]{rn.nextInt(MAP_WIDTH),
            rn.nextInt(MAP_HEIGHT)};
    }

    private void populateRockTile(int x, int y, int chanceMod) {
        int chance = (rockChanceLowerLimit + new Random().
                nextInt(rockChanceUpperLimit - rockChanceLowerLimit + 1))
                - chanceMod;
        boolean generate = chance <= rockChanceThreshold;
        if (generate) {
            map[x][y] = TileType.ROCK;
            populateRockNeighbour(x, y, chanceMod - rockChanceLoss);
        }
    }

    private void populateRockNeighbour(int x, int y, int chanceMod) {
        if (isValidTile(x, y + 1)) {
            populateRockTile(x, y + 1, chanceMod);
        }
        if (isValidTile(x, y - 1)) {
            populateRockTile(x, y - 1, chanceMod);
        }
        if (isValidTile(x + 1, y)) {
            populateRockTile(x + 1, y, chanceMod);
        }
        if (isValidTile(x + 1, y + 1)) {
            populateRockTile(x + 1, y + 1, chanceMod);
        }
        if (isValidTile(x + 1, y - 1)) {
            populateRockTile(x + 1, y - 1, chanceMod);
        }
        if (isValidTile(x - 1, y)) {
            populateRockTile(x - 1, y, chanceMod);
        }
        if (isValidTile(x - 1, y + 1)) {
            populateRockTile(x - 1, y + 1, chanceMod);
        }
        if (isValidTile(x - 1, y - 1)) {
            populateRockTile(x - 1, y - 1, chanceMod);
        }
    }

    private boolean isValidTile(int x, int y) {
        return x >= 0 && x < MAP_WIDTH
                && y >= 0 && y < MAP_HEIGHT
                && map[x][y] == TileType.WATER;
    }

    public TileType[][] getMap() {
        return map;
    }
}
