/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorareplugin;

import go.objects.Player;

/**
 *
 * @author Charles
 */
public class PlayerHolder {
    private Player p;
    private int pingExcessions = 0;
    private boolean updated;
    private boolean waitingToBeKicked = false;

    public boolean isWaitingToBeKicked() {
        return waitingToBeKicked;
    }

    public void setWaitingToBeKicked(boolean waitingToBeKicked) {
        this.waitingToBeKicked = waitingToBeKicked;
    }
    public void increasePingExcessions()
    {
        pingExcessions++;
    }

    public int getPingExcessions() {
        return pingExcessions;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
    public Player getPlayer() {
        return p;
    }

    public PlayerHolder(Player p) {
        this.p = p;
    }
    
}
