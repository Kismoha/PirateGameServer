/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Ship;

import java.util.EnumMap;

/**
 *
 * @author kismoha
 */
public class Sloop extends Ship{
    
    public Sloop(){
        this.type = ShipType.SLOOP;
        
        this.maxHealth = 6;
        this.currentHealth = 6;
        
        this.shotPerAction = 1;
        this.maxLoadedGuns = 6;
        this.currentLoadedGuns = 4;
        this.grapples = 4;
        
        this.speed = 3;
        this.movementSlots = new MovementType[MOVEMENT_SLOTS];
        this.initialMovementAmmount = 3;
        this.movementAmmount = new EnumMap<>(MovementType.class);
        
        this.posX = 0;
        this.posY = 0;
        
        this.direction = new Direction("SOUTH");
        
        init();
        
    }
    
    private void init(){
        resetMovementSlots();
        initMovementAmmount();
    }
}
