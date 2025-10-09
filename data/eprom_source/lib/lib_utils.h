#ifndef LIB_UTILS
#define LIB_UTILS

namespace utl {	
	/*
	 * Spins indefinitely.
	 */
	extern "C" void spin();

	/*
	 * Halts processor.
	 */
	extern "C" void halt();

	/*
	 * Signals to launch debug shell. 
	 */
	extern "C" void debugger();
}

#endif
