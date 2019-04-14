import java.io.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;


public class Restore implements Runnable{

	private static Restore instance;

	private String path;

	private FileManager fileManager;

	private Peer peer;

	private HashMap<Integer, byte[]> restoredBytes;


	public Restore(String restorePath){

		instance = this;
		this.path = restorePath;
		this.fileManager = new FileManager();
		this.peer = Peer.getInstance();
		this.restoredBytes = new HashMap<Integer, byte[]>();
	}

	public void run(){

		try {

			this.fileManager.setFileManagerPath(this.path);

			int port = this.peer.getMulticastControl().getPort();
			InetAddress address = this.peer.getMulticastControl().getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			int chunksNumber = this.fileManager.getNecessaryChunks();
			ArrayList<Chunk> chunks = this.fileManager.getFileChunks();

			for(int i = 0; i < chunksNumber; i++){
					
				byte[] buf = createGETCHUNKMessage(chunks.get(i));

				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);

				datagramSocket.send(datagramPacket);

				try{
					Thread.sleep(500);
				} catch(InterruptedException exception){
					exception.printStackTrace();
				}
			}

			buildRestoredFile();

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	private byte[] createGETCHUNKMessage(Chunk chunk){

		String str = "GETCHUNK ";
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


	private void buildRestoredFile(){

		byte[] fileBytes = this.restoredBytes.get(0);

		for(int i = 1; i < this.restoredBytes.size(); i++){

			byte[] newBody = this.restoredBytes.get(i);
			byte[] combined = new byte[fileBytes.length + newBody.length];

			System.arraycopy(fileBytes, 0, combined, 0, fileBytes.length);
			System.arraycopy(newBody, 0, combined, fileBytes.length, newBody.length);

			fileBytes = combined.clone();
		}

		this.restoredBytes = new HashMap<Integer, byte[]>();

		try{
			String str = this.peer.getServerID() + File.separator + "restored" + File.separator + this.path;
			File newFile = new File(str);
			FileOutputStream fop = new FileOutputStream(newFile);
			fop.write(fileBytes);
			fop.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}


	public static Restore getInstance(){

		return instance;
	}
	public HashMap<Integer, byte[]> getRestoredBytes(){

		return this.restoredBytes;
	}
	public void appendToRestoredBytes(byte[] buf, int order){

		this.restoredBytes.put(order, buf);
	}
}