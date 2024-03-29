package nachos.threads;

import java.util.ArrayList;
import java.util.List;

import nachos.machine.Lib;
import nachos.machine.Machine;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {

	/**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }
    
    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
    	Lib.assertTrue(Machine.interrupt().disabled());
    		       
    	Lib.assertTrue(priority >= priorityMinimum &&
    		   priority <= priorityMaximum);
    	
    	ThreadState threadState = getThreadState(thread);
    	boolean intStatus = Machine.interrupt().disable(); 
    	threadState.setPriority(priority);
    	if (threadState.waitingInQueue != null){
    	threadState.waitingInQueue.queue.remove(threadState);
    	
    	threadState.waitingInQueue.queue.offer(threadState);
    	}
    	Machine.interrupt().restore(intStatus);
    	
        }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    private static class PriorityTest implements Runnable {
    	PriorityTest() {
    	}
    	
    	public void run() {
    		KThread highPri1 = 
    			new KThread(new Runnable() {
    			   public void run() {
					   System.out.println("High priority thread 1 listening");
					   System.out.println("High priority thread 1 heard " + testCommunicator.listen());
    				   for (int i = 0; i < 10; i++)
    				   {
    					   System.out.println("High priority thread 1 loop " + i);
    					   long currentTime = Machine.timer().getTime();
    					   while (Machine.timer().getTime() < currentTime + 500)
    					   {
    						   KThread.yield();
    					   }
    				   }
    		        }
    		        }).setName("High Priority Thread #1");
    		
    		
    		highPri1.fork();
    		((ThreadState)highPri1.schedulingState).setPriority(priorityMaximum);
    		
    		KThread highPri2 = 
    			new KThread(new Runnable() {
    			   public void run() {
					   System.out.println("High priority thread 2 listening");
					   System.out.println("High priority thread 2 heard " + testCommunicator.listen());
    				   for (int i = 0; i < 10; i++)
    				   {
    					   System.out.println("High priority thread 1 loop " + i);
    					   long currentTime = Machine.timer().getTime();
    					   while (Machine.timer().getTime() < currentTime + 500)
    					   {
    						   KThread.yield();
    					   }
    				   }
    		        }
    		        }).setName("High Priority Thread #2");
    		highPri2.fork();
    		((ThreadState)highPri2.schedulingState).setPriority(priorityMaximum);
    		
    		KThread lowPri = 
    			new KThread(new Runnable() {
    			   public void run() {
					   System.out.println("Low priority thread speaking");
    				   testCommunicator.speak(1);
					   System.out.println("Low priority thread speaking");
    				   testCommunicator.speak(1);
    		        }
    		        }).setName("Low Priority Thread");
    		lowPri.fork();
    	}

        private static Communicator testCommunicator = new Communicator();
        }

    private static class DonationTest implements Runnable {
    	DonationTest() {
    	}
    	
    	public void run() {
    		
    		KThread lowPri = 
    			new KThread(new Runnable() {
    			   public void run() {
					   System.out.println("Low priority getting lock");
					   mutex.acquire();
					   ((ThreadState)KThread.currentThread().schedulingState).setPriority(2);
    				   for (int i = 0; i < 15; i++)
    				   {
    					   System.out.println("Low priority thread loop " + i);
    					   long currentTime = Machine.timer().getTime();
    					   while (Machine.timer().getTime() < currentTime + 500)
    					   {
    						   KThread.yield();
    					   }
						   KThread.yield();
    				   }
					   System.out.println("Low priority releasing lock");
					   conditionWait.wake();
					   mutex.release();
    		        }
    		        }).setName("Low Priority Thread");
    		lowPri.fork();
    		((ThreadState)lowPri.schedulingState).setPriority(7);

    		/* wait a bit to make sure the low priority thread get the lock */
			long currentTime = Machine.timer().getTime();
			while (Machine.timer().getTime() < currentTime + 200)
			{
				KThread.yield();
			}
    		
    		KThread midPri1 = 
    			new KThread(new Runnable() {
    			   public void run() {
    				   for (int i = 0; i < 15; i++)
    				   {
    					   System.out.println("Mid priority thread 1 loop " + i);
    					   long currentTime = Machine.timer().getTime();
    					   while (Machine.timer().getTime() < currentTime + 500)
    					   {
    						   KThread.yield();
    					   }
						   KThread.yield();
    				   }
    		        }
    		        }).setName("Mid Priority Thread #1");
    		
    		
    		midPri1.fork();
    		((ThreadState)midPri1.schedulingState).setPriority(4);
    		
    		currentTime = Machine.timer().getTime();
			while (Machine.timer().getTime() < currentTime + 200)
			{
				KThread.yield();
			}
			
    		KThread highPri = 
    			new KThread(new Runnable() {
    			   public void run() {
					   System.out.println("High priority getting lock");
					   mutex.acquire();
    				   for (int i = 0; i < 15; i++)
    				   {
    					   System.out.println("High priority thread loop " + i);
    					   long currentTime = Machine.timer().getTime();
    					   while (Machine.timer().getTime() < currentTime + 500)
    					   {
    						   KThread.yield();
    					   }
						   KThread.yield();
    				   }
					   System.out.println("High priority releasing lock");
					   conditionWait.wake();
					   mutex.release();
    		        }
    		        }).setName("High Priority Thread");
    		highPri.fork();
    		((ThreadState)highPri.schedulingState).setPriority(5);
    	}

        private Lock mutex = new Lock();
        private Condition2 conditionWait = new Condition2(mutex);
        }

    
    /**
     * Tests whether this module is working.
     */
   public static void selfTest() {
	Lib.debug(dbgThread, "Enter PriorityScheduler.selfTest");
	//new KThread(new PriorityTest()).fork();
	KThread donationtest = new KThread(new DonationTest());
	donationtest.fork();
	((ThreadState)donationtest.schedulingState).setPriority(6);
		
    }
    
    protected static final char dbgThread = 't';

    
    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    


    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	PriorityQueue(boolean transferPriority) {
	    this.transferPriority = transferPriority;
	}

	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // assuming that we have everything in order, we should be able to poll the queue.
	    ThreadState threadState = (queue.peek() == null) ? null : queue.poll();
	    if (transferPriority) {
	    	if (lockHolder != null){
	    		lockHolder.donation = 0;
	    	}
	    	lockHolder = threadState;
	    	if (threadState != null){
	    		threadState.donatePriority(threadState.waitingInQueue);
	    		threadState.waitingInQueue = null;
	    		
	    	}
	    	
	    }
	    KThread thread = (threadState == null) ? null : threadState.thread;
	    if (thread != null && threadState != null)
	    	Lib.debug(dbgThread, "Next thread is " + thread.getName() + 
	    			" with effective priority " + threadState.getEffectivePriority());
	    return (threadState == null) ? null : threadState.thread;
	}

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    //  Assuming that we have everything in order, we should be able to peek at the queue
	    return queue.peek();
	}
	
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    for (ThreadState threadState: queue){
	    	System.out.println(threadState.thread + " -- Priority " + threadState.getPriority() + 
	    			" -- Effective Priority " + threadState.getEffectivePriority());
	    }
	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
    protected ThreadState lockHolder;
    protected java.util.PriorityQueue<ThreadState> queue = new java.util.PriorityQueue<ThreadState>();

    }
    
 

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState implements Comparable<ThreadState> {
	
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread){
	    this.thread = thread;
	    this.creationTime = Machine.timer().getTime();
	    setPriority(priorityDefault);
	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	public int getEffectivePriority() {
		
		//  Effective priority should be the larger of the effective priority or the donated priority.
		if (this.donation > this.priority){
			return this.donation;
		}else{
			return this.priority;
		}
		
	}

	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
	    if (this.priority == priority)
		return;
	    
	    
	    this.priority = priority;
	    
	   //implement me
	    
	    if (this.priority >= this.donation){
	    	this.donation = 0;
	    }
	    if (this.waitingInQueue != null){
	    	donatePriority(this.waitingInQueue);
	    }
	    
	    
	}


	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
		this.waitingInQueue = waitQueue;
		this.creationTime = Machine.timer().getTime();
		waitQueue.queue.offer(this);
		donatePriority(waitQueue);
	}

	protected void donatePriority(PriorityQueue waitQueue) {
		donatePriority(waitQueue, new ArrayList<PriorityQueue>());
	}

	private void donatePriority(PriorityQueue waitQueue,
			List<PriorityQueue> visitedQueues) {
		if (waitQueue.lockHolder == null || visitedQueues.contains(waitQueue))
			return;
		
		if (waitQueue.transferPriority 
				&& (waitQueue.lockHolder.getEffectivePriority() < (this.priority)
						|| waitQueue.lockHolder.getEffectivePriority() < (this.donation))){
			
			
	    	boolean intStatus = Machine.interrupt().disable(); 
	    	waitQueue.lockHolder.donation = getEffectivePriority();
	    	if (waitQueue.lockHolder.waitingInQueue != null){
	    		waitQueue.lockHolder.waitingInQueue.queue.remove(waitQueue.lockHolder);
	    	
	    		waitQueue.lockHolder.waitingInQueue.queue.offer(waitQueue.lockHolder);
	    	}
	    	Machine.interrupt().restore(intStatus);

			
			
			
			waitQueue.lockHolder.donation = getEffectivePriority();
			Lib.debug(dbgThread, "Donating priority of " + waitQueue.lockHolder.donation 
					+" to " + waitQueue.lockHolder.thread.getName());
		}
		
		visitedQueues.add(waitQueue);
		
		if (waitQueue.lockHolder.waitingInQueue != null){
			donatePriority(waitQueue.lockHolder.waitingInQueue, visitedQueues);
		}
		
		 
		
	}

	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>wait</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */
	public void acquire(PriorityQueue waitQueue) {
		// jnz-  there should be nothing in the queue, so the first thread through is the lockHolder.
	    
		if (waitQueue.transferPriority){
		waitQueue.lockHolder = this;
	//    waitQueue.queue.offer(this);
		}
	    
	}	

	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected PriorityQueue waitingInQueue;
	protected int priority;
	protected int donation;
	protected long creationTime;
	@Override
	
	// jnz - comparator reverses the natural ordering so that the highest Effective Priority is at the head of the queue
	//  but within a given effective priority, the lowest creation time is at the head of the queue.
	public int compareTo(ThreadState threadState) {
		if (this.getEffectivePriority() == threadState.getEffectivePriority()){
			return new Long(creationTime).compareTo(new Long(threadState.creationTime));
		}else{
			return -(new Integer(getEffectivePriority()).compareTo(threadState.getEffectivePriority()));
		}
		
	}
    }
}
