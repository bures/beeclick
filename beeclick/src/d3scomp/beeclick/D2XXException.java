/*
 * D2XXException.java
 *
 *  Created on: 12.8.2013
 *      Author: Tomas Bures
 */

package d3scomp.beeclick;

public class D2XXException extends Exception {

	public D2XXException() {
	}

	public D2XXException(String message) {
		super(message);
	}

	public D2XXException(Throwable cause) {
		super(cause);
	}

	public D2XXException(String message, Throwable cause) {
		super(message, cause);
	}

	public D2XXException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
