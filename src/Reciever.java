
import java.util.concurrent.ConcurrentLinkedQueue;

public class Reciever implements Runnable {

	public Reciever() {
	}

	public void run() {
		ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<Packet>();

		// Start the TcpReciver 
		if(Configuration.isGO)
			new Thread(new TcpReciever(Configuration.GO_RECEIVE_PORT, packetQueue)).start();
		else{
			new Thread(new TcpReciever(Configuration.RECEIVE_PORT, packetQueue)).start();
		}

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
			
//			System.out.println("Packet :" + p.toString());
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
//				System.out.println("GOT HELLO");
			}
			else{
			//If you're the intendeded target for a non hello message
			if(p.getMac().equals(MeshNetworkManager.getSelf().getMac())){
//				System.out.println("I am the intended recipient\n");
					if(p.getType().equals(Packet.TYPE.HELLO_ACK)){
						MeshNetworkManager.deserializeRoutingTableAndAdd(p.getData());
						MeshNetworkManager.getSelf().setGroupOwnerMac(p.getSenderMac());
						System.out.println("GOT HELLO ACK!");
					}
					else if(p.getType().equals(Packet.TYPE.UPDATE)){
//						System.out.println("GOT UPDATE");
						String emb_mac = Packet.getMacBytesAsString(p.getData(), 0);
						MeshNetworkManager.routingTable.put(emb_mac, new AllEncompasingP2PClient(emb_mac, p.getSenderIP(), p.getMac(), MeshNetworkManager.getSelf().getMac()));
					}
					else if(p.getType().equals(Packet.TYPE.MESSAGE)){
//						System.out.println("GOT MESSAGE");
						System.out.println("Message: " + new String(p.getData()));
					}
				}
				else{
					//otherwise forward it 
//					System.out.println("I (" + MeshNetworkManager.getSelf().getMac() +") am not the recipient ("+ p.getMac() +"). \n");
					int ttl = p.getTtl();
					ttl--;
					if(ttl > 0){
						System.out.println("RESENDING");
						Sender.queuePacket(p);
						p.setTtl(ttl);
					}
				}
			}



		}
	}

}