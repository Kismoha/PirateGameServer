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
public abstract class Ship {

    public static final int MOVEMENT_SLOTS = 4;
    public static final int MAX_MOVEMENT = 4;

    protected ShipType type;
    //health
    protected int damageThreshold;
    protected int currentDamage;
    //sepcial actions
    protected int shotPerAction;
    protected int maxLoadedGuns;
    protected int currentLoadedGuns;
    protected int grapples;
    //movement
    protected int speed;
    protected MovementType[] movementSlots;
    protected int initialMovementAmmount;
    protected EnumMap<MovementType, Integer> movementAmmount;
    
    //position
    protected int posX;
    protected int posY;
    protected Direction direction;

    public void move(MovementType type) {
        switch (type) {
            case FORWARD:
                moveForward();
                break;
            case FORWARD_CRASH:
                sufferDamage(1);
                break;
            case LEFT:
                moveForward();
                turnLeft();
                moveForward();
                break;
            case LEFT_MID_CRASH:
                moveForward();
                turnLeft();
                sufferDamage(1);
                break;
            case LEFT_CRASH:
                turnLeft();
                sufferDamage(1);
                break;
             case RIGHT:
                moveForward();
                turnRight();
                moveForward();
                break;
            case RIGHT_MID_CRASH:
                moveForward();
                turnRight();
                sufferDamage(1);
                break;
            case RIGHT_CRASH:
                turnRight();
                sufferDamage(1);
                break;
            default :
                //No movement
                break;
        }
    }

    public void moveForward() {
        this.posX += direction.getX();
        this.posY += direction.getY();
    }

    public void turnLeft() {
        this.direction.turnLeft();
    }

    public void turnRight() {
        this.direction.turnRight();
    }
    
    public void floatBy(int x, int y){
        this.posX += x;
        this.posY += y;
    }
    
    public void resetMovementSlots(){
        for(MovementType movement : movementSlots){
            movement = MovementType.NONE;
        }
    }
    
    public void sufferDamage(int dmg){
        this.currentDamage += dmg;
    }
    
    protected void initMovementAmmount(){
        for(MovementType mt : MovementType.values()){
            this.movementAmmount.put(mt,this.initialMovementAmmount);
        }
    }
    
    public boolean isWrecked(){
        return currentDamage >= damageThreshold;
    }

    public String shipStateMessage(){
        StringBuilder str = new StringBuilder("");
        str.append(currentDamage).append(",")
                .append(currentLoadedGuns).append(",")
                .append(grapples);
        return str.toString();
    }
    
    public void shot() {
        currentLoadedGuns--;
    }

    public void shotGain(int ammount) {
        int wannaBe = currentLoadedGuns += ammount;
        currentLoadedGuns = wannaBe <= maxLoadedGuns ? wannaBe : maxLoadedGuns;
    }

    public void grapple() {
        grapples--;
    }

    public void grappleGain(int ammount) {
        grapples += ammount;
    }
    
    protected void init(){
        resetMovementSlots();
        initMovementAmmount();
    }
    
    public ShipType getType() {
        return type;
    }

    public void setType(ShipType type) {
        this.type = type;
    }

    public int getDamageThreshold() {
        return damageThreshold;
    }

    public void setDamageThreshold(int damageThreshold) {
        this.damageThreshold = damageThreshold;
    }

    public int getCurrentDamage() {
        return currentDamage;
    }

    public void setCurrentDamage(int currentDamage) {
        this.currentDamage = currentDamage;
    }

    

    public int getShotPerAction() {
        return shotPerAction;
    }

    public void setShotPerAction(int shotPerAction) {
        this.shotPerAction = shotPerAction;
    }

    public int getMaxLoadedGuns() {
        return maxLoadedGuns;
    }

    public void setMaxLoadedGuns(int maxLoadedGuns) {
        this.maxLoadedGuns = maxLoadedGuns;
    }

    public int getCurrentLoadedGuns() {
        return currentLoadedGuns;
    }

    public void setCurrentLoadedGuns(int currentLoadedGuns) {
        this.currentLoadedGuns = currentLoadedGuns;
    }

    public int getGrapples() {
        return grapples;
    }

    public void setGrapples(int grapples) {
        this.grapples = grapples;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public MovementType[] getMovementSlots() {
        return movementSlots;
    }

    public void setMovementSlots(MovementType[] movementSlots) {
        this.movementSlots = movementSlots;
    }

    public int getInitialMovementAmmount() {
        return initialMovementAmmount;
    }

    public void setInitialMovementAmmount(int initialMovementAmmount) {
        this.initialMovementAmmount = initialMovementAmmount;
    }

    public EnumMap<MovementType, Integer> getMovementAmmount() {
        return movementAmmount;
    }

    public void setMovementAmmount(EnumMap<MovementType, Integer> movementAmmount) {
        this.movementAmmount = movementAmmount;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public Direction getDirection() {
        return new Direction(direction);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    
}
