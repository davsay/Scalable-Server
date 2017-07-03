package cs455.scaling.manager;
import cs455.scaling.task.*;
import cs455.scaling.threadpool.*;
import cs455.scaling.server.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
public class ThreadTaskManager implements Runnable
{
	private LinkedList<Task> taskList = new LinkedList<Task>();
	private int threadPoolSize;
	private ArrayList<Worker> workers = new ArrayList<Worker>();
	private Server server;

	public ThreadTaskManager(int threadPoolSize, Server server)
	{
		this.threadPoolSize = threadPoolSize;		
		this.server = server;
	}

	public synchronized Task getNextTask(int workerNumber)
	{
		if(taskList.size() == 0)
		{//If list is empty, wait.
			try{
			//System.out.println("[" + workerNumber +"] Waiting for new tasks in manager...");
			wait();
			//System.out.println("[" + workerNumber +"] Notify. Getting next task.");
			return getNextTask(workerNumber);
			}catch(InterruptedException e){
				System.out.println("[" + workerNumber +"] Waiting Error");
				return null;
			}
		}
		else
		{
			//System.out.println("[" + workerNumber +"] Got Task");
			return taskList.pop();
		}
	}

	public synchronized void addTask(Task task)
	{
		//System.out.println("Task Added");
		taskList.add(task);
		notify();
	}

	public synchronized int getTaskListSize()
	{
		return taskList.size();
	}

	public void decremenentConnectionCounter()
	{
		server.decremenentConnectionCounter();
	}

	public void run() 
	{
		System.out.println("Started ThreadTaskManager Thread");
		System.out.println("Spawing Worker Threads");
		for(int i = 0; i < threadPoolSize; i++)
		{
			Worker worker = new Worker(i, this);
			Thread workerThread = new Thread(worker);
			workerThread.setDaemon(true);
			workerThread.start();
		}
	}
}