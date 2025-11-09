# -- directories --
SRC := src

EPROM_SRC := $(shell find $(SRC) -maxdepth 1 -name "*.cpp")

LIB_SRC := $(SRC)/lib
MEM_MAP := $(LIB_SRC)/conf/memory_map.ld
LIB_SRC_C := $(shell find $(LIB_SRC) -name "*.cpp")
LIB_SRC_S := $(shell find $(LIB_SRC) -name "*.s")

OUT := out

LIB_OUT := $(OUT)/lib
LIB_OUT_C := $(subst $(LIB_SRC), $(LIB_OUT), $(LIB_SRC_C:.cpp=.o))
LIB_OUT_S := $(subst $(LIB_SRC), $(LIB_OUT), $(LIB_SRC_S:.s=.o))

EPROM_OUT := ../emulator/data/eprom
EPROM_ELF := $(patsubst $(SRC)/%.cpp,$(EPROM_OUT)/%.elf,$(EPROM_SRC))

EX_EPROM_ELF := $(wildcard $(EPROM_OUT)/*.elf)
EX_EPROM_NUM := $(words $(EX_EPROM_ELF))

# -- tools --
RISCV_C := riscv32-unknown-elf-gcc
RISCV_S := riscv32-unknown-elf-as
RISCV_L := riscv32-unknown-elf-ld
RISCV_DUMP := riscv32-unknown-elf-objdump
RISCV_RELF := readelf

# -- targets --
all: $(LIB_OUT_C) $(LIB_OUT_S) $(EPROM_ELF)

$(EPROM_OUT)/%.elf: $(SRC)/%.cpp $(LIB_OUT_C) $(LIB_OUT_S) | $(EPROM_OUT)
	@echo "Compiling and linking EPROM source $@"
	@$(RISCV_C) -march=rv32i -mabi=ilp32 -O2 -ffreestanding -nostdlib -fno-exceptions -fno-rtti -c $< -o $(OUT)/$*.o
	@$(RISCV_L) -T $(MEM_MAP) $(OUT)/$*.o $(LIB_OUT_C) $(LIB_OUT_S) -o $@

$(LIB_OUT)/%.o: $(LIB_SRC)/%.cpp | $(LIB_OUT)
	@echo "Compiling lib source $<"
	@mkdir -p $(dir $@)
	@$(RISCV_C) -march=rv32i -mabi=ilp32 -O2 -ffreestanding -nostdlib -fno-exceptions -fno-rtti -mno-small-data-limit=0 -c $< -o $@

$(LIB_OUT)/%.o: $(LIB_SRC)/%.s | $(LIB_OUT)
	@echo "Assembling lib source $<"
	@mkdir -p $(dir $@)
	@$(RISCV_S) -march=rv32i -mabi=ilp32 $< -o $@

$(LIB_OUT):
	@mkdir -p $(LIB_OUT)

clean:
	@echo "Cleaning EPROM build files"
	@rm -rf $(OUT)/* 
	@echo "Cleaning $(EX_EPROM_NUM) EPROM(s) from data dir"
	@rm -rf $(EX_EPROM_ELF)
