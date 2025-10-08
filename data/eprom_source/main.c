#include "lib.h"

int cur = 0;

char mess[] = "Ciao RISC-V!\n";

void print(const char* str) {
	char c;
	while(c = *str++) {
		video[cur * 2] = c;
		cur += 1;
	}
}

int main() {
	mess[3] = 'u';
	print(mess);

	debugger();

	return 0;
}
