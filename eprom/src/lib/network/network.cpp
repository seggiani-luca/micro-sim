#include "network.h"
#include "../string/string.h"
#include "../util/util.h"

namespace net {
	void send_byte(char byte) {
		while(*network.tx_rdy_reg != 1); // busy wait
		*network.tx_reg = byte;	
	}

	char recv_byte() {
		while(*network.rx_rdy_reg != 1); // busy wait
		return *network.rx_reg;
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

	// helper to construct an outbound packet 
	packet pack(void* payload, int payload_size, uint32_t to) {
		if(payload_size > MAX_PAYLOAD_SIZE) {
			utl::panic("Dimensione pacchetto troppo grande");
		}

		packet pckt;
		pckt.src_addr = network.addr;
		pckt.dest_addr = to;
		pckt.len = payload_size;

		str::mcpy(pckt.payload, payload, payload_size);
	
		return pckt;
	}
		
	void send(void* payload, int payload_size, uint32_t to) {
		while(true) {
			if(payload_size > MAX_PAYLOAD_SIZE) {
				// split
				packet pckt = pack(payload, MAX_PAYLOAD_SIZE, to);
				send_pckt(pckt);
				payload = ((char*) payload) + MAX_PAYLOAD_SIZE;
				payload_size -= MAX_PAYLOAD_SIZE;
			} else {
				// last packet, put everything
				packet pckt = pack(payload, payload_size, to);
				send_pckt(pckt);
				break; // payload_size = 0
			}
		}
	}

	int recv(void* buf, int buf_size, bool fill) {
		int size = 0;

		while(size < buf_size) {
			packet pckt = recv_pckt();
			if(pckt.dest_addr != network.addr) continue;

			if(size + pckt.len > buf_size) {
				// read what you can and return it
				int to_copy = buf_size - size;
				str::mcpy(buf, pckt.payload, to_copy);
				break;
			}

			// append this packet
			str::mcpy(buf, pckt.payload, pckt.len);
			buf = ((char*) buf) + pckt.len;
			size += pckt.len;

			if(!fill) break; // quit on first packet if non filling
		}

		return size;
	}
	
} // net::
