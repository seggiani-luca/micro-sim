#ifndef LIB_H
#define LIB_H

#include <stdint.h>

/*
 * Video array, points to the 5 KiB of video memory. Video characters are 2 bytes: first byte is 
 * character codepoint, second byte is style.
 */
volatile uint8_t video[5120] __attribute__((section(".video"))); 

/*
 * Spins indefinitely.
 */
extern void spin();

/*
 * Halts processor.
 */
extern void halt();

/*
 * Signals to launch debug shell. 
 */
extern void debugger();

#endif
