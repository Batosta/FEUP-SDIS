import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Peer implements RMISystem{

	private static int MAX_THREADS = 100;

	private static Peer instance;

	private static String serverID; 			// Peer + identifier
	private static double protocolVersion;		// Protocol version

	private static int currentReplicationDegree;
	private static int desiredReplicationDegree;

	private static ArrayList<byte[]> restoredBytes;

	private static FileManager fileManager;

	private static MulticastControl MC;			// Control Multicast
	private static MulticastBackup MDB;			// Backup Multicast
	private static MulticastRestore MDR;		// Restore Multicast
	
	private static ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(MAX_THREADS);


	private Peer(String ipAddressMC, int portMC, String ipAddressMDB, int portMDB, String ipAddressMDR, int portMDR) {

		try{

			InetAddress mcIpAddress = InetAddress.getByName(ipAddressMC);
			InetAddress mdbIpAddress = InetAddress.getByName(ipAddressMDB);
			InetAddress mdrIpAddress = InetAddress.getByName(ipAddressMDR);

			MC = new MulticastControl(mcIpAddress, portMC);
			MDB = new MulticastBackup(mdbIpAddress, portMDB);
			MDR = new MulticastRestore(mdrIpAddress, portMDR);
			instance = this;
			currentReplicationDegree = 0;
			desiredReplicationDegree = 0;
			fileManager = new FileManager();
			restoredBytes = new ArrayList<byte[]>();

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

        executor.execute(MC);
        executor.execute(MDB);
        executor.execute(MDR);

        // Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); //if CTRL-C is pressed when a Peer is running, it saves his storage so it can be loaded next time it runs
	}
 


	public void backupData(String path, int repDeg){

		try {

			fileManager.setFileManagerPath(path);

			int port = MDB.getPort();
			InetAddress address = MDB.getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			int chunksNumber = fileManager.getNecessaryChunks();
			ArrayList<Chunk> chunks = fileManager.getFileChunks();

			for(int i = 0; i < chunksNumber; i++){

				currentReplicationDegree = 0;
				desiredReplicationDegree = 0;
				int waitingTime = 1000;
				for (int j = 0; j < 5; j++) {
					
					byte[] buf = createPUTCHUNKMessage(chunks.get(i), repDeg);

					DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

					datagramSocket.send(datagramPacket);

					try{

						Thread.sleep(waitingTime);
						waitingTime *= 2;
					} catch(InterruptedException exception){
						
						exception.printStackTrace();
					}

					if(currentReplicationDegree >= desiredReplicationDegree){

						break;
					}
					currentReplicationDegree = 0;
				}
			}

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void deleteData(String path){
		
		try {

			fileManager.setFileManagerPath(path);

			int port = MC.getPort();
			InetAddress address = MC.getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			for(int i = 0; i < 5; i++){

				byte[] buf = createDELETEMessage(fileManager.getFileID());
				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);
				datagramSocket.send(datagramPacket);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}










	public void restoreData(String path){
		
		try {

			fileManager.setFileManagerPath(path);

			int port = MC.getPort();
			InetAddress address = MC.getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			int chunksNumber = fileManager.getNecessaryChunks();
			ArrayList<Chunk> chunks = fileManager.getFileChunks();

			for(int i = 0; i < chunksNumber; i++){
					
				byte[] buf = createGETCHUNKMessage(chunks.get(i));

				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

				datagramSocket.send(datagramPacket);
			}

			System.out.println("SleepStart");
			try{
				Thread.sleep(5000);
			} catch(InterruptedException exception){
				exception.printStackTrace();
			}
			System.out.println("SleepEnd");

			buildRestoredFile();

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}










	public void reclaimSpace(int wantedSpace){
		System.out.println("Peer RECLAIMS");
	}


	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
	private byte[] createPUTCHUNKMessage(Chunk chunk, int repDeg){

		setDesiredReplicationDegree(repDeg);

		String str = "PUTCHUNK ";
		str += protocolVersion;
		str += " ";
		str += serverID;
		str += " ";
		str += chunk.getFileID();
		str += " ";
		str += chunk.getOrder();
		str += " ";
		str += Integer.toString(repDeg);
		str += " ";
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		byte[] chunkContent = chunk.getContent();
		byte[] combined = new byte[strBytes.length + chunkContent.length];

		System.arraycopy(strBytes, 0, combined, 0, strBytes.length);
		System.arraycopy(chunkContent, 0, combined, strBytes.length, chunkContent.length);
		return combined;
	}

	private byte[] createGETCHUNKMessage(Chunk chunk){

		String str = "GETCHUNK ";
		str += protocolVersion;
		str += " ";
		str += serverID;
		str += " ";
		str += chunk.getFileID();
		str += " ";
		str += chunk.getOrder();
		str += " ";
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
	}

	private byte[] createDELETEMessage(String fileID){

		String str = "DELETE ";
		str += protocolVersion;
		str += " ";
		str += serverID;
		str += " ";
		str += fileID;
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
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


	private void buildRestoredFile(){

		System.out.println("buildRestoredFile: " + restoredBytes.size());
		for(int i = 0; i < restoredBytes.size(); i++){

			System.out.println(restoredBytes.get(i));
		}
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
	public static FileManager getFileManager(){

		return fileManager;
	}
	public static Peer getInstance(){

		return instance;
	}
	public static int getCurrentReplicationDegree(){

		return currentReplicationDegree;
	}
	public static int getDesiredReplicationDegree(){

		return desiredReplicationDegree;
	}
	public static void incrementCurrentReplicationDegree(){

		currentReplicationDegree++;
	}
	public static void setDesiredReplicationDegree(int desired){

		desiredReplicationDegree = desired;
	}
	public static ArrayList<byte[]> getRestoredBytes(){

		return restoredBytes;
	}
	public static void appendToRestoredBytes(byte[] buf){

		restoredBytes.add(buf);
	}
}
