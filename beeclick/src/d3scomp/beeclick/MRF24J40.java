/*
 * MRF24J40.java
 *
 *  Created on: 12.8.2013
 *      Author: Tomas Bures
 */

package d3scomp.beeclick;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MRF24J40 {
	public static class RXPacket {
		ByteBuffer data;
		int fcs;
		int lqi;
		int rssi;
	}
	
	/* short registers */
	public static int RXMCR = 0x00;
	public static int PANIDL = 0x01;
	public static int PANIDH = 0x02;
	public static int SADRL = 0x03;
	public static int SADRH = 0x04;
	public static int EADR0 = 0x05;
	public static int EADR1 = 0x06;
	public static int EADR2 = 0x07;
	public static int EADR3 = 0x08;
	public static int EADR4 = 0x09;
	public static int EADR5 = 0x0A;
	public static int EADR6 = 0x0B;
	public static int EADR7 = 0x0C;
	public static int RXFLUSH = 0x0D;
	public static int ORDER = 0x10;
	public static int TXMCR = 0x11;
	public static int ACKTMOUT = 0x12;
	public static int ESLOTG1 = 0x13;
	public static int SYMTICKL = 0x14;
	public static int SYMTICKH = 0x15;
	public static int PACON0 = 0x16;
	public static int PACON1 = 0x17;
	public static int PACON2 = 0x18;
	public static int TXBCON0 = 0x1A;
	public static int TXNCON = 0x1B;
	public static int TXG1CON = 0x1C;
	public static int TXG2CON = 0x1D;
	public static int ESLOTG23 = 0x1E;
	public static int ESLOTG45 = 0x1F;
	public static int ESLOTG67 = 0x20;
	public static int TXPEND = 0x21;
	public static int WAKECON = 0x22;
	public static int FRMOFFSET = 0x23;
	public static int TXSTAT = 0x24;
	public static int TXBCON1 = 0x25;
	public static int GATECLK = 0x26;
	public static int TXTIME = 0x27;
	public static int HSYMTMRL = 0x28;
	public static int HSYMTMRH = 0x29;
	public static int SOFTRST = 0x2A;
	public static int SECCON0 = 0x2C;
	public static int SECCON1 = 0x2D;
	public static int TXSTBL = 0x2E;
	public static int RXSR = 0x30;
	public static int INTSTAT = 0x31;
	public static int INTCON = 0x32;
	public static int GPIO = 0x33;
	public static int TRISGPIO = 0x34;
	public static int SLPACK = 0x35;
	public static int RFCTL = 0x36;
	public static int SECCR2 = 0x37;
	public static int BBREG0 = 0x38;
	public static int BBREG1 = 0x39;
	public static int BBREG2 = 0x3A;
	public static int BBREG3 = 0x3B;
	public static int BBREG4 = 0x3C;
	public static int BBREG6 = 0x3E;
	public static int CCAEDTH = 0x3F;

	/* long registers */
	public static int RFCON0 = 0x200;
	public static int RFCON1 = 0x201;
	public static int RFCON2 = 0x202;
	public static int RFCON3 = 0x203;
	public static int RFCON5 = 0x205;
	public static int RFCON6 = 0x206;
	public static int RFCON7 = 0x207;
	public static int RFCON8 = 0x208;
	public static int SLPCAL0 = 0x209;
	public static int SLPCAL1 = 0x20A;
	public static int SLPCAL2 = 0x20B;
	public static int RFSTATE = 0x20F;
	public static int RSSI = 0x210;
	public static int SLPCON0 = 0x211;
	public static int SLPCON1 = 0x220;
	public static int WAKETIMEL = 0x222;
	public static int WAKETIMEH = 0x223;
	public static int REMCNTL = 0x224;
	public static int REMCNTH = 0x225;
	public static int MAINCNT0 = 0x226;
	public static int MAINCNT1 = 0x227;
	public static int MAINCNT2 = 0x228;
	public static int MAINCNT3 = 0x229;
	public static int ASSOEADR0 = 0x230;
	public static int ASSOEADR1 = 0x231;
	public static int ASSOEADR2 = 0x232;
	public static int ASSOEADR3 = 0x233;
	public static int ASSOEADR4 = 0x234;
	public static int ASSOEADR5 = 0x235;
	public static int ASSOEADR6 = 0x236;
	public static int ASSOEADR7 = 0x237;
	public static int ASSOSADR0 = 0x238;
	public static int ASSOSADR1 = 0x239;
	public static int UPNONCE0 = 0x240;
	public static int UPNONCE1 = 0x241;
	public static int UPNONCE2 = 0x242;
	public static int UPNONCE3 = 0x243;
	public static int UPNONCE4 = 0x244;
	public static int UPNONCE5 = 0x245;
	public static int UPNONCE6 = 0x246;
	public static int UPNONCE7 = 0x247;
	public static int UPNONCE8 = 0x248;
	public static int UPNONCE9 = 0x249;
	public static int UPNONCE10 = 0x24A;
	public static int UPNONCE11 = 0x24B;
	public static int UPNONCE12 = 0x24C;
	
	public static int RXFIFO = 0x300;
	public static int TXNFIFO = 0x000;
	
	private ByteBuffer mpsseBuf = ByteBuffer.allocateDirect(65536);
	
	private D2XX d2xx;
	
	private int panID, shortAddr, channel;
	private byte lastINTSTAT;
	
	static public enum TXState {
		NOTHING_SENT,
		PENDING,
		SUCCESSFUL,
		FAILED
	}
	
	private TXState txState = TXState.NOTHING_SENT;
		
	public MRF24J40() throws D2XXException {
		d2xx = new D2XX();
		initGPIO();
		reset();
	}
	
	private void initGPIO() throws D2XXException {
		clearBuffer();
		
		// Set up the Hi-Speed specific commands for the FTx232H
		mpsseBuf.put((byte) 0x8A ); // Use 60MHz master clock (disable divide by 5)
		mpsseBuf.put((byte) 0x97 ); // Turn off adaptive clocking (may be needed for ARM)
		mpsseBuf.put((byte) 0x8D ); // Disable three-phase clocking

		
		// Set TCK frequency
		// TCK = 60MHz /((1 + [(1 +0xValueH*256) OR 0xValueL])*2)
		mpsseBuf.put((byte) 0x86 );	// Command to set clock divisor
		mpsseBuf.put((byte) 1 );	// Set 0xValueL of clock divisor (15 MHz)
		mpsseBuf.put((byte) 0 ); // Set 0xValueH of clock divisor  (15 MHz)
		
		
		// Set initial states of the MPSSE interface
		// 		- low byte, both pin directions and output values
		// 		Pin name 	Signal	Direction 	Config 	Initial State	Config
		// 		BDBUS0 		TCK/SK	output		1		low				0
		// 		BDBUS1		TDI/DO	output		1		low				0
		//		BDBUS2		TDO/DI	input		0						0
		//		BDBUS3		TMS/CS	output		1		high			1
		//		BDBUS4 		GPIOL0 	input 		0 			 			0
		//		BDBUS5		GPIOL1 	input 		0 						0
		//		BDBUS6		GPIOL2 	input 		0 		 				0
		//		BDBUS7		GPIOL3 	input 		0 			 			0
		mpsseBuf.put((byte) 0x80 ); // Configure data bits low-byte of MPSSE port
		mpsseBuf.put((byte) 0x08 ); // Initial state config above
		mpsseBuf.put((byte) 0x0B ); // Direction config above
		
		// Note that since the data are clocked on the falling edge, inital clock state of low is selected. Clocks will be generated as low-high-low.
		// In this case, data changes on the falling edge to give it enough time to have it available at the device, which will accept data *into* the target device
		// on the rising edge. The input from the device works vice-versa. The device clocks the data on the falling edge and we receive them on the rising edge.

		// Set initial states of the MPSSE interface
		// 		- high byte, both pin directions and output values
		// 		Pin name 	Signal 	Direction 	Config	Initial State Config
		// 		BCBUS0 		GPIOH0 	input 		0 					0
		// 		BCBUS1 		GPIOH1 	output 		1 		low			0
		// 		BCBUS2 		GPIOH2 	input 		0 					0
		// 		BCBUS3 		GPIOH3 	input 		0 					0
		// 		BCBUS4 		GPIOH4 	input 		0 					0
		// 		BCBUS5 		GPIOH5 	input 		0 					0
		// 		BCBUS6 		GPIOH6 	input 		0 					0
		// 		BCBUS7 		GPIOH7 	input 		0 					0
		mpsseBuf.put((byte) 0x82 );
		mpsseBuf.put((byte) 0x00 );	// Initial state config above
		mpsseBuf.put((byte) 0x02 );	// Direction config above

		sendBuffer();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		
		// Release BCBUS1 - RESET#
		mpsseBuf.put((byte) 0x82 );
		mpsseBuf.put((byte) 0x02 );	
		mpsseBuf.put((byte) 0x02 );	

		sendBuffer();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}
	
	private void clearBuffer() {
		mpsseBuf.clear();
	}
	
	private void sendBuffer() throws D2XXException {
		mpsseBuf.flip();
		d2xx.writeAll(mpsseBuf);
		mpsseBuf.clear();
	}
	
	private void recvBuffer(int len) throws D2XXException {
		mpsseBuf.clear();
		mpsseBuf.limit(len);
		d2xx.readAll(mpsseBuf);
	}
	
	private void addLowerCSToBuffer() {
		mpsseBuf.put((byte) 0x80 ); // Lower CS
		mpsseBuf.put((byte) 0x00 );
		mpsseBuf.put((byte) 0x0B );		
	}
	
	private void addRaiseCSToBuffer() {
		mpsseBuf.put((byte) 0x80 ); // Raise CS
		mpsseBuf.put((byte) 0x08 );
		mpsseBuf.put((byte) 0x0B );		
	}
	
	private void addWriteRegToBuffer(int reg, int val) {
		
		if (reg < 0x40) {
			addLowerCSToBuffer();
		
			mpsseBuf.put((byte) 0x11 ); // Clock Data Bytes Out on falling clock edge MSB first (no read) 
		
			mpsseBuf.put((byte) 1 );  // LengthL (2 bytes) 
			mpsseBuf.put((byte) 0 );  // LengthH
			
			mpsseBuf.put((byte) ((reg << 1) | 0x01) );
			mpsseBuf.put((byte) val );
			
			addRaiseCSToBuffer();
			
		} else {
			addWriteLongRegToBuffer(reg, val);
		}
	}

	private void addWriteLongRegToBuffer(int reg, int val) {
		assert(reg < 0x400);
		
		addLowerCSToBuffer();
		
		mpsseBuf.put((byte) 0x11 ); // Clock Data Bytes Out on falling clock edge MSB first (no read) 
		
		mpsseBuf.put((byte) 2 );  // LengthL (3 bytes) 
		mpsseBuf.put((byte) 0 );  // LengthH
			
		mpsseBuf.put((byte) ((reg >> 3) | 0x80) );
		mpsseBuf.put((byte) (((reg & 0x7) << 5) | 0x10) );
		mpsseBuf.put((byte) val );			
		
		addRaiseCSToBuffer();
	}

	private void addReadRegToBuffer(int reg) {
		if (reg < 0x40) {
			addLowerCSToBuffer();
		
			mpsseBuf.put((byte) 0x11 ); // Clock Data Bytes Out on falling clock edge MSB first (no read) 
		
			mpsseBuf.put((byte) 0 );  // LengthL (1 bytes) 
			mpsseBuf.put((byte) 0 );  // LengthH
			
			mpsseBuf.put((byte) (reg << 1) );

			mpsseBuf.put((byte) 0x20 ); // Clock Data Bytes In on rising clock edge MSB first (no write)
			mpsseBuf.put((byte) 0 );  // LengthL (1 bytes) 
			mpsseBuf.put((byte) 0 );  // LengthH
	
			addRaiseCSToBuffer();
		
		} else {
			addReadLongRegToBuffer(reg);
		}
	}
	
	private void addReadLongRegToBuffer(int reg) {
		assert(reg < 0x400);
		
		addLowerCSToBuffer();
		
		mpsseBuf.put((byte) 0x11 ); // Clock Data Bytes Out on falling clock edge MSB first (no read) 
		
		mpsseBuf.put((byte) 1 );  // LengthL (2 bytes) 
		mpsseBuf.put((byte) 0 );  // LengthH
			
		mpsseBuf.put((byte) ((reg >> 3) | 0x80) );
		mpsseBuf.put((byte) ((reg & 0x7) << 5) );

		mpsseBuf.put((byte) 0x20 ); // Clock Data Bytes In on rising clock edge MSB first (no write)
		mpsseBuf.put((byte) 0 );  // LengthL (1 bytes) 
		mpsseBuf.put((byte) 0 );  // LengthH

		addRaiseCSToBuffer();
	}

	private void writeReg(int reg, int val) throws D2XXException {
		clearBuffer();
		addWriteRegToBuffer(reg, val);
		sendBuffer();
	}
	
	private int readReg(int reg) throws D2XXException {
		clearBuffer();
		addReadRegToBuffer(reg);
		addSendImmediateToBuffer();
		sendBuffer();
		
		recvBuffer(1);

		return mpsseBuf.get(0) & 0xFF;
	}
	
	private void addSendImmediateToBuffer() {
		mpsseBuf.put((byte) 0x87 ); // Send immediate
	}
	
	public void reset() throws D2XXException {
		clearBuffer();

		// 1. SOFTRST (0x2A) = 0x07 – Perform a software Reset. The bits will be automatically cleared to ‘0’ by hardware.		
		addWriteRegToBuffer(SOFTRST, 0x07);

		// 2. PACON2 (0x18) = 0x98 – Initialize FIFOEN = 1 and TXONTS = 0x6.
		addWriteRegToBuffer(PACON2, 0x98);

		// 3. TXSTBL (0x2E) = 0x95 – Initialize RFSTBL = 0x9.
		addWriteRegToBuffer(TXSTBL, 0x95);
		
		// 4. RFCON0 (0x200) = 0x03 – Initialize RFOPT = 0x03.
		addWriteRegToBuffer(RFCON0, 0x03);
		
		// 5. RFCON1 (0x201) = 0x01 – Initialize VCOOPT = 0x02.
		addWriteRegToBuffer(RFCON1, 0x01);
		
		// 6. RFCON2 (0x202) = 0x80 – Enable PLL (PLLEN = 1).
		addWriteRegToBuffer(RFCON2, 0x80);
		
		// 7. RFCON6 (0x206) = 0x90 – Initialize TXFIL = 1 and 20MRECVR = 1.
		addWriteRegToBuffer(RFCON6, 0x90);
		
		// 8. RFCON7 (0x207) = 0x80 – Initialize SLPCLKSEL = 0x2 (100 kHz Internal oscillator).
		addWriteRegToBuffer(RFCON7, 0x80);
		
		// 9. RFCON8 (0x208) = 0x10 – Initialize RFVCO = 1.
		addWriteRegToBuffer(RFCON8, 0x10);
		
		// 10. SLPCON1 (0x220) = 0x21 – Initialize CLKOUTEN = 1 and SLPCLKDIV = 0x01.
		addWriteRegToBuffer(SLPCON1, 0x21);

		// Configuration for nonbeacon-enabled devices (see Section 3.8 “Beacon-Enabled and Nonbeacon-Enabled Networks”):
		// 11. BBREG2 (0x3A) = 0x80 – Set CCA mode to ED.
		addWriteRegToBuffer(BBREG2, 0x80);

		// 12. CCAEDTH = 0x60 – Set CCA ED threshold.
		addWriteRegToBuffer(CCAEDTH, 0x60);

		// 13. BBREG6 (0x3E) = 0x40 – Set appended RSSI value to RXFIFO.
		addWriteRegToBuffer(BBREG6, 0x40);

		// 14. Enable interrupts – See Section 3.3 “Interrupts”.
		addWriteRegToBuffer(INTCON, 0xF6); // RXIE and TXNIE interrupts enabled

		// 15. Set channel – See Section 3.4 “Channel Selection”.
		addWriteRegToBuffer(RFCON0, 0x00 | 0x03); // Channel 11 .. 0x00 (up to 26 .. 0xF0), note that 0x03 must still be present to keep RFOPT = 0x03 
		
		// 16. Set transmitter power - See “REGISTER 2-62: RF CONTROL 3 REGISTER (ADDRESS: 0x203)”.
		addWriteRegToBuffer(RFCON3, 0x40); // -10dB
		
		// 17. RFCTL (0x36) = 0x04 – Reset RF state machine.
		addWriteRegToBuffer(RFCTL, 0x04);

		// 18. RFCTL (0x36) = 0x00.
		addWriteRegToBuffer(RFCTL, 0x00);
		
		sendBuffer();

		// 19. Delay at least 192 μs.
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			throw new D2XXException(e);
		}
		
		// Configuring Nonbeacon-Enabled Device
		// 1.Clear the PANCOORD (RXMCR 0x00<3>) bit = 0 to configure as device.
		// + Set promiscuous Mode bit
		addWriteRegToBuffer(RXMCR, 0x01);
		
		// 2.Clear the SLOTTED (TXMCR 0x11<5>) bit = 0 to use Unslotted CSMA-CA mode.
		addWriteRegToBuffer(TXMCR, 0x1C);
		
		sendBuffer();
	}
	
	private boolean isINTSTAT(int mask) throws D2XXException {
		if ((lastINTSTAT & mask) == mask) {
			lastINTSTAT = (byte)(lastINTSTAT & (~mask));
			return true;
		} else {
			lastINTSTAT |= readReg(INTSTAT);

			if ((lastINTSTAT & mask) == mask) {
				lastINTSTAT = (byte)(lastINTSTAT & (~mask));
				return true;
			} else {
				return false;
			}
		}
	}
	
	public RXPacket recvPacket() throws D2XXException {
		if (isINTSTAT(0x08)) {
			RXPacket packet = new RXPacket();
			
			clearBuffer();
			addWriteRegToBuffer(BBREG1, 0x04); // Disable RX
			addReadRegToBuffer(RXFIFO); // Read length
			addSendImmediateToBuffer();
			sendBuffer();
			recvBuffer(1);
			
			int len = mpsseBuf.get(0);
			
			clearBuffer();
			for (int idx = 0; idx < len + 2; idx++) {
				addReadLongRegToBuffer(RXFIFO + 1 + idx);
			}
			addWriteRegToBuffer(BBREG1, 0x00); // Enable RX			
			addSendImmediateToBuffer();
			sendBuffer();
			recvBuffer(len + 2);
			
			mpsseBuf.flip();
			
			packet.data = ByteBuffer.allocateDirect(len - 2);
			for (int idx = 0; idx < len - 2; idx++) {
				packet.data.put(mpsseBuf.get());
			}
			
			packet.data.flip();

			packet.fcs = (mpsseBuf.get() & 0xFF) | ((mpsseBuf.get() & 0xFF) << 8);
			packet.lqi = mpsseBuf.get() & 0xFF;
			packet.rssi = mpsseBuf.get() & 0xFF;
			
			return packet;
		} else {
			return null;
		}
	}

	public void broadcastPacket(ByteBuffer data) throws D2XXException {
		
		int txReg = TXNFIFO;
		clearBuffer();
		
		addWriteLongRegToBuffer(txReg++, 7); // Header length
		addWriteLongRegToBuffer(txReg++, 7 + data.remaining()); // Frame length
		
		// Frame Control (order low-byte, high-byte; MSb)
		// =============================================
		// Source Addressing Mode [15-14] - 0x10 (Short address 16bit)
		// Frame Version [13-12] - 0x00 (802.15.4-2003)
		// Dest. Addressing Mode [11-10] - 0x00 (PAN identifier and address fields are not present)
		// Reserved [9-7]
		// PAN ID Compression [6] - 0x00 (No PAN ID Compression)
		// Acknowledgement Request [5] - 0x00 (No acknowledgement requested)
		// Frame Pending [4] - 0x00 (No Frame Pending)
		// Security Enabled [3] - 0x00 (No security)
		// Frame Type [2-0] - 0x01 (Data Frame)
		addWriteLongRegToBuffer(txReg++, 0x01);
		addWriteLongRegToBuffer(txReg++, 0x80);
		
		addWriteLongRegToBuffer(txReg++, 0x00); // Sequence Number

		addWriteLongRegToBuffer(txReg++, panID & 0xFF); // Source PAN ID (low)
		addWriteLongRegToBuffer(txReg++, panID >> 8); // Source PAN ID (high)
		
		addWriteLongRegToBuffer(txReg++, shortAddr & 0xFF); // Source Address (low)
		addWriteLongRegToBuffer(txReg++, shortAddr >> 8); // Source Address (high)
		
		while (data.remaining() > 0) {
			addWriteLongRegToBuffer(txReg++, data.get());
		}

		addWriteRegToBuffer(TXNCON, 0x01); // Trigger transmission, set: Frame Pending Status Bit = 0, Activate Indirect Transmission bit = 0, TX Normal FIFO Acknowledgement Request = 0, TX Normal FIFO Security Enabled = 0
		
		sendBuffer();
		
		txState = TXState.PENDING;
	} 
	
	public TXState getTXState() throws D2XXException {
		if (txState == TXState.PENDING) {
			if (isINTSTAT(0x01)) {
				int txStat = readReg(TXSTAT);
				
				if ((txStat & 0x01) == 0x01) {
					txState = TXState.FAILED;
				} else {
					txState = TXState.SUCCESSFUL;
				}
			} 
		}
	
		return txState;
	}

	public void cleanup() {
		d2xx.close();
	}
	
	public void setPANID(int newPANID) throws D2XXException {
		panID = newPANID;
		
		clearBuffer();
		addWriteRegToBuffer(PANIDL, newPANID & 0xFF);
		addWriteRegToBuffer(PANIDH, (newPANID >> 8) & 0xFF);
		sendBuffer();
	}
	
	public int readPANID() throws D2XXException {
		clearBuffer();
		addReadRegToBuffer(PANIDL);
		addReadRegToBuffer(PANIDH);
		addSendImmediateToBuffer();
		sendBuffer();
		
		recvBuffer(2);
		
		panID = ((mpsseBuf.get(1) & 0xFF) << 8) | (mpsseBuf.get(0) & 0xFF);
		
		return panID;
	}
	
	public int getPANID() {
		return panID;
	}
	
	public void setShortAddr(int newShortAddr) throws D2XXException {
		shortAddr = newShortAddr;
		
		clearBuffer();
		addWriteRegToBuffer(SADRL, newShortAddr & 0xFF);
		addWriteRegToBuffer(SADRH, (newShortAddr >> 8) & 0xFF);
		sendBuffer();
	}
	
	public int readShortAddr() throws D2XXException {
		clearBuffer();
		addReadRegToBuffer(SADRL);
		addReadRegToBuffer(SADRH);
		addSendImmediateToBuffer();
		sendBuffer();
		
		recvBuffer(2);
		
		shortAddr = ((mpsseBuf.get(1) & 0xFF) << 8) | (mpsseBuf.get(0) & 0xFF);
		return shortAddr;
	}	
	
	public int getShortAddr() {
		return shortAddr;
	}

	public void setChannel(int channelNo) throws D2XXException {
		channel = channelNo;
		
		// Set channel – See Section 3.4 “Channel Selection”.
		addWriteRegToBuffer(RFCON0, (channelNo << 4) | 0x03);  
		
		// RFCTL (0x36) = 0x04 – Reset RF state machine.
		addWriteRegToBuffer(RFCTL, 0x04);

		// RFCTL (0x36) = 0x00.
		addWriteRegToBuffer(RFCTL, 0x00);
		
		sendBuffer();

		// Delay at least 192 μs.
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new D2XXException(e);
		}
	}
	
	public int readChannel() throws D2XXException {
		channel = readReg(RFCON0) >> 4;
		
		return channel;
	}
	
	public int getChannel() {
		return channel;
	}

	void testINTSTAT() throws D2XXException {
		int cnt;
		int cycles = 100000;
		
		long cycleDurations[] = new long[cycles];
		
		long startTS = System.nanoTime();
		
		for (cnt = 0; cnt < cycles; cnt++) {
			long cycleStartTS = System.nanoTime();
			
			readReg(INTSTAT);
			
			long cycleEndTS = System.nanoTime();
			cycleDurations[cnt] = cycleEndTS - cycleStartTS;
		}
		
		long endTS = System.nanoTime();
		
		long duration = endTS - startTS;
		Arrays.sort(cycleDurations);
		
		System.out.format("INTSTAT stress test results\n");
		System.out.format("  read count: %d\n", cnt);
		System.out.format("  total duration (in ns): %d\n", duration);
		System.out.format("  rate (reads/s): %f\n", (double)1000000000 * cycles / duration);
		System.out.format("  avg duration (in ns): %d\n", duration / cycles);
		System.out.format("  duration median (in ns): %d\n", cycleDurations[cycles / 2]);
		System.out.format("  duration 0.1%% quantile (in ns): %d\n", cycleDurations[cycles / 1000]);		
		System.out.format("  duration 1%% quantile (in ns): %d\n", cycleDurations[cycles / 100]);		
		System.out.format("  duration 5%% quantile (in ns): %d\n", cycleDurations[cycles / 20]);		
		System.out.format("  duration 95%% quantile (in ns): %d\n", cycleDurations[cycles * 19 / 20]);		
		System.out.format("  duration 99%% quantile (in ns): %d\n", cycleDurations[cycles * 99 / 100]);		
		System.out.format("  duration 99.9%% quantile (in ns): %d\n", cycleDurations[cycles * 999 / 1000]);		
		System.out.format("  shortest cycle duration (in ns): %d\n", cycleDurations[0]);
		System.out.format("  longest cycle duration (in ns): %d\n", cycleDurations[cycles-1]);
	}
}
