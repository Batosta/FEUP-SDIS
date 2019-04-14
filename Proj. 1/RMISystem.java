import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMISystem extends Remote {

	void backupData(String path, int replicationDeg) throws RemoteException;
	void deleteData(String path) throws RemoteException;
	void restoreData(String path) throws RemoteException;
	void reclaimSpace(int spaceReclaimed) throws RemoteException;
	void peerState() throws RemoteException;
}
