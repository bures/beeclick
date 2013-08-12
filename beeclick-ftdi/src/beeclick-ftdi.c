/*
 * beeclick-ftdi.c
 *
 *  Created on: 12.8.2013
 *      Author: Tomas Bures
 */

#include <windows.h>
#include <stdio.h>
#include <ftd2xx.h>

#include "d3scomp_beeclick_D2XX.h"

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1getLibraryVersion(JNIEnv *env, jclass cls) {
	DWORD libVer;

	FT_GetLibraryVersion(&libVer);

	printf("Library version: %d\n", libVer);

	return (jint)libVer;
}

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1init(JNIEnv *env, jclass cls) {

	FT_STATUS ftStatus;				// Result of each D2XX call
	DWORD dwNumDevs;				// The number of devices
	DWORD dwCount = 0;				// General loop index

	FT_DEVICE_LIST_INFO_NODE *devInfo;

	// -----------------------------------------------------------
	// Does an FTDI device exist?
	// -----------------------------------------------------------

	printf("Checking for FTDI devices...\n");

	ftStatus = FT_CreateDeviceInfoList(&dwNumDevs);

	// Get the number of FTDI devices
	if (ftStatus != FT_OK){					// Did the command execute OK?
		printf("Error in getting the number of devices\n");
		return -1; // Exit with error
	}

	if (dwNumDevs < 1) {					// Exit if we don't see any
		printf("There are no FTDI devices installed\n");
		return -1; // Exit with error
	}

	printf("%d FTDI devices found - the count includes individual ports on a single chip\n", dwNumDevs);

	devInfo = (FT_DEVICE_LIST_INFO_NODE*) malloc(	sizeof(FT_DEVICE_LIST_INFO_NODE) * dwNumDevs);
	ftStatus = FT_GetDeviceInfoList(devInfo, &dwNumDevs);
	if (ftStatus == FT_OK) {
		for (dwCount = 0; dwCount < dwNumDevs; dwCount++) {
			printf("Dev %d:\n", dwCount);
			printf(" Flags=0x%x\n", devInfo[dwCount].Flags);
			printf(" Type=0x%x\n", devInfo[dwCount].Type);
			printf(" ID=0x%x\n", devInfo[dwCount].ID);
			printf(" LocId=0x%x\n", devInfo[dwCount].LocId);
			printf(" SerialNumber=%s\n", devInfo[dwCount].SerialNumber);
			printf(" Description=%s\n", devInfo[dwCount].Description);
			printf(" ftHandle=0x%x\n", devInfo[dwCount].ftHandle);
		}
	}
	free(devInfo);

	return 0;
}

