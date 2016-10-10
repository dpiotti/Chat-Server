/**
 *
 * @author Daniel Piotti (dpiotti@purdue.edu)
 *
 */
public class SessionCookie {


    public static int timeoutLength = 300;
    //id in range 0000 - 9999

    private long lastAccessTime;

    private long id = -1;

    public SessionCookie(long id) {

        this.id = id;
        updateTimeOfActivity();
    }

    public long getID() {
        return this.id;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void updateTimeOfActivity() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean hasTimedOut() {

        if (System.currentTimeMillis() - this.getLastAccessTime() > (timeoutLength * 1000)) return true;

        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        SessionCookie.timeoutLength = 1; //change timeout interval to 1 second

        SessionCookie cookie = new SessionCookie(1234);

        System.out.println(cookie.hasTimedOut()); // should print false

        Thread.sleep(1200); // Sleep for 1.2 seconds

        System.out.println(cookie.hasTimedOut()); // should print true
    }



}
