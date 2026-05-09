#include "network.h"
#include "../string/string.h"
#include "../util/util.h"

namespace net {
	void send_byte(char byte) {
		while(*network.txr_prt != 1); // busy wait
		*network.txb_prt = byte;	
	}

	char recv_byte() {
		while(*network.rxr_prt != 1); // busy wait
		return *network.rxb_prt;
	}

	void send_word(uint32_t word) {
		for(int i = 0; i < 4; i++) {
			send_byte(word & 0xff);
			word >>= 8;
		}
	}

	uint32_t recv_word() {
		uint32_t word = 0;

		for(int i = 0; i < 4; i++) {
			word |= ((uint32_t) recv_byte()) << (8 * i);
		}
	
		return word;
	}

	void send_pckt(packet pckt) {
		send_word(pckt.src_addr);
		send_word(pckt.dest_addr);
		send_word(pckt.len);

		for(uint32_t i = 0; i < pckt.len; i++) {
			send_word(pckt.payload[i]);
		}
	}

	packet recv_pckt() {
		packet pckt;

		pckt.src_addr = recv_word();
		pckt.dest_addr = recv_word();
		pckt.len = recv_word();

		for(uint32_t i = 0; i < pckt.len; i++) {
			pckt.payload[i] = recv_word();
		}

		return pckt;
	}

	/**
	 * Helper that constructs a packet from given data.
	 *
	 * @param payload pointer to payload of packet
	 * @param payload_size size of payload
	 * @param to destination address
	 */
	packet pack(void* payload, int payload_size, uint32_t to) {
		if(payload_size > max_payload_size) {
			utl::panic("Dimensione pacchetto troppo grande");
		}

		packet pckt;
		pckt.src_addr = network.addr;
		pckt.dest_addr = to;
		pckt.len = payload_size;

		str::mcpy(pckt.payload, payload, payload_size);
	
		return pckt;
	}
		
	void sendto(void* payload, int payload_size, uint32_t to) {
		packet pckt = pack(payload, payload_size, to);
		send_pckt(pckt);
	}

	int recvfrom(void* buf, int buf_size, int& from) {	
		packet pckt;
		do {
			pckt = recv_pckt(); 
		} while(pckt.dest_addr != network.addr);

		from = pckt.src_addr;		
		int size = buf_size < pckt.len ? buf_size : pckt.len;
		str::mcpy(buf, pckt.payload, size);
		return size;
	}	
} // net::
