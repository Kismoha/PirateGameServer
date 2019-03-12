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

    public static final int MAP_HEIGHT = 5;
    public static final int MAP_WIDTH = 5;

    private TileType[][] map = new TileType[MAP_HEIGHT][MAP_WIDTH];

    Player playerOne;
    Player playerTwo;

    private Map<ServerOptions, Boolean> options;

    private Player winner;

    private GameState state;

    public Game() {
        options = new EnumMap(ServerOptions.class);
        options.put(ServerOptions.FOG, Boolean.TRUE);
        options.put(ServerOptions.MANEUVER, Boolean.TRUE);
        options.put(ServerOptions.PICK_UP, Boolean.TRUE);
        winner = null;
        playerOne = null;
        playerTwo = null;
        state = GameState.INICIALIZATION;
    }

    //Leszimulál eegy kört
    public void simulateTurn() {
        StringBuilder set1 = new StringBuilder("");
        StringBuilder set2 = new StringBuilder("");
        String[] actions1 = playerOne.getNewState().split("-");
        String[] actions2 = playerTwo.getNewState().split("-");

        for (int i = 0; i < Ship.MAX_MOVEMENT; i++) {
            simulateSegment(actions1[i], actions2[i], set1, set2);
            set1.append("-");
            set2.append("-");
        }

        playerOne.setMoveSet(set1.toString());
        playerTwo.setMoveSet(set2.toString());
    }

    //leszimulál egy lépést
    private void simulateSegment(String move1, String move2,
            StringBuilder set1, StringBuilder set2) {
        String[] actions1 = move1.split(";");//Structure: [MOVEMENT,ACTION,ACTION]
        String[] actions2 = move2.split(";");//Structure: [MOVEMENT,ACTION,ACTION]
        int[][] route1 = calcMovementRoute(MovementType.valueOf(actions1[0]), playerOne);
        int[][] route2 = calcMovementRoute(MovementType.valueOf(actions2[0]), playerTwo);
        int POx0 = route1[0][0],
                POy0 = route1[0][1],
                POx1 = route1[1][0],
                POy1 = route1[1][1],
                POx2 = route1[2][0],
                POy2 = route1[2][1],
                PTx0 = route2[0][0],
                PTy0 = route2[0][1],
                PTx1 = route2[1][0],
                PTy1 = route2[1][1],
                PTx2 = route2[2][0],
                PTy2 = route2[2][1];
        boolean contact1 = false, contact2 = false;
        checkMovementContact(MovementType.valueOf(actions1[0]),
                POx1, POy1, PTx1, PTy1, 1, playerOne, set1, contact1);
        checkMovementContact(MovementType.valueOf(actions2[0]),
                PTx1, PTy1, POx1, POy1, 1, playerTwo, set2, contact2);
        checkMovementContact(MovementType.valueOf(actions1[0]),
                POx2, POy2, PTx2, PTy2, 2, playerOne, set1, contact1);
        checkMovementContact(MovementType.valueOf(actions2[0]),
                PTx2, PTy2, POx2, POy2, 2, playerTwo, set2, contact2);
        //Action feldolgozások
        simulateActions(ActionType.valueOf(actions1[1]),
                ActionType.valueOf(actions1[2]), playerOne, playerTwo, set1);
        simulateActions(ActionType.valueOf(actions2[1]),
                ActionType.valueOf(actions2[2]), playerTwo, playerOne, set2);
    }

    private void checkMovementContact(MovementType move, int x1, int y1,
            int x2, int y2, int counter, Player player, StringBuilder set, boolean contact) {
        if ((x1 < 0 || y1 < 0 || x1 >= MAP_WIDTH || y1 >= MAP_HEIGHT
                || map[x1][y1] == TileType.ROCK
                || (x1 == x2 && y1 == y2)) && !contact) {
            switch (move) {
                case NONE:
                    processMove(MovementType.NONE, set, player);
                    break;
                case FORWARD:
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
            contact = true;
        } else if (counter == 2 && !contact) {
            processMove(move, set, player);
        }
    }

    private void processMove(MovementType type, StringBuilder set, Player player) {
        player.getShip().move(type);
        set.append(type).
                append(":").
                append(player.getShip().getPosX()).
                append(":").
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
                } else if (!outOfBounds && x == otherPlayer.getShip().getPosX()
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
                route[1][0] = x + dir.getX();
                route[1][1] = y + dir.getY();
                dir.turnRight();
                route[2][0] = x + dir.getX();
                route[2][1] = y + dir.getY();
                break;
            case LEFT:
                route[1][0] = x + dir.getX();
                route[1][1] = y + dir.getY();
                dir.turnLeft();
                route[2][0] = x + dir.getX();
                route[2][1] = y + dir.getY();
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
        for (int i = 0; i < MAP_HEIGHT; i++) {
            for (int j = 0; j < MAP_WIDTH; j++) {
                map[i][j] = TileType.WATER;
            }
        }
        playerOne.getShip().setDirection(genStartingDir());
        playerTwo.getShip().setDirection(genStartingDir());
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
                + playerTwo.getShip().getPosX() + ";"
                + playerTwo.getShip().getPosY();
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
