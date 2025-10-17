.PHONY: all run emulator eprom
MAKEFLAGS += --no-print-directory

EMULATOR := emulator
EXECUTABLE := target/micro-sim-app.jar
EPROM := eprom

all: run

debug: emulator eprom
	@echo ">> Running emulator with debug shell..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -d

run: emulator eprom
	@echo ">> Running emulator..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE)

emulator:
	@echo ">> Building emulator..."
	@cd $(EMULATOR) && mvn package
	@echo

eprom:
	@echo ">> Building EPROM..."
	@cd $(EPROM) && $(MAKE)
	@echo
