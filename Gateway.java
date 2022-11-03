// E/17/201 

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Gateway{
	
	// Port that the server is running
	int UDP_Port;
	
	// List that store IDs of connected vital monitors  
    static private List<String> ids = new ArrayList<String>(); 
	
	// Buffer to receive data from socket
	private byte[] buffer ;  
   
    public Gateway() throws IOException{        
        this.UDP_Port = 6000;            // UDP packet receiving port is 6000 
		this.buffer = new byte[1024];    // creating a byte array as a receiving buffer
    }   
	
	// Function to initialize the gateway server 
    public void initConnection() throws ClassNotFoundException{
		
		try{
		// create a datagram socket to receive broadcast messages at port 6000
        DatagramSocket receivedSocket = new DatagramSocket(UDP_Port);
        System.out.println("Gateway is running & listening to broadcast port 6000 ..");
		
		// Keep listening to broadcast messages
		// Create a TCP connection for each vital monitor that is connecting
        while(true){
			
			// Create a datagram packet to receive data 
			DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
			
            try {
                receivedSocket.receive(receivedPacket);
				
                // Get vital monitor from socket     
				Monitor receivedMonitor = convertToMonitor(receivedPacket.getData());
				//System.out.println(receivedMonitor.getMonitorID());
				
				// Get the ip address, port amd ID of the monitor
				InetAddress ipAddress = receivedMonitor.getIp();
				int port = receivedMonitor.getPort();
				String id = receivedMonitor.getMonitorID();
				
				// check if the monitor is already in the list
				if (!ids.contains(ipAddress + ":" + port)) {
					// Add the monitor ID to the list
					ids.add(ipAddress + ":" + port);

					// Print the info about connected monitor 
					System.out.println("Establishing connection to monitor "+id+" at: " + ipAddress + ":" + port);

					// create tcp thread to connect to the monitor
					Thread tcpConnection = new Thread(new TCPconnection(receivedMonitor));

					// start the tcp connection thread
					tcpConnection.start();
				}
				buffer = new byte[65535];
				
            } catch (IOException e) {
				e.printStackTrace();
            }			
        }
		
		}catch (SocketException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }  
	
	// Function that convert byte array to a monitor object
    private static Monitor convertToMonitor(byte[] data){
		
        //initialize Monitor object
        Monitor monitor = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
		
        try (ObjectInputStream ois = new ObjectInputStream(bis)){
            monitor =  (Monitor)ois.readObject();             
        } catch (IOException | ClassNotFoundException | RuntimeException e) {           
            e.printStackTrace();
        }
        return monitor;
		
    }
	
	// Function to remove IDs of disconnected monitors from statis array list  
    public static synchronized void removeMonitorIDfromList(InetAddress ipAddress, int port, String id) {
        
        try {
            ids.remove(ipAddress + ":" + port);
            System.out.println("Vital Monitor "+id +" closed connection!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    // Main function 
    public static void main(String[] args) throws ClassNotFoundException, IOException {
		
        Gateway gateway = new Gateway();
        gateway.initConnection();
        
    }


}