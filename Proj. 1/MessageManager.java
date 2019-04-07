import java.net.DatagramPacket;


/* -------------------------------- */
// import java.util.Arrays;
// import java.net.InetAddress;
// import java.net.UnknownHostException;
// import java.io.File;
// import java.io.FilenameFilter;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Random;
// import java.io.ByteArrayInputStream;
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
/* -------------------------------- */


public class MessageManager implements Runnable {

	private DatagramPacket datagramPacket;

	private static String[] headerData;

	private String messageType;



	public void MessageManager(DatagramPacket datagramPacket){

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

	// TODO
	public void manageHeader(){

		String data = new String(datagramPacket.getData());
		headerData = data.split(" ");
		// for(int i = 0; i < headerData.length; i++){

		// 	System.out.println(headerData[i]);
		// }
	}

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