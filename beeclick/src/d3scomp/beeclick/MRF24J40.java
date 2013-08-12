package d3scomp.beeclick;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MRF24J40 {
	public static class Packet {
		int[] data;
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
	
	private ByteBuffer buffer = ByteBuffer.allocate(65536);
	private D2XX d2xx;
	
	public MRF24J40() throws D2XXException {
		d2xx = new D2XX();
		reset();
	}
	
	private void writeReg(int reg, int val) throws D2XXException {
		int status;
		
		buffer.rewind();
		
		int sizeToTransfer;
		
		// Set CS cmd
		// Clock data cmd
		// LengthL
		// LengthH
		
		if (reg < 0x40) {
			buffer.put((byte)((reg << 1) | 0x01));
			buffer.put((byte)val);
		} else {
			assert(reg < 0x400);
			
			buffer.put((byte)((reg >> 3) | 0x80));
			buffer.put((byte)(((reg & 0x7) << 5) | 0x10));
			buffer.put((byte)val);			
		}

		// Release CS cmd
		
		buffer.flip();
		
		d2xx.write(buffer);

		while (buffer.position() != buffer.limit()) {
			System.println("writeReg [cont]");
			d2xx.write(buffer);
		}
	}
	
	private int readReg(int reg) throws D2XXException {
/*
		int status;
		
		buffer.rewind();
		
		int sizeToTransfer;
		
		if (reg < 0x40) {
			buffer.put((byte)((reg << 1) | 0x01));
			sizeToTransfer = 1;
		} else {
			assert(reg < 0x400);
			
			buffer.put((byte)((reg >> 3) | 0x80));
			buffer.put((byte)(((reg & 0x7) << 5) | 0x10));
			sizeToTransfer = 2;
		}
		
		status = SPI.SPI_Write(ftHandle, buffer, sizeToTransfer, sizeTransferedByRef, SPI.SPI_TRANSFER_OPTIONS_SIZE_IN_BYTES | SPI.SPI_TRANSFER_OPTIONS_CHIPSELECT_ENABLE);
		if (status != SPI.FT_OK) {
			throw new MRF24J40Exception("Call returned with status " + status + ".");
		}

		if (sizeTransferedByRef.getValue() != sizeToTransfer) {
			throw new MRF24J40Exception("Only " + sizeTransferedByRef.getValue() + " bytes out of " + sizeToTransfer + " send.");
		}
		
		status = SPI.SPI_Read(ftHandle, buffer, 1, sizeTransferedByRef, SPI.SPI_TRANSFER_OPTIONS_SIZE_IN_BYTES | SPI.SPI_TRANSFER_OPTIONS_CHIPSELECT_DISABLE);
		if (status != SPI.FT_OK) {
			throw new MRF24J40Exception("Call returned with status " + status + ".");
		}		

		if (sizeTransferedByRef.getValue() != 1) {
			throw new MRF24J40Exception("Received " + sizeTransferedByRef.getValue() + " bytes instead of 1.");
		}
		
		return buffer.get(0);
*/
		return 0;
	}
	
	public void reset() throws D2XXException {
		/* Reset the MRF24J40 chip 
		status = SPI.FT_WriteGPIO(ftHandle, (byte)0x02, (byte)0); // BCBUS1 - RESET#
		if (status != SPI.FT_OK) {
			throw new MRF24J40Exception("Call returned with status " + status + ".");
		}

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			throw new MRF24J40Exception(e);
		}
		
		status = SPI.FT_WriteGPIO(ftHandle, (byte)0x02, (byte)1); // BCBUS1 - RESET#
		if (status != SPI.FT_OK) {
			throw new MRF24J40Exception("Call returned with status " + status + ".");
		}

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			throw new MRF24J40Exception(e);
		}
		*/

		// 1. SOFTRST (0x2A) = 0x07 – Perform a software Reset. The bits will be automatically cleared to ‘0’ by hardware.		
		writeReg(SOFTRST, 0x07);

		// 2. PACON2 (0x18) = 0x98 – Initialize FIFOEN = 1 and TXONTS = 0x6.
		writeReg(PACON2, 0x98);

		// 3. TXSTBL (0x2E) = 0x95 – Initialize RFSTBL = 0x9.
		writeReg(TXSTBL, 0x95);
		
		// 4. RFCON0 (0x200) = 0x03 – Initialize RFOPT = 0x03.
		writeReg(RFCON0, 0x03);
		
		// 5. RFCON1 (0x201) = 0x01 – Initialize VCOOPT = 0x02.
		writeReg(RFCON1, 0x01);
		
		// 6. RFCON2 (0x202) = 0x80 – Enable PLL (PLLEN = 1).
		writeReg(RFCON2, 0x80);
		
		// 7. RFCON6 (0x206) = 0x90 – Initialize TXFIL = 1 and 20MRECVR = 1.
		writeReg(RFCON6, 0x90);
		
		// 8. RFCON7 (0x207) = 0x80 – Initialize SLPCLKSEL = 0x2 (100 kHz Internal oscillator).
		writeReg(RFCON7, 0x80);
		
		// 9. RFCON8 (0x208) = 0x10 – Initialize RFVCO = 1.
		writeReg(RFCON8, 0x10);
		
		// 10. SLPCON1 (0x220) = 0x21 – Initialize CLKOUTEN = 1 and SLPCLKDIV = 0x01.
		writeReg(SLPCON1, 0x21);

		// Configuration for nonbeacon-enabled devices (see Section 3.8 “Beacon-Enabled and Nonbeacon-Enabled Networks”):
		// 11. BBREG2 (0x3A) = 0x80 – Set CCA mode to ED.
		writeReg(BBREG2, 0x80);

		// 12. CCAEDTH = 0x60 – Set CCA ED threshold.
		writeReg(CCAEDTH, 0x60);

		// 13. BBREG6 (0x3E) = 0x40 – Set appended RSSI value to RXFIFO.
		writeReg(BBREG6, 0x40);

		// 14. Enable interrupts – See Section 3.3 “Interrupts”.
		writeReg(INTCON, 0xF6); // RXIE and TXNIE interrupts enabled

		// 15. Set channel – See Section 3.4 “Channel Selection”.
		writeReg(RFCON0, 0x00 | 0x03); // Channel 11 .. 0x00 (up to 26 .. 0xF0), note that 0x03 must still be present to keep RFOPT = 0x03 
		
		// 16. Set transmitter power - See “REGISTER 2-62: RF CONTROL 3 REGISTER (ADDRESS: 0x203)”.
		writeReg(RFCON3, 0x40); // -10dB
		
		// 17. RFCTL (0x36) = 0x04 – Reset RF state machine.
		writeReg(RFCTL, 0x04);

		// 18. RFCTL (0x36) = 0x00.
		writeReg(RFCTL, 0x00);

		// 19. Delay at least 192 μs.
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			throw new D2XXException(e);
		}		

		// Configuring Nonbeacon-Enabled Device
		// 1.Clear the PANCOORD (RXMCR 0x00<3>) bit = 0 to configure as device.
		// + Set promiscuous Mode bit
		writeReg(RXMCR, 0x01);
		
		// 2.Clear the SLOTTED (TXMCR 0x11<5>) bit = 0 to use Unslotted CSMA-CA mode.
		writeReg(TXMCR, 0x1C);
	}
	
	public Packet recvPacket() throws D2XXException {
		int intstat = readReg(INTSTAT);
		
		if ((intstat & 0x08) == 0x08) {
			Packet packet = new Packet();
			
			writeReg(BBREG1, 0x04); // Disable RX
			
			int len = readReg(RXFIFO);
			packet.data = new int[len - 2];
			
			for (int idx = 0; idx < len - 2; idx++) {
				packet.data[idx] = readReg(RXFIFO + 1 + idx);
			}
			
			packet.lqi = readReg(RXFIFO + len + 2);
			packet.rssi = readReg(RXFIFO + len + 2);
			
			writeReg(BBREG1, 0x00); // Enable RX
			
			return packet;
		} else {
			return null;
		}
	}
	
	public void cleanup() {
		d2xx.close();
	}
}
