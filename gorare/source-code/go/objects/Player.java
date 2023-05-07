package go.objects;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author Charles
 */
public class Player {
    private int number;
    private int clientId;
    private String name;
    private String team;
    private String specialty;
    private int ping;    

    public int getClientId() {
        return clientId;
    }
    public String toString()
    {
        String ret = "[Player]";
        ret += "\n" + "Number    :" + getNumber();
        ret += "\n" + "ClientId  :" + getClientId();
        ret += "\n" + "Name      :" + getName();
        ret += "\n" + "Team      :" + getTeam();
        ret += "\n" + "Specialty :" + getSpecialty();
        ret += "\n" + "Ping      :" + getPing();
        return ret;
    }
    public String toRawLine()
    {
        // 0,ClientID=1,Name=|GA| Artkayek,a,a,a,Team=1085,Specialty=2363,Ping=4
        return " " + number + ",ClientID="+clientId+",Name=" + name + ",Team=" + team + ",Specialty="+specialty+",Ping="+ping;
    }
    public String toGRLine()
    {
        String ret = "";
        ret += number + ","+clientId+","+team+","+specialty+","+ping+"|"+name;
        return ret;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
    
}
