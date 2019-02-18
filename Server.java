import java.io.*;
import java.net.*;

class Server
{
   public static void main(String args[]) throws Exception {

      if(args.length != 1){

         System.out.println("Usage: java Server <port_number>");
      }

      DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(args[0]));
      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];
      
      while(true){

         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         serverSocket.receive(receivePacket);
         String sentence = new String(receivePacket.getData());
         System.out.println("RECEIVED: " + sentence);

         InetAddress IPAddress = receivePacket.getAddress();
         int port = receivePacket.getPort();

         sendData = sentence.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

         System.out.println("sendPacket: " + sendPacket);
         serverSocket.send(sendPacket);
      }
   }
}