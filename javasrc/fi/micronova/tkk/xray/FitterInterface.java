package fi.micronova.tkk.xray;

/**
   Abstract interface for thread for automatic fitting.
 */

public interface FitterInterface {
    public void closeWithoutWaiting();
    public void close();
}
