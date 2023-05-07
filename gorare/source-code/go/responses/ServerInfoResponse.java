/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.responses;

import go.GameResponse;

/**
 *
 * @author Charles
 */
public class ServerInfoResponse extends GameResponse {
    private String hostname = "unknown";
    private int port = 28672;
    private String map = "worlds/unknown";
    private String timeleft = "10:00";
    private int numplayers;
    private int maxplayers = 2;
    private int os = 1;
    private int password = 0;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getMaxplayers() {
        return maxplayers;
    }

    public void setMaxplayers(int maxplayers) {
        this.maxplayers = maxplayers;
    }

    public int getNumplayers() {
        return numplayers;
    }

    public void setNumplayers(int numplayers) {
        this.numplayers = numplayers;
    }

    public int getOs() {
        return os;
    }

    public void setOs(int os) {
        this.os = os;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTimeleft() {
        return timeleft;
    }

    public void setTimeleft(String timeleft) {
        this.timeleft = timeleft;
    }
    @Override
    public String toString()
    {
        return "[ServerInfo]\n"+ toRawResponse();
    }

    @Override
    public String toRawResponse() {
        return "Host=" + hostname + ",Port=" + port + ",Map="+map+",Numplayers="+numplayers+",Maxplayers="+maxplayers+",OS="+os+",Password="+password+",Timeleft="+timeleft;
    }

    @Override
    public String toGRResponse() {
        String ret = "";
        ret += hostname + "\n";
        ret += port + "\n";
        ret += map + "\n";
        ret += numplayers + "\n";
        ret += maxplayers + "\n";
        ret += os + "\n";
        ret += password + "\n";
        ret += timeleft + "\n";
        ret += (char) 0;
        return ret;
    }
    
}
