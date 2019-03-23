/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Model.Ship.Direction;
import Model.Ship.MovementType;
import Model.Ship.Ship;
import Util.ActionType;
import Util.GameState;
import Util.ServerOptions;
import Util.TileType;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author kismo
 */
public class Game {

    public static final int MAP_HEIGHT = 50;
    public static final int MAP_WIDTH = 50;

    private TileType[][] map = new TileType[MAP_HEIGHT][MAP_WIDTH];

    Player playerOne;
    Player playerTwo;

    private Map<ServerOptions, Boolean> options;

    private Player winner;

    private GameState state;

    public Game() {
        map = new TileType[MAP_WIDTH][MAP_HEIGHT];
        options = new EnumMap(ServerOptions.class);
        options.put(ServerOptions.FOG, Boolean.TRUE);
        options.put(ServerOptions.MANEUVER, Boolean.TRUE);
        options.put(ServerOptions.PICK_UP, Boolean.TRUE);
        winner = null;
        playerOne = null;
        playerTwo = null;
        state = GameState.INICIALIZATION;
    }

    //Leszimulál egy kört
    public void simulateTurn() {
        StringBuilder set1 = new StringBuilder("");
        StringBuilder set2 = new StringBuilder("");
        String[] actions1 = playerOne.getNewState().split("-");
        String[] actions2 = playerTwo.getNewState().split("-");

        for (int i = 0; i < Ship.MAX_MOVEMENT; i++) {
            simulateSegment(actions1[i], actions2[i], set1, set2);
        }

        playerOne.setMoveSet(set1.deleteCharAt(set1.length() - 1).toString());
        playerTwo.setMoveSet(set2.deleteCharAt(set2.length() - 1).toString());
    }

    //leszimulál egy lépést
    private void simulateSegment(String move1, String move2,
            StringBuilder set1, StringBuilder set2) {
        String[] actions1 = move1.split(";");//Structure: [MOVEMENT,ACTION,ACTION]
        String[] actions2 = move2.split(";");//Structure: [MOVEMENT,ACTION,ACTION]
        int[][] route1 = calcMovementRoute(MovementType.valueOf(actions1[0]), playerOne);
        int[][] route2 = calcMovementRoute(MovementType.valueOf(actions2[0]), playerTwo);

        boolean contact1 = false, contact2 = false;
        contact1 = checkMovementContact(MovementType.valueOf(actions1[0]),
                route1, route2, 1, playerOne, set1, contact1);
        contact2 = checkMovementContact(MovementType.valueOf(actions2[0]),
                route2, route1, 1, playerTwo, set2, contact2);
        if (!contact1) {
            checkMovementContact(MovementType.valueOf(actions1[0]),
                    route1, route2, 2, playerOne, set1, contact1);
        }
        if (!contact2) {
            checkMovementContact(MovementType.valueOf(actions2[0]),
                    route2, route1, 2, playerTwo, set2, contact2);
        }
        //Action feldolgozások
        simulateActions(ActionType.valueOf(actions1[1]),
                ActionType.valueOf(actions1[2]), playerOne, playerTwo, set1);
        simulateActions(ActionType.valueOf(actions2[1]),
                ActionType.valueOf(actions2[2]), playerTwo, playerOne, set2);
    }

