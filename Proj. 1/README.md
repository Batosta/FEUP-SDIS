# Project 1: Distributed backup service

## Sources

The source files are under the project __src__ folder.

## How to compile

The project can be compiled by using the command __make__ inside the directory __Proj.1__.

## How to run

After compiling the classes using the command referred above you can open more terminals to launch peers and call the __TestApp__.

### RMI registry

The implementation of the interface for the project uses RMI, as suggested by the protocol. An instance of __rmiregistry__ must be running in order to run the program.

You can either launch the instance in the background by using ```rmiregistry &``` or simply using ```rmiregistry```.

### Peer

For the user to create peers he shall call it in a different terminal using the command:

``` java Peer <serverID> <protocolVersion> <serviceAccessPoint> <ipAddressMC> <portMC> <ipAddressMDB> <portMDB> <ipAddressMDR> <portMDR> ```

Example:
``` java Peer 1 1.0 3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003 ```

### TestApp

The testing application for the program should be invoked as follows:

``` java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ```

where:

__peer_ap__ : Is the peer's access point.
	
__operation__ : Is the operation the peer of the backup service must execute. Can be either: BACKUP, RESTORE, DELETE or RECLAIM.

__opnd_1__ : Is either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks. The STATE operation takes no operands.

__opnd_2__ : This operand is an integer that specifies the desired replication degree.

Examples:

``` java TestApp 1 BACKUP 1.jpg 3 ```
``` java TestApp 1 RESTORE 1.jpg ```
``` java TestApp 1 DELETE 1.jpg ```
``` java TestApp 1 RECLAIM 0 ```
``` java TestApp 1 STATE ```
