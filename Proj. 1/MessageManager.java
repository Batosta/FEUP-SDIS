import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Random;


public class MessageManager{

	private DatagramPacket datagramPacket;

	private static String[] headerData;
	private static byte[] bodyData;

	private String messageType;


	public MessageManager(DatagramPacket datagramPacket){

		this.datagramPacket = datagramPacket;

		manageDatagramPacket();
	}

	private void manageDatagramPacket(){

		manageHeader();
		manageBody();

		messageType = headerData[0];
		switch(messageType){

			case "PUTCHUNK":
				managePUTCHUNK();
				break;
			case "STORED":
				manageSTORED();
				break;
			case "GETCHUNK": 
				manageGETCHUNK();
				break;
			case "CHUNK": 
				manageCHUNK();
				break;
			case "DELETE":
				manageDELETE();
				break;
			case "REMOVED": 
				manageREMOVED();
				break;
			default: 
				break;
		}
	}

	// PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
	private static void managePUTCHUNK(){
		
		if(!headerData[2].equals(Peer.getInstance().getServerID())){

			String str = Peer.getInstance().getServerID() + File.separator + "backup" + File.separator + headerData[3];
			File newDirectory = new File(str);
			newDirectory.mkdirs();

			try{

				File newFile = new File(str + File.separator + "chk" + headerData[4]);
				FileOutputStream fop = new FileOutputStream(newFile);
				fop.write(bodyData);
				fop.close();

			} catch (Exception exception) {
				exception.printStackTrace();
			}

			sendSTORED();
		}
	}

	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	private static void sendSTORED(){
		
		String stored = "STORED ";
		stored += headerData[1];
		stored += " ";
		stored += headerData[2];
		stored += " ";
		stored += headerData[3];
		stored += " ";
		stored += headerData[4];
		stored += " ";
		stored += "\r\n\r\n";

		byte[] storedBytes = stored.getBytes();

		waitRandomTime();

		MulticastControl mc = Peer.getInstance().getMulticastControl();
		mc.sendDatagramPacket(storedBytes);
	}

	private static void waitRandomTime(){

		Random r = new Random();
		int result = r.nextInt(401);
		try{
			Thread.sleep(result);
		} catch(InterruptedException exception){
			exception.printStackTrace();
		}
	}

	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public void manageSTORED(){
		System.out.println("STORED");
	}

	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public void manageGETCHUNK(){
		System.out.println("GETCHUNK");
	}

	// CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
	public void manageCHUNK(){
		System.out.println("CHUNK");
	}

	// DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	public void manageDELETE(){
		System.out.println("DELETE");
	}

	// REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public void manageREMOVED(){
		System.out.println("REMOVED");
	}



	// Function to parse the header of the messages received
	private void manageHeader(){

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

		try{

			String header = bufferedReader.readLine();
			headerData = header.split(" ");

		} catch(IOException exception){

			exception.printStackTrace();
		}
	}

	// Function to parse the body of the messages received
	private void manageBody() {

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

		int sum = 0, linesSum = 0, start = 0;
		String fullLine = null;

		do {

			try {
				fullLine = bufferedReader.readLine();

				sum += fullLine.length();

				linesSum++;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		} while (!fullLine.isEmpty());

		start = sum + linesSum * "\r\n".getBytes().length;
		this.bodyData = Arrays.copyOfRange(datagramPacket.getData(), start, datagramPacket.getLength());
	}
}