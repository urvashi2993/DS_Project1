/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ds;

   
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
  public static void main(String[] args) throws Exception {
    ServerSocket m_ServerSocket = new ServerSocket(9990);
    int id = 0;
    while (true) {
      Socket clientSocket = m_ServerSocket.accept();
      ClientServiceThreadC cliThread = new ClientServiceThreadC(clientSocket, id++);
      cliThread.start();
    }
  }
}