package d3scomp.beeclick;

import java.nio.ByteBuffer;


public class D2XX {

	static {
		System.loadLibrary("beeclick-ftdi");
	}
	
	static private native int _getLibraryVersion();
	static private native int _init();
	static private native long _open();
	static private native int _close(long handle);
	static private native int _write(long handle, ByteBuffer buffer, int numBytesToWrite);
	static private native int _read(long handle, ByteBuffer buffer, int numBytesToRead);
	static private native int _readAvailable(long handle);
	
	static private boolean initialized = false;
	
	private long ftHandle;

	public D2XX() throws D2XXException {
		if (initialized == false) {
			int result = _init();
			if (result != 0) {
				throw new D2XXException("Cannot initialize the D2XX stack.");
			}
		}
		
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
		
		if (buffer.limit() == buffer.position())
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
		
		if (buffer.limit() == buffer.position())
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
