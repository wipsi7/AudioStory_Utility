package fi.metropolia.audiostoryutility.Login;

public class LoginCredentials {

    private String user;
    private String pass;
    private String id;

    public LoginCredentials(){
        this.user = null;
        this.pass = null;
        this.id = null;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
