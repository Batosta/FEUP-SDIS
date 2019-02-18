import java.io.*;
import java.net.*;

class Client
{
   public static void main(String args[]) throws Exception
   {

      if(args.length != 5 && args.length != 4){
         System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd>*");
         return;
      }

      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      byte[] sendData = new byte[1024];

      String sentence; 

      sentence = args[2] + " " + args[3];
      
      if(args[2] == "register")
         sentence += " " + args[4];
      
      //SEND DATA
      sendData = sentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      clientSocket.send(sendPacket);
      
      // //RECEIVE RESPONSE
      // byte[] receiveData = new byte[sendData.length];
      // DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      // clientSocket.receive(receivePacket);
      
      // //DISPLAY RESPONSE
      // String modifiedSentence = new String(receivePacket.getData());
      // System.out.println("FROM SERVER:" + modifiedSentence);
      // clientSocket.close();
   }
}