ASGwrapper-synctools
--------------------

ASGwrapper-synctools is a library which helps to abstract from calling third party synchronous tools

Currently it includes methods to call following third party tools:

* Cadence(R) Encounter(R) v14.27
* Cadence(R) Incisive v15.20-s047
* Synopsys(R) Design Compiler (R) v2015.06-SP2 
* Synopsys(R) PrimeTime(R) v2015.06-SP2

These tools are not included in this project. The version numbers indicate the versions used for benchmarking purposes and thus are tested to work with this library. Newer or older versions of these tools might or might not work with this library.

All product names, trademarks and registered trademarks are property of their respective owners. All company, product and service names used in this website are for identification purposes only. Use of these names, trademarks and brands does not imply endorsement.

### Build instructions ###

To build ASGwrapper-snyctools, Apache Maven v3.1.1 (or later) and the Java Development Kit (JDK) v1.8 (or later) are required.

1. Build [ASGcommon](https://github.com/hpiasg/asgcommon)
2. Execute `mvn clean install -DskipTests`