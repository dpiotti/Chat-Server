/**
 * @authors Daniel Piotti (dpiotti@purdue.edu)
 *
 */
public class CircularBuffer {

    private int postedMessages = 0; //posted messages
    private int messageIndex = 0; // index for inserting messages into buffer

    private String[] messages = null;

    //track head (oldest message), initially at messages[0] but then shifts as messages wrap around
    private int head = 0; //head starts at 0 and then moves once the buffer starts to overwrite

    // track number of elements in queue
    private int numElements = 0;

    public CircularBuffer(int size) {
        messages = new String[size];
    }

    // message prepends a 4 digit number
    // to the method to form a new string, 0000-9999 and wraps around
    // "0000) root: hello"
    // this method adds the new message to the tail of the buffer
    // if the buffer becomes full, the oldest method is overwritten

    public void put(String message) {

        int number = postedMessages % 10000;
        String displayCount = String.format("%04d) ", number);

        String formattedMessage = displayCount + message;
        messages[messageIndex] = formattedMessage;
        messageIndex = (messageIndex + 1) % messages.length;

        //track the head location which changes on wrap around
        if (numElements == messages.length) {
            head = (head + 1) % messages.length;
        } else {
            numElements++;
        }

        postedMessages++;
    }

    private int getTail () {
        int tail = 0;
        if (postedMessages != 0  ) {
            tail = messageIndex - 1;
            if (tail < 0 ) { //wrap backwards if head is 0
                tail = messages.length - 1;
            }
        }

        return tail;
    }

    //debug string
    public String toString() {

        int tail = getTail();

        String bufferState = postedMessages == 0 ? "No Messages Posted yet" : "After Insert (incremented counts) # "
                + postedMessages;

        StringBuffer sb = new StringBuffer ("BUFFER STATE " +
                bufferState + "\n[  messages.length=" + messages.length +
                ", numElements=" + numElements + ", postedMessages=" +
                postedMessages + ",  messageIndex=" + messageIndex +
                " head(oldest message)=" + head + " tail(newest)=" + tail
                + " \nmessages=\n");

        for (int i = 0; i < messages.length; i++) {
            sb.append("position " + i + ": " + messages[i] + "\n");
        }

        sb.append("]\n\n");
        return sb.toString();

    }

    int msgGen = 0;
    private void addTestMessages(int cnt, boolean verbose) {

        for (int i = 0 ; i < cnt; i++) {
            msgGen++;
            this.put("CS180" + msgGen);

            if (verbose)
                System.out.println(this.toString());

        }

        if (!verbose) //just print last buffer result
            System.out.println(this.toString());


    }

    //TODO: create a JUNIT
    public static void main(String[] args) {

        CircularBuffer cb = new CircularBuffer(10);
        System.out.println(cb.toString());
        cb.addTestMessages(15, true);
        print(cb.getNewest(10));
        // print(cb.getNewest(2));

        //cb.addTestMessages(9998,true);
        //print(cb.getNewest(1));

        //test requested messages > posted
        // String[] r0 = cb.getNewest(1);
        // assertEquals("No messages posted test", r0,new String[0]);


    }

    //debug print getNewest
    public static void print(String[] array) {
        System.out.println("\n" + array.length + " Newest message(s):");
        for (String msg: array) {
            System.out.println("\t" + msg);
        }
    }



    public String[] getNewest(int numMessages) {

        if (numMessages < 0) {
            return null; // invalid number of messages should return null
        } else if (messages.length == 0) {  //Circular Buffer was created with size 0 so it can't hold messages
            return null;
        } else if (numMessages == 0) { // rqmt: if 0 messages are requested return empty array
            String[] s = new String[0];
            return s;
        } else if (postedMessages == 0) { //no messages posted to chatroom yet
            String[] s = new String[0];
            return s;
        }

        int availableMessages = 0; // number of messages in buffer for response
        if (postedMessages > 0 && postedMessages >= messages.length)
            availableMessages = messages.length;
        else
            availableMessages = postedMessages;

        // rqmt: return the least of available or numMessages
        int msgCount = Math.min(availableMessages, numMessages);

        String[] results = new String[msgCount];

        int tail = getTail();

        int resultIndex = tail; //start at newest message and proceed backwards
        int i = 0;


        while (msgCount != 0) {

            //wrap around check
            if (resultIndex < 0) {
                resultIndex = messages.length - 1;
            }

            results[i] = messages[resultIndex];

            i++;
            resultIndex--; //message Index
            msgCount--; //loop exit condition
        }


        return reverse(results);
    }


    private String[] reverse(String[] inArray) {
        String[] outArray = new String[inArray.length];
        int j = 0;
        for (int i = inArray.length; i > 0; i-- ) {
            outArray[j] = inArray[i - 1];
            j++;
        }

        return outArray;
    }


}




