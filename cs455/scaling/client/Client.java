package cs455.scaling.client;
import cs455.scaling.manager.*;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;


public class Client implements Runnable 
{
	private InetAddress address;
	private String serverIP;
	private int port;
	private int messageRate;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(40);
	private List waitingResponseList = new LinkedList();
	private int sendCount;
	private int receivedCount;
	private SocketChannel socketChannel;

	private ArrayList<String> hashArray;

	public String SHA1FromBytes(byte[] data) {

		MessageDigest digest = null;
		try{
		digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e)
		{
			System.out.println("Hash Error");
		}
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		return String.format("%40s", hashInt.toString(16)).replaceAll(" ", "0");
	}
	
	private void makeConnection() throws IOException 
	{
		selector = Selector.open();
		address = InetAddress.getByName(serverIP);
		InetSocketAddress hostAddress = new InetSocketAddress(address, port);
		socketChannel = SocketChannel.open(hostAddress);
		socketChannel.configureBlocking(false);

	
		socketChannel.register(this.selector, SelectionKey.OP_READ);
	}

	public void sendReceiveManager()
	{
		while(true)
		{
			sendMessages();
			listenChannel();

			try{
				Thread.sleep(messageRate);
			} catch (InterruptedException e) {
				System.out.println("Interruptered Sleep");
			}
		}
	}
	public void sendMessages()
	{
		byte [] message = new byte[8192];
		new Random().nextBytes(message);
		addHash(SHA1FromBytes(message));
		ByteBuffer buffer = ByteBuffer.wrap(message);
		try{
			socketChannel.write(buffer);
			//System.out.println(Arrays.toString(message));
		} catch (IOException e) {
			System.out.println("Error sending message");
		}
		sendCount++;

	}

	public void listenChannel()
	{
		try {
			if(selector.select(10) >= 0)//If there is something to select. Time out .01 seconds
			{
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while( selectedKeys.hasNext()) 
				{
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if(!key.isValid()) 
					{
						continue;
					}

					else if(key.isReadable())
					{
						try{
							readMessage(key);
						} catch (IOException e)
						{
							System.out.println("Error writing: Buffer must be full.");

						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Selector Error");
			return;
		}
	}

	public void readMessage(SelectionKey key) throws IOException 
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		readBuffer.clear();

		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			System.out.println("Error reading message");
			key.cancel();
			socketChannel.close();
			return;
		}

		if(numRead == -1)
		{
			System.out.println("Error reading message 2");
			key.channel().close();
			key.cancel();
			return;
		}

		if(numRead < 40)
		{
			System.out.println("Message not fully read");
		}
		byte[] dataCopy = new byte[numRead];
		System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
		String hash = new String(dataCopy);

		removeHash(hash);

	}
	public void removeHash(String hash)
	{
		if(hash.length() != 40)
		{
			System.out.println("Incorrect hash size");
		}
		boolean value = hashArray.remove(hash);
		if(value == false)
		{
			System.out.println("Error removing hash: " + hash);
		}else{
			receivedCount++;
			//System.out.println("Hash Removed successfully");
		}
		// if(hashArray.size() < 3)
		// 	{
		// 		for(int i = 0; i < hashArray.size(); i++)
		// 		{
		// 			System.out.println("In list: " + hashArray.get(i));
		// 		}
		// 	}
	}

	public void addHash(String hash)
	{
		//System.out.println("Adding hash " + hash);
		hashArray.add(hash);
	}

	public Client(String serverHost, int serverPort, int messageRate)
	{
		serverIP = serverHost;
		port = serverPort;
		this.messageRate = 1000 / messageRate;
		hashArray = new ArrayList<String>();
		sendCount = 0;
		receivedCount = 0;
		System.out.println("Client Started.");
		System.out.println("serverHost: " + serverHost);
		System.out.println("serverPort: " + serverPort);
		System.out.println("messageRate: " + messageRate);
	}

	public int getSendCount()
	{
		return sendCount;
	}

	public int getReceivedCount()
	{
		return receivedCount;
	}

	public void run()
	{
		System.out.println("Started Thread");
		try{
			makeConnection();
		} catch (IOException e) {
			System.out.println("Error making connection");
		}
		sendReceiveManager();
	}

	public static void main(String [] args) 
	{
		if(args.length != 3)
		{
			System.out.println("Error: Invalid input paramters. host port messageRate");
			return;
		}

		Client client = null;
		client = new Client(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]));
		Thread clientThread = new Thread(client);
		clientThread.setDaemon(true);
		clientThread.start();
		
		//Prints info evey 10 seconds...
		Scanner keyboard = new Scanner(System.in);

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		while(true)
		{
			try{
			Thread.sleep(10000); //Sleep 10 seconds before printing
			} catch (InterruptedException e)
			{
				System.out.println("Sleep error");
			}
			//System.out.println("Thread is alive: " + clientThread.isAlive());
			System.out.println("[" + dateFormat.format(date) + "] Total Sent Count: " + client.getSendCount() + ", Total Received Count: " + client.getReceivedCount());
		}
		
	}
}