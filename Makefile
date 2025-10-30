.PHONY: all run emulator eprom
MAKEFLAGS += --no-print-directory

EMULATOR := emulator
EXECUTABLE := target/micro-sim-app.jar
EPROM := eprom

all: emulator eprom 

debug: 
	@echo ">> Running emulator with debug shell..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -d

run: 
	@echo ">> Running emulator..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -s 1

emulator:
	@echo ">> Building emulator..."
	@cd $(EMULATOR) && mvn package
	@echo

eprom:
	@echo ">> Building EPROMs..."
	@cd $(EPROM) && $(MAKE)
	@echo

clean:
	@echo ">> Cleaning EPROMs..."
	@cd $(EPROM) && $(MAKE) clean
	@echo
