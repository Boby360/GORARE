/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gorarepluginrps;

import go.GossipEventListener;
import go.gossip.GossipEvent;
import go.gossip.GossipEvent.GossipEventType;
import go.objects.GossipPlayer;

public class myPlugin extends GossipEventListener{

    
    @Override
    public void onGossipEvent(GossipEvent event) {
        if (event.getType() == GossipEventType.TEAMSAY)
        {
            sayOnServer("You said : " + event.getMessage());
        }
        /**
         * These are the available commands
         * - getPlayers (this returns an ArrayList of GossipPlayer objects
         * - updatePlayers (updates the player list with fresh information)
         * - doRequest (will send a request to the game and returns a GameResponse, this response can be either a CustomResponse, ServerInfoResponse or PlayerinfoResponse)
         * - sleepForCacheTime() (will put the program(thread) to sleep for the amount of time that is necessary to make the gorare cache outdated
         * - findPlayer(String name)
         * 
         * Note : the player list will be automatically refreshed when a player joins or leaves the server
         */
//        System.out.println(event.getType());
        if (event.getType() == GossipEventType.SAY)
        {
            if (event.getMessage().equals("rpskill"))
            {
                GossipPlayer supposedWinner = findPlayer(event.getPrimaryPlayer());
                if (supposedWinner == null)
                {
                    updatePlayers();
                    supposedWinner = findPlayer(event.getPrimaryPlayer());
                    if (supposedWinner == null)
                        return;
                }
                
                if (supposedWinner != null)
                {
                    String loser = supposedWinner.getSettingAsString("rpskill");
                    if (loser != null)
                    {
                        GossipPlayer pLoser = findPlayer(loser);
                        if (pLoser != null)
                        {
                            doRequest("r_kill " + pLoser.getClientId());
                        }
                        supposedWinner.putSetting("rpskill", null);
                    }
                }
                return;
            }

            if (event.getMessage().startsWith("rps"))
            {
                if (event.getMessage().endsWith("\\r") || event.getMessage().endsWith("\\p") || event.getMessage().endsWith("\\s"))
                {
                    int newRPSValue = -1;
                    GossipPlayer player = findPlayer(event.getPrimaryPlayer());
                    if (player == null)
                    {
                        updatePlayers();
                        player = findPlayer(event.getPrimaryPlayer());
                        if (player == null)
                            return;
                    }                    
                    
                    if (event.getMessage().endsWith("\\r"))
                    {
                        newRPSValue = 1;
                    } else if (event.getMessage().endsWith("\\p"))
                    {
                        newRPSValue = 2;
                    } else if (event.getMessage().endsWith("\\s"))
                    {
                        newRPSValue = 3;
                    }
                    boolean foundOpponent = false;
                    for (GossipPlayer p : getPlayers())
                    {
                        int playerRPSValue = p.getSettingAsInt("rps");
                        if (playerRPSValue != -1)
                        {
                            foundOpponent = true;
                            
                            switch (newRPSValue)
                            {
                                case 1:
                                    //Rock
                                    switch (playerRPSValue)
                                    {
                                        case 1:
                                            // Rock
                                            // draw
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. The game was a draw : rock-rock");
                                            ;break;
                                        case 2:
                                            // Paper
                                            // p wins
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+p.getName()+" wins : paper-rock");
                                            p.putSetting("rpskill", player.getName());
                                            ;break;
                                        case 3:
                                            // Scis
                                            // p loses
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+player.getName()+" wins : rock-scissors");
                                            player.putSetting("rpskill", p.getName());
                                            ;break;
                                    }
                                    break;
                                case 2:
                                    //Paper
                                    switch (playerRPSValue)
                                    {
                                        case 1:
                                            // Rock
                                            // p loses
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+player.getName()+" wins : paper-rock");
                                            player.putSetting("rpskill", p.getName());
                                            ;break;
                                        case 2:
                                            // Paper
                                            // draw
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. The game was a draw : paper-paper");
                                            ;break;
                                        case 3:
                                            // Scis
                                            // p wins
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+p.getName()+" wins : scissors-paper");
                                            p.putSetting("rpskill", player.getName());
                                            ;break;
                                    }
                                    break;
                                case 3:
                                    //Scis
                                    switch (playerRPSValue)
                                    {
                                        case 1:
                                            // Rock
                                            // p wins
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+p.getName()+" wins : rock-scissors");
                                            p.putSetting("rpskill", player.getName());
                                            ;break;
                                        case 2:
                                            // Paper
                                            // p loses
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. "+player.getName()+" wins : scissors-paper");
                                            player.putSetting("rpskill", p.getName());
                                            ;break;
                                        case 3:
                                            // Scis
                                            // draw
                                            sayOnServer(player.getName() + " and " + p.getName() + " played rps. The game was a draw : scissors-scissors");
                                            ;break;
                                    }
                                    break;
                            }
                            if (foundOpponent)
                                break;
                        }
                    }
                    
                    if (!foundOpponent)
                    {
                        player.putSetting("rps", newRPSValue);
                    } else {
                        for (GossipPlayer p : getPlayers())
                            p.putSetting("rps", -1);
                    }
                } else {
                    doRequest("r_say \"Rock-Paper-Scissors usage: Type [rps \\\\r] or [rps \\\\p] or [rps \\\\s] to play. Type [rpskill] when you won.\"");
                }
            }            
        }
        /*
        if (event.getType() == GossipEventType.SAY)
        {
            doRequest("r_say \"List of players\"");
            doRequest("r_say \"---------------\"");
            int index = 0;
            for (GossipPlayer p : getPlayers())
            {
                index++;
                doRequest("r_say \""+index+". "+p.getName()+"\"");
            }
        }      */

    }

}
