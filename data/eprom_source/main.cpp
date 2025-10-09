#include "lib/lib.h"

char* mess = 
"Come avevamo visto, non programmeremo il nostro processore attraverso il\n" 
"linguaggio macchina, ma con un linguaggio assembler che codifica le istruzioni\n"
"macchina, nella forma gia' vista:\n"
"\tOPCODE source, destination\n"
"Questo linguaggio sara' simile a quello gia' studiato, cioe' dei processori\n"
"Intel x86. La differenza sara' che avremo come problema il dover effettivamente\n"
"codificare cio' che scriviamo in assembler in istruzioni in linguaggio macchina\n"
"da fornire al processore (adesso non stiamo solo programmando, ma anche\n"
"progettando il processore).";

extern "C" void main() {
	utl::debugger();

	vid::print_str(mess);
	
	utl::spin();

	return;
}
