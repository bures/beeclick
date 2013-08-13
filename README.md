beeclick
========

A Java-based library for 802.15.4 communication using BEE click (http://www.mikroe.com/click/bee/) fitted into click USB adapter (http://www.mikroe.com/click/usb-adapter/). Generally, this is MRF24J40 (802.15.4 adapter by Microchip) connected to PC via USB using FT2232H (a USB-SPI bridge by FTDI).

The library uses JNI to interface D2XX drivers of the FT2232H (http://www.ftdichip.com/Drivers/D2XX.htm). On top of this the library employs the MPSSE protocol for SPI-based communication with the MRF24J40.

Currently the library supports only single-hop broadcast and Windows i386 (MinGW) target.
