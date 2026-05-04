#include "lib/lib.h"

#define SERVER_ADDR 1

void main() {
	// set address
	net::network.addr = SERVER_ADDR;

	// print info 
	vid::print_strln("-- Server --");
	vid::print_str("Indirizzo: ");
	vid::print_uint(net::network.addr);
	vid::newline();

	// init buffer
	char buf[net::max_payload_size];

	while(true) {
		// receive packet
		int client_addr;
		int size = net::recvfrom(buf, sizeof(buf), client_addr);

		vid::print_str("Ricevuto messaggio: \"");
		vid::print_str(buf);
		vid::print_str("\" (");
		vid::print_uint(size);
		vid::print_str(" byte) da client: ");
		vid::print_uint(client_addr);
		vid::print_strln(", faccio echo...");

		// send packet back
		net::sendto((void*) buf, size, client_addr);
	}
}
