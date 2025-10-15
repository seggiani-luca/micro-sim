# -- directories --
SRC := src
SRC_C := $(shell find $(SRC) -name "*.cpp")
SRC_S := $(shell find $(SRC) -name "*.s")

OUT := out
OUT_C := $(subst $(SRC), $(OUT), $(SRC_C:.cpp=.o))
OUT_S := $(subst $(SRC), $(OUT), $(SRC_S:.s=.o))

EPROM := ../emulator/data/eprom.elf
MEM_MAP := $(SRC)/lib/memory_map.ld

# -- tools --
RISCV_C := riscv32-unknown-elf-gcc
RISCV_S := riscv32-unknown-elf-as
RISCV_L := riscv32-unknown-elf-ld
RISCV_DUMP := riscv32-unknown-elf-objdump
RISCV_RELF := readelf

# -- targets --
all: $(OUT_C) $(OUT_S)
	@echo "Linking EPROM..."
	@$(RISCV_L) -T $(MEM_MAP) $(OUT_C) $(OUT_S) -o $(EPROM)

$(OUT)/%.o: $(SRC)/%.cpp | $(OUT)
	@echo "Compiling $<..."
	@mkdir -p $(dir $@)
	@$(RISCV_C) -march=rv32i -mabi=ilp32 -O2 -ffreestanding -nostdlib -fno-exceptions -fno-rtti -mno-small-data-limit=0 -c $< -o $@

$(OUT)/%.o: $(SRC)/%.s | $(OUT)
	@echo "Assembling $<..."
	@mkdir -p $(dir $@)
	@$(RISCV_S) -march=rv32i -mabi=ilp32 $< -o $@

dump: 
	@echo "Showing EPROM dump..."
	@$(RISCV_DUMP) -D $(EPROM)

read: 
	@echo "Showing EPROM ELF headers..."
	@$(RISCV_RELF) -a $(EPROM)

$(OUT):
	@mkdir -p $(OUT)

clean:
	@echo "Cleaning EPROM build files..."
	@rm -rf $(OUT) $(EPROM)
