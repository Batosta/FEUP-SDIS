import java.io.IOException;
import java.net.*;
import java.net.InetAddress;


// Implements Runnable so we are not really specialising the thread's behaviour
public abstract class Multicast implements Runnable {

	private int port;
	public MulticastSocket multicastSocket;
	private InetAddress address;

	public Multicast(InetAddress address, int port){

		this.address = address;
		this.port = port;
	}

	public void run(){

		multicastSocketOpening();
		byte[] buffer = new byte[64000];	// The maximum size of each chunks 64KByte (where K stands for 1000)

		while(true){
			try {
				DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
				multicastSocket.receive(datagramPacket);

				MessageManager messageManager = new MessageManager(datagramPacket);

				// missing something

			} catch (IOException exception) {
				exception.printStackTrace();	// Method on Exception instances that prints the stack trace of the instance to System.err
			}
		}

		// multicastSocketClosing();
	}

	private void multicastSocketOpening(){

		try{
			multicastSocket = new MulticastSocket(port);
			multicastSocket.setTimeToLive(1);
			multicastSocket.joinGroup(address);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	// Maybe it is not needed
	// private void multicastSocketClosing(){

	// 	if (multicastSocket != null)
	// 		multicastSocket.close();
	// }
}
