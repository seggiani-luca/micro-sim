#ifndef NETWORK_H
#define NETWORK_H

#include "../conf/hardware.h"

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

	/*
	 * Max size (in bytes) of a packet payload.
	 */
	const int max_payload_size = 50;

	/*
	 * Structure of a network packet sent on the network.
	 */
	struct packet {
		uint32_t src_addr;
		uint32_t dest_addr;
		uint32_t len;
		char payload[max_payload_size];
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
	 * Makes a packet from a payload of given size and sends that packet to a given address.
	 */
	void sendto(void* payload, int payload_size, uint32_t to);

	/*
	 * Receives a packet destined to this host, and returns its payload the address it comes from. 
	 */
	int recvfrom(void* buf, int buf_size, int& from);
} // net::

#endif
