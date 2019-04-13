import java.io.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;



public class Reclaim implements Runnable{

	private static Reclaim instance;

	private FileManager fileManager;

	private Peer peer;

	private int wantedSpace;

	public Reclaim(int wantedSpace){

		instance = this;
		this.fileManager = new FileManager();
		this.peer = Peer.getInstance();
		this.wantedSpace = wantedSpace;
	}

	public void run(){

		try {

			System.out.println("Initiate RECLAIM Protocol: " + this.wantedSpace);

			long totalSpace = this.wantedSpace * 1000; //Bytes

			int port = this.peer.getMulticastControl().getPort();
			InetAddress address = this.peer.getMulticastControl().getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			while(this.fileManager.getUsedSpace() > totalSpace){

				Chunk chunkToDelete = this.fileManager.getMaxSizeChunk();

				String fileID = chunkToDelete.getFileID();
				int chunkID = chunkToDelete.getOrder();

				System.out.println("Chunk fileID: " + fileID);
				System.out.println("Chunk chunkID: " + chunkID);;

				byte[] buf = createREMOVEDMessage(chunkToDelete);
				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

				System.out.println(buf);

				datagramSocket.send(datagramPacket);

			}

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	private byte[] createREMOVEDMessage(Chunk chunk){
		
		String str = "REMOVED ";
		str += this.peer.getProtocolVersion();
		str += " ";
		str += this.peer.getServerID();
		str += " ";
		str += chunk.getFileID();
		str += " ";
		str += chunk.getOrder();
		str += " ";
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
	}

	public void manageREMOVED(String fileID, int chunkID){
		System.out.println("REMOVED");
	}

	public static Reclaim getInstance(){

		return instance;
	}
}