import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = args[0];
		int port = Integer.parseInt(args[1]);
		String userid = args[2];
		Socket s;
		try{
			s = new Socket(host, port);
		}
		catch(Exception e){
			System.out.println(e);
			return;
		}
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		DataInputStream dis = new DataInputStream(s.getInputStream());
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println(dis.readUTF()); //Number of posts
		Object o = ois.readObject(); //Posts list
		ArrayList<Post> posts = new ArrayList<>();

		//Check if received object is an arraylist (To avoid unchecked casting)
		if (o instanceof ArrayList<?>){
			ArrayList<?> o2 = (ArrayList<?>)o;
			for (Object i : o2){
				if (i instanceof Post){
					posts.add((Post)i);
				}
			}
		}

		//Get the users private key
		ObjectInputStream prvIn = new ObjectInputStream(new FileInputStream(userid + ".prv"));
		PrivateKey privateKey = (PrivateKey) prvIn.readObject();
		prvIn.close();

		for (Post post : posts){
			try{
				System.out.println("Sender: " + post.getUserid());
				System.out.println("Date: " + post.getTimestamp());
				Base64.Decoder decoder = Base64.getDecoder();
				byte[] encryptedMessageBytes = decoder.decode(post.getMessage().getBytes("UTF8"));
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] decryptedByteArray = cipher.doFinal(encryptedMessageBytes);
				String decryptedMessage = new String(decryptedByteArray, "UTF8");
				System.out.println("Message: " + decryptedMessage + "\n");
			}
			catch (IllegalArgumentException | BadPaddingException e) {
				System.out.println("Message: " + post.getMessage() + "\n");
		    }
			catch (Exception e){
				System.out.println(e);
				s.close();
				return;
			}
		}

 		//Do you want to add a new post?
		Boolean vaildNewPostResponse = false;
		while (vaildNewPostResponse == false){
			System.out.println("Do you want to add a post? [y/n]");
			String response = reader.readLine();
			if (response.toLowerCase().equals("y")){
				dos.writeBoolean(true);
				vaildNewPostResponse = true;
			}
			else if (response.toLowerCase().equals("n")){
				dos.writeBoolean(false);
				s.close();
				return;
			}
		}

		System.out.println("\nEnter the recipient userid (type \"all\" for posting without encryption):");
		String recipient = reader.readLine();
		
		System.out.println("\nEnter your message:");
		String msg = reader.readLine();

		String processedMessage;
		if (recipient.equals("all")){
			processedMessage = msg;
		}
		else { //Encrypt with recipients public key
			try{
				ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(recipient + ".pub"));
				PublicKey publicKey = (PublicKey) objIn.readObject();
				objIn.close();
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				byte[] encryptedMsgBytes = cipher.doFinal(msg.getBytes("UTF8"));
				Base64.Encoder encoder = Base64.getEncoder();
				processedMessage = encoder.encodeToString(encryptedMsgBytes);
			}
			catch (Exception e){
				System.out.println(e);
				s.close();
				return;
			}
		}

		Date timestamp = new Date();
		Post newPost = new Post(userid, processedMessage, timestamp);

		//Create signature with users private key
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initSign(privateKey);
		sig.update((userid + processedMessage + timestamp.toString()).getBytes());
		byte[] signature = sig.sign();
		
		oos.writeObject(signature);
		oos.writeObject(newPost);

		s.close();
	}
}
final class Post implements java.io.Serializable{
	private String userid;
	private String message;
	private Date timestamp;

	public Post(String userid, String message, Date timestamp) {
		this.userid = userid;
		this.message = message;
		this.timestamp = timestamp;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}