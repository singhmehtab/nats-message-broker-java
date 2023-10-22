# nats-server

This is a java implementation of nats server which supports some of the functionality of the nats server.

Features Present
1. Connect - This will be used as the first command when you connect to the server. It only supports one functionality, which is verbose.
   1. `CONNECT {verbose: true}`
   2. if set to true you will get response from the server for your message. all other keys passed would be ignored for now.
2. SUB - This command can be used to subscribe to a topic to receive all the messages which would be publisehd to it.
   1. `SUB <topic Name> <subscription id>`
   2. To subscribe to a topic named test with subscription id as 1, you can run the below command
   3. `SUB TEST 1`, and you will reveive +OK if verbose is set to true.
3. PUB - This command can be used to publish a message to a given topic.
   1. `PUB <topic name> <msg size>` Now press enter and write message in the next line
   2. `<Message>`
   3. Or you can give it in one line as `PUB <topic name> <msg size>\r\n<Message>`
4. UNSUB - This command can be used to unsubscribe from a given subscription
   1. `UNSUB <subscription id>`

## Zero allocation byte parser
While building the project, special emphasis has been given to limit the amount of garbage collection wherever possible.
This will allow the server to process more messages and use less resources because of less garbage collection.
Even though we have not created fully zero allocation byte parser, an effort has been made to create temporary variables to as less as possible.

## Running the jar
You can run the jar with the following command
`java -jar nats-java-server-1.0-SNAPSHOT.jar`

After the server has run, run this command to connect to the server
`telnet localhost 4222`
