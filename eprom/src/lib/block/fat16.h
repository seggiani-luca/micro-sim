#ifndef FAT16_H
#define FAT16_H

#include <cstdint>

namespace blk {
	namespace fat {
		/**
		 * Boot sector.
		 */
		struct bpb {
			uint16_t log_sec_len; // in bytes
			uint8_t cluster_len;  // in log. sectors
			uint16_t reserved_secs;
			uint8_t n_fats;
			uint16_t root_dir_entries;
			uint16_t n_log_secs;
			uint8_t media_desc;
			uint16_t fat_len;
			uint16_t phys_sec_per_track;
			uint16_t n_heads;
			uint32_t hidden_secs;
			uint32_t large_secs;
		} __attribute__((packed));
		struct ebpb {
			uint8_t phys_drive_num;
			uint8_t reserved;
			uint8_t ex_boot_sign;
			uint32_t serial;
			char label[11];
			char fs_type[8];
		} __attribute__((packed));
		struct vbr {
			char jump[3];
			char oem_name[8];
			bpb param;
			ebpb ex_param;
			char boot_code[
				510 -
				sizeof(jump) -
				sizeof(oem_name) -
				sizeof(bpb) -
				sizeof(ebpb)
			];
			uint16_t magic;
		} __attribute__((packed));
	} // fat::
} // blk::

#endif
