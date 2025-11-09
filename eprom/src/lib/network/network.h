#ifndef NETWORK_H
#define NETWORK_H

#include "../conf/hardware.h"

#define MAX_PAYLOAD_SIZE 50

namespace net {
	/*
	 * Reference to network device.
	 */
	inline hwr::dev::network_device& network = hwr::dev::network;

	/*
	 * Sends a single byte.
	 */
	void send_byte(char byte);

	/*
	 * Receives a single byte.
	 */
	char recv_byte();

	/*
	 * Sends a single 32 bit word.
	 */
	void send_word(uint32_t word);

	/*
	 * Receives a single 32 bit word.
	 */
	uint32_t recv_word();

	struct packet {
		uint32_t src_addr;
		uint32_t dest_addr;
		uint32_t len;
		char payload[MAX_PAYLOAD_SIZE];
	};

	/*
	 * Sends a packet.
	 */
	void send_pckt(packet pckt);

	/*
	 * Receives a packet.
	 */
	packet recv_pckt();

	/*
	 * Sends a payload of given size to a given address.
	 */
	void send(void* payload, int payload_size, uint32_t to);

	/*
	 * Receives a payload destined to this host. The fill flag signals if the function should wait 
	 * for the buffer to be completely filled. Otherwise reads number of read bytes.
	 */
	int recv(void* buf, int buf_size, bool fill = false);
} // net::

#endif
