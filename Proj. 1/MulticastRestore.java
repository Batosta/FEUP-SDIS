import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.net.InetAddress;
import java.util.Arrays;



public class MulticastRestore extends Multicast {

	public MulticastRestore(InetAddress address, int port){

		super(address, port);
	}

	public boolean checkForCHUNK(){

		try{
			MulticastSocket multicastSocket = new MulticastSocket(port);
			multicastSocket.setTimeToLive(1);
			multicastSocket.joinGroup(address);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		byte[] buffer = new byte[65000];	// The maximum size of each chunks 64KByte (where K stands for 1000)

		try {

			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			multicastSocket.receive(datagramPacket);

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

			String header = bufferedReader.readLine();
			String[] headerData = header.split(" ");

			if(headerData[0] == "CHUNK"){
				System.out.println("Already received CHUNK: " + headerData[4]);
				return true;
			}
			else{
				System.out.println("Did not receive CHUNK: " + headerData[4]);
				return false;
			}

		} catch (IOException exception) {
			exception.printStackTrace();	// Method on Exception instances that prints the stack trace of the instance to System.err
		}

		return false;
	}
}