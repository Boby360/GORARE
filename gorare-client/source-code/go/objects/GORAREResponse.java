/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package go.objects;

/**
 *
 * @author Charles
 */
public class GORAREResponse extends GameResponse{

    private String rawResponse;

    public GORAREResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    @Override
    public String toRawResponse() {
        return rawResponse;
    }

}