    private boolean checkMovementContact(MovementType move, int[][] route1, int[][] route2,
            int counter, Player player, StringBuilder set, boolean contact) {
        if ((route1[counter][0] < 0
                || route1[counter][1] < 0
                || route1[counter][0] >= MAP_WIDTH
                || route1[counter][1] >= MAP_HEIGHT
                || map[route1[counter][0]][route1[counter][1]] == TileType.ROCK
                || (route1[counter][0] == route2[counter][0]
                && route1[counter][1] == route2[counter][1])
                || crossContactDetection(route1, route2, counter))
                && !contact) {
            switch (move) {
                case NONE:
                    processMove(MovementType.NONE, set, player);
                    break;
                case FORWARD:
                    if (counter == 2) {
                        return true;
                    }
                    processMove(MovementType.FORWARD_CRASH, set, player);
                    break;
                case RIGHT:
                    processMove(counter == 1 ? MovementType.RIGHT_CRASH
                            : MovementType.RIGHT_MID_CRASH, set, player);
                    break;
                case LEFT:
                    processMove(counter == 1 ? MovementType.LEFT_CRASH
                            : MovementType.LEFT_MID_CRASH, set, player);
                    break;
            }
            return true;
        }
        if (counter == 2 && !contact) {
            processMove(move, set, player);
            return true;
        }
        return false;
    }

    private boolean crossContactDetection(int[][] route1, int[][] route2, int counter) {
        return route1[counter][0] == route2[counter - 1][0]
                && route1[counter][1] == route2[counter - 1][1]
                && route2[counter][0] == route1[counter - 1][0]
                && route2[counter][1] == route1[counter - 1][1];
    }

    private void processMove(MovementType type, StringBuilder set, Player player) {
        player.getShip().move(type);
        set.append(type).
                append(",").
                append(player.getShip().getPosX()).
                append(",").
                append(player.getShip().getPosY()).
                append(";");
    }

    private void simulateActions(ActionType leftAction, ActionType rightAction,
            Player player, Player otherPlayer, StringBuilder set) {
        Direction dir = player.getShip().getDirection();
        dir.turnLeft();
        checkActionContact(leftAction, player, otherPlayer, dir, set, true);
        dir = player.getShip().getDirection();
        dir.turnRight();
        checkActionContact(rightAction, player, otherPlayer, dir, set, false);
    }

    private void checkActionContact(ActionType type, Player player,
            Player otherPlayer, Direction dir, StringBuilder set, boolean isLeft) {
        int x = player.getShip().getPosX();
        int y = player.getShip().getPosY();
        boolean outOfBounds = false;
        switch (type) {
            case NONE:
                set.append(ActionType.NONE.toString()).append(";");
                break;
            case SHOOT:
                x += dir.getX();
                y += dir.getY();
                if (x < 0 || x >= MAP_WIDTH || y < 0 || y > MAP_HEIGHT) {
                    outOfBounds = true;
                }
                if (!outOfBounds && map[x][y] == TileType.ROCK) {
                    set.append(isLeft ? ActionType.SHOOT_HIT_LEFT_1
                            : ActionType.SHOOT_HIT_RIGHT_1).append(";");
                }
                if (!outOfBounds && x == otherPlayer.getShip().getPosX()
                        && y == otherPlayer.getShip().getPosY()) {
                    set.append(isLeft ? ActionType.SHOOT_HIT_LEFT_1
                            : ActionType.SHOOT_HIT_RIGHT_1).append(";");
                    otherPlayer.getShip().sufferDamage(1);
                }
                x += dir.getX();
                y += dir.getY();
                if (x < 0 || x >= MAP_WIDTH || y < 0 || y > MAP_HEIGHT) {
                    outOfBounds = true;
                }
                if (!outOfBounds && map[x][y] == TileType.ROCK) {
                    set.append(isLeft ? ActionType.SHOOT_HIT_LEFT_2
                            : ActionType.SHOOT_HIT_RIGHT_2).append(";");
                } else if (!outOfBounds && x == otherPlayer.getShip().getPosX()
                        && y == otherPlayer.getShip().getPosY()) {
                    set.append(isLeft ? ActionType.SHOOT_HIT_LEFT_2
                            : ActionType.SHOOT_HIT_RIGHT_2).append(";");
                    otherPlayer.getShip().sufferDamage(1);
                }
                break;
            case GRAPPLE:
                x += dir.getX();
                y += dir.getY();
                set.append(isLeft ? ActionType.GRAPPLE_LEFT
                        : ActionType.GRAPPLE_RIGHT).append(";");
                if (x == otherPlayer.getShip().getPosX()
                        && y == otherPlayer.getShip().getPosY()) {
                    winner = player;
                }
                break;
        }
        if (!isLeft) {
            set.deleteCharAt(set.length() - 1);
            set.append("-");

        }
    }

