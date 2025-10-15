# micro-sim
micro-sim è un emulatore scritto in Java per un sistema basato su [RISC-V](riscv.org).

Le componenti simulate sono:
- Processore che implementa l'ISA RV32I;
- Spazio di memoria a 32 bit, composto da EPROM in sola lettura, RAM e VRAM;
- Interfaccia video in modalità testo;
- Interfaccia tastiera;
- Interfaccia timer.

Il firmware (caricato nell'EPROM simulata) di sistema deve essere compilato o assemblato per 
architettura RISC-V, ISA RV32I. Viene resa disponibile una libreria scritta in C++, e file di 
configurazione per la toolchain 
[riscv-gnu-toolchain](https://github.com/riscv-collab/riscv-gnu-toolchain).

## Guida rapida
Sotto vengono descritte le procedure per compilare emulatore e firmware.
Viene anche fornito un Makefile per automatizzarle.

Dalla directory base, per compilare firmware, emulatore e quindi eseguire, inserire:
```shell
make
```

Si possono usare anche i comandi `make emulator` e `make eprom` per compilare separatamente le due 
componenti.

## Compilare l'emulatore 
`emulator` contiene il sorgente dell'emulatore (`emulator/src`), file di configurazione 
(`emulator/conf`) e dati relativi all'EPROM e ROM caratteri del sistema (`emulator/data`).

Il progetto viene gestito attraverso [Maven](https://maven.apache.org/).
Dalla directory `emulator`, per compilare inserire:
```shell
$ mvn package
```
Questo creera un pacchetto `.jar` in `emulator/target`. Per eseguire inserire:
```shell
$ java -jar target/micro-sim.jar
```

La configurazione di default è cercata in `emulator/conf`. Si possono specificare altri file di 
configurazione attraverso l'opzione `-c <file-di-configurazione>`.

## Compilare il firmware
Per eseguire, l'emulatore ha bisogno di un firmware da caricare nell'EPROM simulata. Questo è 
rappresentato da un file [ELF](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format), che 
di default verrà cercato in `emulator/data/eprom.elf`. Si possono specificare altri file di EPROM 
attraverso l'opzione `-e <file-eprom>`.

Nella directory `eprom` è contenuto il sorgente del firmware di default (scritto in C++), assieme 
ad una piccola libreria (in `eprom/src/lib`) per funzioni base e accesso alle interfacce.

La compilazione viene gestita attraverso [Make](https://en.wikipedia.org/wiki/Make_(software)), e 
richiede la toolchain [riscv-gnu-toolchain](https://github.com/riscv-collab/riscv-gnu-toolchain) 
(su Arch Linux è disponibile 
nell'[AUR](https://aur.archlinux.org/packages/riscv32-gnu-toolchain-elf-bin)).

Dalla directory `eprom`, per compilare inserire:
```shell
$ make
```

Sono disponibili anche altri comandi, fra cui `make dump` per visualizzare il contenuto dell'EPROM,
e `make read` per stampare informazioni sugli header ELF.
