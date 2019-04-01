/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pirategameserver;

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

    public static final int MAX_PLAYERS = 2;
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
        //Waits for game opions from clients
        System.out.println("Waiting fo Options");
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
            String message = game.getPlayerOne().getMoveSet()
                    + "/"
                    + game.getPlayerTwo().getMoveSet();
            messageSenderHelper(MessageType.GAMESTATE, message, message);
        }
    }

    private void waitForConnections() {
        try {
            System.out.println("Waiting for Connection1");
            Socket connection = server.accept();
            System.out.println("Connection");
            game.setPlayerOne(new Player(connection));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Waiting for Connection2");
            Socket connection = server.accept();
            System.out.println("Connection");
            game.setPlayerTwo(new Player(connection));
            game.getPlayerTwo().getShip().setPosX(2);
            game.getPlayerTwo().getShip().setPosY(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Connections done");
    }

    private void waitForClientReady() {
        readFromClients();
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
            case OPTION:
                if (game.getState() != GameState.INICIALIZATION) {
                    System.out.println("State mishap");
                    System.exit(0);
                }
                game.updateOptions(messageContent);
                break;
            case READY:
                if (game.getState() != GameState.READY) {
                    System.out.println("State mishap");
                    System.exit(0);
                }
                player.setReady(true);
                break;
            case GAMESTATE:
                if (game.getState() != GameState.GAME) {
                    System.out.println("State mishap");
                    System.exit(0);
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(PirateGameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PirateGameServer();
    }

}
