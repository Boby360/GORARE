/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorareplugin;

import go.GameInterface;
import go.objects.GameResponse;
import go.objects.Player;
import go.objects.PlayerInfoResponse;
import gorare.sys.util.Settings;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PingKicker extends Thread{
    private static PingKicker _this;
    private PingKicker(){}
    private int cacheTime = 1000;
    private int maxPingExcessions = 5;
    private int maxPing = 350;
    private ArrayList<PlayerHolder> lstPlayerHolders = new ArrayList<PlayerHolder>();
    public static PingKicker getInstance()
    {
        if (_this == null)
            _this = new PingKicker();
        return _this;
    }

    private GameResponse doRequest(String request)
    {
        if (!request.equals("playerinfo"))
            Logger.getLogger("cvg").info("Sent command ["+request+"]");
        
        return GameInterface.getInstance().doRequest(request);
    }
    @Override
    public void run()
    {
        cacheTime = GameInterface.getInstance().getGORARECacheTime();
        try {
            maxPing = Integer.parseInt(Settings.getInstance().getSetting("maxping"));
        } catch (Exception ex)
        {
            maxPing = 350;
            Logger.getLogger("cvg").warning("Wrong maxping setting. Using default " + maxPing);
        }
        try {
            maxPingExcessions = Integer.parseInt(Settings.getInstance().getSetting("maxpingexcessions"));
        } catch (Exception ex)
        {
            maxPingExcessions = 5;
            Logger.getLogger("cvg").warning("Wrong maxpingexcessions setting. Using default " + maxPingExcessions);
        }
        PlayerInfoResponse response = (PlayerInfoResponse) doRequest("playerinfo");
        for (Player p : response.getLstPlayers())
        {
            PlayerHolder ph = new PlayerHolder(p);
            lstPlayerHolders.add(ph);
            Logger.getLogger("cvg").info("Player " + ph.getPlayer().getName() + " connected. ");
        }
        while (true)
        {
            response = (PlayerInfoResponse) doRequest("playerinfo");
            for (Player p : response.getLstPlayers())
            {
                if (!p.getName().equals("NEWPLAYER"))
                {
                    PlayerHolder ph = findPlayerHolder(p);

                    if (ph != null)
                    {
                        ph.setUpdated(true);
                    } else {
                        ph = new PlayerHolder(p);
                        ph.setUpdated(true);
                        lstPlayerHolders.add(ph);
                        Logger.getLogger("cvg").info("Player " + ph.getPlayer().getName() + " connected.");
                    }

                    if (!ph.isWaitingToBeKicked())
                    {
                        if (p.getPing() > maxPing)
                        {
                            ph.increasePingExcessions();
                        }
                        if (ph.getPingExcessions() >= maxPingExcessions)
                        {
                            ph.setWaitingToBeKicked(true);
                            doRequest("r_say \"Kicking player "+ph.getPlayer().getName()+". Ping is too high or unstable.\"");
                            //doRequestInThreeSeconds("r_say \"demo:r_kick "+ph.getPlayer().getClientId()+"\"");
                            doRequestInThreeSeconds("r_kick " + ph.getPlayer().getClientId());
                        }
                    }
                }
            }
            for (int i=0;i<lstPlayerHolders.size();i++)
            {
                PlayerHolder ph = lstPlayerHolders.get(i);
                if (!ph.isUpdated())
                {
                    Logger.getLogger("cvg").info("Player " + ph.getPlayer().getName() + " disconnected.");
                    lstPlayerHolders.remove(i);
                    i--;
                }
            }
            for (PlayerHolder ph:lstPlayerHolders)
                ph.setUpdated(false);

            sleepForCacheTime();
        }
        
    }
    private void doRequestInThreeSeconds(final String req)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PingKicker.class.getName()).log(Level.SEVERE, null, ex);
                }
                Logger.getLogger("cvg").info("Doing " + req);
                doRequest(req);
            }
        }.start();
    }
    private PlayerHolder findPlayerHolder(Player p)
    {
        for (PlayerHolder ph : lstPlayerHolders)
        {
            if (ph.getPlayer().equals(p))
                return ph;
        }
        return null;
    }
    private void sleepForCacheTime()
    {
        try {
            Thread.sleep(cacheTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(PingKicker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
