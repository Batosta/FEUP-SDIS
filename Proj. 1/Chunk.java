public class Chunk{

	private int order;						// Order of the chunck
	private String fileID;					// Chunk's file identifier

	private byte[] content;					// Chunk's content
	private int size;						// Chunk's size


	public Chunk(int order, String fileID, byte[] content, int size){

		this.order = order;
		this.fileID = fileID;
		this.content = content;
		this.size = size;
	}


	public int getOrder(){

		return order;
	}
	public String getFileID(){

		return fileID;
	}
	public byte[] getContent(){

		return content;
	}
	public int getSize(){

		return size;
	}
}