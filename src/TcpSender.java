

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpSender {
	
	Socket tcpSocket = null;
	
	public boolean sendPacket(String ip, int port, byte[] data) {
		
		try {
			InetAddress serverAddr = InetAddress.getByName(ip);
			tcpSocket = new Socket(serverAddr, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		OutputStream os = null;
		
		try {
			os = tcpSocket.getOutputStream();
			os.write(data);
			os.flush();
			tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
}
