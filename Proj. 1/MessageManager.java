import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.DatagramPacket;

/* -------------------------------- */
// import java.util.Arrays;
// import java.net.InetAddress;
// import java.net.UnknownHostException;
// import java.io.File;
// import java.io.FilenameFilter;
// import java.util.ArrayList;
// import java.util.Random;
/* -------------------------------- */


public class MessageManager implements Runnable {

	private DatagramPacket datagramPacket;

	private static String[] headerData;

	private String messageType;


	public MessageManager(DatagramPacket datagramPacket){

		this.datagramPacket = datagramPacket;
	}

	@Override
	public void run(){

		manageHeader();

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

	public void manageHeader(){

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

		try{
			String header = bufferedReader.readLine();
			headerData = header.split(" ");

		} catch(IOException exception){

			exception.printStackTrace();
		}
	}

	// TODO
	public void managePUTCHUNK(){
		System.out.println("PUTCHUNK");
	}
	public void manageSTORED(){
		System.out.println("STORED");
	}
	public void manageGETCHUNK(){
		System.out.println("GETCHUNK");
	}
	public void manageCHUNK(){
		System.out.println("CHUNK");
	}
	public void manageDELETE(){
		System.out.println("DELETE");
	}
	public void manageREMOVED(){
		System.out.println("REMOVED");
	}
}