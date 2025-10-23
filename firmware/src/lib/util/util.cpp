#include "util.h"
#include "../video/video.h"
#include "../keyboard/keyboard.h"

namespace utl {
	void wait() {
		vid::print_str("Premi un tasto qualsiasi...");
		kyb::get_char();
		vid::newline();

		return;
	}

	void panic(const char* msg) {
		vid::newline();
		vid::print_strln("Panic!");
		vid::print_strln(msg);
		
		wait();
		halt();
	}
} // utl::
