import java.io.*;
import java.net.*;
import java.util.HashMap; 
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.Timer;
import java.util.TimerTask;

class Server
{
   public static HashMap<String, String> map = new HashMap<>();

   public static void main(String args[]) throws IOException {

      if(args.length != 4){
         System.out.println("Usage: java Server <ip_multicast> <port_number_multicast> <ip_register> <port_number_register>"); 
         return;
      } 
      
      String ipMulticast = args[0];
      Integer portMulticast = Integer.parseInt(args[1]);
      String ipRegister = args[2];
      Integer portRegister = Integer.parseInt(args[3]);

      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];
      byte[] multicastData = new byte[1024];

      String send = new String();
      
      String multicastSend = ipRegister + " " + Integer.toString(portRegister);
      System.out.println(multicastSend);


      MulticastSocket multicastSocket = new MulticastSocket(portMulticast);
      multicastData = multicastSend.getBytes();
      DatagramPacket multicastPacket = new DatagramPacket(multicastData, multicastData.length, InetAddress.getByName(ipMulticast), portMulticast);


      Timer multicastTimer = new Timer();
      multicastTimer.scheduleAtFixedRate(new TimerTask() {  
         @Override
         public void run(){
            try{

               int tt1 = multicastSocket.getTimeToLive();
               multicastSocket.setTimeToLive(1);
               multicastSocket.send(multicastPacket);
               multicastSocket.setTimeToLive(tt1);

            } catch(IOException e){
               e.printStackTrace();
            }
         }

      }, 0, 1000);

      multicastData = new byte[1024];

      DatagramSocket serverSocket = new DatagramSocket(portRegister);

      while(true){

          DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

          serverSocket.receive(receivePacket);
          String sentence = new String(receivePacket.getData());
          System.out.println(sentence);

          sentence.trim();
          System.out.println("RECEIVED: " + sentence);
          String[] sentenceAux = sentence.split(" ");
         
          if(sentenceAux[0].equals("register")) send = register(sentenceAux[1].trim(), sentenceAux[2].trim());
          if(sentenceAux[0].equals("lookup")) send = lookup(sentenceAux[1].trim());

          InetAddress IPAddress = receivePacket.getAddress();
          int port = receivePacket.getPort();

          sendData = send.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

          serverSocket.send(sendPacket);
         
          receiveData = new byte[1024];
          sendData = new byte[1024];
          multicastSend = new String();
      }
   }

   public static String lookup(String plate){

      if(!isPlate(plate)) return "NOT_FOUND (Invalid plate)";

      if(map.containsKey(plate)){
         String name = map.get(plate);

        String result = plate + " " + name;
         
         return result;

      }else return "NOT_FOUND";
   
   }

   public static String register(String name, String plate){

      if(map.containsKey(plate) && map.containsValue(name)) return "-1";

      if(name.length() > 256) return "-1";

      if(!isPlate(plate)) return "-1";

      map.put(plate, name);
      int size = map.size();
      String str = Integer.toString(size);
      return str;
   }
   
   public static Boolean isPlate(String plate){

      if(plate.length()!=8) return false;

      for(int i = 0; i < plate.length(); i++){
             
         char c = plate.charAt(i);
             
            if(i != 2 && i != 5){
               if(!Character.isDigit(c) && !Character.isLetter(c)) return false;
             }else{
               if(c != '-') return false;
             }
         }
         return true;
     }

}