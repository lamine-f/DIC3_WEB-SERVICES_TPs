import java.util.HashMap;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;

public class ChatRoomServer {

    private final HashMap<String, Integer> subscribers = new HashMap<>();

    public ChatRoomServer() {
    }

    public boolean subscribe(String username, int clientPort) {
        if (subscribers.containsKey(username)) {
            System.out.println("Subscription failed: " + username + " is already subscribed.");
            return false;
        }
        subscribers.put(username, clientPort);
        System.out.println(username + " has subscribed on port " + clientPort);
        return true;
    }

    public boolean unsubscribe(String username) {
        if (!subscribers.containsKey(username)) {
            System.out.println("Unsubscription failed: " + username + " is not subscribed.");
            return false;
        }
        subscribers.remove(username);
        System.out.println(username + " has unsubscribed.");
        return true;
    }

    public boolean postMessage(String username, String message) {
        System.out.println("Message from " + username + ": " + message);

        for (String clientUsername : subscribers.keySet()) {
            if (!clientUsername.equals(username)) {
                Integer clientPort = subscribers.get(clientUsername);
                try {
                    XmlRpcClient client = new XmlRpcClient("http://localhost:" + clientPort + "/RPC2");
                    Vector<Object> params = new Vector<>();
                    params.addElement(username);
                    params.addElement(message);
                    client.execute("client.displayMessage", params);
                } catch (Exception e) {
                    System.err.println("Failed to send message to " + clientUsername + ": " + e.getMessage());
                }
            }
        }
        return true;
    }
}
