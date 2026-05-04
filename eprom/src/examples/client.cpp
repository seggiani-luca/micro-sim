#include "lib/lib.h"

#define SERVER_ADDR 1

void main() {
	// print info
	vid::print_strln("-- Client --");
	vid::print_str("Parlo con il server all'indirizzo: ");
	vid::print_uint(SERVER_ADDR);
	vid::newline();

	// init buffers
	char in_buf[net::max_payload_size];
	char out_buf[net::max_payload_size];
	
	while(true) {
		// get message
		vid::print_str("Inserisci indirizzo sorgente: ");
		int client_addr = kyb::read_uint();
		net::network.addr = client_addr;
		
		vid::print_str("Inserisci messaggio: ");
		kyb::read_str(out_buf, sizeof(out_buf));
	
		int dim = str::len(out_buf) + 1; // including terminator

		// send message
		net::sendto((void*) out_buf, dim, SERVER_ADDR);
		vid::print_str("Inviato messaggio al server: \"");
		vid::print_str(out_buf);
		vid::print_str("\" (");
		vid::print_int(dim);
		vid::print_str(" byte)");
		vid::newline();

		// receive echo
		int server_addr; // unused
		net::recvfrom((void*) in_buf, sizeof(in_buf), server_addr);
		vid::print_str("Ricevuto echo dal server: \"");
		vid::print_str(in_buf);
		vid::print_strln("\"");
	}
}
