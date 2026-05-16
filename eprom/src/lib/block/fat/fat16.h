#ifndef FAT16_H
#define FAT16_H

#include "../../string/string.h"

#define MAX_CLUSTER_SIZE 4096

namespace blk {
	/**
	 * Namespace for FAT16 filesystem definitions and helpers. 
	 */
	namespace fat {
		/**
		 * BIOS Parameter Block (BPB). Contains filesystem geometry and layout 
		 * information.
		 */
		struct bpb {
			/// length of logical sector, in bytes
			uint16_t log_sec_len;
			/// length of cluster, in sectors
			uint8_t cluster_len;
			/// reserved sectors (mainly for VBR)
			uint16_t reserved_secs;
			/// number of fat tables
			uint8_t n_fats;
			/// number of entries in the rootdir
			uint16_t root_dir_entries;
			/// number of logical sectors in the disk (legacy)
			uint16_t n_log_secs;
			/// media descriptor (floppy, hard disk, etc...)
			uint8_t media_desc;
			/// length of fat table
			uint16_t fat_len;
			/// physical sectors per track
			uint16_t phys_sec_per_track;
			/// number of drive heads
			uint16_t n_heads;
			/// number of hidden sectors (befores this partition)
			uint32_t hidden_secs;
			/// large number of logical sectors in the disk (for FAT16B) 
			uint32_t large_secs;
		} __attribute__((packed));

		/**
		 * Extended BIOS Parameter Block (EBPB). Additional metadata for FAT 
		 * volumes.
		 */
		struct ebpb {
			/// number of this physical drive
			uint8_t phys_drive_num;
			/// reserved octet
			uint8_t reserved;
			/// extended boot signature (signals that EBP is used)
			uint8_t ex_boot_sign;
			/// serial number of this physical drive
			uint32_t serial;
			/// label of this drive
			char label[11];
			/// filesystem on this drive
			char fs_type[8];
		} __attribute__((packed));

		/**
		 * Volume Boot Record (VBR). First sector of a FAT filesystem.
		 */
		struct vbr {
			/// jump code for boot drive
			char jump[3];
			/// OEM name of drive 
			char oem_name[8];
			/// BPB (BIOS Parameter Block)
			bpb param;
			/// EBPB (Extended BIOS Parameter Block)
			ebpb ex_param;
			/// boot code
			char boot_code[
				510 -
				sizeof(jump) -
				sizeof(oem_name) -
				sizeof(bpb) -
				sizeof(ebpb)
			];
			/// magic number for VBR
			uint16_t magic;
		} __attribute__((packed));

		/**
		 * Directory entry (8.3 FAT format). Represents a file or directory in 
		 * a directory listing.
		 */
		struct dir_ent {
			/// name of entry
			char filename[11];
			/// attributes (is the entry a directory? a file?)
			uint8_t attrib;
			/// reserved octets
			uint8_t reserved;
			/// creation time in tenths of second
			uint8_t creat_time_ds;
			/// creation time in hour, minute, second
			uint16_t creat_time;
			/// creation date in year, month, day
			uint16_t creat_date;
			/// last access date in year, month, day
			uint16_t last_date;
			/// high part of FAT table index (unused in FAT16)
			uint16_t cluster_hi_reserved;
			/// last modification time in hour, minute, second
			uint16_t modif;
			/// last modification date in year, month, day
			uint16_t modif_date;
			/// low part of FAT table index: points to actual file cluster
			uint16_t cluster_lo;
			/// size of file, in bytes
			uint32_t filesize;
		} __attribute__((packed));

		/**
		 * Attributes for directory.
		 */
		inline uint8_t dir_attrib = 0x10; 
		
		/**
		 * Attributes for files.
		 */
		inline uint8_t file_attrib = 0x20; 
		
		/**
		 * Attributes for volume ID.
		 */
		inline uint8_t vol_id_attrib = 0x08;

		/**
		 * First byte of emptied directory entry.
		 */
		inline uint8_t free_dir_ent = 0xe5;

		/**
		 * Checks for free directory entries.
		 *
		 * @param ent entry to check
		 * @return is the entry free?
		 */
		inline bool is_free(const dir_ent& ent) { 
			return ent.filename[0] == free_dir_ent; 
		}
		
