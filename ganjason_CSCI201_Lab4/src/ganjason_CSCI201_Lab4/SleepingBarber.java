package ganjason_CSCI201_Lab4;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SleepingBarber extends Thread {

	private int maxSeats;
	private int totalCustomers;
	private ArrayList<Customer> customersWaiting;
	private Lock barberLock;
	private Condition sleepingCondition;
	private boolean moreCustomers;
	private String barberName;
	public SleepingBarber(String barberName) {
		maxSeats = 3;
		totalCustomers = 10;
		moreCustomers = true;
		customersWaiting = new ArrayList<Customer>();
		barberLock = new ReentrantLock();
		sleepingCondition = barberLock.newCondition();
		this.barberName = barberName;
		this.start();
	}
	public synchronized boolean addCustomerToWaiting(Customer customer) {
		if (customersWaiting.size() == maxSeats) {
			return false;
		}
		Util.printMessage("Customer " + customer.getCustomerName() + " is waiting");
		customersWaiting.add(customer);
		String customersString = "";
		for (int i=0; i < customersWaiting.size(); i++) {
			customersString += customersWaiting.get(i).getCustomerName();
			if (i < customersWaiting.size() - 1) {
				customersString += ",";
			}
		}
		Util.printMessage("Customers currently waiting: " + customersString);
		return true;
	}
	public void wakeUpBarber() {
		try {
			barberLock.lock();
			sleepingCondition.signal();
		} finally {
			barberLock.unlock();
		}
	}
	public void run() {
		while(moreCustomers) {
			while(!customersWaiting.isEmpty()) {
				Customer customer = null;
				synchronized(this) {
					customer = customersWaiting.remove(0);
				}
				customer.startingHaircut();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					System.out.println("ie when " + barberName + " cutting customer's hair" + ie.getMessage());
				}
				customer.finishingHaircut();
				Util.printMessage(barberName + " is checking for more customers...");		
			}
			try {
				barberLock.lock();
				Util.printMessage("No customers, so time for " + barberName + " to sleep...");
				sleepingCondition.await();
				Util.printMessage("Someone woke " + barberName + " up!");
			} catch (InterruptedException ie) {
				System.out.println("ie while sleeping: " + ie.getMessage());
			} finally {
				barberLock.unlock();
			}
		}
		Util.printMessage("All done for today!  Time to go home!");
		
	}
	public static void main(String [] args) {
		SleepingBarber sb = new SleepingBarber("Baber1");
		ExecutorService executors = Executors.newCachedThreadPool();
		for (int i=0; i < sb.totalCustomers; i++) {
			Customer customer = new Customer(i, sb);
			executors.execute(customer);
			try {
				Random rand = new Random();
				int timeBetweenCustomers = rand.nextInt(2000);
				Thread.sleep(timeBetweenCustomers);
			} catch (InterruptedException ie) {
				System.out.println("ie in customers entering: " + ie.getMessage());
			}
		}
		executors.shutdown();
		while(!executors.isTerminated()) {
			Thread.yield();
		}
		Util.printMessage("No more customers coming today...");
		sb.moreCustomers = false;
		sb.wakeUpBarber();
	}
}
