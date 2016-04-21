package fi.metropolia.audiostoryutility.tasks;

import android.os.AsyncTask;

import fi.metropolia.audiostoryutility.interfaces.AsyncResponse;
import fi.metropolia.audiostoryutility.server.ServerConnection;

/**
 * Created by wipsi on 4/18/2016.
 */
public class LoginTask extends AsyncTask<String, Void, ServerConnection> {
    public AsyncResponse onLoginResult = null;
    private String apiKey = null;

    private String DEBUG_TAG = "LoginTask";

    public void setOnLoginResult(AsyncResponse onLoginResult) {
        this.onLoginResult = onLoginResult;
    }

    protected ServerConnection doInBackground(String ...params){

        ServerConnection serverConnection = new ServerConnection();

        try {
            serverConnection.auth(params[0], params[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serverConnection;
    }

    protected void onPostExecute(ServerConnection result){
        onLoginResult.onProcessFinish(result);
    }
}
