package uk.ac.cam.cig23.fjava.tick4;

public class SafeMessageQueue<T> implements MessageQueue<T> {
    private static class Link<L> {
        L val;
        Link<L> next;

        Link(L val) {
            this.val = val;
            this.next = null;
        }
    }

    private Link<T> first = null;
    private Link<T> last = null;

    public synchronized void put(T val) {

        Link newLast = new Link(val);
        if(first==null) {
            first = newLast;
            last = newLast;
        } else {
            last.next = newLast;
            last = newLast;
        }
        this.notify();
    }

    public synchronized T take() {
        while (first == null) { // use a loop to block thread until data is available
            try {
                this.wait();
            } catch (InterruptedException ie) {
                // Ignored exception

                //This exception is thrown when the thread is interrupted. As sleep is a blocking method
                // it throws this exception. This will probably be an
                //interrupt from the writer thread (if we look at this as an isolated program then
                // it will be but it could be other processes on the computer interupting). By catching the
                // interupt we are stopping it from actually happening so we should probably do anything this thread
                //needs to finish safely (nothing as it is just sleeping). We should manually interupt the thread with
                //Thread().currentThread().interrupt(). We also might want to break the while loop because once
                //the thread has control again there will probably be something in the queue to read.
            }
        }

        T val = first.val;
        first = first.next;
        return val;
    }

}