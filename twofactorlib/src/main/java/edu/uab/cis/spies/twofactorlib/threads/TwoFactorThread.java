
package edu.uab.cis.spies.twofactorlib.threads;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public abstract class TwoFactorThread extends Thread {

    private boolean sTimeToDie = false;

    public TwoFactorThread(ThreadGroup tGroup, String name) {
        super(tGroup, name);
        setDaemon(true);
    }

    /**
     * In some cases, you can use application specific tricks. For example, if a
     * thread is waiting on a known socket, you can close the socket to cause
     * the thread to return immediately. Unfortunately, there really isn't any
     * technique that works in general. It should be noted that in all
     * situations where a waiting thread doesn't respond to Thread.interrupt, it
     * wouldn't respond to Thread.stop either. Such cases include deliberate
     * denial-of-service attacks, and I/O operations for which thread.stop and
     * thread.interrupt do not work properly.
     */
    @Override
    public void interrupt() {
        super.interrupt();
        this.sTimeToDie = true;
    }

    @Override
    public boolean isInterrupted() {
        return this.sTimeToDie || super.isInterrupted();
    }

    @Override
    public void run() {
        try {
            mainloop();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    protected abstract void mainloop() throws InterruptedException;

    protected void takeRest(long time) throws InterruptedException {
        Thread.sleep(time);
    }
}