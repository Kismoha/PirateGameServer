/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Model.Enums.MovementType;
import Model.Map_Elements.Ship;
import Model.Enums.GameState;
import Model.Enums.TileType;

/**
 *
 * @author kismo
 */
public class Game {

    public static final int MAP_WIDTH = 46;
    public static final int MAP_HEIGHT = 22;

    private TileType[][] map = new TileType[MAP_WIDTH][MAP_HEIGHT];

    Player playerOne;
    Player playerTwo;

    private GameState state;
    private boolean isGameEnded;

    public Game() {
        map = new TileType[MAP_WIDTH][MAP_HEIGHT];
        playerOne = null;
        playerTwo = null;
        state = GameState.INICIALIZATION;
        isGameEnded = false;
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

        playerOne.getShip().shotGain(2);
        playerTwo.getShip().shotGain(2);
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
                route1, route2, 1, playerOne, playerTwo, set1, contact1);
        contact2 = checkMovementContact(MovementType.valueOf(actions2[0]),
                route2, route1, 1, playerTwo, playerOne, set2, contact2);

        if (!contact1) {
            checkMovementContact(MovementType.valueOf(actions1[0]),
                    route1, route2, 2, playerOne, playerTwo, set1, contact1);
        }
        if (!contact2) {
            checkMovementContact(MovementType.valueOf(actions2[0]),
                    route2, route1, 2, playerTwo, playerOne, set2, contact2);
        }
        
        isGameEnded = checkWinner();

        processCurrent(playerOne, playerTwo, set1);
        processCurrent(playerTwo, playerOne, set2);
        
        isGameEnded = checkWinner();

        //Action feldolgozások
        simulateActions(actions1[1], actions1[2], playerOne, playerTwo, set1);
        simulateActions(actions2[1], actions2[2], playerTwo, playerOne, set2);

