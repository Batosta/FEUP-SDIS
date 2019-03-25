import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


public class Peer implements RMISystem{

	//private static threads

	private static int ID;

	private static MulticastControl MulticastControl;
	private static MulticastBackup MulticastBackup;
	private static MulticastRestore MulticastRestore;


	private Peer(String addressMC, int portMC, String addressMDB, int portMDB, String addressMDR, int portMDR) {

		this.MulticastControl = new MulticastControl(addressMC, portMC);
		this.MulticastBackup = new MulticastBackup(addressMDB, portMDB);
		this.MulticastRestore = new MulticastRestore(addressMDR, portMDR);
	}

	// TODO
	public static void main(String[] args){

	}

	// TODO
	public void backupData(String path, int repDeg){

	}
	public void deleteData(String path){

	}
	public void restoreData(String path){

	}
	public void reclaimSpace(int wantedSpace){

	}

	public static int getID(){

		return this.ID;
	}

	public static MulticastControl getMulticastControl(){

		return this.MulticastControl;
	}
	public static MulticastBackup getMulticastBackup(){

		return this.MulticastBackup;
	}
	public static MulticastRestore getMulticastRestore(){

		return this.MulticastRestore;
	}
}
