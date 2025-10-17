# micro-sim
micro-sim è un emulatore scritto in Java per un sistema basato su [RISC-V](riscv.org).

Le componenti simulate sono:
- Processore che implementa l'ISA RV32I;
- Spazio di memoria a 32 bit, composto da EPROM in sola lettura, RAM e VRAM;
- Interfaccia video in modalità testo;
- Interfaccia tastiera;
- Interfaccia timer.

Il firmware (caricato nell'EPROM simulata) di sistema deve essere compilato o assemblato per 
architettura RISC-V, ISA RV32I. Per compilare il proprio firmware iene resa disponibile una 
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
```

## Compilare l'emulatore 
La directory `emulator` contiene il sorgente dell'emulatore (`emulator/src`), i file di 
configurazione (`emulator/conf`) e dati relativi all'EPROM e ROM caratteri del sistema 
(`emulator/data`).

Il progetto viene gestito attraverso [Maven](https://maven.apache.org/).

Dalla directory `emulator`, per compilare inserire:
```shell
# compila l'emulatore
$ mvn package
```
Questo creerà due pacchetti `.jar` in `emulator/target`:
-   `micro-sim.jar`, che contiene il pacchetto `microsim` come libreria Java, e non può essere 
    eseguito;
-   `micro-sim-app.jar`, che contiene tutte le dipendenze, definisce un entry point, e può essere 
    eseguito.

```shell
# esegui il pacchetto dell'emulatore
$ java -jar target/micro-sim-app.jar
```

La configurazione di default è cercata in `emulator/conf`. Si possono specificare altri file di 
configurazione attraverso le opzioni:
-   `-ci <configurazione-interfacce`: definisce il modo in cui vengono gestite le interfacce con 
    l'utente (finestra video, tastiera, ecc...);
-   `-cs <configurazione-simulazione>`: definisce la struttura del calcolatore simulato (mappa 
    memoria, dispositivi montati, ecc...) ed alcune politiche (accesso all'EPROM, ecc...).

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

Dalla directory `eprom`, l'utilizzo è il seguente:
```shell
# compila il firmware
$ make

# stampa il dump del firmware
$ make dump

# stampa gli header dell'ELF generato
$ make read

# pulisce le directory degli oggetti e l'ELF generato
$ make clean
```
