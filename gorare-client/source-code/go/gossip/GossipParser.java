/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.gossip;

import java.util.logging.Logger;

public class GossipParser {
    private static GossipParser _this;
    private GossipParser(){}
    private GossipEvent retEvent = null;
    
    public static GossipParser getInstance()
    {
        if (_this == null)
            _this = new GossipParser();
        return _this;
    }
    public GossipEvent parseMessage(String message)
    {
        if (doSinglePlayerAction(message))
            return retEvent;

        if (doNoPlayerAction(message))
            return retEvent;

        if (doTwoPlayerAction(message))
            return retEvent;

        Logger.getLogger("cvg").info("GOSSIP Parser did not parse event [" + message + "]");
        return null;
    }
    private boolean doTwoPlayerAction(String message)
    {
        /**
         *  "|GA| Artkayek<CT>" killed "Player<TERRORIST>" with "MP5/10" (17)
            "|GA| Artkayek<TERRORIST>" healed "|GA| Artkayek<TERRORIST>" for 8.120000
            "Player<TERRORIST>" teamkilled "|GA| Artkayek<TERRORIST>" with "AK 47" (21)
         */
        int indexOfHealed = message.indexOf("\" healed \"");
        if (indexOfHealed != -1)
        {
            try {
                String healerName = message.substring(1,message.indexOf("<",1));
                String healedName = message.substring(indexOfHealed+10,message.indexOf("<",indexOfHealed+10));
                float amountOfMorphine = Float.parseFloat(message.substring(message.lastIndexOf(" ")+1));
                if (healerName.equals(healedName))
                {
                    this.retEvent = new GossipEvent(healerName, healedName, 2, GossipEvent.GossipEventType.SELFHEALED);
                } else {
                    this.retEvent = new GossipEvent(healerName, healedName, 2, GossipEvent.GossipEventType.HEALED);
                }
                this.retEvent.setAmountOfMorphine(amountOfMorphine);
                return true;
            } catch (NumberFormatException ex)
            {
                ex.printStackTrace();
                // warn?
            }
        }

        int indexOfKilled = message.indexOf("\" killed \"");
        if (indexOfKilled != -1)
        {
            try {
                String killerName = message.substring(1,message.indexOf("<",1));
                String victimName = message.substring(indexOfKilled+10,message.indexOf("<",indexOfKilled+10));
                int weapon = Integer.parseInt(message.substring(message.lastIndexOf("(")+1,message.lastIndexOf(")")));
                
                this.retEvent = new GossipEvent(killerName, victimName, 2, GossipEvent.GossipEventType.KILLED);
                this.retEvent.setWeapon(weapon);
                
                return true;
            } catch (NumberFormatException ex)
            {
                // warn?
            }
        }

        int indexOfTeamKilled = message.indexOf("\" teamkilled \"");
        if (indexOfTeamKilled != -1)
        {
            try {
                String killerName = message.substring(1,message.indexOf("<",1));
                String victimName = message.substring(indexOfTeamKilled+14,message.indexOf("<",indexOfTeamKilled+14));
                int weapon = Integer.parseInt(message.substring(message.lastIndexOf("(")+1,message.lastIndexOf(")")));
                
                this.retEvent = new GossipEvent(killerName, victimName, 2, GossipEvent.GossipEventType.TEAMKILL);
                this.retEvent.setWeapon(weapon);
                
                return true;
            } catch (NumberFormatException ex)
            {
                // warn?
            }
        }

        return false;
    }
    private boolean doNoPlayerAction(String message)
    {
        if (message.startsWith("Team") && message.endsWith("loses"))
        {
            this.retEvent = new GossipEvent(null, null, 0, GossipEvent.GossipEventType.TEAMLOSE);
            this.retEvent.setTeam(message.substring(message.indexOf("\"")+1,message.lastIndexOf("\"")));
            return true;
        } else if (message.startsWith("Team") && message.endsWith("wins"))
        {
            this.retEvent = new GossipEvent(null, null, 0, GossipEvent.GossipEventType.TEAMWIN);
            this.retEvent.setTeam(message.substring(message.indexOf("\"")+1,message.lastIndexOf("\"")));
            return true;
        } else if (message.startsWith("Team") && message.contains("receiving cash"))
        {
            try {
                this.retEvent = new GossipEvent(null, null, 0, GossipEvent.GossipEventType.TEAMRECEIVEDCASH);
                this.retEvent.setTeam(message.substring(message.indexOf("\"")+1,message.lastIndexOf("\"")));
                int cash = Integer.parseInt(message.substring(message.lastIndexOf(" ") +1));
                this.retEvent.setCash(cash);
                return true;
            } catch (NumberFormatException ex)
            {
                // warn?
            }
        } else if (message.startsWith("Started map"))
        {
            this.retEvent = new GossipEvent(null, null, 0, GossipEvent.GossipEventType.MAPSTARTED);
            this.retEvent.setMap(message.substring(message.indexOf("\"")+8,message.lastIndexOf("\"")));
            return true;
        }
        return false;
    }
    private boolean doSinglePlayerAction(String message)
    {
            int index = message.lastIndexOf(">");
            if (index != -1)
            {
                String playerName = message.substring(message.indexOf("\"") + 1,message.lastIndexOf("<"));
                String action = null;
                try {
                    action = message.substring(index+3,message.indexOf("\"",index+2)-1);
                } catch (StringIndexOutOfBoundsException ex)
                {
                    try {
                        action = message.substring(index+3,message.indexOf("$",index+2)-1);
                    } catch (StringIndexOutOfBoundsException ex2)
                    {
                        try {
                            action = message.substring(index+3);
                        } catch (StringIndexOutOfBoundsException ex3)
                        {
                            return false;
                        }
                    }
                }
                //System.out.println("--------------\nPlayer [" + playerName + "]\nAction [" + action+"]\n------------");
                if (action.equals("joined team"))
                {
                    String teamName = message.substring(0,message.length()-1);
                    teamName = teamName.substring(teamName.lastIndexOf("\"")+1);
                    
                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.JOINTEAM);
                    retEvent.setTeam(teamName);
                    return true;

                } else if (action.equals("changed role to"))
                {
                    String roleName = message.substring(0,message.length()-1);
                    roleName = roleName.substring(roleName.lastIndexOf("\"")+1);

                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.CHANGEROLE);
                    this.retEvent.setRole(roleName);
                    return true;
                    
                }else if (action.equals("set cash"))
                {
                    try {
                        int cash = Integer.parseInt(message.substring(message.lastIndexOf("$")+1));

                        this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.SETCASH);
                        this.retEvent.setCash(cash);
                        return true;
                        
                    } catch (NumberFormatException ex)
                    {
                        // warn?
                    }

                }else if (action.equals("say"))
                {
                    String msg = message.substring(message.indexOf("say",index+3)+4);
                    msg = msg.substring(1,msg.length()-1);

                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.SAY);
                    this.retEvent.setMessage(msg);
                    return true;
                    
                } else if (action.equals("teamsay"))
                {
                    String msg = message.substring(message.indexOf("teamsay",index+3)+8);
                    msg = msg.substring(1,msg.length()-1);

                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.TEAMSAY);
                    this.retEvent.setMessage(msg);
                    return true;

                } else if (action.equals("left the game"))
                {
                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.QUITGAME);
                    return true;
                } else if (action.equals("committed suicide"))
                {
                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.SUICIDED);
                    return true;
                } else if (action.equals("rescued the rescue item"))
                {
                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.RESCUEDITEM);
                    return true;
                } else if (action.equals("picked up the rescue item"))
                {
                    this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.PICKEDUPITEM);
                    return true;
                } else if (action.equals("sub cash"))
                {
                    try {
                        int cash = Integer.parseInt(message.substring(message.lastIndexOf("$")+1));

                        this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.SUBCASH);
                        this.retEvent.setCash(cash);
                        return true;

                    } catch (NumberFormatException ex)
                    {
                        // warn?
                    }
                } else if (action.equals("add cash"))
                {
                    try {
                        int cash = Integer.parseInt(message.substring(message.lastIndexOf("$")+1));

                        this.retEvent = new GossipEvent(playerName, null, 1, GossipEvent.GossipEventType.ADDCASH);
                        this.retEvent.setCash(cash);
                        return true;

                    } catch (NumberFormatException ex)
                    {
                        // warn?
                    }
                }
            }
            
        
        return false;
    }
    /*
     * L 01/16/2010 - 16:59:37: "" connected
L 01/16/2010 - 17:15:21: Log file closed
L 01/16/2010 - 17:15:26: Log file started
L 01/16/2010 - 16:59:41: "Player<>" entered the game
L 01/16/2010 - 16:59:44: "|GA| Artkayek<TERRORIST>" sub cash $600
L 01/16/2010 - 17:13:27: "|GA| Artkayek<CT>" killed "Player<TERRORIST>" with "MP5/10" (17)
L 01/16/2010 - 17:13:38: "|GA| Artkayek<CT>" committed suicide
L 01/16/2010 - 17:00:21: "|GA| Artkayek<TERRORIST>" healed "Player<TERRORIST>" for 10.000000
L 01/16/2010 - 17:00:24: "|GA| Artkayek<TERRORIST>" healed "|GA| Artkayek<TERRORIST>" for 8.000000
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" teamkilled "|GA| Artkayek<TERRORIST>" with "AK 47" (21)
L 01/16/2010 - 17:01:24: "|GA| Artkayek<TERRORIST>" picked up the rescue item
L 01/16/2010 - 17:02:40: "|GA| Artkayek<TERRORIST>" rescued the rescue item
L 01/16/2010 - 17:02:40: "UNKNOWN PERSON<>" completed objective "IDS_OBJECTIVE1_ANT_CAPTURE"
L 01/16/2010 - 17:02:41: Team "Krongen" loses
L 01/16/2010 - 17:02:41: Team "Krongen" receiving cash 3000
L 01/16/2010 - 17:02:41: Team "PSI" wins
L 01/16/2010 - 17:02:41: Team "PSI" receiving cash 4000
L 01/16/2010 - 17:02:41: "Player<TERRORIST>" add cash $4000
L 01/16/2010 - 17:02:41: "|GA| Artkayek<TERRORIST>" add cash $4000
L 01/16/2010 - 17:02:56: Server say "Teams will be auto balanced at the next end of round"
L 01/16/2010 - 17:03:23: Server say "Map will switch at the next end of round"
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" sub cash $50
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" sub cash $50
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" sub cash $50
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" sub cash $50
L 01/16/2010 - 17:03:29: "Player<TERRORIST>" sub cash $700
L 01/16/2010 - 17:03:45: "|GA| Artkayek<CT>" add cash $0
L 01/16/2010 - 17:03:46: "|GA| Artkayek<CT>" add cash $0
L 01/16/2010 - 17:03:47: "|GA| Artkayek<CT>" sub cash $750
L 01/16/2010 - 17:03:48: "|GA| Artkayek<CT>" add cash $750
*/
}
