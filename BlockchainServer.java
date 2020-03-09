/*
	Blockchain
	2 PM, 21st July 2019
	Server
*/

// Note: I've "String" even in place of integer values and that is to provide simplicity for my programming and nothing else.
//export CLASSPATH=$CLASSPATH:/usr/share/java/mysql-connector-java.jar

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class BlockchainServer{

	static final String url="jdbc:mysql://localhost:3306/Elections?useSSL=false";
	static final String user="root";
	static final String password="root";
	
	public static void main(String args[]) throws Exception
		{
		try
			{
			int i=1;
			ServerSocket socket= new ServerSocket(9999);
			
			while(true)
				{
				Socket incoming = socket.accept();
				System.out.println("Client no. " + i + " connected!!!");
				//ThreadHandler
				Runnable runn= new ThreadedHandler(incoming);
				Thread thread= new Thread(runn);
				thread.start();
				i++;
				}
			}
		catch(Exception e)
			{
			e.printStackTrace();
			}
		}	
}

/*
	* This class handles the client input for one or more server socket connection.
*/

class ThreadedHandler implements Runnable{
	
	private Socket incoming;
	DataInputStream dis;
	DataOutputStream dos;
	/*
		*Construct a handler
		i for incoming socket
	*/
	public ThreadedHandler(Socket i) throws Exception
		{
		incoming = i;
		dis = new DataInputStream(incoming.getInputStream());
		dos = new DataOutputStream(incoming.getOutputStream());
		}
		
	public void run()
		{
		Scanner sc= new Scanner(System.in);
		try
			{
			// for each new connection following variables are newly created
			String systemId, aadharNumber, mobileNumber;
			//Boolean serverReplyForSytemId;
			
			systemId = dis.readUTF();
			
			Class.forName("com.mysql.jdbc.Driver");
			
			Connection con=DriverManager.getConnection(BlockchainServer.url, BlockchainServer.user, BlockchainServer.password);
			// On same grounds we will have a ServerIdCheckPS, which will be implemented in future version
			// add date function or constraint as well or avoid that by adding new systemid's everyday!!!
			PreparedStatement systemIdCheckPS= con.prepareStatement("select * from SystemIdTable where systemid = ?");
			systemIdCheckPS.setString(1,systemId);
			
			ResultSet systemIdRS= systemIdCheckPS.executeQuery();
			if(systemIdRS.next())
				{
				dos.writeUTF("true");
				dos.flush();
				}
			// entered wrong system id, so wrong connection
			// close the connection
			else
				{
				dos.writeUTF("false");
				// close the connection
				incoming.close();
				// destroy thread
				System.exit(0);
				// if above doesn't work, use return
				}
			// format of clientRequest sent to server will be 
			// "00" + aadharNumber + mobileNumber
			// e.g "001122334455669988776655" where 112233445566 is aadharNumber and 9988776655 is his mobileNumber
			
			// new user from below
			while(true)
				{
				Boolean repeat = true;
				// wait till proper details are verified
				do
					{
					String clientRequest= dis.readUTF();
					// substring function(starting_index, ending_index +1)
					aadharNumber = clientRequest.substring(2,14);
					mobileNumber = clientRequest.substring(14,24);
			
					PreparedStatement aadharDetailsPS= con.prepareStatement("select * from AadharDetailsTable where aadhar = ? and mobile = ? and status='N'");
			
					aadharDetailsPS.setString(1,aadharNumber);
					aadharDetailsPS.setString(2,mobileNumber);
			
					ResultSet aadharDetailsRS= aadharDetailsPS.executeQuery();
			
					if(aadharDetailsRS.next())
						{
						dos.writeUTF("true");
						repeat= false;
						}
					else
						dos.writeUTF("false");
					dos.flush();
				}while(repeat);
				// now read userid, choice, timestamp, systemid to server 
				// eg. "11" + "abcd12341234abcd" + "5" + "101010" + "abcc11223344bcdd"
				// above equals sending "11abcd12341234abcd5101010abcc11223344bcdd" to server.
				String oneTimeUniqueId, choice, timeOfChoice,checkSystemId;
				// use decode hash function here
				String clientWriteRequest= dis.readUTF();
			
				checkSystemId = clientWriteRequest.substring(25,41);
				// if possible also check time
				if(checkSystemId.equals(systemId))
					{
					oneTimeUniqueId = clientWriteRequest.substring(2,18);
					choice = clientWriteRequest.substring(18,19);
					timeOfChoice = clientWriteRequest.substring(19,25);
				
					PreparedStatement writeToTempBlockPS= con.prepareStatement("insert into TempBlock(userid,choice,time,systemid) values (?,?,?,?)");
					PreparedStatement changeElectionStatusPS= con.prepareStatement("update AadharDetailsTable SET status='Y' where aadhar=?");
				
					writeToTempBlockPS.setString(1,oneTimeUniqueId);
					writeToTempBlockPS.setString(2,choice);
					writeToTempBlockPS.setString(3,timeOfChoice);
					writeToTempBlockPS.setString(4,systemId);
				
					changeElectionStatusPS.setString(1,aadharNumber);
										
					int writeToTempBlockRS= writeToTempBlockPS.executeUpdate();
					
					int changeElectionStatusRS= changeElectionStatusPS.executeUpdate();
			
					if(writeToTempBlockRS != 0 && changeElectionStatusRS!=0)
						dos.writeUTF("true");
					else
						dos.writeUTF("false");
				
					}
				else	
					dos.writeUTF("false");
				dos.flush();
				}
			}
		catch(Exception e)
			{
			e.printStackTrace();
			}
		}
}

