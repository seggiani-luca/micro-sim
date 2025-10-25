# micro-sim
micro-sim è un emulatore scritto in Java per un sistema basato su [RISC-V](riscv.org).

Le componenti simulate sono:
- Processore che implementa l'ISA RV32I;
- Spazio di memoria a 32 bit, composto da EPROM in sola lettura, RAM e VRAM:
    - EPROM: `[0x00000000, 0x0000ffff]`;
    - RAM: `[0x00010000, 0x0001ffff]`;
    - VRAM: `[0x00020000, 0x0002ffff]`.
- Interfaccia video in modalità testo a `0x00030000`;
- Interfaccia tastiera a `0x00040000`;
- Interfaccia timer a `0x00050000`.

Il firmware (caricato nelle EPROM simulate) dei sistemi emulati deve essere compilato o assemblato 
per architettura RISC-V, ISA RV32I. Per compilare il proprio firmware viene resa disponibile una 
libreria scritta in C++, e file di configurazione per la toolchain 
[riscv-gnu-toolchain](https://github.com/riscv-collab/riscv-gnu-toolchain).

## Guida rapida
Nelle prossime sezioni verranno descritte le procedure per compilare emulatore e firmware.
Nel caso si voglia eseguire subito, nella directory base viene fornito un Makefile che automatizza 
tutto.

L'utilizzo è il seguente:
```shell
# esegue
$ make run

# esegue con la shell di debug attiva
$ make debug

# compila tutto
$ make

# compila solo l'emulatore
$ make emulator

# compila solo il firmware
$ make eprom

# pulisce le directory degli oggetti e il firmware compilato 
$ make eprom
```

## Compilare l'emulatore 
La directory `emulator` contiene il sorgente dell'emulatore (`emulator/src`) e i dati relativi 
all'EPROM e alla ROM caratteri del sistema (`emulator/data`).

Il progetto viene gestito attraverso [Maven](https://maven.apache.org/).

Dalla directory `emulator`, per compilare inserire:
```shell
# compila l'emulatore
$ mvn package
```
Questo creerà due pacchetti `.jar` in `emulator/target`:
-   `micro-sim.jar`, che contiene il pacchetto `microsim` come libreria Java, e non può essere 
    eseguito;
-   `micro-sim-app.jar`, che contiene tutte le dipendenze, definisce un entry point, e può quindi 
    essere eseguito.

```shell
# esegui il pacchetto dell'emulatore
$ java -jar target/micro-sim-app.jar
```

Quando viene lanciato, l'emulatore cerca file ELF contenenti firmware all'interno di 
`emulator/data/eprom`, e crea una nuova istanza di simulazione per ciascun file trovato. 

## Compilare il firmware
Per eseguire, l'emulatore ha bisogno di firmware da caricare nelle EPROM simulate. Il firmware è 
contenuto in file [ELF](https://en.wikipedia.org/wiki/Executable_and_Linkable_Format), che di 
default vengono cercati in `emulator/data/eprom`. Si possono specificare altri luoghi dove cercare 
file ELF attraverso l'opzione `-e <directory-elf>`.

Nella directory `eprom` è contenuto il sorgente del firmware di default (scritto in C++), assieme 
ad una piccola libreria (scritta in C++ e assembly RISC-V, in `eprom/src/lib`) per funzioni base e 
accesso alle interfacce.

La compilazione viene gestita attraverso [Make](https://en.wikipedia.org/wiki/Make_(software)), e 
richiede la toolchain [riscv-gnu-toolchain](https://github.com/riscv-collab/riscv-gnu-toolchain) 
(su Arch Linux è disponibile 
nell'[AUR](https://aur.archlinux.org/packages/riscv32-gnu-toolchain-elf-bin)).

Dalla directory `eprom`, l'utilizzo è il seguente:
```shell
# compila il firmware
$ make

# pulisce le directory degli oggetti e il firmware compilato 
$ make clean
```
