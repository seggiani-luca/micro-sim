SRC := $(shell find source -name "*.java")
OUT := out
MAIN := microsim.Main

all: $(OUT)
	@javac -d $(OUT) $(SRC)

run: all
	@java -cp $(OUT) $(MAIN) -e data/eprom.dat $(ARGS)

clean:
	@rm -rf $(OUT)
