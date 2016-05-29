package gavinh.eve;

import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gavin
 */
public class Runner {
    
    private static final Logger log = LoggerFactory.getLogger(Runner.class);
    
    private final int totalPermits;
    private final Semaphore semaphore;
    
    public Runner(int threads) {
        this.totalPermits = threads;
        semaphore = new Semaphore(threads);
    }
    
    public boolean isFinished() {
        return totalPermits == semaphore.availablePermits();
    }
    
    public synchronized void run(Runnable torun) {
        try {
            semaphore.acquire();
            new Thread(new Slave(torun)).start();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
    public class Slave implements Runnable {
        
        private final Runnable torun;
        
        public Slave(Runnable torun) {
            this.torun = torun;
        }

        @Override
        public void run() {
            try {
                torun.run();
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            } finally {
                semaphore.release();
            }
        }
    }
}
