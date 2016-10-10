/**
 *
 * @author Daniel Piotti (dpiotti@purdue.edu)
 * 
 */

public class User {

    private String username;
    private String password;
    private SessionCookie cookie;


    public User(String username, String password, SessionCookie cookie) {
        this.username = username;
        this.password = password;
        this.cookie = cookie;


    }

    public String getName() {
        return this.username;

    }

    public boolean checkPassword(String password) {
        if (password.equals(this.password))
            return true;

        return false;

    }

    public SessionCookie getCookie() {
        return this.cookie;

    }

    public void setCookie(SessionCookie cookie) {

        this.cookie = cookie;

    }

}
