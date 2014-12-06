import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;


/**
 * The main CLI program
 * @author Peter Henderson
 *
 */
public class Main {
	static String echo_ascii = (
"               )    )  \n" +
"       (   ( /( ( /(  \n" +
"  (     )\\  )\\()))\\())  \n" +
"   )\\  (((_)((_)\\((_)\\   \n" + 
" ((_) )\\___ _((_) ((_)  \n" +
" | __((/ __| || |/ _ \\  \n" +
" | _| | (__| __ | (_) | \n" +
" |___| \\___|_||_|\\___/  \n" +
"                      \n");
	
	public static void main(String[] args) {
	
		
		/*
		 * Intro message
		 */
		System.out.println("      WELCOME TO");
		for(int i=0;i<echo_ascii.length();i++){
			System.out.print(echo_ascii.charAt(i));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		 * If you're hanging after the echo message, probably need to disable your firewall 
		 */
		
		Reciever r = new Reciever();
		new Thread(r).start();
		
		Sender s = new Sender();
		new Thread(s).start();
		
		/*
		 * spoof your mac 
		 */
		
		String mac = randomMACAddress();
		
		/*
		 * set yourself with some temporary info about group owner and name, this will be updated on HELLO_ACK
		 */
		try{
			AllEncompasingP2PClient allenmc = new AllEncompasingP2PClient(mac, "127.0.0.1", "meh", "00:00:00:00:00:00");
			MeshNetworkManager.setSelf(allenmc);
			MeshNetworkManager.newClient(allenmc);
		}catch(Exception e){
			System.out.println("Exception. Please restart the tool.");
			e.printStackTrace();
		}
		
		/*
		 * Send a Hello 
		 */
		Sender.queuePacket(new Packet(Packet.TYPE.HELLO, new byte[0], null, mac));
		
		Scanner scanner = new Scanner(System.in);
		String input = "";
		
		boolean sendToSingle = false;
		String macSingle = "00:00:00:00:00:00";
		
		while(input != "quit"){
			input = scanner.nextLine();
			if(sendToSingle){
				sendToSingle = false;
				AllEncompasingP2PClient c = MeshNetworkManager.routingTable.get(macSingle);
				Sender.queuePacket(new Packet(Packet.TYPE.MESSAGE, input.getBytes(), c.getMac(), mac));
			}
			else if(input.startsWith("Send to ")){
				macSingle = (String) input.substring(8, input.length());
//				System.out.println("Mac single: " + macSingle);
				sendToSingle = true;
			}
			else{
				for(AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()){
					if(c.getMac().equals(MeshNetworkManager.getSelf().getMac())) continue;
					Sender.queuePacket(new Packet(Packet.TYPE.MESSAGE, input.getBytes(), c.getMac(), mac));
				}
			}
		}
		
		scanner.close();
		
	}
	
	/**
	 * helper to spoof your mac
	 * @return
	 */
	private static String randomMACAddress(){
	    Random rand = new Random();
	    byte[] macAddr = new byte[6];
	    rand.nextBytes(macAddr);

	    macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

	    StringBuilder sb = new StringBuilder(18);
	    for(byte b : macAddr){

	        if(sb.length() > 0)
	            sb.append(":");

	        sb.append(String.format("%02x", b));
	    }


	    return sb.toString();
	}
}
