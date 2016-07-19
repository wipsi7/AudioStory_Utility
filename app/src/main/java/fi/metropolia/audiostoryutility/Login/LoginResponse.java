package fi.metropolia.audiostoryutility.Login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("api_key")
    @Expose
    private String api_key;

    public String getApi_key() {
        return api_key;
    }
}
