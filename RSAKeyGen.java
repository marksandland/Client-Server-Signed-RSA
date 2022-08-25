import java.io.*;
import java.security.*;

public class RSAKeyGen {

	public static void main(String [] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java RSAKeyGen userid");
			System.exit(-1);
		}

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();

		ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(args[0] + ".pub"));
		objOut.writeObject(kp.getPublic());
		objOut.close();

		objOut = new ObjectOutputStream(new FileOutputStream(args[0] + ".prv"));
		objOut.writeObject(kp.getPrivate());
	}
}