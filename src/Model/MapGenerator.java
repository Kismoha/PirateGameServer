/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import static Model.Game.MAP_HEIGHT;
import static Model.Game.MAP_WIDTH;
import Model.Enums.TileType;
import Model.Map_Elements.Current;
import java.util.Random;

/**
 *
 * @author kismo
 */
public class MapGenerator {

    private final int rockCount = (int) Math.floor((MAP_WIDTH * MAP_HEIGHT) / 125);
    private final int rockChanceLowerLimit = 0;
    private final int rockChanceUpperLimit = 10000;
    private final int rockChanceLoss = 2500;
    private final int rockChanceThreshold;
    private final int rockBaseChance;

    private final int currentCount = (int) Math.floor((MAP_WIDTH * MAP_HEIGHT) / 125);
    private final int currentLengthMin = 3;
    private final int currentLengthMax = Math.max(MAP_WIDTH, MAP_HEIGHT);

    private int[][] rockStartNodes;
    private Current[] currents;
    private TileType[][] map;

    public MapGenerator() {
        rockChanceThreshold
                = (rockChanceUpperLimit - rockChanceLowerLimit) / 8;
        rockBaseChance = rockChanceUpperLimit - rockChanceThreshold;

        map = new TileType[MAP_HEIGHT][MAP_WIDTH];
        rockStartNodes = new int[rockCount][2];
        currents = new Current[currentCount];
    }

    public void genMap() {
        fillMapWithWater();
        generateRockNodes();
        generateCurrents();
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

    private void generateRockNodes() {
        for (int i = 0; i < rockCount; i++) {
            rockStartNodes[i] = generateNodeCoords();
        }
    }

    private void generateCurrents() {
        for (int i = 0; i < currentCount; i++) {
            int x, y;
            do {
                Random rn = new Random();
                x = rn.nextInt(MAP_WIDTH);
                y = rn.nextInt(MAP_HEIGHT);
            } while (!isValidTile(x, y));
            int length = currentLengthMin
                    + new Random().nextInt(currentLengthMax - currentLengthMin + 1);
            currents[i] = new Current(length, x, y, randomDir());
        }
    }

    private void populateCurrents() {
        for (int i = 0; i < currentCount; i++) {
            placeCurrent(currents[i]);
        }
    }

    private void placeCurrent(Current current) {
        int i = 0;
        int x = current.getStartX();
        int y = current.getStartY();
        while (isValidTile(x, y) && i < current.getLength()) {
            map[x][y] = current.getType();
            x += current.getDir().getX();
            y += current.getDir().getY();
            i++;
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

    private Direction randomDir() {
        int random = new Random().nextInt(4);
        switch (random) {
            case 0:
                return new Direction("NORTH");
            case 1:
                return new Direction("EAST");
            case 2:
                return new Direction("SOUTH");
            case 3:
                return new Direction("WEST");
            default:
                System.out.println("random dir default");
                return null;
        }
    }

    public TileType[][] getMap() {
        return map;
    }
}
