package d3scomp.beeclick;

public class Test {

	public static void main(String[] args) throws D2XXException {
	
		MRF24J40 bee = new MRF24J40();
	
		bee.setChannel(3);		
		System.out.format("Listening on channel %d.\n", bee.getChannel());

		while (true) {
			MRF24J40.Packet packet = bee.recvPacket();
			
			if (packet != null) {
				System.out.format("Packet received (len = %d)\n", packet.data.length);
				break;
			} else {
				System.out.println("Nothing");
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		bee.cleanup();
	}

}
