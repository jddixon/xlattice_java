# xlattice_java

This is the 2003-2006 Java implementation of XLattice, being gradually
migrated here from Sourceforge (http://xlattice.sourceforge.net).  The
process is gradual because the code needs to be modified to reflect
changes in the global environment, the Internet, over the last several
years.

XLattice consists of a number of components.  The Java version includes
jars for 

* **util**, basic facilities such as string and file handling;
* **corexml**, routines for building and navigating XML documents;
* **crypto**, cryptographic libraries;
* **transport**, code implementing XLattice's notion of inter-node communications;
* **protocol**, building blocks for protocols;
* **overlay**, facilities supporting address domains such as TCP/IP address blocks;
* **node**, the basic XLattice building block, a process(or) with a cryptographic identity, communications, and optionally a local file system;
* **httpd**; code for a server with an embedded node and supporting HTTP(S);
* **cryptoserver**, a web server using content-keyed store;
* **projmgr**, a Java project management tool

## Project Status

The Java version of XLattice is reasonably stable and well-tested.
It is a more complete implementation than the 
(Go version of XLattice)[https://jddixon.github.io/xlattice_go]
and far more complete than the
(Pytho version of XLattice)[https://jddixon.github.io/xlattice_py],
which is a library supporting some XLattice functionality.

## On-line Documentation

More information on the **xlattice_java** project can be found 
[here](https://jddixon.github.io/xlattice_java)
