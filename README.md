# Simple Cloud System
A Simple cloud system with encrypted and securised storage.

This project is a challenge that i wrote in two days (18 hrs 30 mins of work) using scala.
The program offers very simple distant file storage control, (upload/download/listing)

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

## JDK & Dependencies Used 
`Java 16`  
`Scala-v2.13.6`  
`BouncyCastle-v1.69` (for encryption)  
`snakeyaml` (for the `organization.yaml` file parsing)  

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
User password : password
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


## How the solution could be further improved
By Using the [Linkit Framework](https://github.com/Override-6/Linkit), 
I would have been able to develop this same application with many more features, and writing much less code since the data transfer is already taken care of by the project.   Moreover, it is possible with this framework to control objects belonging to remote machines, here is an example that would have allowed me to send a file to the server much more easily: 
```scala
serverEngine = network.getEngine("CloudServer")
val serverStatics = serverEngine.staticsAccessor
val serverDestination = serverStatics[Path].of("/MyDistantFile.txt")
val sourceFileOut = Files.newOutputStream(Path.of("MySourceFile.txt"))
serverStatics[Files].write(sourceFileOut)
```
You can also imagine a segmented algorythmn, or using this system for uploading/downloading folders.  
Here is a remote event feature that could be added in two seconds using Linkit :
```scala
serverEngine = network.getEngine("CloudServer")
val serverStatics = serverEngine.staticsAccessor
val serverWatchService = serverStatics[FileSystems].getDefault.newWatchService()
serverStatics[Path].of("MyDistantFile.txt").register(serverWatchService)
while (true) {
   val key = service.poll()
   key.pollEvents().forEach { event =>
      println(s"Remote event $event for cloud path : ${key.watchable()}")
      doSomeStuffWithTheDistantEvent(event)
   }
}
```
You should think that this would completely blow up the security : Clients would be able to do anything with the server's file system (doing a `serverStatics[Files].delete("/System/Critical Stuff.dll")`, or even a `serverStatics[System].exit(1)` which is indeed true, excepted if the server defines a behavior file.
This way, the server is able to control and encapsulate the actions of the distant engines over its statics methods and objects.  
Here is a (quick and simplified) example of how the above examples can be securised: 
```
describe class java.nio.file.Path {
   //Enables the Path.of(String) static method for RMIs and describe its behavior
   enable static 'of' as {
       args {
           str {
               //Here, an argument modifier is applied when an engine calls the server's Path.of(String) method.
               //The modifier will modify the `str` argument of the method, and the resulting argument will be 
               //the concatenation of the engine's cloud root folder + the original argument.
               //This way, if a malicious client tries to access "/System32/", the modifier will change the path to something like :
               // "D:/CloudStorage/Engine1/System32". So like this, we can avoid malicious code injurings
               modifier for remote -> ${(arg, engineID, engineProperties) => engineProperties("cloud_root_folder") + "/" + arg}
           }
       }
   }
}
//Any RMI to the System class of the server would throw an exception on the engines
hide class java.lang.System 
```
