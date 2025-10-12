#ifndef LIB_UTILS
#define LIB_UTILS

namespace vid {
	void print_str(const char*);
	void print_strln(const char*);
	void newline();
}
namespace keyb {
	char get_char();
}

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

	/*
	 * Waits for any character.
	 */
	void wait() {
		vid::print_str("Premi un tasto qualsiasi...");
		keyb::get_char();
		vid::newline();

		return;
	}

	/*
	 * Panics printing a message and quits.
	 */
	void panic(const char* msg) {
		vid::newline();
		vid::print_strln("Panic!");
		vid::print_strln(msg);
		
		wait();
		halt();
	}
}

#endif
