import java.util.*;

/**
 * <b> CS 180 - Project 4 - Chat Server Skeleton </b>
 * <p>
 *
 * This is the skeleton code for the ChatServer Class. This is a private chat
 * server for you and your friends to communicate.
 *
 * @authors Daniel Piotti <(dpiotti@purdue.edu)> && Tyler Jonites
 *          <(tjonites@purdue.edu)>
 *
 * @lab (817)
 *
 * @version (11/17/15)
 *
 */
public class ChatServer {

    User[] users = null;
    CircularBuffer cb = null;

    public ChatServer(User[] users, int maxMessages) {
        synchronized (this) {

            this.cb = new CircularBuffer(maxMessages);
            ArrayList<User> userList = new ArrayList<User>();
            User[] newUsers = null;
            if (users != null && users.length > 0) {

                for (int i = 0; i < users.length; i++) {
                    if (users[i] != null)
                        userList.add(users[i]);
                }

                newUsers = new User[userList.size() + 1];
                newUsers[0] = new User("root", "cs180", null);

                for (int i = 0; i < userList.size(); i++) {
                    newUsers[i + 1] = userList.get(i);
                }

                // newUsers[newUsers.length - 1] = new User("root", "cs180",
                // null);

            } else {
                newUsers = new User[1];
                newUsers[0] = new User("root", "cs180", null);
            }

            this.users = newUsers;

        }
        // users[0] = new User("root", "cs180", null);
        // users[0].setCookie(null);
    }

    /**
     * This method begins server execution.
     */
    public void run() {
        boolean verbose = false;
        System.out.printf("The VERBOSE option is off.\n\n");
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.printf("Input Server Request: ");
            String command = in.nextLine();

            // this allows students to manually place "\r\n" at end of command
            // in prompt
            command = replaceEscapeChars(command);

            if (command.startsWith("kill"))
                break;

            if (command.startsWith("verbose")) {
                verbose = !verbose;
                System.out.printf("VERBOSE has been turned %s.\n\n",
                        verbose ? "on" : "off");
                continue;
            }

            String response = null;
            try {
                response = parseRequest(command);
            } catch (Exception ex) {
                response = MessageFactory.makeErrorMessage(
                        MessageFactory.UNKNOWN_ERROR,
                        String.format("An exception of %s occurred.",
                                ex.getMessage()));
            }

            // change the formatting of the server response so it prints well on
            // the terminal (for testing purposes only)
            if (response.startsWith("SUCCESS\t"))
                response = response.replace("\t", "\n");

            // print the server response
            if (verbose)
                System.out.printf("response:\n");
            System.out.printf("\"%s\"\n\n", response);
        }

