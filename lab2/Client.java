import java.io.*;
import java.net.*;

class Client
{
   public static void main(String args[]) throws Exception {

      if(args.length != 5 && args.length != 4){
         System.out.println("Usage: java Client <ip_multicast> <port_number_multicast> <oper> <opnd>*");
         return;
      }

      String ipMulticast = args[0];
      Integer portMulticast = Integer.parseInt(args[1]);

      //Cria a socket multicast
      MulticastSocket multicastSocket = new MulticastSocket(portMulticast);
      
      //Da join ao grupo multicast
      multicastSocket.joinGroup(InetAddress.getByName(ipMulticast));
      
      byte[] receiveMulticastData = new byte[1024];
      DatagramPacket receiveMulticastPacket = new DatagramPacket(receiveMulticastData, receiveMulticastData.length);
      multicastSocket.receive(receiveMulticastPacket);
      multicastSocket.close();

      System.out.println(receiveMulticastPacket);

      //Código do Lab1
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      byte[] sendData = new byte[1024];

      String sentence, message;
      
      if(args[2].equals("register") && !(args[2].equals("lookup"))){
          sentence = args[2] + " " + args[3] + " " + args[4];
          message = "REGISTER ";
      }
      else{
          sentence = args[2] + " " + args[3];
          message = "LOOKUP ";
      }

      //SEND DATA
      sendData = sentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      clientSocket.send(sendPacket);
      
      //RECEIVE RESPONSE
      byte[] receiveData = new byte[1024];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);
      
      //DISPLAY RESPONSE
      String modifiedSentence = new String(receivePacket.getData());
      System.out.println(message + modifiedSentence);
      sendData = null;
      clientSocket.close();
   }
}