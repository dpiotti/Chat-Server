
public class LaunchServer {

    /**
     * This main method is for testing purposes only.
     * @param args - the command line arguments
     */
    public static void main(String[] args) {
        // Create a ChatServer and start it
        (new ChatServer(new User[1], 10)).run();
    }

}

//USER-LOGIN\troot\tcs180\r\n