/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientServiceThreadC extends Thread {

    Socket clientSocket;
    int clientID = -1;
    boolean running = true;
   

     String currentDir,completeFilePath;  
    BufferedReader cin;
    PrintStream cout;
    BufferedReader stdin;
    DataInputStream dIn;
    DataOutputStream dOut;
     ObjectInputStream oIn;
    ObjectOutputStream oOut;
    ClientServiceThreadC(Socket s, int i) throws IOException {
        clientSocket = s;
        clientID = i;
        
        cin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        cout = new PrintStream(clientSocket.getOutputStream());
        stdin = new BufferedReader(new InputStreamReader(System.in));
        dIn = new DataInputStream(clientSocket.getInputStream());
        dOut = new DataOutputStream(clientSocket.getOutputStream());
         oOut = new ObjectOutputStream(clientSocket.getOutputStream());
        currentDir = System.getProperty("user.dir");
    }

    public void run() {
        System.out.println("Accepted Client : ID - " + clientID + " : Address - "
                + clientSocket.getInetAddress().getHostName());
        try {
            String line;
            String commands[];
            while (true) {

               
                line = cin.readLine();// get inpt from client 
                commands = line.split("\\s+");
               if( commands[0].equalsIgnoreCase("put")){// recieve file 
                   System.out.println("Server put");
                    cout.println("Ready");  
                    String Fname = commands[1];
                    byte b[] = readNewFile(); // read new file
                    FileOutputStream fos = new FileOutputStream(currentDir+"\\Up_"+Fname);
                    fos.write(b);
                    fos.close();
                   
                 
                     

                } else if (commands[0].equalsIgnoreCase("get")) {
                    System.out.println("Server get"); 
                    completeFilePath = currentDir+"\\"+commands[1];
                    
                    if(exist(completeFilePath)){
                    byte b[] = convertToBytes(completeFilePath);
                    dOut.writeInt(b.length); // write length of the file
                    dOut.write(b);
                    }
                       dOut.writeInt(0); // not exist  

                }
                else if (commands[0].equalsIgnoreCase("delete")) {
                    System.out.println("Server delete"); 
                    completeFilePath = currentDir+"\\"+commands[1];
                    
                    if(delete(completeFilePath)){
                     
                    cout.println("deleted");
                    }
                    else
                       cout.println("not exist"); // not exist  

                }
                 else if (commands[0].equalsIgnoreCase("pwd")) {
                    System.out.println("Server pwd");              
                    cout.println( currentDir );
                    // not exist  

                }
                else if (commands[0].equalsIgnoreCase("ls")) {
                    System.out.println("Server ls");              
                    oOut.writeObject(ls());
                    // not exist  

                }
                else if (commands[0].equalsIgnoreCase("cd")) {
                    System.out.println("Server cd");              
                    cd(commands[1]);
                    // not exist  

                }
               else if (commands[0].equalsIgnoreCase("mkdir")) {
                    System.out.println("Server mkdir");              
                    if (makeDir(commands[1])) 
                         cout.println("created");
                    else
                            cout.println("not created");

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientServiceThreadC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }




    private byte[] readNewFile() throws IOException {
        int length = dIn.readInt();                    // read length of incoming message
        byte[] message = null;
        if (length > 0) {
            message = new byte[length];
            dIn.readFully(message, 0, message.length); // read the message
        }
 
        return message;
    }
    
    public byte[] convertToBytes(String file){
         
    
        byte[] fileInBytes = null;
        try {
            fileInBytes = Files.readAllBytes(new File(file).toPath());
        } catch (IOException ex) {
            System.out.print("Error in convrting file to bytes");
            
        }
        return fileInBytes;
       }   
public boolean exist(String file){
        return new File(file).exists();
    }
public boolean delete(String file){
        if( !exist(file))
            return false;
        return new File(file).delete();
    }

public String[] ls(){
        File dir = new File(currentDir);
        return dir.list();
    }
public void cd(String arg){
        if (arg.equals(".."))
            currentDir = currentDir.substring(0,currentDir.lastIndexOf("\\"));
        else if ( new File(currentDir).isDirectory() )
            currentDir = arg ;
        else
            cout.println("error");
    }

public boolean makeDir(String arg){
        if (!exist(arg))
            return new File(currentDir+"\\"+arg).mkdir();
         else
            return false;
    }

}
