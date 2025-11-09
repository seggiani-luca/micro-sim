#include "lib/lib.h"

#define BUF_SIZE 50

void main() {
	vid::print_strln("-- Server --");
	vid::print_str("Indirizzo: ");
	vid::print_uint(net::network.addr);
	vid::newline();

	while(true) {
		// receive packet
		net::packet pckt = net::recv_pckt();
		if(pckt.dest_addr != net::network.addr) continue;

		char buf[BUF_SIZE];
		str::mcpy((void*) buf, pckt.payload, pckt.len);

		vid::print_str("Ricevuto messaggio: \"");
		vid::print_str(buf);
		vid::print_str("\" da client: ");
		vid::print_uint(pckt.src_addr);
		vid::print_strln(", faccio echo...");

		// send packet back
		net::send((void*) buf, pckt.len, pckt.src_addr);
	}
}