    //Kiszámítja egy lépés útját a pályán (ütközéseket figyelmen kívül hagyva)
    private int[][] calcMovementRoute(MovementType move, Player player) {
        Direction dir = player.getShip().getDirection();
        int x = player.getShip().getPosX();
        int y = player.getShip().getPosY();
        int[][] route = {{x, y}, {x, y}, {x, y}};

        switch (move) {
            case FORWARD:
                route[1][0] = x + dir.getX();
                route[1][1] = y + dir.getY();
                route[2] = route[1];
                break;
            case RIGHT:
                x += dir.getX();
                y += dir.getY();
                route[1][0] = x;
                route[1][1] = y;
                dir.turnRight();
                x += dir.getX();
                y += dir.getY();
                route[2][0] = x;
                route[2][1] = y;
                break;
            case LEFT:
                x += dir.getX();
                y += dir.getY();
                route[1][0] = x;
                route[1][1] = y;
                dir.turnLeft();
                x += dir.getX();
                y += dir.getY();
                route[2][0] = x;
                route[2][1] = y;
                break;
        }
        return route;
    }

    //Egy beérkezett üzenet alapján frissíti a játék beállításokat
    public void updateOptions(String options) {
        String[] values = options.split(";");
        if (values[0].equalsIgnoreCase("0")) {
            this.options.put(ServerOptions.FOG, Boolean.FALSE);
        }
        if (values[1].equalsIgnoreCase("0")) {
            this.options.put(ServerOptions.MANEUVER, Boolean.FALSE);
        }
        if (values[2].equalsIgnoreCase("0")) {
            this.options.put(ServerOptions.PICK_UP, Boolean.FALSE);
        }
    }

    public void generateMap() {
        MapGenerator mapGen = new MapGenerator();
        mapGen.genMap();
        map = mapGen.getMap();
        playerOne.getShip().setDirection(genStartingDir());
        playerTwo.getShip().setDirection(genStartingDir());

        playerOne.getShip().setPosX(2);
        playerOne.getShip().setPosY(2);
        playerTwo.getShip().setPosX(4);
        playerTwo.getShip().setPosY(4);

    }

    private Direction genStartingDir() {
        return new Direction("SOUTH");
    }

    private int[] genStartingPos(int i) {
        int[] temp = new int[2];
        temp[0] = i;
        temp[1] = i;
        return temp;
    }

    public String genMinGame() {
        StringBuilder mapString = new StringBuilder("");
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                if (i == MAP_HEIGHT - 1 && j == MAP_WIDTH - 1) {
                    mapString.append(map[i][j].toString());
                } else {
                    mapString.append(map[i][j].toString() + ";");
                }

            }
        }
        return MAP_HEIGHT + ";" + MAP_WIDTH + "-" + mapString.toString() + "-"
                + playerOne.getShip().getPosX() + ";"
                + playerOne.getShip().getPosY() + ";"
                + playerOne.getShip().getDirection().getDir() + ";"
                + playerTwo.getShip().getPosX() + ";"
                + playerTwo.getShip().getPosY() + ";"
                + playerTwo.getShip().getDirection().getDir();
    }

    public TileType[][] getMap() {
        return map;
    }

    public void setMap(TileType[][] map) {
        this.map = map;
    }

    public Map<ServerOptions, Boolean> getOptions() {
        return options;
    }

    public void setOptions(Map<ServerOptions, Boolean> options) {
        this.options = options;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(Player playerOne) {
        this.playerOne = playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(Player playerTwo) {
        this.playerTwo = playerTwo;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

}
