package cs455.scaling.server;
import cs455.scaling.manager.*;
import cs455.scaling.task.*;
import cs455.scaling.util.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Server implements Runnable
{
	private InetAddress ip;
	private String hostname;
	private int port;
	private int threadPoolSize;

	private ServerSocketChannel serverChannel;
	private Selector selector;

	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	private ThreadTaskManager manager;
	private Thread managerThread;
	private int connectionCount = 0;
	private long messagesReceivedCount = 0;

	public Server(int port, int threadPoolSize) throws IOException
	{
		System.out.println("Started Server on port:" + port);
		this.port = port;
		this.threadPoolSize = threadPoolSize;
		try
		{
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostAddress();
	    	System.out.println("Your IP: " + hostname);
    	} catch (UnknownHostException e) {
            e.printStackTrace();
        }
        manager = new ThreadTaskManager(threadPoolSize, this);
        managerThread = new Thread(manager);
		managerThread.setDaemon(true);
		managerThread.start();

        this.selector = createSelector();

	}

	private Selector createSelector() throws IOException
	{

		Selector socketSelector = null;
		try{
		socketSelector = SelectorProvider.provider().openSelector();
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		InetSocketAddress isa = new InetSocketAddress(this.ip,this.port);
		serverChannel.socket().bind(isa);

		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		} catch (IOException e){
			System.out.println("Error creating selector. Try an open port.");
		}
		return socketSelector;
	}

	public void run()
	{
		System.out.println("Listening for new Connections");
		while(true) 
		{
			try {
				if(this.selector.select() >= 0)//If there is something to select.
				{
					//System.out.println("Top of loop");
					Iterator selectedKeys = this.selector.selectedKeys().iterator();

					while( selectedKeys.hasNext()) 
					{
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();

						if(!key.isValid()) 
						{
							continue;
						}

						if(key.isAcceptable())
						{
							this.acceptConnection(key);
						}
						else if(key.isReadable())
						{
							//If it's readable. Add it to the Thread Task Manager

							Task task = new Task(Task.READ_TASK, key);
							Object attachment = key.attachment();
							if(attachment == null)
							{//This task hasn't been added yet.
								//System.out.println("Adding read to task list");
								attachment = new AttachedObject();
								key.attach(attachment);
								manager.addTask(task);
								incrementmessagesReceivedCount();
							}
							else
							{//This task has already been add. Don't add again.
								//System.out.println("Already Attached");
							}
							//System.out.println("List size: " + manager.getTaskListSize());
						}
						else
						{
							System.out.println("Selector error");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void acceptConnection(SelectionKey key) throws IOException
	{
		incrementConnectionCount();
		System.out.println("New connection added");
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);

		socketChannel.register(this.selector, SelectionKey.OP_READ);
	}
	public synchronized int getConnectionCount()
	{
		return connectionCount;
	}

	public synchronized void incrementConnectionCount()
	{
		connectionCount++;
	}

	public synchronized void decremenentConnectionCounter()
	{
		connectionCount--;
	}

	public synchronized float getCountsPerSecond()
	{
		float time = 5; //seconds
		float  count = messagesReceivedCount / time;
		messagesReceivedCount = 0;
		return count;
	}

	public synchronized void incrementmessagesReceivedCount()
	{
		messagesReceivedCount++;
	}

	public static void main(String [] args) 
	{


		if(args.length != 2)
		{
			System.out.println("Error: Invalid input paramters. portnum thread-pool-size");
			return;
		}

		Server server = null;

		try{
			server = new Server(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
		} catch (IOException e) {
			System.out.println("Error starting server");
		}

		Thread serverThread = new Thread(server);
		serverThread.setDaemon(true);
		serverThread.start();

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		while(true)
		{
			try{
			Thread.sleep(5000); //Sleep 10 seconds before printing
			} catch (InterruptedException e)
			{
				System.out.println("Sleep error");
			}
			//System.out.println("Thread is alive: " + clientThread.isAlive());
			System.out.println("[" + dateFormat.format(date) + "] Current Server Throughput: " + server.getCountsPerSecond() + 
				" messages/s, Active Client Connections: " + server.getConnectionCount());
		}
	}
}