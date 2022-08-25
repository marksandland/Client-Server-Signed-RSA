import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    public static void main(String [] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Awaiting Connections...");
			ArrayList<Post> posts = new ArrayList<Post>();
            while(true) {
				try{
					Socket s = ss.accept();
					System.out.println("Opening Connection...");
					DataInputStream dis = new DataInputStream(s.getInputStream());
					DataOutputStream dos = new DataOutputStream(s.getOutputStream());
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					
					dos.writeUTF("There are " + posts.size() + " post(s).\n");
					oos.writeObject(posts);
				
					Boolean newPostResponse = dis.readBoolean();
					if (newPostResponse == false) {s.close(); System.out.println("Connection Closed..."); continue;}
					
					byte[] signature = (byte[]) ois.readObject();
					Post receivedPost = (Post) ois.readObject();

					System.out.println("\nRecieved Post: \n");
					System.out.println("Sender: " + receivedPost.getUserid());
					System.out.println("Date: " + receivedPost.getTimestamp());
					System.out.println("Message: " + receivedPost.getMessage() + "\n");

					//Get the public key of the supposed sender
					ObjectInputStream pubIn = new ObjectInputStream(new FileInputStream(receivedPost.getUserid() + ".pub"));
					PublicKey publicKey = (PublicKey) pubIn.readObject();
					pubIn.close();

					//Verify the signature using the senders public key
					Signature sig = Signature.getInstance("SHA1withRSA");
					sig.initVerify(publicKey);
					sig.update((receivedPost.getUserid() + receivedPost.getMessage() + receivedPost.getTimestamp().toString()).getBytes());
					boolean validSignature = sig.verify(signature);
					if (validSignature) {
						System.out.println("Post Accepted");
						posts.add(receivedPost);
					}
					else {
						System.out.println("Post Rejected");
					}

					s.close();
					System.out.println("Connection Closed...");
				}
				catch (Exception e){
					System.out.println("An Exception Occurred, Connection Closed...");
				}
            }
        }
	}
}

final class Post implements java.io.Serializable {
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

