/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.interpreters;

import go.responses.ServerInfoResponse;

public class ServerInfoParser {
    private static ServerInfoParser _this;
    private ServerInfoParser(){}
    public static ServerInfoParser getInstance()
    {
        if (_this == null)
            _this = new ServerInfoParser();
        return _this;
    }
    public ServerInfoResponse parseServerinfo(String line)
    {
        String hostname = line.substring(5,line.indexOf(","));
        String rest = line.substring(line.indexOf(",")+1);
        String[] elements = rest.split(",");
        int cntr = 0;
        int port = -1;
        String map = "";
        int numplayers = -1;
        int maxplayers = -1;
        int os = -1;
        int password = -1;
        String timeleft = "";
        for (String element : elements)
        {
            cntr++;
            String[] pairs = element.split("=");
            switch (cntr)
            {
                case 1: port = Integer.parseInt(pairs[1]);break;
                case 2: map = pairs[1];break;
                case 3: numplayers = Integer.parseInt(pairs[1]);break;
                case 4: maxplayers = Integer.parseInt(pairs[1]);break;
                case 5: os = Integer.parseInt(pairs[1]);break;
                case 6: password = Integer.parseInt(pairs[1]);break;
                case 7: timeleft = pairs[1];break;
            }
        }
        ServerInfoResponse si = new ServerInfoResponse();
        si.setHostname(hostname);
        si.setMap(map);
        si.setMaxplayers(maxplayers);
        si.setNumplayers(numplayers);
        si.setOs(os);
        si.setPassword(password);
        si.setPort(port);
        si.setTimeleft(timeleft);
        return si;

    }
}
