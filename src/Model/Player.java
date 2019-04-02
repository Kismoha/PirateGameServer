/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Model.Map_Elements.Sloop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author kismoha
 */
public class Player {
    
    private Sloop ship;
    private Socket socket;
    private boolean ready;
    private boolean won;
    
    private BufferedReader in;
    private PrintWriter out;
    
    private String newState;
    private String moveSet;
    
    public Player(Socket socket){
        this.socket = socket;
        ship = new Sloop();
        ready = false;
        won = false;
        try{
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(this.socket.getOutputStream());
        }catch(Exception e){
            
        }
    }

    public InputStream getInputStream() throws IOException{
        return this.socket.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException{
        return this.socket.getOutputStream();
    }
    
    public Sloop getShip() {
        return ship;
    }

    public void setShip(Sloop ship) {
        this.ship = ship;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getMoveSet() {
        return moveSet;
    }

    public void setMoveSet(String moveSet) {
        this.moveSet = moveSet;
    }

    public boolean hasWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }
    
    
}
