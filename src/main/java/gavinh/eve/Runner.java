/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gavinh.eve;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gavin
 */
public class Runner {
    
    private static final Logger log = LoggerFactory.getLogger(Runner.class);
    
    private final Semaphore semaphore;
    
    public Runner(int threads) {
        semaphore = new Semaphore(threads);
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
