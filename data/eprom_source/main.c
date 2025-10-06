#include <stdint.h>

volatile uint8_t video[5120] __attribute__((section(".video"))); 
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

	return 0;
}
