import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

class TCPServer {
    private static String FILE_NAME = "StudentData.txt";
    // private ServerSocket server = new ServerSocket(6790);
    // private Socket connectionSocket = server.accept();

    // try {
    //     server = new ServerSocket(6790);
    //     connectionSocket = server.accept();
    // } catch (IOException e){
    //     System.out.println(e);
    // }

    public static void main(String argv[]) throws Exception {
        ServerSocket server = new ServerSocket(6790);
        Socket connectionSocket = server.accept();

        System.out.println("Server starting...");

        Boolean stop = false;

        // ServerSocket server = new ServerSocket(6790);
        System.out.println("Server initiated at port: " + server.getLocalPort());

        // Socket connectionSocket = server.accept();
        System.out.println("Client Socket: " + connectionSocket.getPort());

        // DataInputStream inFromClient = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));
        // DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        while (!stop) {
            String clientSentence = getFromClient(connectionSocket);
            System.out.println("Message received: " + clientSentence);

            String command = Character.toString(clientSentence.charAt(0));
            String data = clientSentence.substring(1);

            switch (command) {
                case "0":
                    stop = true;
                    // outToClient.close();
                    break;
                case "1":
                    // add a new student's information into the database
                    outToClient(connectionSocket, "Adding new record: " + data);
                    try {
                        saveToDb(data);
                        outToClient(connectionSocket, "Record saved.");
                        printDb();
                    } catch (IOException e) {
                        System.out.println(e);
                        outToClient(connectionSocket, "IO Exception: " + e);
                    }
                    clientSentence = null;
                    break;
                case "2":
                    // display record by id
                    String record = getOneRecord(data);
                    String[] recordParsed = record.split(" ");
                    if (recordParsed[0].equals("Record")) { // check for error message
                        System.out.println(record);
                        outToClient(connectionSocket, record);
                    } else {
                        System.out.println("Request satisfied.");
                        outToClient(connectionSocket, "Requested record: " + record + " saved to db.\n");
                    }
                    String dbPrint2 = printDb();
                    outToClient(connectionSocket, dbPrint2);
                    clientSentence = null;
                    break;
                case "3":
                    // display all records above the sent score. - setup sub route for displaying
                    String dbPrint3 = getDbSubset(data);
                    outToClient(connectionSocket, dbPrint3);
                    clientSentence = null;
                    break;
                case "4":
                    // display all records
                    String dbPrint4 = printDb();
                    outToClient(connectionSocket, dbPrint4);
                    clientSentence = null;
                    break;
                case "5":
                    // delete record by id
                    String deleted = deleteOneRecord(data);
                    if (deleted.equals("true")){
                        outToClient(connectionSocket, "Record\n" + data + "\nwas deleted.\n");
                        String dbPrint5 = printDb();
                        outToClient(connectionSocket, dbPrint5);
                    } else {
                        outToClient(connectionSocket, deleted);
                    }
                    clientSentence = null;
                    break;
                default:
                    System.out.println("Server is pending next command...");
            }
        }

        System.out.println("Server closing...");
        connectionSocket.close();
        server.close();
    }

    private static void outToClient(Socket connectionSocket, String data) throws IOException {
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        System.out.println("connection socket closed? " + connectionSocket.isClosed());
        System.out.println("connection socket connected? " + connectionSocket.isConnected());
        outToClient.writeUTF(data);
        // outToClient.close();
    }

    private static String getFromClient(Socket connectionSocket) throws IOException {
        String clientSentence = null;
        DataInputStream inFromClient = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));
        while((clientSentence = inFromClient.readUTF()) == null) { }
        // inFromClient.close();
        return clientSentence;
    }

    private static String getDbSubset(String score) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(FILE_NAME));
        int compare = Integer.parseInt(score);
        String messageToClient = "";
        for(String line : fileLines) {
            String[] parsed = line.split(" ");
            int storedScore = Integer.parseInt(parsed[3]);
            if(compare >= storedScore) {
                System.out.println(line);
                messageToClient += line;
            }
        }
        return messageToClient;
    }

    private static String deleteOneRecord(String recordNum) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(FILE_NAME));
        String remove = "";
        for(String line : fileLines) {
            String[] lineParsed = line.split(" ");
            if(recordNum.equals(lineParsed[0])){
                remove = line;
            }
        }
        if (!remove.equals("")) {
            fileLines.remove(remove);
            Files.write(Paths.get(FILE_NAME), fileLines);
            return "true";
        }
        return "Record does not exist";
    }

    private static String getOneRecord(String recordNum) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(FILE_NAME));
        ListIterator<String> recordIterator = fileLines.listIterator();
        while(recordIterator.hasNext()){
            String recordString = recordIterator.next().toString();
            String[] recordParsed = recordString.split(" ");
            if (recordParsed[0].equals(recordNum)) {
                return recordString;
            }
        }
        return "Record does not exist";
    }

    private static String printDb() throws IOException {
        String messageToClient = "";
        List<String> fileLines = Files.readAllLines(Paths.get(FILE_NAME));
        for(String line : fileLines) {
            System.out.println(line);
            messageToClient += line + "\n";
        }
        return messageToClient;
    }

    private static void saveToDb(String newData) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(FILE_NAME));
        fileLines.add(newData);
        Files.write(Paths.get(FILE_NAME), fileLines);
        System.out.println("Updated file content: " + newData);
    }
}


// 1. add(ID, Fname, Lname, score): this request adds a new student's information into the database.

// 2. display(ID): this request sends the ID of a student to the server and the server returns the information of the student.

// 2. display(score): this request sends a score to the server and the server returns the information of all the students whose scores are above the sent score.

// 3. display_all: this request displays the information of all the students currently in the database.

// 4. delete(ID): this request deletes the student entry with that ID.

// // A Java program for a Server
// import java.net.*;
// import java.io.*;

// public class Server
// {
//     //initialize socket and input stream
//     private Socket          socket   = null;
//     private ServerSocket    server   = null;
//     private DataInputStream in       =  null;

//     // constructor with port
//     public Server(int port)
//     {
//         // starts server and waits for a connection
//         try
//         {
//             server = new ServerSocket(port);
//             System.out.println("Server started");

//             System.out.println("Waiting for a client ...");

//             socket = server.accept();
//             System.out.println("Client accepted");

//             // takes input from the client socket
//             in = new DataInputStream(
//                 new BufferedInputStream(socket.getInputStream()));

//             String line = "";

//             // reads message from client until "Over" is sent
//             while (!line.equals("Over"))
//             {
//                 try
//                 {
//                     line = in.readUTF();
//                     System.out.println(line);

//                 }
//                 catch(IOException i)
//                 {
//                     System.out.println(i);
//                 }
//             }
//             System.out.println("Closing connection");

//             // close connection
//             socket.close();
//             in.close();
//         }
//         catch(IOException i)
//         {
//             System.out.println(i);
//         }
//     }

//     public static void main(String args[])
//     {
//         Server server = new Server(5000);
//     }
// }
