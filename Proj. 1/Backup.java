import java.io.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.ArrayList;


public class Backup implements Runnable{

	private static Backup instance;

	private String path;

	private int repDeg;

	private FileManager fileManager;

	private Peer peer;

	private int currentReplicationDegree;
	private int desiredReplicationDegree;



	public Backup(String backupPath, int backupRepDeg){

		instance = this;
		this.path = backupPath;
		this.repDeg = backupRepDeg;
		this.fileManager = new FileManager();
		this.peer = Peer.getInstance();

		this.currentReplicationDegree = 0;
		this.desiredReplicationDegree = 0;
	}

	public void run(){

		try {

			this.fileManager.setFileManagerPath(this.path);

			int port = this.peer.getMulticastBackup().getPort();
			InetAddress address = this.peer.getMulticastBackup().getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			int chunksNumber = this.fileManager.getNecessaryChunks();
			ArrayList<Chunk> chunks = this.fileManager.getFileChunks();

			for(int i = 0; i < chunksNumber; i++){

				int waitingTime = 1000;

				this.currentReplicationDegree = 0;
				this.desiredReplicationDegree = 0;
				
				for (int j = 0; j < 5; j++) {
					
					byte[] buf = createPUTCHUNKMessage(chunks.get(i), this.repDeg);

					DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

					datagramSocket.send(datagramPacket);

					try{

						Thread.sleep(waitingTime);
						waitingTime *= 2;
					} catch(InterruptedException exception){
						
						exception.printStackTrace();
					}

					if(this.currentReplicationDegree >= this.desiredReplicationDegree){

						break;
					}
					this.currentReplicationDegree = 0;
				}
			}

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
	private byte[] createPUTCHUNKMessage(Chunk chunk, int repDeg){

		setDesiredReplicationDegree(repDeg);

		String str = "PUTCHUNK ";
		str += this.peer.getProtocolVersion();
		str += " ";
		str += this.peer.getServerID();
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


	public void incrementCurrentReplicationDegree(){

		this.currentReplicationDegree++;
	}
	public void setDesiredReplicationDegree(int desired){

		this.desiredReplicationDegree = desired;
	}

	public static Backup getInstance(){

		return instance;
	}
	public int getCurrentReplicationDegree(){

		return this.currentReplicationDegree;
	}
	public int getDesiredReplicationDegree(){

		return this.desiredReplicationDegree;
	}
}