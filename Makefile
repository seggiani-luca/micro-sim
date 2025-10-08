# -- directories --
# program
SRC := $(shell find source -name "*.java")
OUT := out
MAIN := microsim.Main

# eprom
EPROM_SRC := data/eprom_source
EPROM_SRC_C := $(shell find $(EPROM_SRC) -name "*.c")
EPROM_SRC_S := $(shell find $(EPROM_SRC) -name "*.s")
EPROM_OUT := data/eprom_out
EPROM_OBJ_C := $(patsubst $(EPROM_SRC)/%, $(EPROM_OUT)/%, $(EPROM_SRC_C:.c=.o))
EPROM_OBJ_S := $(patsubst $(EPROM_SRC)/%, $(EPROM_OUT)/%, $(EPROM_SRC_S:.s=.o))
EPROM := data/eprom.elf
EPROM_MEM_MAP := $(EPROM_SRC)/memory_map.ld

# documentation
DOC := docs

# -- tools --
# java
JAVAC := javac
JAVA := java

# eprom
RISCV_C := riscv32-unknown-elf-gcc
RISCV_S := riscv32-unknown-elf-as
RISCV_L := riscv32-unknown-elf-ld
RISCV_DUMP := riscv32-unknown-elf-objdump
RISCV_RELF := readelf

# documentation
JAVADOC := javadoc

# -- targets --
# program
all: $(OUT)
	@$(JAVAC) -d $(OUT) $(SRC)

run: all
	@$(JAVA) -cp $(OUT) $(MAIN) -e $(EPROM) $(ARGS)

debug: all
	@$(JAVA) -cp $(OUT) $(MAIN) -e $(EPROM) -d $(ARGS)

$(OUT):
	@mkdir -p $(OUT)

clean: clean_eprom
	@rm -rf $(OUT)

# eprom
$(EPROM_OUT)/%.o: $(EPROM_SRC)/%.c | $(EPROM_OUT)
	@$(RISCV_C) -march=rv32i -mabi=ilp32 -O0 -ffreestanding -nostdlib -c $< -o $@

$(EPROM_OUT)/%.o: $(EPROM_SRC)/%.s | $(EPROM_OUT)
	@$(RISCV_S) -march=rv32i -mabi=ilp32 $< -o $@

eprom: $(EPROM_OBJ_C) $(EPROM_OBJ_S)
	@$(RISCV_L) -T $(EPROM_MEM_MAP) $(EPROM_OBJ_C) $(EPROM_OBJ_S) -o $(EPROM)

eprom_dump: eprom
	@$(RISCV_DUMP) -D $(EPROM)

eprom_read: eprom
	@$(RISCV_RELF) -a $(EPROM)

$(EPROM_OUT):
	@mkdir -p $(EPROM_OUT)

clean_eprom:
	@rm -rf $(EPROM_OUT) $(EPROM)

# documentation
docs:
	@$(JAVADOC) -quiet -Xdoclint:none -d $(DOC) -private $(SRC)
