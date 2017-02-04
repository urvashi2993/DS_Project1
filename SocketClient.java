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
import java.net.UnknownHostException;
import java.nio.file.Files;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SocketClient {

    private String hostname;
    private int port;
    Socket socketClient;
    public static int Shared = 1;
    BufferedReader sin;
    PrintStream sout;
    BufferedReader stdin;
    DataInputStream dIn;
   
        String currentDir;
           ObjectInputStream oIn;
    ObjectOutputStream oOut;
    public SocketClient(String hostname, int port) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.hostname = hostname;
        this.port = port;
        Shared++;
        
        currentDir = System.getProperty("user.dir");
    }

    public boolean connect() throws UnknownHostException, IOException {
        System.out.println("Attempting to connect to " + hostname + ":" + port);
        try{
        socketClient = new Socket(hostname, port); 
        System.out.println("Connection Established");
        }
        catch (Exception e ){
            System.out.println("Error in socket Connection ");
            return false;
        }
        try{
        sin = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
        sout = new PrintStream(socketClient.getOutputStream());
        stdin = new BufferedReader(new InputStreamReader(System.in));
        dIn = new DataInputStream(socketClient.getInputStream());
         oIn = new ObjectInputStream(socketClient.getInputStream());
         }
        catch(Exception e){
            System.out.println("Error in creating streams ");
             return false;
        }
         return true;
    }

    

    public void sendFile(String s, byte[] b) throws IOException, NoSuchAlgorithmException, Exception {
        long start = 0 , finish = 0 ;
        String Hash;
        sout.println(s);// send file name to server w 
        s = sin.readLine(); // read respons from server
        if (s.equalsIgnoreCase("ready")) { // if serever sent "ready"
            DataOutputStream dOut = new DataOutputStream(socketClient.getOutputStream()); 
             dOut.writeInt(b.length); // write length of the file
             dOut.write(b); // send the file
             dOut.close();
        }

   

    }

    public boolean recieveFile(String command, String fileName) throws IOException, NoSuchAlgorithmException, Exception {
          File file = new File( currentDir + "\\s" + fileName); 
        sout.println(command); // request file
        int length = dIn.readInt();        // read length of incoming file
        byte[] message = null;
        if (length > 0) {
            message = new byte[length];
            dIn.readFully(message, 0, message.length); // read the file
             FileOutputStream fop = new FileOutputStream(file,false); 
            fop.write(message); 
            fop.close();
            return true;
        }
        else{ // no file in server with this name
            
            return false;
          
        }
           
        } 
 
    

    public boolean DeleteFile(String s) throws IOException, NoSuchAlgorithmException, Exception {
        sout.println(s);// send file name to server w 
        s = sin.readLine();
         if (s.equalsIgnoreCase("deleted")) {
            return true;
        }
        return false;

    }
  public boolean printWD(String s) throws IOException, NoSuchAlgorithmException, Exception {
        sout.println(s);// send file name to server w 
        s = sin.readLine();
        System.out.println("W.D:" + s);
        return false;

    }
    public boolean ls(String s) throws IOException, NoSuchAlgorithmException, Exception {
        sout.println(s);// send file name to server w 
        String filesList[] = (String[])oIn.readObject();
        for(int i = 0 ; i < filesList.length; i++)
        System.out.print(filesList[i] + "\t");
        
        System.out.println("");
        return false;

    }
     public boolean cd(String s) throws IOException, Exception {
        sout.println(s);// send file name to server w 
         s = sin.readLine();
         if (s.equalsIgnoreCase("error")) {
            return false;
        } 
         return true;
    }
    public boolean makeDir(String s) throws IOException,  Exception {
        sout.println(s);// send file name to server w 
        s = sin.readLine();
        if (s.equalsIgnoreCase("created")) {
             System.out.println("dir created!");
            return true;
        } 
         System.out.println("dir can't be created/already exist!"); //  
         return false;
          
    }
      public boolean delete(String s) throws IOException, Exception {
        sout.println(s);// send file name to server w 
        return true;
    }
public boolean exist(String file){
        return new File(file).exists();
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
                    
    public static void main(String arg[]) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Creating a SocketClient object
        SocketClient client = new SocketClient("localhost", 9990);
        String line,completeFilePath;
      
        String commands[];
        Scanner read = new Scanner(System.in);
        if (client.connect()){
        while(true){
            System.out.print( client.currentDir );
            System.out.print("myftp >");
            line = read.nextLine();
            commands = line.split("\\s+");
            if( commands[0].equalsIgnoreCase("put")){
                
                
                completeFilePath = client.currentDir+"\\"+commands[1];
                if( client.exist(completeFilePath) )
                {
                    try {
                        client.sendFile(line, client.convertToBytes(completeFilePath));
                    } catch (Exception ex) {
                        Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
                else {
                    System.out.println("File is not exist!");
                }         
                
            }
                if( commands[0].equalsIgnoreCase("get")){
                    System.out.println("Client get:");
                    try{
                         if ( !client.recieveFile(line,commands[1]))
                             System.out.println("No File Found!");
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
                    
                   
                    
            }    
              if( commands[0].equalsIgnoreCase("delete")){
                    System.out.println("Client delete:");
                    try{
                         if ( !client.DeleteFile(line))
                             System.out.println("No File Found!");
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            
            
            
        }
        if( commands[0].equalsIgnoreCase("pwd")){
                    System.out.println("Client pwd:");
                    try{
                          client.printWD(line); 
                              
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            }
        
        if( commands[0].equalsIgnoreCase("ls")){
                    System.out.println("Client ls:");
                    try{
                          client.ls(line); 
                              
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            }
        
        if( commands[0].equalsIgnoreCase("cd")){
                    System.out.println("Client cd:");
                    try{
                          client.cd(line); 
                              
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            }
        
        if( commands[0].equalsIgnoreCase("pwd")){
                    System.out.println("Client pwd:");
                    try{
                          client.printWD(line); 
                              
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            }
         if( commands[0].equalsIgnoreCase("mkdir")){
                    System.out.println("Client pwd:");
                    try{
                          client.makeDir(line); 
                              
                    }
                    catch(Exception e){
                        System.out.println("Error in revieving File");
                        e.printStackTrace();
                    }
            }
        
        
        
        }
        
        }
    }
}
        
        
    

