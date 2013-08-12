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

	DWORD dwNumBytesToSend = 0;		// Index to the output buffer
	DWORD dwNumBytesSent = 0;		// Count of actual bytes sent - used with FT_Write
	DWORD dwNumBytesToRead = 0;		// Number of bytes available to read in the driver's input buffer
	DWORD dwNumBytesRead = 0;		// Count of actual bytes read - used with FT_Read

	// -----------------------------------------------------------
	// Open the port - For this application note, we'll assume the first device is a
	// FT2232H or FT4232H. Further checks can be made against the device
	// descriptions, locations, serial numbers, etc. before opening the port.
	// -----------------------------------------------------------

	ftStatus = FT_Open(1, &ftHandle);
	if (ftStatus != FT_OK) {
		printf("Open failed with error %d\n", ftStatus);
		return 0; // Exit with error
	}

	// Configure port parameters
	ftStatus |= FT_ResetDevice(ftHandle);		//Reset USB device

	// Purge USB receive buffer first by reading out all old data from FT2232H receive buffer
	ftStatus |= FT_GetQueueStatus(ftHandle, &dwNumBytesToRead); 	// Get the number of bytes in the FT2232H receive buffer

	if ((ftStatus == FT_OK) && (dwNumBytesToRead > 0)) {
		FT_Read(ftHandle, &byInputBuffer, dwNumBytesToRead, &dwNumBytesRead);	// Read out the data from FT2232H receive buffer

		if (ftStatus != FT_OK) {
			printf("Error [1] in initializing the MPSSE %d\n", ftStatus);
			FT_Close(ftHandle);
			return 0; // Exit with error
		}
	}

	ftStatus |= FT_SetUSBParameters(ftHandle, 65536, 65535);	// Set USB request transfer sizes to 64K
	ftStatus |= FT_SetChars(ftHandle, 0, 0, 0, 0);				// Disable event and error characters

	ftStatus |= FT_SetTimeouts(ftHandle, 5000, 5000);	// Sets the read and write timeouts in milliseconds
	ftStatus |= FT_SetLatencyTimer(ftHandle, 1);	// Set the latency timer to 1mS (default is 16mS)
	ftStatus |= FT_SetFlowControl(ftHandle, FT_FLOW_RTS_CTS, 0x00, 0x00);	// Turn on flow control to synchronize IN requests


	ftStatus |= FT_SetBitMode(ftHandle, 0x0, 0x00); 	// Reset controller
	ftStatus |= FT_SetBitMode(ftHandle, 0x0, 0x02);		// Enable MPSSE mode

	if (ftStatus != FT_OK) {
		printf("Error [2] in initializing the MPSSE %d\n", ftStatus);
		FT_Close(ftHandle);
		return 0; // Exit with error
	}

	Sleep(50); // Wait for all the USB stuff to complete and work

	// Enable internal loop-back
	byOutputBuffer[dwNumBytesToSend++] = 0x84;		// Enable loopback

	ftStatus = FT_Write(ftHandle, byOutputBuffer, dwNumBytesToSend,	&dwNumBytesSent);	// Send off the loopback command
	dwNumBytesToSend = 0; // Reset output buffer pointer
	if (ftStatus != FT_OK) {
		printf("Error [3] in initializing the MPSSE %d\n", ftStatus);
		FT_Close(ftHandle);
		return 0; // Exit with error
	}

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
	if (ftStatus != FT_OK) {
		printf("Error [4] in initializing the MPSSE %d\n", ftStatus);
		FT_Close(ftHandle);
		return 0; // Exit with error
	}

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
	if (ftStatus != FT_OK) {
		printf("Error [5] in initializing the MPSSE %d\n", ftStatus);
		FT_Close(ftHandle);
		return 0; // Exit with error
	}


	// Check the receive buffer - it should be empty
	ftStatus = FT_GetQueueStatus(ftHandle, &dwNumBytesToRead); // Get the number of bytes in the FT2232H receive buffer
	if (dwNumBytesToRead != 0) {
		printf("Error - MPSSE receive buffer should be empty\n");

		FT_SetBitMode(ftHandle, 0x0, 0x00);	// Reset the port to disable MPSSE
		FT_Close(ftHandle); // Close the USB port
		return 0; // Exit with error
	}

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
	BYTE *buf = (*env)->GetDirectBufferAddress(env, bufObj);
	DWORD dwBytesWritten;

	if (buf == 0) {
		printf("Error: The buffer must be allocated with ByteBuffer.allocateDirect.");
		return -1;
	}

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
	BYTE *buf = (*env)->GetDirectBufferAddress(env, bufObj);
	DWORD dwBytesRead;

	if (buf == 0) {
		printf("Error: The buffer must be allocated with ByteBuffer.allocateDirect.");
		return -1;
	}

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
