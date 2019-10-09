# ChatBroadcast
- Run MyServer.java
- Run MyClient.java
You can open several Clients but they need to be on the same network.
 
> Change the IP Address of Server in clientChat() 
```
s=new Socket(InetAddress.getByName("Your_IP"),MyServer.PORT);
```
> Create a database to store usernames and passwords and change database details in login() 

```
con = DriverManager.getConnection("jdbc:mysql://localhost:3306/<database name>", "<user>", "<password>");
```
