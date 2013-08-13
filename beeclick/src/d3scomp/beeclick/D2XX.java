/*
 * D2XX.java
 *
 *  Created on: 12.8.2013
 *      Author: Tomas Bures
 */

package d3scomp.beeclick;

import java.nio.ByteBuffer;


public class D2XX {

	static {
		System.loadLibrary("beeclick-ftdi");
	}
	
	static public native void _printSummary();
	static private native long _open();
	static private native int _close(long handle);
	static private native int _write(long handle, ByteBuffer buffer, int numBytesToWrite);
	static private native int _read(long handle, ByteBuffer buffer, int numBytesToRead);
	static private native int _getNumOfBytesAvailableToRead(long handle);
	
	private long ftHandle;

	public D2XX() throws D2XXException {
		ftHandle = _open();
		if (ftHandle == 0) {
			throw new D2XXException("Cannot open a MPSSE port.");
		}
	}
	
	public void close() {
		assert(ftHandle != 0);
		
		_close(ftHandle);
		ftHandle = 0;
	}
	
	public void writeAll(ByteBuffer buffer) throws D2XXException {
//		System.out.format("W[%d] ", buffer.limit() - buffer.position());
		
		while (write(buffer) > 0) {
//			System.out.print(".");
		}
		
//		System.out.println();
	}
	
	public void readAll(ByteBuffer buffer) throws D2XXException {
//		System.out.format("R[%d] ", buffer.limit() - buffer.position());
		
		while (read(buffer) > 0) {
//			System.out.print(".");
		}
		
//		System.out.println();
	}
	
	public int write(ByteBuffer buffer) throws D2XXException {
		assert(buffer.isDirect());
		
		if (buffer.remaining() == 0)
			return 0;
		
		ByteBuffer slicedBuf = buffer.slice();

		int bytesWritten = _write(ftHandle, slicedBuf, slicedBuf.limit());
		
		if (bytesWritten < 0) {
			throw new D2XXException("Error when writing to MPSSE port.");
		}
		
		buffer.position(buffer.position() + bytesWritten);
		
		return bytesWritten;
	}
	
	public int read(ByteBuffer buffer) throws D2XXException {
		assert(buffer.isDirect());
		
		if (buffer.remaining() == 0)
			return 0;
		
		ByteBuffer slicedBuf = buffer.slice();

		int bytesRead = _read(ftHandle, buffer, slicedBuf.limit());
		
		buffer.position(buffer.position() + bytesRead);
		
		if (bytesRead < 0) {
			throw new D2XXException("Error when writing to MPSSE port.");
		}
		
		return bytesRead;
	}

}
