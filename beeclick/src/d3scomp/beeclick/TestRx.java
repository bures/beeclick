package d3scomp.beeclick;

public class TestRx {

	public static void main(String[] args) throws D2XXException {
	
		MRF24J40 bee = new MRF24J40();
	
		bee.setChannel(1);
		bee.setPANID(0xBABA);
		bee.setShortAddr(0x0002);
		
		System.out.format("Reading info: channel=%d panID=%x shortAddr=%x.\n", bee.readChannel(), bee.readPANID(), bee.readShortAddr());

		while (true) {
			MRF24J40.RXPacket packet = bee.recvPacket();
			
			if (packet != null) {
				System.out.format("Packet received: len=%d fcs=%x lqi=%d rssi=%d\n  ", packet.data.remaining(), packet.fcs, packet.lqi, packet.rssi);
				
				while (packet.data.remaining() > 0) {
					System.out.printf("%x ", packet.data.get() & 0xFF);
					
				}
				System.out.println();
				
				break;
			} else {
				System.out.println(".");
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		bee.cleanup();
	}

}
