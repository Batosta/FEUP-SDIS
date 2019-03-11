import java.io.*;
import java.net.*;
import java.util.HashMap; 
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Server
{
   public static HashMap<String, String> map = new HashMap<>();

   public static void main(String args[]) throws Exception {

      if(args.length != 1) System.out.println("Usage: java Server <port_number>");

      DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(args[0]));
      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];

      String send = new String();
      
      while(true){

         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

         serverSocket.receive(receivePacket);
         String sentence = new String(receivePacket.getData());

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