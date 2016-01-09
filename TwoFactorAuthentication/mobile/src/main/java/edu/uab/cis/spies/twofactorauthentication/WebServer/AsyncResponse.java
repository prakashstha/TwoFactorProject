package edu.uab.cis.spies.twofactorauthentication.WebServer;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface AsyncResponse {
    void handleResponse(String json);
}
