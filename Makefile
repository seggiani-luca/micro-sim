EMULATOR := emulator
EXECUTABLE := target/micro-sim.jar
EPROM := eprom

all: run

run: emulator eprom
	@cd $(EMULATOR) && java -jar $(EXECUTABLE)

emulator:
	@cd $(EMULATOR) && mvn package

eprom:
	@cd $(EPROM) && $(MAKE)
