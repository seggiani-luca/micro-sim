#include "cluster.h"
#include "../table/table.h"
#include "../sector/sector.h"
#include "../fat/fat16.h"
#include "../../string/string.h"

namespace blk {
	namespace clu {
		void read(int idx, void* data, int size) {
			// get cluster address
			int addr = fat::get_cluster(tab::cur_vbr, idx);
			
			// read clusters until size reached 
			uint8_t* bdata = (uint8_t*) data;	
			for(int i = 0; i < fat::get_cluster_len(tab::cur_vbr); i++) {
				if(size >= sec::size) {
					// read full sector
					sec::read(addr + i, bdata);

					// advance pointer and size by full sector
					bdata += sec::size;
					size -= sec::size;
				} else {
					// read least sector
					uint8_t last[sec::size];
					sec::read(addr + i, last);

					// copy last sector and break
					mem::cpy(bdata, last, size);
					break;
				}
			}
		}

		void write(int idx, const void* data, int size) {
			// get cluster address
			int addr = fat::get_cluster(tab::cur_vbr, idx);

			// write all sectors of cluster
			uint8_t* bdata = (uint8_t*) data;	
			for(int i = 0; i < fat::get_cluster_len(tab::cur_vbr); i++) {
				if(size >= sec::size) {
					// write full sector 
					sec::write(addr + i, bdata);
					
					// advance pointer and size by full sector
					bdata += sec::size;
					size -=  sec::size;
				} else {
					// prepare last sector
					uint8_t last[sec::size];
					mem::set(last, 0, sec::size);
					mem::cpy(last, bdata, size);

					// write last sector and break
					sec::write(addr + i, last);
					break;
				}
			}
		}

		void zero(int idx) {
			// get cluster address
			int addr = fat::get_cluster(tab::cur_vbr, idx);
			
			// zero all sectors
			for(int i = 0; i < fat::get_cluster_len(tab::cur_vbr); i++) {
				sec::zero(addr + i);
			}
		}
	} // clu::
} // blk::
