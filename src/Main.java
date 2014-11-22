import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;



public class Main {

	public static void main(String[] args) {
	
		
		Reciever r = new Reciever();
		new Thread(r).start();
		
		System.out.println("START");
		Sender s = new Sender();
		new Thread(s).start();
		
		String mac = randomMACAddress();
		
		try{
		if(Configuration.isGO){
			System.out.println("I am the GO");
			AllEncompasingP2PClient allenmc = new AllEncompasingP2PClient(mac, "127.0.0.1", "meh", mac);
			MeshNetworkManager.setSelf(allenmc);
			MeshNetworkManager.newClient(allenmc);
		}
		else{
			System.out.println("I am not");
			AllEncompasingP2PClient allenmc = new AllEncompasingP2PClient(mac, "127.0.0.1", "meh", "00:00:00:00:00:00");
			MeshNetworkManager.setSelf(allenmc);
			MeshNetworkManager.newClient(allenmc);
		}
		}catch(Exception e){
			System.out.println("EXCEPTION");
			e.printStackTrace();
		}
		
	
		if(!Configuration.isGO){
			Sender.queuePacket(new Packet(Packet.TYPE.HELLO, new byte[0], null, mac));
		}
		
		Scanner scanner = new Scanner(System.in);
		String input = "";
		while(input != "quit"){
			input = scanner.nextLine();
			for(AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()){
				if(c.getMac().equals(MeshNetworkManager.getSelf().getMac())) continue;
				Sender.queuePacket(new Packet(Packet.TYPE.MESSAGE, input.getBytes(), c.getMac(), mac));
			}
		}
		
		scanner.close();
		
	}
	
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
