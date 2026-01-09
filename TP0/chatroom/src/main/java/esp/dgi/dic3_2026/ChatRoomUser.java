package esp.dgi.dic3_2026;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatRoomUser extends Remote {
    void displayMessage(String pseudonym, String message) throws RemoteException;
}
