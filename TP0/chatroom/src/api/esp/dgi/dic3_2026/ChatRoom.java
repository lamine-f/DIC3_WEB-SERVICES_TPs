package esp.dgi.dic3_2026;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatRoom extends Remote {
    void subscribe(MessageListener client, String username) throws RemoteException;
    void unsubscribe(String username) throws RemoteException;
    void postMessage(String username, String message) throws RemoteException;
}
