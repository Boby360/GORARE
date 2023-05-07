/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.objects;

import java.util.ArrayList;

/**
 *
 * @author Charles
 */
public class PlayerInfoResponse extends GameResponse {
    private ArrayList<Player> lstPlayers;

    public PlayerInfoResponse(ArrayList<Player> lstPlayers) {
        this.lstPlayers = lstPlayers;
    }
    @Override
    public String toString()
    {
        String ret = "[Playerinfo]";
        if (lstPlayers != null)
            for (Player p : lstPlayers)
            {
                ret += "\n" + p.toRawLine();
            }
            
        return ret;
    }

    public ArrayList<Player> getLstPlayers() {
        return lstPlayers;
    }
    
    @Override
    public String toRawResponse() {
        String ret = "";
        if (lstPlayers != null)
        {
            for (Player p : lstPlayers)
            {
                ret += p.toRawLine() + "\n";
            }
            if (lstPlayers.size() == 0)
            ret = "NO PLAYER YET";
        }        

        return ret;
    }


    
}
