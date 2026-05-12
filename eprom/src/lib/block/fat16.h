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

		/**
		 * Directory entry.
		 */
		struct dir_ent {
			char filename[11];
			uint8_t attrib;
			uint8_t reserved;
			uint8_t creat_time_ds; // tenths of second
			uint16_t creat_time;   // hour, minute, second 
			uint16_t creat_date;   // year, month, day
			uint16_t last_date;    // year, month, day
			uint16_t cluster_hi_reserved;
			uint16_t modif;        // hour, minute, second 
			uint16_t modif_date;   // year, month, day
			uint16_t cluster_lo;
			uint32_t filesize;     // in bytes	
		} __attribute__((packed));

		/**
		 * Attributes for directory.
		 */
		inline uint8_t dir_attrib = 0x10; 
		
		/**
		 * Attributes for volume ID.
		 */
		inline uint8_t vol_id_attrib = 0x08;

		/**
		 * Returns fat table at index (counting from 0).
		 */
		inline int get_fat(const vbr& bs, int idx) {
			int base = bs.param.reserved_secs;
			int size = bs.param.fat_len;
			return base + size * idx;
		}
	
		/**
		 * Return size of fat table.
		 */
		inline int get_fat_len(const vbr& bs) {
			return bs.param.fat_len;
		}

		/**
		 * Returns root directory. 
		 */
		inline int get_rootdir(const vbr& bs) {
			int base = bs.param.reserved_secs;
			int size = bs.param.fat_len;
			int num = bs.param.n_fats;
			return base + size * num;
		}

		/**
		 * Returns size of root directory.
		 */
		inline int get_rootdir_len(const vbr& bs) {
			int sector_size = bs.param.log_sec_len;
			int entry_size = sizeof(dir_ent);
			int entries = bs.param.root_dir_entries;
			return (entry_size * entries) / sector_size;
		}

		/**
		 * Converts cluster to sector.
		 */
		inline int get_cluster(const vbr& bs, int idx) {
			int cluster_size = bs.param.cluster_len;
			int cluster_base = get_rootdir(bs) + get_rootdir_len(bs);
			return cluster_base + cluster_size * (idx - 2); // 2 reserved
		}

		/**
		 * Returns size of cluster in sctors.
		 */
		inline int get_cluster_secs(const vbr& bs) {
			return bs.param.cluster_len;
		}

		/**
		 * Returns size of cluster in bytes.
		 */
		inline int get_cluster_len(const vbr& bs) {
			int cluster_size = bs.param.cluster_len;
			int sector_size = bs.param.log_sec_len;
			return cluster_size * sector_size;
		}

		/**
		 * Free cluster fat entry.
		 */
		inline uint16_t free_cluster = 0x0000; 
		
		/**
		 * End of chain fat entry.
		 */
		inline uint16_t end_of_chain = 0xffff;

		/**
		 * Checks for end of chain fat entry.
		 */
		inline bool is_end_of_chain(uint16_t val) {
			return val >= 0xfff8;
		}
	} // fat::
} // blk::

#endif
