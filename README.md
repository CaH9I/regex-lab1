How to use
==========
- Clone repository:
```bash
$ git clone https://github.com/CaH9I/regex-lab1
```
- Build the project ([maven](https://maven.apache.org) and [java8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) is required)
```bash
$ cd regex-lab1
$ mvn clean install
```
- Run
```bash
$ cd target
$ java -jar regex-lab1-1.0.jar <PATH_TO_FILE>
```
File should have the following format:
```
regex test_string1 test_string2 … test_stringN
```