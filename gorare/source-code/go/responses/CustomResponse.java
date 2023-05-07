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
public class CustomResponse extends GameResponse {
    private String rawResponse;

    public CustomResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getRawResponse() {
        return rawResponse;
    }
    
    @Override
    public String toString()
    {
        return "[Custom Response]\nRaw response : " + rawResponse;
    }

    @Override
    public String toRawResponse() {
        return rawResponse;
    }

    @Override
    public String toGRResponse() {
        return toRawResponse() + (char) 0;
    }
}
