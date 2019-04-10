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

	private static void managePUTCHUNK(){
		
		if(!headerData[2].equals(Peer.getInstance().getServerID())){

			String str = Peer.getInstance().getServerID() + File.separator + "backup" + File.separator + headerData[3];
			File newDirectory = new File(str);
			newDirectory.mkdirs();

			try{

				File newFile = new File(str + File.separator + "chk" + headerData[4]);
				if(!newFile.exists()){
					
					FileOutputStream fop = new FileOutputStream(newFile);
					fop.write(bodyData);
					fop.close();
				}

			} catch (Exception exception) {
				exception.printStackTrace();
			}

			sendSTORED();
		}
	}

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

	// STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public void manageSTORED(){
		
		if(headerData[2].equals(Peer.getInstance().getServerID())){
			Peer.getInstance().incrementCurrentReplicationDegree();
		}
	}

	// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	public void manageGETCHUNK(){
		System.out.println("GETCHUNK");
	}

	//Não temos a certeza se é assim
	private static void sendCHUNK(Chunk chunk){

		String chk = "CHUNK ";
		chk += headerData[1];
		chk += " ";
		chk += headerData[2];
		chk += " ";
		chk += headerData[3];
		chk += " ";
		chk += headerData[4];
		chk += " ";
		chk += "\r\n\r\n";

		byte[] chkBytes = chk.getBytes();
		byte[] chunkContent = chunk.getContent();
		byte[] combined = new byte[chkBytes.length + chunkContent.length];

		System.arraycopy(chkBytes, 0, combined, 0, chkBytes.length);
		System.arraycopy(chunkContent, 0, combined, chkBytes.length, chunkContent.length);
		
		MulticastRestore mdr = Peer.getInstance().getMulticastRestore();
		mdr.sendDatagramPacket(combined);
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

	// Function that waits a random delay uniformly distributed between 0 and 400 ms
	private static void waitRandomTime(){

		Random r = new Random();
		int result = r.nextInt(401);
		try{
			Thread.sleep(result);
		} catch(InterruptedException exception){
			exception.printStackTrace();
		}
	}
}