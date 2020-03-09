/*
	Blockchain
	2 PM, 21st July 2019
	Client
*/

// Note: I've "String" even in place of integer values and that is to provide simplicity for my programming and nothing else.

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

class Star extends Thread{
	public void run()
		{
		System.out.println("****************");
		}
}

public class BlockchainClient{
/*
	** Note that for people without Aadhar card the system shall ask them for Election ID and their Name or some other parameter.

*/	Socket socket= new Socket("localhost", 9999);

	public static String systemId, checkSystemId, aadharNumber, mobileNumber;
	public static String timeOfChoice, oneTimeUniqueId,  choice; 
	public static DataInputStream dis;
	public static DataOutputStream dos;
	public static Scanner sc;
	
	public BlockchainClient() throws Exception
		{
		
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

		systemId="aaaa0000aaaa0000";
		checkSystemId="aaaa0000aaaa0000";
		aadharNumber="12345678901234";
		mobileNumber="1234567890";
		timeOfChoice="123456";
		oneTimeUniqueId="aaaa0000aaaa0000";
		choice="4";
		}
	
	public static void main(String args[]) throws Exception
		{
			sc= new Scanner(System.in);
		// *** use OOP to module code
		// Assuming number of Electorial Candidates is 9 and one is "NONE"
		int numberOfCandidates = 10;
	
		// Storing Candidate informatiom sequencially in a list
		ArrayList<String> list = new ArrayList<String>();
	
		// "A", "B",... "I" are Candidates.	
		String candidate[] = new String[10];
		candidate[0] = "A";
		candidate[1] = "B";
		candidate[2] = "C";
		candidate[3] = "D";
		candidate[4] = "E";
		candidate[5] = "F";
		candidate[6] = "G";
		candidate[7] = "H";
		candidate[8] = "I";
		candidate[9] = "NONE";
			 
			// adding Candidates in the list
		for(int i=0; i<numberOfCandidates; i++)
			{
			list.add(candidate[i]);
			}
		
		try
			{

		/*
			** Socket is created from client to server with above id and socket number mentioned above
			** dis will receive our data from server, along with replies
			** dos will send our geneated data to server for storage
		*/	
		
			Boolean serverReplyForSytemId;
			// At the very start ask for SystemId
			System.out.println("Enter valid SystemId:\t");
			systemId = sc.next();
						System.out.println("Hi\t");
			dos.writeUTF(systemId);
						System.out.println("Hlo\t");
			dos.flush();
		
			serverReplyForSytemId= Boolean.parseBoolean(dis.readUTF());
			if(serverReplyForSytemId == false)
				{
				System.out.println("You Entered Wrong System Id, you will be tracked and reported.");
				return;
				}
			// aadharNumber is considered String for convinience and so is mobile number
	
			// while the reply from server is negative (i.e The user hasn't entered valid credentials) please go on executing the loop
			Boolean serverReply = true;
			// use the same systemId only for thousand votes!
			 for(int j=0; j< 1000; j++)
			 	{
				do{
					if(!serverReply)
						System.out.println("You Entered wrong credentials, please try again!!!");
						
					System.out.println("Please Enter your 12 digit Aadhar number.\n");
					aadharNumber = sc.next();
					System.out.println("Please Enter your 10 digit Mobile number.\n");
					mobileNumber = sc.next();
		
					// format of clientRequest sent to server will be
					// for now consider "00" to be a hash 
					// "00" + aadharNumber + mobileNumber
					// e.g "001122334455669988776655" where 112233445566 is aadharNumber and 9988776655 is his mobileNumber
					String clientRequest= "00" + aadharNumber + mobileNumber;
					dos.writeUTF(clientRequest);
					dos.flush();
		
					serverReply= Boolean.parseBoolean(dis.readUTF());
					// use this if latter doesn;t work (serverReply.equalsto("false")
					}while(!serverReply);
				// After user has entered valid credentials, move to below section code
		
				// *** use OOP -> GenerateOneTimeUniqueId();
				oneTimeUniqueId = null;
				for(int i=0; i<16; i++)
					{
					// for now only decimal allowed, but will change to hex in later versions
					int randomNumber = ((int)Math.random()) % 10;
					// *** use OOP ->ConvertToHex(number);
					Integer inti = new Integer(randomNumber);
					
					oneTimeUniqueId += inti.toString();
					}
				// Assume that the user will give proper choice	
				Boolean check= false;
				
				//String sequence[10];
				// show user choices
				do{
					// user had entered wrong choice
					if(check)
						System.out.println("You Entered wrong choice, try again!!!");
				
					System.out.println("Please Choose from the given Candidates\n");
					for(int i=0; i<numberOfCandidates; i++)
						{
						System.out.println(i + ". " + candidate[i]);
						}
					System.out.println("Please Enter Candidate number\n");
					choice= sc.next();
					
					check= (Integer.parseInt(choice) >= numberOfCandidates)|| (Integer.parseInt(choice) < 0);
					// we have only 10 choices i.e 0-9
				}while(check);
		
				// write userid, choice, timestamp, systemid to server 
				// eg. "11" + "abcd12341234abcd" + "5" + "101010" + "abcc11223344bcdd"
				// above equals sending "11abcd12341234abcd5101010abcc11223344bcdd" to server.
		
				String clientWriteRequest= "11" + oneTimeUniqueId + choice + timeOfChoice + systemId ;
				dos.writeUTF(clientWriteRequest);
				dos.flush();
				// Conform from server if recorded
				System.out.println("Waiting for server reply...\n");
			
				Boolean finalReply;
				finalReply = Boolean.parseBoolean(dis.readUTF());
				if(finalReply == false)
					{
					System.out.println("Something went wrong, try again!!!");
					return;
					}
				// show user his uniqueId and block where is choice is stored
				System.out.println("Your choice has been recorded, Please keep note of below things");
				System.out.println("Your unique Id is: " + oneTimeUniqueId );
				System.out.println("Your choice number is :" + choice);
				System.out.println("Your timeOfChoice  is :" + timeOfChoice);
			// Go back and take next user's choice
				}
			}
		catch(Exception e)
			{
			System.out.println(e);
			}	
		}
}

