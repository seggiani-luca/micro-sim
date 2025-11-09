#include "lib/lib.h"

#define BUF_SIZE 50

void main() {
	vid::print_strln("-- Client 0 --");
	vid::print_str("Indirizzo: ");
	vid::print_uint(net::network.addr);
	vid::newline();

	vid::print_str("Inserire indirizzo server: ");
	uint32_t dest_addr = kyb::read_uint();

	// init buffers
	char in_buf[BUF_SIZE];
	char out_buf[BUF_SIZE];
	
	while(true) {
		// get message
		vid::print_str("Inserisci messaggio: ");
		kyb::read_str(out_buf, BUF_SIZE);
	
		int dim = str::len(out_buf) + 1;

		// send message
		net::send((void*) out_buf, dim, dest_addr);
		vid::print_str("Inviato messaggio al server: \"");
		vid::print_str(out_buf);
		vid::print_str("\", dimensione: ");
		vid::print_int(dim);
		vid::newline();

		// receive echo
		net::recv((void*) in_buf, BUF_SIZE);
		vid::print_str("Ricevuto echo dal server: \"");
		vid::print_str(in_buf);
		vid::print_strln("\"");
	}
}
