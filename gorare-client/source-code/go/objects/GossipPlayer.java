/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.objects;

import java.util.HashMap;

/**
 *
 * @author Charles
 */
public class GossipPlayer {
    private String role = "unknownrole";
    private String name = "unknownname";
    private int ping = -1;
    private String team = "unknownteam";
    private int clientId = -1;
    private HashMap<String,Object> lstSettings = new HashMap<String, Object>();
    public GossipPlayer(String name)
    {
        this.name = name;
    }
    public void putSetting(String key,String value)
    {
        lstSettings.put(key, value);
    }
    public void putSetting(String key,int value)
    {
        lstSettings.put(key, value);
    }
    public void putSetting(String key,float value)
    {
        lstSettings.put(key, value);
    }
    public float getSettingAsFloat(String key)
    {
        Object obj = lstSettings.get(key);
        float ret = -1;
        if (obj != null)
        {
            String objVal = obj.toString();
            try {
                ret = Float.parseFloat(objVal);
            } catch (NumberFormatException ex)
            {
                //ignore
            }
        }
        return ret;
    }
    public String getSettingAsString(String key)
    {
        Object obj = lstSettings.get(key);
        String ret = "";
        if (obj != null)
        {
            ret = String.valueOf(obj);
        }
        return ret;
    }
    public int getSettingAsInt(String key)
    {
        Object obj = lstSettings.get(key);
        int ret = -1;
        if (obj != null)
        {
            String objVal = obj.toString();
            try {
                ret = Integer.parseInt(objVal);
            } catch (NumberFormatException ex)
            {
                //ignore
            }
        }
        return ret;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }


    public String getTeam() {
        return team;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTeam(String team) {
        this.team = team;
    }
    @Override
    public String toString()
    {
        String ret = "[GossipPlayer]";
        ret += "\n" + "ClientId  :" + getClientId();
        ret += "\n" + "Name      :" + getName();
        ret += "\n" + "Team      :" + getTeam();
        ret += "\n" + "Role      :" + getRole();
        ret += "\n" + "Ping      :" + getPing();
        return ret;
    }
}
