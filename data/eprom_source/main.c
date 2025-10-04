#include <stdint.h>

volatile uint8_t video[5120] __attribute__((section(".video"))); 
int cur = 0;

const char mess[] = "Ciao RISC-V!\n";

void print(const char* str) {
	char c;
	while(c = *str++) {
		video[cur] = c;
		cur += 2;
	}
}

int main() {
	print(mess);

	return 0;
}
