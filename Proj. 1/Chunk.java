public class Chunk{

	private int order;						// Order of the chunck
	private int fileID;						// Chunk's file identifier

	private int supposedRepDeg;				// Minimum replication degree
	private int currentRepDeg = 0;			// Current replication degree
	
	private byte[] content;					// Chunk's content
	private int size;						// Chunk's size


	public Chunk(int order, byte[] content, int size){

		this.order = order;
		this.content = content;
		this.size = size;
	}


	public int getOrder(){

		return order;
	}
	public int getFileID(){

		return fileID;
	}
	public int getSupposedRepDeg(){

		return supposedRepDeg;
	}
	public int getCurrentRepDeg(){

		return currentRepDeg;
	}
	public byte[] getContent(){

		return content;
	}
	public int getSize(){

		return size;
	}
}