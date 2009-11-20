package nachos.network;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>VMProcess</tt> that supports networking syscalls.
 */
public class NetProcess extends VMProcess {
    /**
     * Allocate a new process.
     */
    public NetProcess() {
    	super();
    	connection = new Connection();
    }

    private static final int
	syscallConnect = 11,
	syscallAccept = 12;
    
    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>11</td><td><tt>int  connect(int host, int port);</tt></td></tr>
     * <tr><td>12</td><td><tt>int  accept(int port);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
    switch (syscall) {
	case syscallAccept:
		return handleAccept(a0);
	case syscallConnect:
		return handleConnect(a0, a1);
	default:
	    return super.handleSyscall(syscall, a0, a1, a2, a3);
	}
    }
    
	/**
     * Attempt to initiate a new connection to the specified port on the specified
     * remote host, and return a new file descriptor referring to the connection.
     * connect() does not give up if the remote host does not respond immediately.
     *
     * Returns the new file descriptor, or -1 if an error occurred.
     */
    private int handleConnect(int a0, int a1) {
		return connection.connect(a0, a1);
	}

    /**
     * Attempt to accept a single connection on the specified local port and return
     * a file descriptor referring to the connection.
     *
     * If any connection requests are pending on the port, one request is dequeued
     * and an acknowledgement is sent to the remote host (so that its connect()
     * call can return). Since the remote host will never cancel a connection
     * request, there is no need for accept() to wait for the remote host to
     * confirm the connection (i.e. a 2-way handshake is sufficient; TCP's 3-way
     * handshake is unnecessary).
     *
     * If no connection requests are pending, returns -1 immediately.
     *
     * In either case, accept() returns without waiting for a remote host.
     *
     * Returns a new file descriptor referring to the connection, or -1 if an error
     * occurred.
     */
	private int handleAccept(int a0) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	private int handleRead (int a0, int a1, int a2){
		return -1;
	}
	
	
 	private int handleWrite(int a0, int a1, int a2) {
 		return -1;
 	}

	
	Connection connection;
}
