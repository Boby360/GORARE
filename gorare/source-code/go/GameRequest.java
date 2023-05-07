package go;

import go.responses.CustomResponse;
import go.responses.PlayerInfoResponse;
import go.responses.ServerInfoResponse;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Charles
 */
public class GameRequest {
    private String rawRequest;

    public GameRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }
    
    /**
     * Wether this request is waiting for the give response
     * @param response The response that is being offered
     * @return True if this is the correct response for this request, false otherwise
     */
    public boolean wantsResponse(GameResponse response)
    {
        if (response instanceof CustomResponse)
        {
            String expectedLine = "COMMAND " + getRawRequest() + " executed on the server!";
            CustomResponse cResp = (CustomResponse) response;
            if (cResp.getRawResponse().equals(expectedLine))
                return true;
        }
        
        if (response instanceof PlayerInfoResponse)
        {
            if (getRawRequest().equals("playerinfo"))
                return true;
        }
        if (response instanceof ServerInfoResponse)
        {
            if (getRawRequest().equals("serverinfo"))
                return true;
        }
        
        return false;
    }

    public boolean isCustom() {
        if (getRawRequest().equals("serverinfo") || getRawRequest().equals("playerinfo"))
            return false;
        return true;
    }
    public String getRawRequest()
    {
        return rawRequest;
    }
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof GameRequest)
        {
            GameRequest you = (GameRequest) o;
            if (you.getRawRequest().equals(getRawRequest()))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.rawRequest != null ? this.rawRequest.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString()
    {
        return rawRequest;
    }
}
