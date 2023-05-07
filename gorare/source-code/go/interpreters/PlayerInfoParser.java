package go.interpreters;

import go.objects.Player;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */




public class PlayerInfoParser {
    private static PlayerInfoParser _this;
    private PlayerInfoParser(){}
    public static PlayerInfoParser getInstance()
    {
        if (_this == null)
            _this = new PlayerInfoParser();
        return _this;
    }

    public Player parsePlayerInfoLine(String playerInfoLine) throws InvalidPlayerInfoLineException
    {
        // 0,ClientID=1,Name=|GA| Artkayek| Day4,Team=1074,Specialty=2363,Ping=4
        
        Player p = new Player();
        p.setNumber(getNumber(playerInfoLine));
        p.setClientId(getCliendId(playerInfoLine));
        p.setName(getName(playerInfoLine));
        p.setPing(getPing(playerInfoLine));
        p.setSpecialty(getSpecialty(playerInfoLine));
        p.setTeam(getTeam(playerInfoLine));

        return p;
    }
    private String getTeam(String line) throws InvalidPlayerInfoLineException
    {
        try {
            int startIndex = line.lastIndexOf("am=")+3;
            String team = line.substring(startIndex,line.indexOf(",",startIndex));
            return team;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable team");
        }
    }
    private String getSpecialty(String line) throws InvalidPlayerInfoLineException
    {
        try {
            int startIndex = line.lastIndexOf("ty=")+3;
            String spec = line.substring(startIndex,line.lastIndexOf(","));
            return spec;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable specialty");
        }
    }
    private int getPing(String line) throws InvalidPlayerInfoLineException
    {
        try {
            int startIndex = line.lastIndexOf("ng=")+3;
            String strPing = line.substring(startIndex);
            int ping = Integer.parseInt(strPing);
            return ping;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable ping");
        }
    }
    private String getName(String line) throws InvalidPlayerInfoLineException
    {
        try {
            int startIndex = line.indexOf("me=")+3;
            int lastDelimiter = line.lastIndexOf(",");
            int specDelimiter = line.substring(startIndex,lastDelimiter).lastIndexOf(",") + startIndex;
            int teamDelimiter = line.substring(startIndex,specDelimiter).lastIndexOf(",") + startIndex;
            String name = line.substring(startIndex,teamDelimiter);
            return name;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable name");
        }
    }
    private int getCliendId(String line) throws InvalidPlayerInfoLineException
    {
        try {
            int startIndex = line.indexOf("ID=")+3;
            String strClientId = line.substring(startIndex,line.indexOf(",",startIndex));
            int clientId = Integer.parseInt(strClientId);
            return clientId;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable clientId");
        }
    }
    private int getNumber(String line) throws InvalidPlayerInfoLineException
    {

        try {
            String strNumber = line.substring(0,line.indexOf(","));
            if (strNumber.startsWith(" "))
                strNumber = strNumber.substring(1);
            int number = Integer.parseInt(strNumber);
            return number;
        } catch (Exception ex)
        {
            throw new InvalidPlayerInfoLineException(line, "Unparsable line number");
        }

    }
}
