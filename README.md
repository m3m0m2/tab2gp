# Summary

**tab2gp** convert a guitar tab from plain text to GP format.


# Dependency

This project uses tuxguitar. It's expected to be installed in /opt/tuxguitar/.
If it's on different location, update `TG_PATH` in the script and build as:

```
$ mvn -Dtg.path=<your_tuxguitar_path> package
```


# Run

Specify input text file and output gp file. For example:

```
$ ./tab2gp.sh -i README.md -n 'Simple Song' README.gp
```

```
E|------------------------------------------------------ 3--------
B|---------------------------- 4-- 7------ 4-------- 4-- 4------ 4
G|------------------------ 3 5---- 6------ 3---- 5 5---- 4------ 3
D|-------------------------------- 5------ 4-------------------- 2
A|-------------------------------------------------------------- 3
E|-------------------------------- 5------ 4------------ 3--------
```

This example is part of a tab found on http://www.lickbyneck.com/


# Caveats

Some html pages may work because tags are stripped out, however there are many
limitations:

- Chords and lyrics are currently ignored.
- Expects tab input as in the example above.
- Little testing done.
- Long list of TODOs.


# Author

[Mauro Meneghin](https://github.com/m3m0m2)


# License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
