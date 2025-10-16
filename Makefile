.PHONY: all run emulator eprom
MAKEFLAGS += --no-print-directory

EMULATOR := emulator
EXECUTABLE := target/micro-sim-jar-with-dependencies.jar
EPROM := eprom

all: run

run: emulator eprom
	@echo ">> Running emulator..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -s 2

emulator:
	@echo ">> Building emulator..."
	@cd $(EMULATOR) && mvn package
	@echo

eprom:
	@echo ">> Building EPROM..."
	@cd $(EPROM) && $(MAKE)
	@echo
