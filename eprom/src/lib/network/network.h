#ifndef NETWORK_H
#define NETWORK_H

#include "../conf/hardware.h"

/**
 * Namespace for networking, packet definitions, sendto/recvfrom primitives. 
 */
namespace net {
	/**
	 * Reference to network device.
	 */
	inline hwr::dev::network_device& network = hwr::dev::network;

	/**
	 * Sends a single byte on the network device.
	 *
	 * @param byte byte to send
	 */
	void send_byte(char byte);

	/**
	 * Receives a single byte from the network device.
	 *
	 * @return received byte
	 */
	char recv_byte();

	/**
	 * Sends a single 32 bit word on the network device.
	 *
	 * @param word word to send
	 */
	void send_word(uint32_t word);

	/**
	 * Receives a single 32 bit word from the network device.
	 *
	 * @return received word
	 */
	uint32_t recv_word();

	/**
	 * Max size (in bytes) of a packet payload.
	 */
	const int max_payload_size = 50;

	/**
	 * Structure of a network packet sent on the network.
	 */
	struct packet {
		/**
		 * Source address of packet.
		 */
		uint32_t src_addr;
		
		/**
		 * Destinaation address of packet.
		 */
		uint32_t dest_addr;
		
		/**
		 * Length of packet payload.
		 */
		uint32_t len;
		
		/**
		 * Packet payload.
		 */
		char payload[max_payload_size];
	};

	/**
	 * Sends a packet on the network.
	 *
	 * @param pckt packet to send
	 */
	void send_pckt(packet pckt);

	/**
	 * Receives a packet from the network.
	 *
	 * @return received packet
	 */
	packet recv_pckt();

	/**
	 * Makes a packet from a payload of given size and sends that packet to a 
	 * given address.
	 *
	 * @param payload pointer to payload of packet
	 * @param payload_size size of payload
	 * @param to destination address
	 */
	void sendto(void* payload, int payload_size, uint32_t to);

	/**
	 * Receives a packet destined to this host, and returns its payload the 
	 * address it comes from. 
	 *
	 * @param buf buffer to fill with payload
	 * @param buf_size size of buffer
	 * @param from int reference to fill with receive address
	 * @return size of received payload
	 */
	int recvfrom(void* buf, int buf_size, int& from);
} // net::

#endif
