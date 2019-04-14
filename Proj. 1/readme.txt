-COMPILING:

	make

-STARTING RMI:

	rmiregistry 
	OR
	rmiregistry &

-STARTING PEERS:

	java Peer <serverID> <protocolVersion> <serviceAccessPoint> <ipAddressMC> <portMC> <ipAddressMDB> <portMDB> <ipAddressMDR> <portMDR> 

	Ex: java Peer 1 1.0 3 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003
	
-RUNNING:
	
	-BACKUP:
		java TestApp <peer_access_point> BACKUP <file_path> <replication_degree>
		Ex: java TestApp 1 BACKUP 1.pdf 3 

	-RESTORE:
		java TestApp <peer_access_point> RESTORE <file_path>
		Ex: java TestApp 1 RESTORE 1.pdf

	-DELETE:
		java TestApp <peer_access_point> DELETE <file_path>
		Ex: java TestApp 1 DELETE 1.pdf

	-RECLAIM:
		java TestApp <peer_access_point> RECLAIM <maximum_disk_space>
		Ex: java TestApp 1 RECLAIM 0

	-STATE: 
		java TestApp <peer_access_point> DELETE <file_path>
		Ex: java TestApp 1 STATE
