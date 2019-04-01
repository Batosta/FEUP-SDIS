import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Peer implements RMISystem{

	private static int MAX_THREADS = 100;	// idk about this one

	private static int serverID;
	private static double protocolVersion;
	// private static String serviceAccessPoint;

	// Private static threads:
	private static MulticastControl MC;
	private static MulticastBackup MDB;
	private static MulticastRestore MDR;

	private static ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(MAX_THREADS);


	private Peer(String ipAddressMC, int portMC, String ipAddressMDB, int portMDB, String ipAddressMDR, int portMDR) {

		try{

			InetAddress mcIpAddress = InetAddress.getByName(ipAddressMC);
			InetAddress mdbIpAddress = InetAddress.getByName(ipAddressMDB);
			InetAddress mdrIpAddress = InetAddress.getByName(ipAddressMDR);

			this.MC = new MulticastControl(mcIpAddress, portMC);
			this.MDB = new MulticastBackup(mdbIpAddress, portMDB);
			this.MDR = new MulticastRestore(mdrIpAddress, portMDR);
		} catch (Exception exc) {

 			System.err.println(exc);
		}
	}

	// TODO
	public static void main(String[] args){

		if(args.length != 9){
			System.out.println("Usage:\n");
			System.out.println("Peer <serverID> <protocolVersion> <serviceAccessPoint> <ipAddressMC> <portMC> <ipAddressMDB> <portMDB> <ipAddressMDR> <portMDR>");
			return;
		}

		serverID = Integer.parseInt(args[0]);
		protocolVersion = Double.parseDouble(args[1]);
		String serviceAccessPoint = args[2];
		String ipAddressMC = args[3];
		int portMC = Integer.parseInt(args[4]);
		String ipAddressMDB = args[5];
		int portMDB = Integer.parseInt(args[6]);
		String ipAddressMDR = args[7];
		int portMDR = Integer.parseInt(args[8]);

		Peer peer = new Peer(ipAddressMC, portMC, ipAddressMDB, portMDB, ipAddressMDR, portMDR);
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

	public int getServerID(){

		return this.serverID;
	}
	public double getProtocolVersion(){

		return this.protocolVersion;
	}
	public MulticastControl getMulticastControl(){

		return this.MC;
	}
	public MulticastBackup getMulticastBackup(){

		return this.MDB;
	}
	public MulticastRestore getMulticastRestore(){

		return this.MDR;
	}
}
