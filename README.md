# Simple Cloud System
A Simple cloud system with encrypted and securised storage.

This project is a challenge that i wrote in two days (18 hrs 30 mins of work) using scala.
## How to start
The project consists of two executable parts : The client and the server  
Let's launch the server first
### Step 1 : The server
The server needs a port and a folder path in which client's clouds spaces will be stored.
```
java -jar Server.jar --server-port <port> -folder <folder>
```
Once the server fully launched, you should have a console looking like this :
```
Using D:\Stockage\SimpleCloudSystem as storage folder.
Found 1 users cloud space.
Starting server on port 48483...
Server successfully started.
```
The server will now listen to clients connection. So let's start our client !
### Step 2 : The client
__Required argument :__  
 --secrets-folder 'path' default: None (the program shutdowns with an error message)  
__Optional :__  
 --server-address 'host' default: localhost  
 --server-port 'port' default: 48483   
 --password 'password' default: Asks in console  
```
java -jar Client.jar --server-address <host> --server-port <port> --secrets-folder <path> --password <password>
```
when running client for the first time, you should have a console like this :  
```
//This line only appears if -password not specified
User password : maxbat66
Creating User Secrets, This can take a while...
Creating Keystore and certificate...
Keystore created, extracting certificate...
Certificate created.
Connecting to localhost:48483...
Connection successfully bound to server.
Enter command >
```
Note: The keystore and certificates is created from a ressource file stored in the Client.jar.  
You can modify the `organization.yaml` to use your own credentials.
#### The Client Commands
Well, it's here that the application begin to be usable.
