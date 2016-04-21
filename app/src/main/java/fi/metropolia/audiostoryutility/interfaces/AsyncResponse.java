package fi.metropolia.audiostoryutility.interfaces;


import fi.metropolia.audiostoryutility.server.ServerConnection;

public interface AsyncResponse {

    void onProcessFinish(ServerConnection result);

}