        in.close();
    }

    /**
     * Replaces "poorly formatted" escape characters with their proper values.
     * For some terminals, when escaped characters are entered, the terminal
     * includes the "\" as a character instead of entering the escape character.
     * This function replaces the incorrectly inputed characters with their
     * proper escaped characters.
     *
     * @param str
     *            - the string to be edited
     * @return the properly escaped string
     */
    private static String replaceEscapeChars(String str) {
        str = str.replace("\\r", "\r");
        str = str.replace("\\n", "\n");
        str = str.replace("\\t", "\t");

        return str;
    }

    /**
     * Determines which client command the request is using and calls the
     * function associated with that command.
     *
     * @param request
     *            - the full line of the client request (CRLF included)
     * @return the server response
     */
    public String parseRequest(String request) {
        // replace any improperly formatted escape chars from scanner input

        synchronized (this) {


            String cleanRequest = replaceEscapeChars(request);

            // format check
            if (!cleanRequest.endsWith("\r\n")) {
                return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
            }

            cleanRequest = cleanRequest.replace("\r\n", "");

            String[] userCommands = cleanRequest.split("\t");

            // should be > 1 param
            if (userCommands.length < 1)
                return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);

            if (userCommands[0].equals("ADD-USER")) {

                if (userCommands.length != 4)
                    return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);

                boolean hasCookie = false;
                int index = -1;

                long cookieID;

                try {
                    cookieID = (long) Integer.parseInt(userCommands[1]);
                } catch (Exception e) {
                    return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
                }

                for (int i = 0; i < users.length; i++) {
                    if (users[i].getCookie() != null
                            && users[i].getCookie().getID() == cookieID)
                        index = i;

                    if (users[i].getName().equals(userCommands[2]))
                        return MessageFactory.makeErrorMessage(MessageFactory.USER_ERROR);
                }

                if (index == -1)
                    return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);

                // user already added
                if (users[index].getName().equals(userCommands[2]))
                    return MessageFactory.makeErrorMessage(MessageFactory.USER_ERROR);

                // see if cookie is valid and has NOT timed out
                if (users[index].getCookie().getID() == cookieID
                        && !users[index].getCookie().hasTimedOut()) {
                    users[index].getCookie().updateTimeOfActivity();
                    hasCookie = true;
                }
                // see if cookie is valid and HAS timed out + set cookie to null
                if (users[index].getCookie().getID() == cookieID
                        && users[index].getCookie().hasTimedOut()) {
                    users[index].setCookie(null);
                    return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
                }

                // //see if cookie is NOT valid
                if (!hasCookie)
                    return MessageFactory.makeErrorMessage(MessageFactory.UNKNOWN_ERROR);

                return addUser(userCommands);

            } else if (userCommands[0].equals("USER-LOGIN")) {
                if (userCommands.length != 3)
                    return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);

                boolean userExists = false;

                // if (userCommands.length != 2)
                // return
                // MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);

                for (int i = 0; i < users.length; i++) {

                    if (users[i].getName() != null
                            && users[i].getName().equals(userCommands[1]))
                        userExists = true;

                    if (users[i].getName() != null
                            && users[i].getName().equals(userCommands[1])
                            && users[i].getCookie() != null)
                        return MessageFactory.makeErrorMessage(MessageFactory.USER_CONNECTED_ERROR);

                    if (users[i].getName() != null
                            && users[i].getName().equals(userCommands[1])
                            && !users[i].checkPassword(userCommands[2]))
                        return MessageFactory.makeErrorMessage(MessageFactory.AUTHENTICATION_ERROR);

                    else if (users[i].getName() != null
                            && users[i].getName().equals(userCommands[1])
                            && users[i].getCookie() == null)
                        return userLogin(userCommands);

                }

                if (!userExists)
                    return MessageFactory.makeErrorMessage(MessageFactory.USERNAME_LOOKUP_ERROR);

            } else if (userCommands[0].equals("POST-MESSAGE")) {

                boolean noUserPostMessage = true;
                boolean cookieExists = false;
                int index = -1;

                if (userCommands.length != 3)
                    return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);


                String username = "";
                long cookieID;
                String trimmedString = userCommands[2].trim();

                if (!trimmedString.trim().isEmpty())
                    userCommands[2] = trimmedString;
                else
                    return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);

                try {
                    cookieID = (long) Integer.parseInt(userCommands[1]);
                } catch (Exception e) {
                    return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
                }

                for (int i = 0; i < users.length; i++) {
                    if (users[i].getCookie() != null
                            && users[i].getCookie().getID() == cookieID)
                        index = i;
                }

                if (index == -1)
                    return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);

                // USER NOT LOGGED IN
                if (users[index].getCookie().getID() == cookieID)
                    cookieExists = true;

                // if (cookieExists)
                // return
                // MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);

                if (users[index].getCookie().getID() == cookieID
                        && !users[index].getCookie().hasTimedOut()) {
                    username = users[index].getName();
                    users[index].getCookie().updateTimeOfActivity();
                }

                if (users[index].getCookie().getID() == cookieID
                        && users[index].getCookie().hasTimedOut())
                    return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);

                if (username.equals(""))
                    return MessageFactory.makeErrorMessage(MessageFactory.USER_ERROR);

                return postMessage(userCommands, username);

            } else if (userCommands[0].equals("GET-MESSAGES")) {
                boolean cookieExists = false;
                int index = -1;

                if (userCommands.length != 3)
                    return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);




                long cookieID;

                try {
                    cookieID = (long) Integer.parseInt(userCommands[1]);
                } catch (Exception e) {
                    return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
                }

                for (int i = 0; i < users.length; i++) {
                    if (users[i].getCookie() != null
                            && users[i].getCookie().getID() == cookieID)
                        index = i;
                }

                if (index == -1)
                    return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);

                // timeout error
                if (users[index].getCookie() != null
                        && users[index].getCookie().getID() == cookieID
                        && users[index].getCookie().hasTimedOut()) {
                    users[index].setCookie(null);
                    return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
                }

                // USER NOT LOGGED IN
                if (users[index].getCookie() != null
                        && users[index].getCookie().getID() == cookieID)
                    cookieExists = true;

                // null cookie??
                if (cookieExists == false)
                    return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);

                // update time of activity
          /*  if (users[index].getCookie().getID() == cookieID
                    && !users[index].getCookie().hasTimedOut()) {
                users[index].getCookie().updateTimeOfActivity();
            }*/

                try {
                    if (Integer.parseInt(userCommands[2]) < 1)
                        return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
                } catch (Exception e) {
                    return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
                }

                return getMessages(userCommands);
            }

            return MessageFactory.makeErrorMessage(MessageFactory.UNKNOWN_COMMAND_ERROR);
        }
    }

    // Usernames and passwords can only contain alphanumerical values
    // [A-Za-z0-9].
    // Usernames must be between 1 and 20 characters in length (inclusive).
    // Password must be between 4 and 40 characters in length (inclusive).

    public String addUser(String[] args) {

        int index = -1;
        long cookieID;

        try {
            cookieID = (long) Integer.parseInt(args[1]);
        } catch (Exception e) {
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
        }

        for (int i = 0; i < users.length; i++) {
            if (users[i].getCookie() != null
                    && users[i].getCookie().getID() == cookieID)
                index = i;

            if (users[i].getName().equals(args[2]))
                return MessageFactory.makeErrorMessage(MessageFactory.USER_ERROR);

        }

        if (index == -1)
            return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);


        if (users[index].getCookie().getID() == cookieID
                && users[index].getCookie().hasTimedOut()) {
            users[index].setCookie(null);
            return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
        }



        if (!args[2].matches("^[a-zA-Z0-9]+$")
                || !args[3].matches("^[a-zA-Z0-9]+$") || args[2].length() < 1
                || args[2].length() > 20 || args[3].length() < 4
                || args[3].length() > 40)
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);


        User[] newUsers = new User[users.length + 1];
        for (int i = 0; i < users.length; i++)
            newUsers[i] = users[i];

        newUsers[newUsers.length - 1] = new User(args[2], args[3], null);

        users = newUsers;



        return "SUCCESS\r\n";


    }

    public String userLogin(String[] args) {


        for (int i = 0; i < users.length; i++) {
            if (users[i].getName() != null
                    && users[i].getName().equals(args[1])
                    && users[i].getCookie() != null)
                return MessageFactory.makeErrorMessage(MessageFactory.USER_CONNECTED_ERROR);
        }




        // conditions
        // the user must have been created through add user method
        // the given user shouldn't already be authenticated. The session cookie
        // should be null
        // the pw must be correct

        //
        Random r = new Random();
        long randomCookie = (long) r.nextInt(10000);

        // fail until succeed
        String fail = MessageFactory.makeErrorMessage(MessageFactory.USERNAME_LOOKUP_ERROR);

        String formattedCookie = null;
        boolean loginOk = false;

        // protect against multi-threaded access

        for (int i = 0; i < users.length; i++) {
            if (users[i].getCookie() != null
                    && users[i].getCookie().getID() == randomCookie) {
                randomCookie = r.nextInt() * 10000;
                i = 0;
            }
            if (users[i].getName() != null
                    && users[i].getName().equals(args[1])
                    && !users[i].checkPassword(args[2]))
                return MessageFactory.makeErrorMessage(MessageFactory.AUTHENTICATION_ERROR);

            if (users[i].getName() != null
                    && users[i].getName().equals(args[1])
                    && users[i].checkPassword(args[2])) {
                SessionCookie cookie = new SessionCookie(randomCookie);
                users[i].setCookie(cookie);
                formattedCookie = String.format("%04d", randomCookie);
                loginOk = true;

            }
        }


        if (loginOk)
            return "SUCCESS\t" + formattedCookie + "\r\n";

        return fail;

    }

    public String postMessage(String[] args, String name) {

        if (args[2] == null || args[2].trim().equals("")) {
            return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);
        }


        String message = name + ": " + args[2];
        cb.put(message);



        return "SUCCESS\r\n";

    }

    public String getMessages(String[] args) {

        try {

            if (Integer.parseInt(args[2]) < 1)
                return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR);

        } catch (Exception e) { return MessageFactory.makeErrorMessage(MessageFactory.INVALID_VALUE_ERROR); }

        String[] response = cb.getNewest(Integer.parseInt(args[2]));
        String messages = "SUCCESS\t";

        for (int i = 0; i < response.length; i++) {
            messages += response[i];
            if (i !=  response.length - 1)
                messages += "\t";

        }



        return messages + "\r\n";

    }
}

