package nachos.network;

import static nachos.network.ConnectionState.*;
import static nachos.network.ConnectionEvent.*;
import nachos.machine.OpenFile;
import nachos.threads.KThread;

public final class Connection extends OpenFile {
	
	private ConnectionState connectionState = CLOSED;
	
	void testTransition(){
		ConnectionEvent event = FIN;
		try {
			ConnectionTransition.doEvent(this, event);
		} catch (FailSyscall e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolDeadlock e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}

	public void send(ConnectionEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void block() {
		// TODO Auto-generated method stub
		
	}

	public boolean dequeue() {
		// TODO Auto-generated method stub
		return false;
	}

	public void wakeConnectThread() {
		// TODO Auto-generated method stub
		
	}

	public void queue() {
		// TODO Auto-generated method stub
		
	}

	public void shiftSendWindow() {
		// TODO Auto-generated method stub
		
	}

	public boolean isSendQueueEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void resendPackets() {
		// TODO Auto-generated method stub
		
	}

	public void sendData() {
		// TODO Auto-generated method stub
		
	}

	public void clearSendWindow() {
		// TODO Auto-generated method stub
		
	}

	public int connect(int a0, int a1) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void close() {
		try {
			ConnectionTransition.doEvent(this, CLOSE);
			new KThread(new ConnectionFinisher(this)).fork();
		} catch (FailSyscall e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolDeadlock e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public int read(byte[] buf, int offset, int length) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int read(int pos, byte[] buf, int offset, int length) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int write(byte[] buf, int offset, int length) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int write(int pos, byte[] buf, int offset, int length) {
		// TODO Auto-generated method stub
		return -1;
	}

	public void finish() {
		// TODO Auto-generated method stub
		
	}
	

}