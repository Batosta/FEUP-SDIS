import java.io.IOException;
import java.net.*;


// Implements Runnable so we are not really specialising the thread's behaviour
public abstract class Multicast implements Runnable {
	
	private int port;
	
	public Multicast(){}

	public void run(){		// not finished

		try {
			MulticastSocket socket = new MulticastSocket(port);
			socket.setTimeToLive(1);
			socket.joinGroup(address);

		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] buf = new byte[64000];	// The maximum size of each chunks 64KByte (where K stands for 1000)
		
		while(true){

		}

	}
}
