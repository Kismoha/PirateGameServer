/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Map_Elements;

import Model.Direction;
import Model.Enums.TileType;

/**
 *
 * @author kismo
 */
public class Current {
    
    private final int length;
    private final int startX;
    private final int startY;
    private final Direction dir;
    private TileType type;

    public Current(int length, int startX, int startY, Direction dir) {
        this.length = length;
        this.startX = startX;
        this.startY = startY;
        this.dir = dir;
        this.type = TileType.valueOf("CURRENT_"+this.dir.getDir());
    }

    public int getLength() {
        return length;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Direction getDir() {
        return dir;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }
}
