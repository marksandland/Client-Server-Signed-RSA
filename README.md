# Client-Server-Signed-RSA
## client and server for uploading posts to a server that can be made to be only decryptable by the intended recipient with RSA. Also signatures are used to verify the sender of a post.


* Use "java RSAKeyGen.java {userid}" to create a RSA keypair for a new user with that userid. Place copies of appropriate keys into the client and server folders.
* Use command "java Server.java {port}" to start the server program with a suitable port number.
* Use command "java Client.Java localhost {port} {userid}" to start the client program with the same port number and an existing userid.
