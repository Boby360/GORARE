/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.gossip;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Charles
 */

public class GossipEvent {
    public enum GossipEventType {
        QUITGAME,SAY,SETCASH,CHANGEROLE,JOINTEAM,SUBCASH,ADDCASH,HEALED,SELFHEALED,TEAMSAY,TEAMKILL,PICKEDUPITEM,RESCUEDITEM,TEAMLOSE,TEAMRECEIVEDCASH,TEAMWIN,SUICIDED,KILLED,MAPSTARTED,JOINGAME
    }
    private String primaryPlayer;
    private String secondaryPlayer;
    private int amountOfInvolvedPlayers;
    private GossipEventType type;
    private int cash;
    private String message;
    private String role;
    private String team;
    private int weapon;
    private String map;
    private float amountOfMorphine;

    public float getAmountOfMorphine() {
        return amountOfMorphine;
    }

    protected void setAmountOfMorphine(float amountOfMorphine) {
        this.amountOfMorphine = amountOfMorphine;
    }



    public String getMap() {
        return map;
    }

    protected void setMap(String map) {
        this.map = map;
    }
   protected void setWeapon(int weapon) {
        this.weapon = weapon;
    }
    public int getWeapon() {
        return weapon;
    }

    public GossipEvent(String primaryPlayer, String secondaryPlayer, int amountOfInvolvedPlayers, GossipEventType type) {
        this.primaryPlayer = primaryPlayer;
        this.secondaryPlayer = secondaryPlayer;
        this.amountOfInvolvedPlayers = amountOfInvolvedPlayers;
        this.type = type;
    }

    protected void setCash(int cash) {
        this.cash = cash;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    protected void setRole(String role) {
        this.role = role;
    }

    protected void setTeam(String team) {
        this.team = team;
    }

    public int getAmountOfInvolvedPlayers() {
        return amountOfInvolvedPlayers;
    }

    public int getCash() {
        return cash;
    }

    public String getMessage() {
        return message;
    }

    public String getPrimaryPlayer() {
        return primaryPlayer;
    }

    public String getRole() {
        return role;
    }

    public String getSecondaryPlayer() {
        return secondaryPlayer;
    }

    public String getTeam() {
        return team;
    }

    public GossipEventType getType() {
        return type;
    }
    @Override
    public String toString()
    {
        String ret =  "[GossipEvent]";
        Object obj;
        for (Field f : getClass().getDeclaredFields())
        {
            try {
                obj = f.get(this);
                if (obj != null)
                    ret += "\n" + f.getName() + "\t = " + obj;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GossipEvent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GossipEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret + "\n------------------";
    }
}
