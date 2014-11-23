
import java.util.concurrent.ConcurrentLinkedQueue;

public class Reciever implements Runnable {
	public static int clientNumber = 0;
	public Reciever() {
	}

	public void run() {
		ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<Packet>();
		new Thread(new TcpReciever(Configuration.RECEIVE_PORT, packetQueue)).start();
		

		Packet p;

		while (true) {
			while (packetQueue.isEmpty()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			p = packetQueue.remove();	
			
			if(p.getType().equals(Packet.TYPE.HELLO)){
				//Put it in your routing table
				for(AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()){
					if(c.getMac().equals(MeshNetworkManager.getSelf().getMac())
							|| c.getMac().equals(p.getSenderMac())) continue;
					Packet update = new Packet(Packet.TYPE.UPDATE, Packet.getMacAsBytes(p.getSenderMac()), c.getMac(), MeshNetworkManager.getSelf().getMac());
					Sender.queuePacket(update);
				}

				MeshNetworkManager.routingTable.put(p.getSenderMac(), new AllEncompasingP2PClient(p.getSenderMac(), p.getSenderIP(), p.getSenderMac(), MeshNetworkManager.getSelf().getMac()));
				//Send update to all nodes in your routing table


				//Send routing table back as HELLO_ACK
				byte[] rtable = MeshNetworkManager.serializeRoutingTable();


				Packet ack = new Packet(Packet.TYPE.HELLO_ACK, rtable, p.getSenderMac(), MeshNetworkManager.getSelf().getMac());
				Sender.queuePacket(ack);
			}
			else{
			//If you're the intendeded target for a non hello message
			if(p.getMac().equals(MeshNetworkManager.getSelf().getMac())){
//				System.out.println("I am the intended recipient\n");
					if(p.getType().equals(Packet.TYPE.HELLO_ACK)){
						MeshNetworkManager.deserializeRoutingTableAndAdd(p.getData());
						MeshNetworkManager.getSelf().setGroupOwnerMac(p.getSenderMac());
						System.out.println("You're now connected to the Echo group.");
						System.out.println("\n\n ");
						System.out.println("Here is who's here:\n");
						for(AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()){
							System.out.println("["+ clientNumber++ + "]" + c.getMac());
						}
						System.out.println("To send a message to client [X] please type the related mac address from the table above an press enter: Send to XX:XX:XX:XX:XX:XX");
						System.out.println("The next line will allow you to send the message ONLY to that person.");
					}
					else if(p.getType().equals(Packet.TYPE.UPDATE)){
//						System.out.println("GOT UPDATE");
						String emb_mac = Packet.getMacBytesAsString(p.getData(), 0);
						MeshNetworkManager.routingTable.put(emb_mac, new AllEncompasingP2PClient(emb_mac, p.getSenderIP(), p.getMac(), MeshNetworkManager.getSelf().getMac()));
						System.out.println(emb_mac + " joined the chat.");
					}
					else if(p.getType().equals(Packet.TYPE.MESSAGE)){
//						System.out.println("GOT MESSAGE");
						System.out.println(p.getSenderMac() +" said : " + new String(p.getData()));
					}
				}
				else{
					//otherwise forward it 
//					System.out.println("I (" + MeshNetworkManager.getSelf().getMac() +") am not the recipient ("+ p.getMac() +"). \n");
					int ttl = p.getTtl();
					ttl--;
					if(ttl > 0){
						Sender.queuePacket(p);
						p.setTtl(ttl);
					}
				}
			}



		}
	}

}