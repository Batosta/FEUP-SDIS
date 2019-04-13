import java.io.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;



public class Delete implements Runnable{

	private static Delete instance;

	private String path;

	private FileManager fileManager;

	private Peer peer;

	public Delete(String deletePath){

		instance = this;
		this.path = deletePath;
		this.fileManager = new FileManager();
		this.peer = Peer.getInstance();
	}

	public void run(){

		try {

			this.fileManager.setFileManagerPath(this.path);

			int port = this.peer.getMulticastControl().getPort();
			InetAddress address = this.peer.getMulticastControl().getAddress();
			DatagramSocket datagramSocket = new DatagramSocket();

			for(int i = 0; i < 5; i++){

				byte[] buf = createDELETEMessage(this.fileManager.getFileID());
				DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, address, port);
				datagramSocket.send(datagramPacket);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	private byte[] createDELETEMessage(String fileID){

		String str = "DELETE ";
		str += this.peer.getProtocolVersion();
		str += " ";
		str += this.peer.getServerID();
		str += " ";
		str += fileID;
		str += "\r\n\r\n";

		byte[] strBytes = str.getBytes();
		return strBytes;
	}

	public void manageDELETE(String fileID){

		String str = this.peer.getServerID() + File.separator + "backup" + File.separator + fileID;
		File toBeDeleteDirectory = new File(str);

		String[] entries = toBeDeleteDirectory.list();
		if(entries != null){
			for(String s: entries){
			    File currentFile = new File(toBeDeleteDirectory.getPath(),s);
			    currentFile.delete();
			}
		}
	}

	public static Delete getInstance(){

		return instance;
	}
}