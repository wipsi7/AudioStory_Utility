package fi.metropolia.audiostoryutility.interfaces;

import fi.metropolia.audiostoryutility.Login.LoginRequest;
import fi.metropolia.audiostoryutility.Login.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {

    @POST("plugins/api_auth/auth.php")
    Call<LoginResponse> getApiKey(@Body LoginRequest loginRequest);
}
