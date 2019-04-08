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

    private final int[][] rockStartNodes;
    private final Current[] currents;
    private final TileType[][] map;
    private final int[][] coloredMap;
    private int color;

    private Direction dirOne;
    private int posXOne;
    private int posYOne;

    private Direction dirTwo;
    private int posXTwo;
    private int posYTwo;

    public MapGenerator() {
        rockChanceThreshold
                = (rockChanceUpperLimit - rockChanceLowerLimit) / 8;
        rockBaseChance = rockChanceUpperLimit - rockChanceThreshold;

        map = new TileType[MAP_WIDTH][MAP_HEIGHT];
        rockStartNodes = new int[rockCount][2];
        currents = new Current[currentCount];
        coloredMap = new int[MAP_WIDTH][MAP_HEIGHT];
        color = 1;
    }

    public void genMap() {
        fillMapWithWater();
        generateRockNodes();
        generateCurrents();
        populateMap();
        colorMap();
        genStartingPositions();
        genStartingDirections();
    }
    
    private void printColoredMap(){
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                System.out.print(coloredMap[j][i] != -1 ? " "+coloredMap[j][i] : coloredMap[j][i]);
            }
            System.out.println("");
        }
    }

    private void colorMap() {
        setupColoredMap();
        int[] coords = findAColorlessCoord();
        while (coords[0] != -1) {
            colorACoord(coords[0], coords[1]);
            coords = findAColorlessCoord();
            color++;
        }
        printColoredMap();
    }

    private int[] findAColorlessCoord() {
        int[] coords = {-1, -1};
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (coloredMap[j][i] == 0) {
                    coords[0] = j;
                    coords[1] = i;
                    return coords;
                }
            }
        }
        return coords;
    }

    private void setupColoredMap() {
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                switch (map[j][i]) {
                    case ROCK:
                        coloredMap[j][i] = -1;
                        break;
                    default:
                        coloredMap[j][i] = 0;
                        break;
                }
            }
        }
    }

    private void colorACoord(int x, int y) {
        if (coloredMap[x][y] == 0) {
            coloredMap[x][y] = color;
            if (isValidCoord(x, y + 1)) {
                colorACoord(x, y + 1);
            }
            if (isValidCoord(x, y - 1)) {
                colorACoord(x, y - 1);
            }
            if (isValidCoord(x + 1, y)) {
                colorACoord(x + 1, y);
            }
            if (isValidCoord(x - 1, y)) {
                colorACoord(x - 1, y);
            }
        }
    }

    private void genStartingDirections() {
        do {
            dirOne = randomDir();
            dirTwo = randomDir();
        } while (!validDirections());
    }

    private boolean validDirections() {
        try {
            boolean first = map[posXOne + dirOne.getX()][posYOne + dirOne.getY()]
                    != TileType.ROCK;
            boolean second = map[posXTwo + dirTwo.getX()][posYTwo + dirTwo.getY()]
                    != TileType.ROCK;
            return first && second;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private void genStartingPositions() {
        do {
            randomizeCoords(1);
            randomizeCoords(2);
        } while (!validShipDistance() || !shipsAreReachAble());
    }

    private void randomizeCoords(int player) {
        Random rnd = new Random();
        int rndX, rndY;
        do {
            rndX = rnd.nextInt(MAP_WIDTH);
            rndY = rnd.nextInt(MAP_HEIGHT);
        } while (!validStartingPos(rndX, rndY));
        if (player == 1) {
            posXOne = rndX;
            posYOne = rndY;
        } else if (player == 2) {
            posXTwo = rndX;
            posYTwo = rndY;
        }
    }

    private boolean validStartingPos(int x, int y) {
        return map[x][y] == TileType.WATER;
    }

    private boolean shipsAreReachAble() {
        return coloredMap[posXOne][posYOne] == coloredMap[posXTwo][posYTwo];
    }

    private boolean validShipDistance() {
        double a = Math.abs(posXOne - posXTwo);
        double b = Math.abs(posYOne - posYTwo);
        double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        return c < 10 && c > 3;
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

    private boolean isValidCoord(int x, int y) {
        return x >= 0 && x < MAP_WIDTH
                && y >= 0 && y < MAP_HEIGHT;
    }

    private boolean isValidTile(int x, int y) {
        return isValidCoord(x, y) && map[x][y] == TileType.WATER;
    }

    public Direction randomDir() {
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
                return null;
        }
    }

    public TileType[][] getMap() {
        return map;
    }

    public int getPosXOne() {
        return posXOne;
    }

    public int getPosYOne() {
        return posYOne;
    }

    public int getPosXTwo() {
        return posXTwo;
    }

    public int getPosYTwo() {
        return posYTwo;
    }

}
