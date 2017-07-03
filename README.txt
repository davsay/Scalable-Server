Author: David Sahud
Date: 3/7/17

Project Description:

—————————————————————————————————————————————————————————————————————————————————————————————
To Run:
	Server.java: java cs455.scaling.server.Server <Port> <Number of Worker Threads>
	Client.java: java cs455.scaling.client.Client <Server Adress> <Server Port> <Message Rate>

—————————————————————————————————————————————————————————————————————————————————————————————
File Descriptions:

	cs455.scaling.client:
		Client.java - Connects with Server. Client which sends randomized byte arrays of size 8192 to Server. When server returns hashed value, Client checked to see if the hash matches the orginal message.

	cs455.scaling.server:
		Server.java - Server that Clients connect to. Spawns ThreadTaskManager which takes all in comming messages from Clients and sends back a hash of the message.

	cs455.scaling.manager:
		ThreadTaskManager.java - Creates n Worker threads it deligates read, hash, and write tasks to.

	cs455.scaling.util:
		AttachedObject.java - Empty object that is attached to Keys to prevent duplicate task from being added.

	cs455.scaling.task:
		Task.java - Task object that contains the task type (Read, Hash, or Write) and contains the nessisary information to complete the task (Channel, Data, etc.)

	cs455.scaling.threadpool:
		Worker.java - Worker Threads read, hash or write task from the ThreadTaskManager can does completes the task. Adds a new task to ThreadTaskManager if nessisary.
		
