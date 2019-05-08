/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author kismo
 */
public class Direction {
    private String dir;
    private int x;
    private int y;
    
    public Direction(Direction dir){
        this.dir = dir.dir;
        this.x = dir.x;
        this.y = dir.y;
    }
    
    public Direction(String dir){
        switch(dir){
            case "NORTH" :
                x = 0;
                y = -1;
                this.dir = dir;
                break;
            case "EAST" :
                x = 1;
                y = 0;
                this.dir = dir;
                break;
            case "WEST" :
                x = -1;
                y = 0;
                this.dir = dir;
                break;
            case "SOUTH" :
                x = 0;
                y = 1;
                this.dir = dir;
                break;
        }
    }
    
    public void turnRight() {
        switch (this.dir) {
            case "NORTH":
                changeDirection(new Direction("EAST"));
                break;
            case "SOUTH":
                changeDirection(new Direction("WEST"));
                break;
            case "EAST":
                changeDirection(new Direction("SOUTH"));
                break;
            case "WEST":
                changeDirection(new Direction("NORTH"));
                break;
        }
    }

    public void turnLeft() {
        switch (this.dir) {
            case "NORTH":
                changeDirection(new Direction("WEST"));
                break;
            case "SOUTH":
                changeDirection(new Direction("EAST"));
                break;
            case "EAST":
                changeDirection(new Direction("NORTH"));
                break;
            case "WEST":
                changeDirection(new Direction("SOUTH"));
                break;
        }
    }

    private void changeDirection(Direction newDir){
        this.x = newDir.x;
        this.y = newDir.y;
        this.dir = newDir.dir;
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
