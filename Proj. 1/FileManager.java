import java.io.File;
import java.lang.Math;

public class FileManager{

	private static int CHUNK_MAX_SIZE = 64000;

	private File file;
	private Chunk[] fileChunks;
	private String fileID;

	public FileManager(String path) {

		System.out.println("");

		this.file = new File(path);
		// createFileID();
		createFileChunks();
	}

	public void createFileID(){
		System.out.println("createFileID");
	}

	public void createFileChunks(){

		double fileSize = file.length();
		int necessaryChunks = getNecessaryChunks(fileSize);

		for(int i = 0; i < necessaryChunks; i++){

			byte[] content = new byte[CHUNK_MAX_SIZE];
			// Chunk chunk = new Chunk(0, content, size);
		}
	}

	private int getNecessaryChunks(double fileSize){
		
		if(fileSize % CHUNK_MAX_SIZE == 0) // If the file size is a multiple of the chunk size
			return (int) ((fileSize / CHUNK_MAX_SIZE) + 1);
		else
			return (int) Math.ceil(fileSize / CHUNK_MAX_SIZE);
	}
}