		/**
		 * Checks for end of directory entries.
		 *
		 * @param ent entry to check
		 * @return is the entry the last one?
		 */
		inline bool is_end(const dir_ent& ent) { 
			return ent.filename[0] == '\0'; 
		}

		/**
		 * Checs for directory directory entries.
		 *
		 * @param ent entry to check
		 * @return is the entry a directory? 
		 */
		inline bool is_dir(const dir_ent& ent) { 
			return ent.attrib & dir_attrib; 
		}
		
		/**
		 * Checks for file directory entries.
		 *
		 * @param ent entry to check
		 * @return is the entry a file? 
		 */
		inline bool is_file(const dir_ent& ent) {
			return ent.attrib & file_attrib; 
		}

		/**
		 * Returns FAT table at index (counting from 0). Index is needed as a
		 * single FAT filesystem might have multiple FAT tables.
		 *
		 * @param bs VBR of current filesystem
		 * @param idx FAT table index
		 * @return first sector of FAT table at index 
		 */
		inline int get_fat(const vbr& bs, int idx) {
			int base = bs.param.reserved_secs;
			int size = bs.param.fat_len;
			return base + size * idx;
		}
	
		/**
		 * Return size of FAT table.
		 *
		 * @param bs VBR of current filesystem
		 * @return size (in sectors) of the FAT table
		 */
		inline int get_fat_len(const vbr& bs) {
			return bs.param.fat_len;
		}

		/**
		 * Returns root directory. 
		 *
		 * @param bs VBR of current filesystem
		 * @return first sector of root directory
		 */
		inline int get_rootdir(const vbr& bs) {
			int base = bs.param.reserved_secs;
			int size = bs.param.fat_len;
			int num = bs.param.n_fats;
			return base + size * num;
		}

		/**
		 * Returns size of root directory.
		 *
		 * @param bs VBR of current filesystem
		 * @return size (in sectors) of root directory
		 */
		inline int get_rootdir_len(const vbr& bs) {
			int sector_size = bs.param.log_sec_len;
			int entry_size = sizeof(dir_ent);
			int entries = bs.param.root_dir_entries;
			return (entry_size * entries) / sector_size;
		}

		/**
		 * Converts cluster to sector.
		 * 
		 * @param bs VBR of current filesystem
		 * @param idx index of needed cluster (in FAT table index space)
		 * @return first sector of cluster
		 */
		inline int get_cluster(const vbr& bs, int idx) {
			int cluster_size = bs.param.cluster_len;
			int cluster_base = get_rootdir(bs) + get_rootdir_len(bs);
			return cluster_base + cluster_size * (idx - 2); // 2 reserved
		}

		/**
		 * Returns size of cluster in sectors.
		 *
		 * @param bs VBR of current filesystem
		 * @return size of cluster in sectors
		 */
		inline int get_cluster_len(const vbr& bs) {
			return bs.param.cluster_len;
		}

		/**
		 * Returns size of cluster in bytes.
		 *
		 * @param bs VBR of current filesystem
		 * @return size of cluster in bytes 
		 */
		inline int get_cluster_bts(const vbr& bs) {
			int cluster_size = bs.param.cluster_len;
			int sector_size = bs.param.log_sec_len;
			return cluster_size * sector_size;
		}

		/**
		 * Free cluster FAT table entry.
		 */
		inline uint16_t free_cluster = 0x0000; 
		
		/**
		 * End of chain FAT table entry.
		 */
		inline uint16_t end_of_chain = 0xffff;

		/**
		 * Checks for end of chain FAT table entry.
		 *
		 * @param val entry of FAT table
		 * @return bool is the entry an end of chain?
		 */
		inline bool is_end_of_chain(uint16_t val) {
			return val >= 0xfff8;
		}

		/**
		 * Filename for "this directory" entry.
		 */
		const char dot_filename[] = ".          ";
		
		/**
		 * Filename for "prveious directory" entry.
		 */
		const char ddot_filename[] = "..         ";
	} // fat::
} // blk::

#endif
