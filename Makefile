.PHONY: all run emulator eprom docs clean
MAKEFLAGS += --no-print-directory

EMULATOR := emulator
EXECUTABLE := target/micro-sim-app.jar
EPROM := eprom
DOCS := docs

all: emulator eprom 

debug: 
	@echo ">> Running emulator with debug shell..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -d

run: 
	@echo ">> Running emulator..."
	@cd $(EMULATOR) && java -jar $(EXECUTABLE) -s 2

emulator:
	@echo ">> Building emulator..."
	@cd $(EMULATOR) && mvn package
	@echo

eprom:
	@echo ">> Building EPROMs..."
	@cd $(EPROM) && $(MAKE)
	@echo

docs:
	@echo ">> Making documentation..."
	@cd $(EMULATOR) && mvn javadoc:javadoc -Dshow=private
	@echo

clean:
	@echo ">> Cleaning EPROMs..."
	@cd $(EPROM) && $(MAKE) clean
	@echo