        isGameEnded = checkWinner();
        //removing last char 
        set1.deleteCharAt(set1.length() - 1);
        set1.append("-");
        set2.deleteCharAt(set2.length() - 1);
        set2.append("-");
    }

    private boolean checkMovementContact(MovementType move, int[][] route1, int[][] route2,
            int counter, Player player, Player otherPlayer, StringBuilder set, boolean contact) {
        if ((route1[counter][0] < 0
                || route1[counter][1] < 0
                || route1[counter][0] >= MAP_WIDTH
                || route1[counter][1] >= MAP_HEIGHT
                || map[route1[counter][0]][route1[counter][1]] == TileType.ROCK
                || (route1[counter][0] == route2[counter][0]
                && route1[counter][1] == route2[counter][1])
                || crossContactDetection(route1, route2, counter)
                || crashContactDetection(route1, route2, counter))
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
                    if (player.getShip().isWrecked() && !isGameEnded) {
                        otherPlayer.setWon(true);
                    }
                    break;
                case RIGHT:
                    processMove(counter == 1 ? MovementType.RIGHT_CRASH
                            : MovementType.RIGHT_MID_CRASH, set, player);
                    if (player.getShip().isWrecked() && !isGameEnded) {
                        otherPlayer.setWon(true);
                    }
                    break;
                case LEFT:
                    processMove(counter == 1 ? MovementType.LEFT_CRASH
                            : MovementType.LEFT_MID_CRASH, set, player);
                    if (player.getShip().isWrecked() && !isGameEnded) {
                        otherPlayer.setWon(true);
                    }
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

    private boolean crashContactDetection(int[][] route1, int[][] route2, int counter) {
        return route1[counter][0] == route2[0][0]
                && route1[counter][1] == route2[0][1]
                && (route2[1][0] < 0 || route2[1][0] >= MAP_WIDTH
                || route2[1][1] < 0 || route2[1][1] >= MAP_HEIGHT
                || map[route2[1][0]][route2[1][1]] == TileType.ROCK);
    }

    private void processMove(MovementType type, StringBuilder set, Player player) {
        player.getShip().move(type);
        set.append(type).
                append(",").
                append(player.getShip().getPosX()).
                append(",").
                append(player.getShip().getPosY());
        //.append(";");
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

    private void processCurrent(Player player, Player otherPlayer, StringBuilder set) {
        int x = player.getShip().getPosX();
        int y = player.getShip().getPosY();
        int otherX = otherPlayer.getShip().getPosX();
        int otherY = otherPlayer.getShip().getPosY();
        TileType tile = map[x][y];
        switch (tile) {
            case CURRENT_NORTH:
                manageCurrentTile(player, tile, 0, 1, otherX, otherY, set);
                break;
            case CURRENT_EAST:
                manageCurrentTile(player, tile, 1, 0, otherX, otherY, set);
                break;
            case CURRENT_SOUTH:
                manageCurrentTile(player, tile, 0, -1, otherX, otherY, set);
                break;
            case CURRENT_WEST:
                manageCurrentTile(player, tile, -1, 0, otherX, otherY, set);
                break;
            default:
                appendCurrent(player, "NONE", set);
                break;
        }
        set.append(";");
    }

    private void manageCurrentTile(Player player, TileType tile,
            int byX, int byY, int otherX, int otherY, StringBuilder set) {
        int toX = player.getShip().getPosX() + byX;
        int toY = player.getShip().getPosY() + byY;
        if (toX >= 0 && toX < MAP_WIDTH && toY >= 0 && toY < MAP_HEIGHT
                && map[toX][toY] != TileType.ROCK
                && toX != otherX && toY != otherY) {
            player.getShip().floatBy(byX, byY);
            appendCurrent(player, tile.toString(), set);
        } else {
            appendCurrent(player, "NONE", set);
            player.getShip().sufferDamage(1);
        }
    }

    private void appendCurrent(Player player, String type, StringBuilder set) {
        set.append("=").append(type).append(",").
                append(player.getShip().getPosX()).append(",").
                append(player.getShip().getPosY());
    }

    private void simulateActions(String leftAction, String rightAction,
            Player player, Player otherPlayer, StringBuilder set) {
        Direction dir = player.getShip().getDirection();
        dir.turnLeft();
        checkActionContact(leftAction, player, otherPlayer, dir, set);
        dir = player.getShip().getDirection();
        dir.turnRight();
        checkActionContact(rightAction, player, otherPlayer, dir, set);
    }

    private void checkActionContact(String type, Player player,
            Player otherPlayer, Direction dir, StringBuilder set) {
        int x = player.getShip().getPosX();
        int y = player.getShip().getPosY();
        boolean shot = false;
        switch (type) {
            case "NONE":
                set.append(type).append(";");
                break;
            case "SHOOT":
                if (player.getShip().getCurrentLoadedGuns() != 0) {
                    for (int i = 0; i < 3; i++) {
                        x += dir.getX();
                        y += dir.getY();
                        if (x < 0 || x >= MAP_WIDTH || y < 0 || y > MAP_HEIGHT) {
                            set.append("SHOOTMISS").append(",")
                                    .append(x - dir.getX()).append(",").
                                    append(y - dir.getY()).append(";");
                            shot = true;
                            break;
                        } else if (map[x][y] == TileType.ROCK) {
                            set.append("SHOOTHIT").append(",")
                                    .append(x).append(",").append(y).append(";");
                            shot = true;
                            break;
                        } else if (x == otherPlayer.getShip().getPosX()
                                && y == otherPlayer.getShip().getPosY()) {
                            set.append("SHOOTHIT").append(",")
                                    .append(x).append(",").append(y).append(";");
                            otherPlayer.getShip().sufferDamage(1);
                            if (otherPlayer.getShip().isWrecked() &&
                                !isGameEnded) {
                                    player.setWon(true);
                            }
                            shot = true;
                            break;
                        }
                    }
                    if (!shot) {
                        set.append("SHOOTMISS").append(",")
                                .append(x).append(",").append(y).append(";");
                    }
                } else {
                    set.append("NONE").append(";");
                }
                player.getShip().shot();
                break;
            case "GRAPPLE":
                if (player.getShip().getGrapples() != 0) {
                    x += dir.getX();
                    y += dir.getY();
                    if (x < 0 || x >= MAP_WIDTH || y < 0 || y > MAP_HEIGHT) {
                        set.append("NONE").append(",")
                                .append(x - dir.getX()).append(",").append(y - dir.getY()).append(";");
                        break;
                    } else {
                        set.append(type).append(",")
                                .append(x).append(",").append(y).append(";");
                        if (x == otherPlayer.getShip().getPosX()
                                && y == otherPlayer.getShip().getPosY()) {
                            if (!isGameEnded) {
                                player.setWon(true);
                            }
                        }
                    }
                } else {
                    set.append("NONE").append(";");
                }
                player.getShip().grapple();
                break;
        }
    }

    private boolean checkWinner() {
        return playerOne.hasWon() || playerTwo.hasWon();
    }

    public String genEndMessage(Player player, Player otherPlayer) {
        StringBuilder str = new StringBuilder("");
        StringBuilder cause = new StringBuilder("");
        if (player.hasWon() && otherPlayer.hasWon()) {
            cause.append("Döntetlen! ");
            if (player.getShip().isWrecked() && otherPlayer.getShip().isWrecked()) {
                cause.append("Egyszerre vittétek be az utolsó lövést!");
            } else {
                cause.append("Egyszerre csáklyáztátok meg egymást!");
            }
        } else if (player.hasWon()) {
            cause.append("Nyertél! ");
            if (otherPlayer.getShip().isWrecked()) {
                cause.append("Sikerült elsüllyesztened az ellenfeled hajóját");
            } else {
                cause.append("Sikerült megcsákyláznod az ellenfeled hajóját");
            }
        } else {
            cause.append("Vesztettél! ");
            if (player.getShip().isWrecked()) {
                cause.append("Az ellenfeled elsüllyesztette a hajód!");
            } else {
                cause.append("Az ellenfeled megcsáklyázta a hajód!");
            }
        }
        str.append(cause);
        return str.toString();
    }

    public void generateMap() {
        MapGenerator mapGen = new MapGenerator();
        mapGen.genMap();
        map = mapGen.getMap();
        playerOne.getShip().setDirection(mapGen.randomDir());
        playerTwo.getShip().setDirection(mapGen.randomDir());

        playerOne.getShip().setPosX(mapGen.getPosXOne());
        playerOne.getShip().setPosY(mapGen.getPosYOne());
        playerTwo.getShip().setPosX(mapGen.getPosXTwo());
        playerTwo.getShip().setPosY(mapGen.getPosYTwo());

    }

    public String genMinGame() {
        StringBuilder mapString = new StringBuilder("");
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                if (i == MAP_WIDTH - 1 && j == MAP_HEIGHT - 1) {
                    mapString.append(map[i][j].toString());
                } else {
                    mapString.append(map[i][j].toString()).append(";");
                }

            }
        }
        return MAP_WIDTH + ";" + MAP_HEIGHT + "-" + mapString.toString() + "-"
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

    public boolean isIsGameEnded() {
        return isGameEnded;
    }

}
