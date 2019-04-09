import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TestApp{

	private static String type;
	private static RMISystem stub;

	public TestApp(String[] args){

		try{

			Registry registry = LocateRegistry.getRegistry(null);
			String peerID = "P" + args[0];
			System.out.println(peerID);
			stub = (RMISystem) registry.lookup(peerID);
		} catch(Exception exception){

			exception.printStackTrace();
		}

		switch(type){

			case "BACKUP":
				try {
	               stub.backupData(args[2], Integer.parseInt(args[3]));
	            } catch (RemoteException exception) {
	            	exception.printStackTrace();
	            }
				break;
			case "RESTORE":
				try{
					stub.deleteData(args[2]);
				} catch (RemoteException exception) {
	            	exception.printStackTrace();
	            }
				break;
			case "DELETE":
				try{
					stub.restoreData(args[2]);
				} catch (RemoteException exception) {
	            	exception.printStackTrace();
	            }
				break;
			case "RECLAIM":
				try{
					stub.reclaimSpace(Integer.parseInt(args[2]));
				} catch (RemoteException exception) {
	            	exception.printStackTrace();
	            }
				break;
			default:
				break;
		}
	}

	public static void main(String[] args){

		if(args.length != 4){

			System.out.println("Usage:\n");
			System.out.println("java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
			return;
		}
		type = args[1];
		if(!type.equals("BACKUP") && !type.equals("RESTORE") && !type.equals("DELETE") && !type.equals("RECLAIM")){

			System.out.println("Possible operations:\n");
			System.out.println("BACKUP | RESTORE | DELETE | RECLAIM");
			return;
		}
		TestApp testApp = new TestApp(args);
	}
}