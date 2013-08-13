package d3scomp.beeclick;

import java.nio.ByteBuffer;

public class TestTx {

	public static void main(String[] args) throws D2XXException {
	
		MRF24J40 bee = new MRF24J40();
		
		bee.setChannel(1);
		bee.setPANID(0xBABA);
		bee.setShortAddr(0x0001);
		
		System.out.format("Reading info channel=%d panID=%x shortAddr=%x.\n", bee.readChannel(), bee.readPANID(), bee.readShortAddr());

		ByteBuffer data = ByteBuffer.allocateDirect(16);
		data.put((byte) 0xAB);
		data.put((byte) 0xCD);
		data.flip();
		
		bee.broadcastPacket(data);
		
		MRF24J40.TXState txState;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			txState = bee.getTXState();
			
			System.out.println(txState);
		} while (txState == MRF24J40.TXState.PENDING);
		
		bee.cleanup();
	}

}
