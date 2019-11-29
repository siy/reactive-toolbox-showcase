### Reactive Toolbox Sample

This project is an implementation of money transfer service of imaginary bank.

Implementation is based on several assumptions/simplifications/etc.

 - No actual persistence layer (everything is stored in memory)
 - No transaction support while persisting data
 
### Build/Run

#### Prerequisites
 - Maven 3.6.0+
 - Java 11+

```bash
# mvn clean package
# java -jar target/showcase-0.0.1-SNAPSHOT.jar
```

### Implementation Notes

 - This implementation uses Jooby as a framework for serving requests.
 - __The implementation is not finished - it misses JSON configuration for
 requests (and probably some responses), so not all REST API's are working.__
 - For demonstration purposes application creates few accounts (reported in log)
 with some currency amount available at one of them.
