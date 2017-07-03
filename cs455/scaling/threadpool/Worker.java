package cs455.scaling.threadpool;
import cs455.scaling.manager.*;
import cs455.scaling.task.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;


public class Worker implements Runnable
{
	private ThreadTaskManager manager;
	private int workerNumber;

	public Worker(int number, ThreadTaskManager manager)
	{
		this.manager = manager;
		workerNumber = number;
		System.out.println("Worker " + number + " is starting");
	}

	private void processTask(Task task)
	{
		//System.out.println("[" + workerNumber +"] New Task Received. Processing Next task");

		switch(task.getType())
		{
			case Task.READ_TASK:
				//System.out.println("[" + workerNumber +"] Read Task Received");
				try{
					readMessage(task.getKey());
				} catch (IOException e){
					System.out.println("Error reading input");
				}
				break;

			case Task.HASH_TASK:
				//System.out.println("[" + workerNumber +"] Hash Task Received");
				hashMessage(task.getData(), task.getChannel());
				break;

			case Task.SEND_TASK:
				//System.out.println("[" + workerNumber +"] Hash Task Received");
				writeMessage(task.getData(), task.getChannel());
				break;

			default:
				System.out.println("Default");
		}
	}
	public void run()
	{
		while(true)
		{
			Task nextTask = manager.getNextTask(workerNumber);
			processTask(nextTask);
		}
	}

	public void readMessage(SelectionKey key) throws IOException 
	{//This will need to be turned into a task later...
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(8192);

		int numRead = 0;
		while(numRead < 8192){
			//readBuffer.rewind();
			//System.out.print("Not"); //Still waiting for message to fully sent
			try {
				numRead += socketChannel.read(readBuffer);
			} catch (IOException e) {
				System.out.println("[" + workerNumber +"] error reading message");
				key.cancel();
				manager.decremenentConnectionCounter();
				socketChannel.close();
				return;
			}

			if(numRead == -1)
			{
				System.out.println("[" + workerNumber +"] error reading message 2");
				manager.decremenentConnectionCounter();
				key.channel().close();
				key.cancel();
				return;
			}
			if(numRead < 8192)
			{
				try{
					Thread.sleep(100); //Wait for numRead to be filled.
				} catch (InterruptedException e) {
					System.out.println("Sleep error");
				}

			}

		}
		// socketChannel, this.readBuffer.array(), numRead); 
		byte[] dataCopy = new byte[8192];
		readBuffer.position(0);
		readBuffer.get(dataCopy, 0, 8192);
		//System.arraycopy(readBuffer.array(), 0, dataCopy, 0, 8192);
		//System.out.println("[" + workerNumber +"] Message Received");
		//System.out.println(Arrays.toString(dataCopy));
		Task newTask = new Task(2, dataCopy, socketChannel);
		//System.out.println("[" + workerNumber +"] Added a Hash Task");
		manager.addTask(newTask);
		//System.out.println("[" + workerNumber +"] Removing key attachment");
		key.attach(null);
	}

	public void hashMessage(byte[] data, SocketChannel channel)
	{
		//System.out.println(data.length);
		//System.out.println("Received: " + Arrays.toString(data));
		String hash = SHA1FromBytes(data);
		//System.out.println("[" + workerNumber +"] Hash: " + hash);
		Task newTask = new Task(Task.SEND_TASK, hash.getBytes(), channel);
		manager.addTask(newTask);

	}

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

	public void writeMessage(byte[] data, SocketChannel channel) 
	{
		try{
			//System.out.println("[" + workerNumber +"] Writing Data for hash: " + new String(data));
			ByteBuffer writeBuffer = ByteBuffer.wrap(data);
			//System.out.println(data.length);
			channel.write(writeBuffer);
		} catch (IOException e)
		{
			System.out.println("[" + workerNumber +"] Write Error");

		}
	}
}