import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.net.InetAddress;


public class Peer implements RMISystem{

	private static int MAX_THREADS = 100;

	private static Peer instance;

	private static String serverID; 			// Peer + identifier
	private static double protocolVersion;		// Protocol version

	private static MulticastControl MC;			// Control Multicast
	private static MulticastBackup MDB;			// Backup Multicast
	private static MulticastRestore MDR;		// Restore Multicast

	private static Backup backupProtocol;
	private static Delete deleteProtocol;
	private static Restore restoreProtocol;
	private static Reclaim reclaimProtocol;
	

	private Peer(String ipAddressMC, int portMC, String ipAddressMDB, int portMDB, String ipAddressMDR, int portMDR) {

		try{

			InetAddress mcIpAddress = InetAddress.getByName(ipAddressMC);
			InetAddress mdbIpAddress = InetAddress.getByName(ipAddressMDB);
			InetAddress mdrIpAddress = InetAddress.getByName(ipAddressMDR);

			MC = new MulticastControl(mcIpAddress, portMC);
			MDB = new MulticastBackup(mdbIpAddress, portMDB);
			MDR = new MulticastRestore(mdrIpAddress, portMDR);

			instance = this;
			
			backupProtocol = new Backup("", 0);
			deleteProtocol = new Delete("");
			restoreProtocol = new Restore("");

			createNeededDirectories();

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

        // executor.execute(MC);
        // executor.execute(MDB);
        // executor.execute(MDR);

        new Thread(MC).start();
        new Thread(MDB).start();
		new Thread(MDR).start();

        // Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); //if CTRL-C is pressed when a Peer is running, it saves his storage so it can be loaded next time it runs
	}


	public void backupData(String path, int repDeg){

		Backup backupProtocol = new Backup(path, repDeg);
		new Thread(backupProtocol).start();
	}

	public void deleteData(String path){
		
		deleteProtocol = new Delete(path);
		new Thread(deleteProtocol).start();
	}

	public void restoreData(String path){
		
		restoreProtocol = new Restore(path);
		new Thread(restoreProtocol).start();
	}


	public void reclaimSpace(int wantedSpace){
		
		reclaimProtocol = new Reclaim(wantedSpace);
		new Thread(restoreProtocol).start();
	}



	private byte[] createREMOVEDMessage(Chunk chunk){

		String str = "REMOVED ";
		str += protocolVersion;
		str += " ";
		str += serverID;
		str += " ";
		str += chunk.getFileID();
		str += " ";
		str += chunk.getOrder();
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
	}


	private void createNeededDirectories(){

		String commonStr = this.serverID + File.separator;
		
		String backupStr = commonStr + "backup";
		String restoredStr = commonStr + "restored";
		
		File backupDirectory = new File(backupStr);
		File restoredDirectory = new File(restoredStr);
		
		backupDirectory.mkdirs();
		restoredDirectory.mkdirs();
	}


	public static String getServerID(){

		return serverID;
	}
	public static double getProtocolVersion(){

		return protocolVersion;
	}
	public static MulticastControl getMulticastControl(){

		return MC;
	}
	public static MulticastBackup getMulticastBackup(){

		return MDB;
	}
	public static MulticastRestore getMulticastRestore(){

		return MDR;
	}
	public static Peer getInstance(){

		return instance;
	}
}
