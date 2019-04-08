import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.io.IOException;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ArrayList;

public class FileManager{

	private static int CHUNK_MAX_SIZE = 64000;

	private File file;
	private String path;
	private ArrayList<Chunk> fileChunks;
	private String fileID;

	public FileManager(String path) {

		this.path = path;
		this.file = new File(path);
		createFileID();
		createFileChunks();
	}

	public void createFileChunks(){

		int fileSize = (int) file.length();
		int necessaryChunks = getNecessaryChunks(fileSize);
		int createdSize = 0;

		this.fileChunks = new ArrayList<Chunk>();

		try{

			FileInputStream fileInputStream = new FileInputStream(this.file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

			for(int i = 0; i < necessaryChunks; i++){

				byte[] buf = new byte[CHUNK_MAX_SIZE];
				bufferedInputStream.read(buf);

				if(i == (necessaryChunks - 1)){

					byte[] content = Arrays.copyOf(buf, fileSize - createdSize);
					Chunk newChunk = new Chunk(i, this.fileID, content, fileSize - createdSize);
					this.fileChunks.add(newChunk);
				} else{

					byte[] content = Arrays.copyOf(buf, CHUNK_MAX_SIZE);
					Chunk newChunk = new Chunk(i, this.fileID, content, CHUNK_MAX_SIZE);
					this.fileChunks.add(newChunk);
					createdSize += CHUNK_MAX_SIZE;
				}
			}
		} catch(IOException exception){

			exception.printStackTrace();
		}
	}

	private int getNecessaryChunks(double fileSize){
		
		if(fileSize % CHUNK_MAX_SIZE == 0) // If the file size is a multiple of the chunk size
			return (int) ((fileSize / CHUNK_MAX_SIZE) + 1);
		else
			return (int) Math.ceil(fileSize / CHUNK_MAX_SIZE);
	}

	public void createFileID(){

		// Get the file name
		String fileName = this.file.getName();

		// Get the file last modification date
		String fileModificationDate = Long.toString(this.file.lastModified());
		String fileOwner = "";

		// Get the owner of the file
		Path path = Paths.get(this.path);
		FileOwnerAttributeView foav = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
		try{
			fileOwner = foav.getOwner().toString();
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		// Applying the hash256 in order to get the file identifier
		this.fileID = hash256(fileName + "|" + fileModificationDate + "|" + fileOwner);
	}

	/*
	* Credits: https://www.geeksforgeeks.org/sha-256-hash-in-java/
	*/
	private static String hash256(String input){ 
        try { 
  
            // Static getInstance method is called with hashing SHA 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
  
            // digest() method called to calculate message digest of an input and return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
  
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        } 
        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) { 
            System.out.println("Exception thrown" + " for incorrect algorithm: " + e); 
            return null; 
        }
    } 


    public File getFile(){

    	return this.file;
    }
    public String getPath(){
    	
    	return this.path;
    }
    public ArrayList<Chunk> getFileChunks(){
    	
    	return this.fileChunks;
    }
    public String getFileID(){
    	
    	return this.fileID;
    }
}