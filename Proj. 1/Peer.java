import java.io.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.net.InetAddress;

@SuppressWarnings("unchecked")
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
	

	private static ConcurrentHashMap<String, String> pathFileID; // path, fileID
	private static ConcurrentHashMap<String, Integer> backupFileDesiredRepDeg; // <path, desiredRepDeg>
	private static ConcurrentHashMap<String, ArrayList<Integer>> backupFileChunks; // <fileID, indexes of all filedID's chunks saved in this peer>
	private static ConcurrentHashMap<Map<String, Integer>, Integer> backupFileCurrentRepDeg; // <Map<path, index>, currentRepDeg


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
			reclaimProtocol = new Reclaim(0);

			createNeededDirectories();

			loadFromDisk();
			System.out.println(pathFileID);
			System.out.println(backupFileDesiredRepDeg);
			System.out.println(backupFileChunks);
			System.out.println(backupFileCurrentRepDeg);
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

		backupProtocol = new Backup(path, repDeg);
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
		new Thread(reclaimProtocol).start();
	}

	/*
	For each file whose backup it has initiated:
		The file pathname
		The backup service id of the file
		The desired replication degree
		For each chunk of the file:
		Its id
		Its perceived replication degree
	For each chunk it stores:
		Its id
		Its size (in KBytes)
		Its perceived replication degree
	The peer's storage capacity, i.e. the maximum amount of disk space that can be used to store 
	chunks, and the amount of storage (both in KBytes) used to backup the chunks.
	*/

	public void peerState(){
		
		for (Object o : pathFileID.entrySet()) {

			System.out.println(o);
		}
	}


	private void createNeededDirectories(){

		File databaseDirectory = new File("db" + File.separator + this.serverID);
		databaseDirectory.mkdirs();

		String commonStr = this.serverID + File.separator;
		
		String backupStr = commonStr + "backup";
		String restoredStr = commonStr + "restored";
		
		File backupDirectory = new File(backupStr);
		File restoredDirectory = new File(restoredStr);
		
		backupDirectory.mkdirs();
		restoredDirectory.mkdirs();
	}

	private static void loadFromDisk(){

		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		String str = "db" + File.separator + serverID + File.separator;
		File file = null;

		try{

			file = new File(str + "pathFileID.ser");
			if(file.exists()){
				
				fileInputStream = new FileInputStream(str + "pathFileID.ser");
		        objectInputStream = new ObjectInputStream(fileInputStream);
		        pathFileID = (ConcurrentHashMap<String, String>)objectInputStream.readObject();
		    } else
		    	pathFileID = new ConcurrentHashMap<String, String>();


		    file = new File(str + "backupFileDesiredRepDeg.ser");
		    if(file.exists()){
		        
		        fileInputStream = new FileInputStream(str + "backupFileDesiredRepDeg.ser");
		        objectInputStream = new ObjectInputStream(fileInputStream);
		        backupFileDesiredRepDeg = (ConcurrentHashMap<String, Integer>)objectInputStream.readObject();
		    } else
		    	backupFileDesiredRepDeg = new ConcurrentHashMap<String, Integer>();


		    file = new File(str + "backupFileChunks.ser");
		    if(file.exists()){
		        
		        fileInputStream = new FileInputStream(str + "backupFileChunks.ser");
		        objectInputStream = new ObjectInputStream(fileInputStream);
		        backupFileChunks = (ConcurrentHashMap<String, ArrayList<Integer>>)objectInputStream.readObject();
		    } else
		    	backupFileChunks = new ConcurrentHashMap<String, ArrayList<Integer>>();
		    

		    file = new File(str + "backupFileCurrentRepDeg.ser");
		    if(file.exists()){
		        fileInputStream = new FileInputStream(str + "backupFileCurrentRepDeg.ser");
		        objectInputStream = new ObjectInputStream(fileInputStream);
		        backupFileCurrentRepDeg = (ConcurrentHashMap<Map<String, Integer>, Integer>)objectInputStream.readObject();
		    } else
		    	backupFileCurrentRepDeg = new ConcurrentHashMap<Map<String, Integer>, Integer>();

	        // objectInputStream.close();
	        // fileInputStream.close();

        } catch(Exception exception){

	    	exception.printStackTrace();
	    }
	}

	private static void saveInDisk(String toBeSaved){

		FileOutputStream fileOutputStream = null;

		try{

			ObjectOutputStream objectOutputStream = null;
			String str = "db" + File.separator + serverID + File.separator;

			switch(toBeSaved){

				case "pathFileID": 
					fileOutputStream = new FileOutputStream(str + "pathFileID.ser");
					objectOutputStream = new ObjectOutputStream(fileOutputStream);
            		objectOutputStream.writeObject(pathFileID);
            		break;

            	case "backupFileDesiredRepDeg": 
					fileOutputStream = new FileOutputStream(str + "backupFileDesiredRepDeg.ser");
					objectOutputStream = new ObjectOutputStream(fileOutputStream);
            		objectOutputStream.writeObject(backupFileDesiredRepDeg);
            		break;

            	case "backupFileChunks": 
					fileOutputStream = new FileOutputStream(str + "backupFileChunks.ser");
					objectOutputStream = new ObjectOutputStream(fileOutputStream);
            		objectOutputStream.writeObject(backupFileChunks);
            		break;

            	case "backupFileCurrentRepDeg": 
					fileOutputStream = new FileOutputStream(str + "backupFileCurrentRepDeg.ser");
					objectOutputStream = new ObjectOutputStream(fileOutputStream);
            		objectOutputStream.writeObject(backupFileCurrentRepDeg);
            		break;

            	default: 
					break;
			}

			objectOutputStream.close();
			fileOutputStream.close();
		} catch (Exception exception) {
            exception.printStackTrace();
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
	public static Peer getInstance(){

		return instance;
	}
	public static ConcurrentHashMap<String, String> getPathFileID(){

		return pathFileID;
	}
	public static ConcurrentHashMap<String, ArrayList<Integer>> getBackupFileChunks(){

		return backupFileChunks;
	}
	public static ConcurrentHashMap<String, Integer> getBackupFileDesiredRepDeg(){

		return backupFileDesiredRepDeg;
	}
	public static ConcurrentHashMap<Map<String, Integer>, Integer> getBackupFileCurrentRepDeg(){

		return backupFileCurrentRepDeg;
	}


	public static void setPathFileID(String path, String fileID){

		pathFileID.put(path, fileID);
		saveInDisk("pathFileID");
	}

	public static void createBackupFileDesiredRepDeg(String path, int repDeg){

		backupFileDesiredRepDeg.put(path, repDeg);
		saveInDisk("backupFileDesiredRepDeg");
	}

	public static void addIndexToBackupFileChunks(String path, int index){

		if(backupFileChunks.get(path) == null){
			backupFileChunks.put(path, new ArrayList<Integer>());
		}
		ArrayList chunksIndex = backupFileChunks.get(path);
		chunksIndex.add(index);
		backupFileChunks.replace(path, chunksIndex);
		saveInDisk("backupFileChunks");
	}

	public static void addBackupFileCurrentRepDeg(String path, int index, int repDeg){

		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(path, index);
		backupFileCurrentRepDeg.put(map, repDeg);
		saveInDisk("backupFileCurrentRepDeg");
	}
}
