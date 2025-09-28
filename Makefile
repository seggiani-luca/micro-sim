SRC := $(shell find source -name "*.java")
OUT := out
MAIN := microsim.Main
ASSEMBLER := tools/assembler.py
EPROM_SRC := data/eprom.s
EPROM := data/eprom.dat

all: $(OUT)
	@javac -d $(OUT) $(SRC)

assemble:
	@python $(ASSEMBLER) $(EPROM_SRC) $(EPROM) 

run: all
	@java -cp $(OUT) $(MAIN) -e $(EPROM) $(ARGS)

clean:
	@rm -rf $(OUT)
