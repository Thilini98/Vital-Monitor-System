// E/17/201

import java.io.*;
import java.net.*;

public class TCPconnection implements Runnable{
	
	// Parameters for monitor object
	private final InetAddress ip;
    private final String monitorID;
    private final int port;

    public TCPconnection(Monitor receivedMonitor){
		
        // store the parameters of connected monitor 
        this.ip = receivedMonitor.getIp();
        this.monitorID = receivedMonitor.getMonitorID();
        this.port = receivedMonitor.getPort();
    }

    public void createTCPconnection() {
        try {
			// Create a socket to connect to the monitor 
			Socket clientSocket = new Socket(this.ip,this.port);

			// Keep reading data from the monitor
			try{
				while(true){
				// Read data from socket to buffer 
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				// Print data received and Thread ID				
				String message = in.readLine();
                System.out.println("Message : " +message+ " -running in Thread ID: " + Thread.currentThread().getId());
				}			
			}catch(NullPointerException e) {
				 System.out.println("Client monitor: " + this.monitorID+" closed connection");
	        }catch (SocketException e){
			    
	        }
			
			// Removing the ID of the monitor from the static ArrayList inside the Gateway class
            Gateway.removeMonitorIDfromList(this.ip, this.port, this.monitorID);

            // Close the socket connection 
            clientSocket.close();

            // Exit from the thread here after
        		
        }catch (Exception e) {
             System.out.println("Exception in thread: " + Thread.currentThread().getId());
        }

    }

    // implementing the run() method inherited from the Runnable interface
    @Override
    public void run() {
        this.createTCPconnection();
    }
}