JNIEXPORT jlong JNICALL Java_d3scomp_beeclick_D2XX__1open(JNIEnv *env, jclass cls) {
	// -----------------------------------------------------------
	// Variables
	// -----------------------------------------------------------
	FT_HANDLE ftHandle;				// Handle of the FTDI device
	FT_STATUS ftStatus;				// Result of each D2XX call

	BYTE byOutputBuffer[8];			// Buffer to hold MPSSE commands and data to be sent to the FT2232H
	BYTE byInputBuffer[8];			// Buffer to hold data read from the FT2232H

	DWORD dwCount = 0;				// General loop index
	DWORD dwNumBytesToSend = 0;		// Index to the output buffer
	DWORD dwNumBytesSent = 0;		// Count of actual bytes sent - used with FT_Write
	DWORD dwNumBytesToRead = 0;		// Number of bytes available to read in the driver's input buffer
	DWORD dwNumBytesRead = 0;		// Count of actual bytes read - used with FT_Read

	DWORD dwClockDivisor = 1; // Value of clock divisor, SCL Frequency = 60/((1+1)*2)(MHz) = 15Mhz


	// -----------------------------------------------------------
	// Open the port - For this application note, we'll assume the first device is a
	// FT2232H or FT4232H. Further checks can be made against the device
	// descriptions, locations, serial numbers, etc. before opening the port.
	// -----------------------------------------------------------

	ftStatus = FT_Open(0, &ftHandle);
	if (ftStatus != FT_OK) {
		printf("Open Failed with error %d\n", ftStatus);
		return 0; // Exit with error
	}

	// Configure port parameters
	ftStatus |= FT_ResetDevice(ftHandle);		//Reset USB device

	// Purge USB receive buffer first by reading out all old data from FT2232H receive buffer
	ftStatus |= FT_GetQueueStatus(ftHandle, &dwNumBytesToRead); 	// Get the number of bytes in the FT2232H receive buffer

	if ((ftStatus == FT_OK) && (dwNumBytesToRead > 0))
		FT_Read(ftHandle, &byInputBuffer, dwNumBytesToRead, &dwNumBytesRead);	// Read out the data from FT2232H receive buffer

	ftStatus |= FT_SetUSBParameters(ftHandle, 65536, 65535);	// Set USB request transfer sizes to 64K
	ftStatus |= FT_SetChars(ftHandle, 0, 0, 0, 0);				// Disable event and error characters

	ftStatus |= FT_SetTimeouts(ftHandle, 5000, 5000);	// Sets the read and write timeouts in milliseconds
	ftStatus |= FT_SetLatencyTimer(ftHandle, 1);	// Set the latency timer to 1mS (default is 16mS)
	ftStatus |= FT_SetFlowControl(ftHandle, FT_FLOW_RTS_CTS, 0x00, 0x00);	// Turn on flow control to synchronize IN requests


	ftStatus |= FT_SetBitMode(ftHandle, 0x0, 0x00); 	// Reset controller
	ftStatus |= FT_SetBitMode(ftHandle, 0x0, 0x02);		// Enable MPSSE mode

	if (ftStatus != FT_OK) {
		printf("Error in initializing the MPSSE %d\n", ftStatus);
		FT_Close(ftHandle);
		return 0; // Exit with error
	}

	Sleep(50); // Wait for all the USB stuff to complete and work

	// Enable internal loop-back
	byOutputBuffer[dwNumBytesToSend++] = 0x84;		// Enable loopback
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent);	// Send off the loopback command
	dwNumBytesToSend = 0; // Reset output buffer pointer

	// Check the receive buffer - it should be empty
	ftStatus = FT_GetQueueStatus(ftHandle, &dwNumBytesToRead);	// Get the number of bytes in the FT2232H receive buffer
	if (dwNumBytesToRead != 0) {
		printf("Error - MPSSE receive buffer should be empty\n");

		FT_SetBitMode(ftHandle, 0x0, 0x00);	// Reset the port to disable MPSSE
		FT_Close(ftHandle); 				// Close the USB port

		return 0; // Exit with error
	}


	// -----------------------------------------------------------
	// Synchronize the MPSSE by sending a bogus opcode (0xAB),
	// The MPSSE will respond with "Bad Command" (0xFA) followed by
	// the bogus opcode itself.
	// -----------------------------------------------------------

	byOutputBuffer[dwNumBytesToSend++] = 0xAB;	// Add bogus command ‘0xAB’ to the queue

	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent);	// Send off the BAD command
	dwNumBytesToSend = 0; // Reset output buffer pointer

	do {
		ftStatus = FT_GetQueueStatus(ftHandle, &dwNumBytesToRead);	// Get the number of bytes in the device input buffer
	} while ((dwNumBytesToRead == 0) && (ftStatus == FT_OK));		// or Timeout

	ftStatus = FT_Read(ftHandle, &byInputBuffer, dwNumBytesToRead, &dwNumBytesRead);	//Read out the data from input buffer
	if (!(dwNumBytesRead == 2 && byInputBuffer[0] == 0xFA && byInputBuffer[1] == 0xAB)) {
		printf("Error in synchronizing the MPSSE\n");
		FT_Close(ftHandle);
		return 0; // Exit with error
	}

	// Disable internal loop-back
	byOutputBuffer[dwNumBytesToSend++] = 0x85;	// Disable loopback
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent); // Send off the loopback command
	dwNumBytesToSend = 0; // Reset output buffer pointer

	// Check the receive buffer - it should be empty
	ftStatus = FT_GetQueueStatus(ftHandle, &dwNumBytesToRead); // Get the number of bytes in the FT2232H receive buffer
	if (dwNumBytesToRead != 0) {
		printf("Error - MPSSE receive buffer should be empty\n");

		FT_SetBitMode(ftHandle, 0x0, 0x00);	// Reset the port to disable MPSSE
		FT_Close(ftHandle); // Close the USB port
		return 0; // Exit with error
	}


	// -----------------------------------------------------------
	// Configure the MPSSE settings for JTAG
	// Multple commands can be sent to the MPSSE with one FT_Write
	// -----------------------------------------------------------

	dwNumBytesToSend = 0; // Start with a fresh index

	// Set up the Hi-Speed specific commands for the FTx232H
	byOutputBuffer[dwNumBytesToSend++] = 0x8A; // Use 60MHz master clock (disable divide by 5)
	byOutputBuffer[dwNumBytesToSend++] = 0x97; // Turn off adaptive clocking (may be needed for ARM)
	byOutputBuffer[dwNumBytesToSend++] = 0x8D; // Disable three-phase clocking
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent); // Send off the HS-specific commands
	dwNumBytesToSend = 0; // Reset output buffer pointer

	// Set TCK frequency
	// TCK = 60MHz /((1 + [(1 +0xValueH*256) OR 0xValueL])*2)
	byOutputBuffer[dwNumBytesToSend++] = 0x86;	// Command to set clock divisor
	byOutputBuffer[dwNumBytesToSend++] = dwClockDivisor & 0xFF;	// Set 0xValueL of clock divisor
	byOutputBuffer[dwNumBytesToSend++] = (dwClockDivisor >> 8) & 0xFF; // Set 0xValueH of clock divisor
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent); // Send off the clock divisor commands
	dwNumBytesToSend = 0; // Reset output buffer pointer


	// Set initial states of the MPSSE interface
	// 		- low byte, both pin directions and output values
	// 		Pin name 	Signal	Direction 	Config 	Initial State	Config
	// 		ADBUS0 		TCK/SK	output		1		high			1
	// 		ADBUS1		TDI/DO	output		1		low				0
	//		ADBUS2		TDO/DI	input		0						0
	//		ADBUS3		TMS/CS	output		1		high			1
	//		ADBUS4 		GPIOL0 	output 		1 		low 			0
	//		ADBUS5		GPIOL1 	output 		1 		low 			0
	//		ADBUS6		GPIOL2 	output 		1 		high 			1
	//		ADBUS7		GPIOL3 	output 		1 		high 			1
	byOutputBuffer[dwNumBytesToSend++] = 0x80; // Configure data bits low-byte of MPSSE port
	byOutputBuffer[dwNumBytesToSend++] = 0xC9; // Initial state config above
	byOutputBuffer[dwNumBytesToSend++] = 0xFB; // Direction config above
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent); // Send off the low GPIO config commands
	dwNumBytesToSend = 0; // Reset output buffer pointer

	// Note that since the data in subsequent sections will be clocked on the rising edge, the
	// inital clock state of high is selected. Clocks will be generated as high-low-high.
	// For example, in this case, data changes on the rising edge to give it enough time
	// to have it available at the device, which will accept data *into* the target device
	// on the falling edge.


	// Set initial states of the MPSSE interface
	// 		- high byte, both pin directions and output values
	// 		Pin name 	Signal 	Direction 	Config Initial State Config
	// 		ACBUS0 		GPIOH0 	input 		0 					0
	// 		ACBUS1 		GPIOH1 	input 		0 					0
	// 		ACBUS2 		GPIOH2 	input 		0 					0
	// 		ACBUS3 		GPIOH3 	input 		0 					0
	// 		ACBUS4 		GPIOH4 	input 		0 					0
	// 		ACBUS5 		GPIOH5 	input 		0 					0
	// 		ACBUS6 		GPIOH6 	input 		0 					0
	// 		ACBUS7 		GPIOH7 	input 		0 					0
	byOutputBuffer[dwNumBytesToSend++] = 0x82;
	byOutputBuffer[dwNumBytesToSend++] = 0x00;	// Initial state config above
	byOutputBuffer[dwNumBytesToSend++] = 0x00;	// Direction config above
	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent); // Send off the high GPIO config commands
	dwNumBytesToSend = 0; // Reset output buffer pointer

	for (dwCount = 0; dwCount < 8; dwCount++) { // Clear out the input and output buffers
		byInputBuffer[dwCount] = 0x00;
		byOutputBuffer[dwCount] = 0x00;
	}

	printf("MPSSE port configured for use.\n");

	return (jlong)ftHandle;
}

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1close(JNIEnv *env, jclass cls, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus;

	FT_Close(ftHandle);

	if (ftStatus != FT_OK) {
		printf("Error when closing the MPSSE %d\n", ftStatus);
		return -1; // Exit with error
	}

	printf("MPSSE port closed.\n");

	return 0;
}

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1write(JNIEnv *env, jclass cls, jlong handle, jobject bufObj, jint numBytesToWrite) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus;
	jbyte *buf = (*env)->GetDirectBufferAddress(env, bufObj);
	DWORD dwBytesWritten;

	ftStatus = FT_Write(ftHandle, buf, numBytesToWrite, &dwBytesWritten);

	if (ftStatus != FT_OK) {
		printf("Error when writing to MPSSE port %d\n", ftStatus);
		return -1; // Exit with error
	}

	return (jint)dwBytesWritten;
}

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1read(JNIEnv *env, jclass cls, jlong handle, jobject bufObj, jint numBytesToRead) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus;
	jbyte *buf = (*env)->GetDirectBufferAddress(env, bufObj);
	DWORD dwBytesRead;

	ftStatus = FT_Read(ftHandle, buf, numBytesToRead, &dwBytesRead);

	if (ftStatus != FT_OK) {
		printf("Error when reading from MPSSE port %d\n", ftStatus);
		return -1; // Exit with error
	}

	return (jint)dwBytesRead;
}

JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1readAvailable(JNIEnv *env, jclass cls, jlong handle) {
	FT_HANDLE ftHandle = (FT_HANDLE)handle;
	FT_STATUS ftStatus;
	DWORD dwBytesToRead;

	ftStatus = FT_GetQueueStatus(ftHandle, &dwBytesToRead);

	if (ftStatus != FT_OK) {
		printf("Error when getting status of MPSSE port %d\n", ftStatus);
		return -1; // Exit with error
	}

	return (jint)dwBytesToRead;
}
