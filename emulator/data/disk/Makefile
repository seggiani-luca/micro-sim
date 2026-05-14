.PHONY: purge mount umount

purge:
	@echo ">> Removing all disks"
	rm -f -- *.img

mount:
	sudo mount -t vfat main.img /mnt/fat

umount:
	sudo umount /mnt/fat
