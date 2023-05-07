/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.responses;

import go.GameResponse;
import go.objects.Player;
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

    @Override
    public String toGRResponse() {
        String ret = "";
        if (lstPlayers != null)
        {
            for (Player p : lstPlayers)
            {
                ret += p.toGRLine() + "\n";
            }
            if (lstPlayers.size() == 0)
            ret = "NO PLAYER YET";
        }   
        return ret + (char) 0;
    }
    
}
