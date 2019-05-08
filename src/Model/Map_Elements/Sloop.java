/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Map_Elements;

import Model.Direction;
import Model.Enums.ShipType;
import Model.Enums.MovementType;
import java.util.EnumMap;

/**
 *
 * @author kismoha
 */
public class Sloop extends Ship{
    
    public Sloop(){
        this.type = ShipType.SLOOP;
        
        this.damageThreshold = 6;
        this.currentDamage = 0;
        
        this.shotPerAction = 1;
        this.maxLoadedGuns = 8;
        this.currentLoadedGuns = 6;
        this.grapples = 2;
        
        this.speed = 3;
        this.movementSlots = new MovementType[MOVEMENT_SLOTS];
        this.initialMovementAmmount = 3;
        this.movementAmmount = new EnumMap<>(MovementType.class);
        
        this.posX = 0;
        this.posY = 0;
        
        this.direction = new Direction("SOUTH");
        
        init();
    }
}
