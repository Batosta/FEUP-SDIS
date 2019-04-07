import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TestApp{


	public TestApp(){}

	public static void main(String[] args){

		// try{

		// 	if(args.length > 4){

		// 		System.out.println("Usage:\n");
		// 		System.out.println("TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
		// 		return;
		// 	}

		// 	String peer_ap = args[0];			// Peer's access point
		// 	String sub_protocol  = args[1];		// BACKUP, RESTORE, DELETE, RECLAIM
		// 	// String opnd_1 = args[2];			// Path name of the file OR maximum amount of disk space (for RECLAIM case)
		// 	// int opnd_2 = args[3];				// Desired replication degree

		// 	try {

	 //            Registry reg = LocateRegistry.getRegistry("localhost");
	 //            stub = (RMISystem) registry.lookup(peer_ap);			// idk de onde veio o lookup

	 //        } catch (Exception exception) {

	 //            exception.printStackTrace();
	 //        }

		// }
		// catch(Exception exception){

		// 	e.printStackTrace();
		// }
	}
}