import java.io.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;



public class Reclaim implements Runnable{

	private static Reclaim instance;

	private Peer peer;

	private int wantedSpace;

	public Reclaim(int wantedSpace){

		instance = this;
		this.peer = Peer.getInstance();
		this.wantedSpace = wantedSpace;
	}

	public void run(){

		try {

			int totalSpace = this.wantedSpace * 1000; //Bytes
			int usedSpace = 0;

			int port = this.peer.getMulticastControl().getPort();
			InetAddress address = this.peer.getMulticastControl().getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			String str = this.peer.getServerID() + File.separator + "backup";
			File backupDirectory = new File(str);
			String[] backupEntries = backupDirectory.list();

			if(backupEntries != null){	// in case there are files saved in the backup directory 

				for(String fileEntry: backupEntries){

					String strAux = str + File.separator + fileEntry;
					File backupFileDirectory = new File(strAux);
					String[] backupFileEntries = backupFileDirectory.list();
					if(backupFileEntries != null){	// in case there are chunks saved in the file of the backup directory

						for(String chunkEntry: backupFileEntries){

							String strAuxAux = strAux + File.separator + chunkEntry;
							File currentChunk = new File(strAuxAux);	// estou num chunk

							if((usedSpace + currentChunk.length()) > totalSpace){

								currentChunk.delete();

								byte[] buf = createREMOVEDMessage(fileEntry, chunkEntry.substring(3));

								DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

								datagramSocket.send(datagramPacket);
								
							} else{

								usedSpace += currentChunk.length();
							}
						}
					}
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	private byte[] createREMOVEDMessage(String fileID, String order){
		
		String str = "REMOVED ";
		str += this.peer.getProtocolVersion();
		str += " ";
		str += this.peer.getServerID();
		str += " ";
		str += fileID;
		str += " ";
		str += order;
		str += " ";
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
	}

	public void manageREMOVED(String fileID, int chunkID){
		
		this.peer.reduceBackupFileCurrentRepDeg(fileID, chunkID);
	}

	public static Reclaim getInstance(){

		return instance;
	}
}