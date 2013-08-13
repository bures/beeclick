package d3scomp.beeclick;

public class TestINTSTAT {

	public static void main(String[] args) throws D2XXException {
	
		MRF24J40 bee = new MRF24J40();
	
		bee.testINTSTAT();
		
		bee.cleanup();
	}

}
