/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go;

import go.gossip.*;
import go.gossip.GossipEvent.GossipEventType;
import go.objects.GameResponse;
import go.objects.GossipPlayer;
import go.objects.Player;
import go.objects.PlayerInfoResponse;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Charles
 */
public abstract class GossipEventListener {
    public abstract void onGossipEvent(GossipEvent evt);
    private final static ArrayList<GossipPlayer> lstPlayers = new ArrayList<GossipPlayer>();
    private static boolean isFirstRun = true;
    private int cacheTime = -1;
    public void sleepForCacheTime()
    {
        if (cacheTime == -1)
            cacheTime = GameInterface.getInstance().getGORARECacheTime();
        
        try {
            Thread.sleep(cacheTime);
        } catch (InterruptedException ex) {
            Logger.getLogger("cvg").severe(ex.getMessage());
        }
    }
    public GameResponse doRequest(String request)
    {
        return GameInterface.getInstance().doRequest(request);
    }
    public void sayOnServer(String message)
    {
        message = message.replace("\"","");
        doRequest("r_say \""+message+"\"");
    }
    public ArrayList<GossipPlayer> getPlayers()
    {
        return (ArrayList<GossipPlayer>) lstPlayers.clone();
    }
    public static void updatePlayers()
    {
        synchronized (lstPlayers) {
            PlayerInfoResponse playerinfo = (PlayerInfoResponse) GameInterface.getInstance().doRequest("playerinfo");
            GossipPlayer currentPlayer = null;
            ArrayList<GossipPlayer> lstTempUpdatedPlayers = new ArrayList<GossipPlayer>();
            for (Player p : playerinfo.getLstPlayers())
            {
                currentPlayer = statFindPlayer(p.getName());
                if (currentPlayer == null)
                {
                    // This player wasn't found, lets add it to the list
                    
                    currentPlayer = new GossipPlayer(p.getName());

                    currentPlayer.setClientId(p.getClientId());
                    currentPlayer.setPing(p.getPing());
                    Logger.getLogger("cvg").info(currentPlayer.getName() + " connected.");                    
                    lstPlayers.add(currentPlayer);
                    lstTempUpdatedPlayers.add(currentPlayer);
                    GameInterface.getInstance().dispatchGossipEvent(new GossipEvent(currentPlayer.getName(), null, 1, GossipEventType.JOINGAME));
                } else {
                    currentPlayer.setClientId(p.getClientId());
                    currentPlayer.setTeam(p.getTeam());
                    lstTempUpdatedPlayers.add(currentPlayer);
                }
            }
            for (int i=0;i<lstPlayers.size();i++)
            {
                if (!lstTempUpdatedPlayers.contains(lstPlayers.get(i)))
                {
                    // Player is gone
                    Logger.getLogger("cvg").info(lstPlayers.get(i).getName() + " disconnected.");
                    lstPlayers.remove(i);
                    i--;
                }
            }
        }
    }
    protected static void preProcessGossipEvent(GossipEvent evt)
    {
        if (evt.getType() == GossipEventType.JOINTEAM)
        {
            if (statFindPlayer(evt.getPrimaryPlayer()) == null)
            {
                updatePlayers();
            }
            GossipPlayer player = statFindPlayer(evt.getPrimaryPlayer());
            if (player != null)
            {
                player.setTeam(evt.getTeam());
            }
        }
        if (evt.getType() == GossipEventType.CHANGEROLE)
        {
            GossipPlayer player = statFindPlayer(evt.getPrimaryPlayer());
            if (player != null) {
                player.setRole(evt.getRole());
            }
        }
        if (evt.getType() == GossipEventType.QUITGAME)
        {
            if (statFindPlayer(evt.getPrimaryPlayer()) != null)
            {
                updatePlayers();
            }
        }
        if (evt.getType() == GossipEventType.MAPSTARTED)
        {
            synchronized (lstPlayers)
            {
                 while (lstPlayers.size() > 0)
                {
                    Logger.getLogger("cvg").info(lstPlayers.remove(0).getName() + " disconnected (map starting).");
                }
            }
        }
    }
    private static GossipPlayer statFindPlayer(String playerName)
    {
        for (GossipPlayer p : lstPlayers)
        {
            if (p.getName().equals(playerName))
                return p;
        }
        return null;
    }
    public GossipPlayer findPlayer(String playerName)
    {
        return GossipEventListener.statFindPlayer(playerName);
    }
    public GossipEventListener() {
        GameInterface.getInstance().registerGossipListener(this);
        if (isFirstRun)
        {
            updatePlayers();
        }
    }
    
}
