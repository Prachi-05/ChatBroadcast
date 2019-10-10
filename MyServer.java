import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

class MyServer
{
    ArrayList al=new ArrayList();
    ArrayList users=new ArrayList();
    ServerSocket ss;
    Socket s;

    public final static int PORT=3000;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="logmeout:";
    public MyServer()
    {
        try
        {
            ss=new ServerSocket(PORT);
            System.out.println("Server Started "+ss);
            while(true)
            {
                s=ss.accept();
                Runnable r=new MyThread(s,al,users);
                Thread t=new Thread(r);
                t.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("Server constructor"+e);
        }
    }
    public static void main(String [] args)
    {
        new MyServer();
    }
}

class MyThread implements Runnable
{
    Socket s;
    ArrayList al;
    ArrayList users;
    String username;
    String password;
    MyThread (Socket s, ArrayList al,ArrayList users)
    {
        this.s=s;
        this.al=al;
        this.users=users;
        try
        {
            DataInputStream dis=new DataInputStream(s.getInputStream());
            DataOutputStream dos=new DataOutputStream(s.getOutputStream());
            username=dis.readUTF();
            password=dis.readUTF();
            //System.out.print(username+" "+password);
            boolean check = login(username, password);
            //System.out.println(check);
            if(check)
            {
                dos.writeUTF("valid");
                al.add(s);
                users.add(username);
                tellEveryOne("****** "+ username+" Logged in at "+(new Date())+" ******");
                sendNewUserList();
            }
            else
                dos.writeUTF("invalid");
        }
        catch(Exception e)
        {
            System.err.println("MyThread constructor  "+e);
        }
    }
    public boolean login(String uname, String pass)
    {
        Connection con;
        Statement stmt;
        ResultSet rs;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "alohomora");
            stmt = con.prepareStatement("select * from user;");
            rs = stmt.executeQuery("select * from user where uname='"+uname+"' and pass ='"+pass+"';");
            if(rs.next())
            {
                return true;
            }
            else
                return false;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public void run()
    {
        String s1;
        try
        {
            DataInputStream dis=new DataInputStream(s.getInputStream());
            do
            {
                s1=dis.readUTF();
                if(s1.toLowerCase().equals(MyServer.LOGOUT_MESSAGE)) 
                    break;
                tellEveryOne(username+" said: "+" : "+s1);
            }
            while(true);
            DataOutputStream tdos=new DataOutputStream(s.getOutputStream());
            tdos.writeUTF(MyServer.LOGOUT_MESSAGE);
            tdos.flush();
            users.remove(username);
            tellEveryOne("****** "+username+" Logged out at "+(new Date())+" ******");
            sendNewUserList();
            al.remove(s);
            s.close();
        }
        catch(Exception e)
        {
            System.out.println("MyThread Run"+e);
        }
    }
    public void sendNewUserList()
    {
        tellEveryOne(MyServer.UPDATE_USERS+users.toString());
    }

    public void tellEveryOne(String s1)	
    {
        Iterator i=al.iterator();
        while(i.hasNext())
        {
            try
            {
                Socket temp=(Socket)i.next();
                DataOutputStream dos=new DataOutputStream(temp.getOutputStream());
                dos.writeUTF(s1);
                dos.flush();
            }
            catch(Exception e)
            {
                System.err.println("TellEveryOne "+e);
            }
        }
    }
}
