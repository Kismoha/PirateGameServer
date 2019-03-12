/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Ship;

/**
 *
 * @author kismo
 */
public class Direction {
    String dir;
    private int x;
    private int y;
    
    public Direction(String dir){
        switch(dir){
            case "NORTH" :
                x = 0;
                y = 1;
                this.dir = dir;
                break;
            case "EAST" :
                x = -1;
                y = 0;
                this.dir = dir;
                break;
            case "WEST" :
                x = 1;
                y = 0;
                this.dir = dir;
                break;
            case "SOUTH" :
                x = 0;
                y = -1;
                this.dir = dir;
                break;
        }
    }
    
    public void turnRight() {
        switch (this.dir) {
            case "NORTH":
                x = 1;
                y = 0;
                this.dir = "EAST";
                break;
            case "SOUTH":
                x = -1;
                y = 0;
                this.dir = "WEST";
                break;
            case "EAST":
                x = 0;
                y = -1;
                this.dir = "SOUTH";
                break;
            case "WEST":
                x = 0;
                y = 1;
                this.dir = "NORTH";
                break;
        }
    }

    public void turnLeft() {
        switch (this.dir) {
            case "NORTH":
                x = -1;
                y = 0;
                this.dir = "WEST";
                break;
            case "SOUTH":
                x = 1;
                y = 0;
                this.dir = "EAST";
                break;
            case "EAST":
                x = 0;
                y = 1;
                this.dir = "NORTH";
                break;
            case "WEST":
                x = 0;
                y = -1;
                this.dir = "SOUTH";
                break;
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
    
    
}
