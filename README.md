# Simple Cloud System
A Simple cloud system with encrypted and securised storage.

This project is a challenge that i wrote in two days (18 hrs 30 mins of work) using scala.
The program offers very simple distant file storage control, (upload/download/listing)
## How to start
The project consists of two executable parts : The client and the server  
Let's launch the server first
### Step 1 : The server
The server needs a port and a folder path in which clients' clouds spaces will be stored.
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
//This line only appears if -password is not specified
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
If you do `help` you'll see that there is three commands available : 
- `ls <path>` to print the content of a distant folder 
- `upload <source> -dest <cloudPath>` to upload a `source` folder/file into the cloud
- `download <cloudPath> -dest <path>` to download a folder/file located at `cloudPath`.

## Relevant Design Choices 
The project is divided in 5 Modules : 
* `Common` that defines the common classes used in `Server` and `Client`
* `Stream` that defines Input/Output Stream and Reader/Writer classes
* `Encryption` that defines data encryption and decryption classes
* `Server` The server executable module
* `Client` The client executable module

ObjectInput/Output streams has been used in order to keep packet transfer simple (KIS(~S)!)  
Even if java's serialization system is not known to be secure, the main intention of this project is to secure the storage and transport of data, not to secure their persistence 
  
CLI was chosen in order to keep the application simple, and because i had only two days of developping the whole program. (+ i'm bad at UI design ~)

## Dependencies Used 
`BouncyCastle` (for encryption)

