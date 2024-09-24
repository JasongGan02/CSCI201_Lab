package ganjason_CSCI201_Lab4;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Customer extends Thread {

	private int customerName;
	private SleepingBarber sb;
	private Lock customerLock;
	private Condition gettingHaircutCondition;
	public Customer(int customerName, SleepingBarber sb) {
		this.customerName = customerName;
		this.sb = sb;
		customerLock = new ReentrantLock();
		gettingHaircutCondition = customerLock.newCondition();
	}
	
	public int getCustomerName() {
		return customerName;
	}
	public void startingHaircut() {
		Util.printMessage("Customer " + customerName + " is getting hair cut.");
	}
	public void finishingHaircut() {
		Util.printMessage("Customer " + customerName + " is done getting hair cut.");
		try {
			customerLock.lock();
			gettingHaircutCondition.signal();
		} finally {
			customerLock.unlock();
		}
	}
	public void run() {
		boolean seatsAvailable = sb.addCustomerToWaiting(this);
		if (!seatsAvailable) {
			Util.printMessage("Customer " + customerName + " leaving...no seats available.");
			return;
		}
		sb.wakeUpBarber();
		try {
			customerLock.lock();
			gettingHaircutCondition.await();
		} catch (InterruptedException ie) {
			System.out.println("ie getting haircut: " + ie.getMessage());
		} finally {
			customerLock.unlock();
		}
		Util.printMessage("Customer " + customerName + " is leaving.");
	}
}
