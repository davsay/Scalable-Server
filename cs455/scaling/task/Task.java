package cs455.scaling.task;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;

public class Task
{
	public final static int READ_TASK = 1;
	public final static int HASH_TASK = 2;
	public final static int SEND_TASK = 3;
	/*
	1 - The data needs to be read.
	2 - The data needs to be hashed.
	3 - The data needs to be sent.
	*/
	private int type;

	private SelectionKey key;
	private byte[] data;
	private SocketChannel channel;

	public Task(int type, byte[] data, SocketChannel channel)
	{
		this.type = type;
		this.data = Arrays.copyOf(data, data.length);
		this.channel = channel;
	}

	public Task(int type, SelectionKey key)
	{
		this.type = type;
		this.key = key;
	}

	public int getType()
	{
		return type;
	}

	public byte[] getData()
	{
		return data;
	}

	public SocketChannel getChannel()
	{
		return channel;
	}

	public SelectionKey getKey()
	{
		return key;
	}

}