/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Model.Game;
import Model.Player;
import Model.Enums.GameState;
import Model.Enums.MessageType;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kismoha
 */
public class PirateGameServer {
    public static final int PORT = 65535;

    private ServerSocket server;

    private Game game;

    public PirateGameServer() {
        game = new Game();
        //Starts up the server
        createServer();
        //Waits for 2 connections
        waitForConnections();
        //Generates Map
        game.generateMap();
        //Waits for first sign from client
        System.out.println("Waiting for first sign from client");
        readFromClients();
        game.setState(GameState.READY);
        //Sends out gameState
        System.out.println("Sending Game State");
        String minGame = game.genMinGame();
        messageSenderHelper(MessageType.FIRST_GAMESTATE, minGame + "/1", minGame + "/2");
        //Waits for clients to be ready
        System.out.println("Waiting fo Ready");
        waitForClientReady();
        game.setState(GameState.GAME);
        if (game.getPlayerOne().isReady() && game.getPlayerTwo().isReady()) {
            start();
        }
    }

    private void createServer() {
        try {
            server = new ServerSocket(PORT);
            System.out.println(server.getLocalPort());
        } catch (IOException e) {
            System.out.println("Failed to create server");
            e.printStackTrace();
        }
    }

    private void start() {
        System.out.println("Sending START");
        messageSenderHelper(MessageType.STARTGAME, "YOU_CAN_START_THE_GAME",
                "YOU_CAN_START_THE_GAME");
        while (true) {
            readFromClients();
            game.simulateTurn();
            String mainMessage = game.getPlayerOne().getMoveSet()
                    + "/"
                    + game.getPlayerTwo().getMoveSet();
            messageSenderHelper(MessageType.GAMESTATE,
                    genGameStateMessage(mainMessage, game.getPlayerOne(),
                            game.getPlayerTwo()),
                    genGameStateMessage(mainMessage, game.getPlayerTwo(),
                            game.getPlayerOne()));
        }
    }

    private void waitForConnections() {
        try {
            System.out.println("Waiting for Connection1");
            Socket connection = server.accept();
            System.out.println("Connection");
            game.setPlayerOne(new Player(connection));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Waiting for Connection2");
            Socket connection = server.accept();
            System.out.println("Connection");
            game.setPlayerTwo(new Player(connection));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connections done");
    }

    private void waitForClientReady() {
        readFromClients();
    }

    private String genGameStateMessage(String start, Player player,
            Player otherPlayer) {
        StringBuilder str = new StringBuilder("");
        str.append(start).append(":")
                .append(player.getShip().shipStateMessage())
                .append(":")
                .append(game.isIsGameEnded()
                        ? game.genEndMessage(player, otherPlayer) : "NOPE");
        return str.toString();
    }

    private void readFromClients() {
        String message;
        message = readMessageFrom(game.getPlayerOne());
        processMessage(message, game.getPlayerOne());
        message = readMessageFrom(game.getPlayerTwo());
        processMessage(message, game.getPlayerTwo());
    }

    private void processMessage(String message, Player player) {
        MessageType messageType;
        String messageContent;
        String[] splitMessage = message.split(":");
        messageType = MessageType.valueOf(splitMessage[0]);
        messageContent = splitMessage[1];
        switch (messageType) {
            case SIGN:
                if (game.getState() != GameState.INICIALIZATION) {
                    stateMishap();
                }
                break;
            case READY:
                if (game.getState() != GameState.READY) {
                    stateMishap();
                }
                player.setReady(true);
                break;
            case GAMESTATE:
                if (game.getState() != GameState.GAME) {
                    stateMishap();
                }
                player.setNewState(messageContent);
                break;
            default:
                System.exit(0);
                break;
        }
    }

    private String readMessageFrom(Player player) {
        String message = "NONE:NO_MESSAGE_RECIEVED";
        try {
            System.out.println("Waiting for message");
            message = player.getIn().readLine();
            System.out.println("Message recieved");
        } catch (IOException e) {
            errorMessage(player);
        }
        return message;
    }

    private void writeToClients(String message1, String message2) {
        writeToClient(message1, game.getPlayerOne());
        writeToClient(message2, game.getPlayerTwo());
    }

    private void writeToClient(String message, Player player) {
        try {
            PrintWriter out = player.getOut();
            System.out.println("Sending message: " + message);
            out.println(message);
            out.flush();
        } catch (Exception e) {
            errorMessage(player);
        }
    }

    private void stateMishap() {
        errorMessage(game.getPlayerOne());
        errorMessage(game.getPlayerTwo());
        System.out.println("State mishap");
        waitAndExit();
    }

    private void messageSenderHelper(MessageType type, String message1, String message2) {
        String messageStart = type.toString() + ":";
        writeToClients(messageStart + message1, messageStart + message2);

    }

    private void errorMessage(Player player) {
        if (player == game.getPlayerOne()) {
            writeToClient("ERROR:ERROR", game.getPlayerTwo());
        } else {
            writeToClient("ERROR:ERROR", game.getPlayerOne());
        }
        waitAndExit();
    }

    private void waitAndExit() {
        Thread wait = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PirateGameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        wait.start();
        try {
            wait.join();
        } catch (InterruptedException ex) {
            System.out.println("waitAndExit got interrupted");
            System.exit(0);
        }
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PirateGameServer();
    }

}
