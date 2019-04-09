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

	private static int MAX_THREADS = 100;

	private static String serverID; 			// Peer + identifier
	private static double protocolVersion;		// Protocol version

	// Client communication's RMI access point 
	// private static String serviceAccessPoint;

	private static MulticastControl MC;			// Control Multicast
	private static MulticastBackup MDB;			// Backup Multicast
	private static MulticastRestore MDR;		// Restore Multicast
	
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

	public static void main(String[] args){

		try{

			if(args.length != 9){
				System.out.println("Usage:\n");
				System.out.println("java Peer <serverID> <protocolVersion> <serviceAccessPoint> <ipAddressMC> <portMC> <ipAddressMDB> <portMDB> <ipAddressMDR> <portMDR>");
				return;
			}

			serverID = "P" + args[0];
			System.out.println(serverID);
			protocolVersion = Double.parseDouble(args[1]);
			String serviceAccessPoint = args[2];
			String ipAddressMC = args[3];
			int portMC = Integer.parseInt(args[4]);
			String ipAddressMDB = args[5];
			int portMDB = Integer.parseInt(args[6]);
			String ipAddressMDR = args[7];
			int portMDR = Integer.parseInt(args[8]);

			Peer peer = new Peer(ipAddressMC, portMC, ipAddressMDB, portMDB, ipAddressMDR, portMDR);
			RMISystem rmiSystem = (RMISystem) UnicastRemoteObject.exportObject(peer, 0);
			Registry registry = LocateRegistry.getRegistry();
	  		registry.bind(serverID, rmiSystem);

	    } catch (Exception exception) {

 			exception.printStackTrace();
		}

        // deserializeStorage(); //loads storage

        executor.execute(MC);
        executor.execute(MDB);
        executor.execute(MDR);

        // Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); //if CTRL-C is pressed when a Peer is running, it saves his storage so it can be loaded next time it runs
	}
 
	// TODO
	public void backupData(String path, int repDeg){}
	public void deleteData(String path){}
	public void restoreData(String path){}
	public void reclaimSpace(int wantedSpace){}


	public String getServerID(){

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
