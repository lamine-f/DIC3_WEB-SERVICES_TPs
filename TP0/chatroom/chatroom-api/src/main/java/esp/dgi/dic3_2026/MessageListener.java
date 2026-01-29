package esp.dgi.dic3_2026;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MessageListener extends Remote {
    void displayMessage(String username, String message) throws RemoteException;
}
