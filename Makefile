SRC := $(shell find src -name "*.java")
OUT := out
MAIN := microsim.Main

all: $(OUT)
	@javac -d $(OUT) $(SRC)

run: all
	@java -cp $(OUT) $(MAIN)

clean:
	@rm -rf $(OUT)
