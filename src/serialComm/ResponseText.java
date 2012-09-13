package serialComm;

import java.io.IOException;
import java.sql.SQLException;

public class ResponseText extends SerialSms {
	private String returnCode;
	private String action;
	private String destinationMobileNumber;
	private String date;
	private String time;
	private String actualData;
	private Message m1;
	
	public ResponseText(String response) {
		String readyMessage=null;
		
		int pos;
			try {
			System.out.println("Inside Response Text :");
			System.out.print("<Response >"+response);
			System.out.print("</Response>");
//			System.out.println(response);
			String[] datas = response.split(",");
			String tempMessage;
			
			/* Actual CleanUp Code
			 * ---------------------
			 * It formats the output from device and removes the square boxes from 
			 * Device Response
			 */
			for(int i=0;i<datas.length;i++) {
				if(datas[i].indexOf("\r\r\n")>0) {
					pos = datas[i].indexOf("\r\r\n");
					datas[i] = datas[i].substring(pos+3,datas[i].length());
					datas[i]=datas[i].replace(" ","#");
					datas[i]=datas[i].replace("\""," ");
					datas[i]=datas[i].trim();
					datas[i]=datas[i].replace("#"," ");
				}
				else {
					datas[i]=datas[i].replace(" ","#");
					datas[i]=datas[i].replace("\""," ");
					datas[i]=datas[i].trim();
					datas[i]=datas[i].replace("#"," ");
					
				}
			}	
			
			/* Output Array in readable format like as :
			 * Datas[0] : Zero
			 * Datas[1] : One
			 * 
			 * and so on
			 */
			readAble(datas);
			
			System.err.println("Debug Mode");
			
			m1 = new Message();
			
			if(datas[0].equals("RING")) {
				System.out.println("New Call Received.");
			}
			
//			datas[0] = "+CMT:  +9779808405223";
//			datas[1]= "";
//			datas[2]= "10/03/07";
//			datas[3]= "15:13:49+23 BAL c123 1234";
				
			if(datas.length>3) {
				if(datas[0].indexOf("CMT")>0) {
					
						String[] num;
						num = datas[0].split(":");
						action = num[0];
						destinationMobileNumber = num[1];
						System.out.println("destinationMobileNumber : "+destinationMobileNumber);
						trimArray(num);					// Trims the array
						tempMessage = datas[3].substring(12,datas[3].length());
						System.out.println("Message Requested from M1 : "+tempMessage);
						m1.setMessage(tempMessage);
						m1.setDestinationNumber(destinationMobileNumber.trim());
						m1.debug();
					
//						readyMessage = prepareMessage(m1.getIncomeAccountNumber(),m1.getIncomePincode(),m1.getAction());
						readyMessage = prepareMessage(m1);
						System.out.println(" <ReadyMessage> " + readyMessage);
						try {
							
							//Reply with message;
							if(readyMessage !=null) {
								sendReplyMessage(destinationMobileNumber,readyMessage);
							}
							else {
								System.out.println(" Ready Message is Null ! Deferred Sending. ");
								System.out.println(" Requested Action : ");
								System.err.println(m1.getAction());
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
				}
				
			}
		}catch(StringIndexOutOfBoundsException e) {
			e.getCause();
			e.getMessage();
			e.printStackTrace();
//			String datas = " ";
		}

}
	
	/*Prepare Message to Reply */

	/**
	 * Compose the message independently, it works for all types of message, since the operationCode is already bound with the 
	 * original message, and extracted during the message composition. Thus, we can use single prepareMessage() function everywhere,
	 * removes lots of headaches.
	 * @param <code>sourceMessage</code> Message that contains the data from which the message is composed, eg: message destination, 
	 *          message action,
	 */
	public synchronized String prepareMessage(Message sourceMessage) {
		System.err.println("Inside prepareMessage");
		JdbcConnect jb = new JdbcConnect();
	
		String replyMessage = null;
		
		try {
				BalanceMessage bm = new BalanceMessage();
				bm.debug();		/*Debug the balance message object*/
				
				String accountNumber = sourceMessage.bm.getIncomeAccountNumber();
				String accountPincode = sourceMessage.bm.getIncomePincode();
				System.out.println("SourceMessage Action : " + sourceMessage.getAction());
				
				System.out.println("accountNumber : "+accountNumber);
				System.out.println("accountPincode : "+accountPincode);
				
				System.out.println("m1.getdestinationNumber() : "+m1.getdestinationMobileNumber());
				
				
				String partialNumber = m1.getdestinationMobileNumber();
				partialNumber = partialNumber.substring(4,partialNumber.length());	/*Removes the +977 from starting of mobileNumber */
				System.out.println("partialNumber : "+partialNumber);
				
				
				switch(sourceMessage.getAction()) {
				case BAL:
					System.out.println("Inside Balance Case :");
					/* Do we have the valid user ? */
//					if(jb.isValidUser("a123","12345")) {
						if(jb.isValidUser(accountNumber,accountPincode)) {
							System.out.println("<Valid User>");
							System.out.println(" Processing Request ");
							String currentBalance = jb.getNewBalance(partialNumber, m1.bm.getIncomeAccountNumber(), m1.bm.getIncomePincode());
//							String currentBalance = jb.getNewBalance("9808405223", "c123","12345");
							if(currentBalance == null) {
//								replyMessage = null;
								return replyMessage;
							}else {
								replyMessage = "Account "+accountNumber+" has Rs. "+currentBalance+" balance.";
							}
						}
						else
						{
							System.out.println("Account Number / Pincode Invalid ");
						}
						return replyMessage;
				case CHP:
					try {
						System.out.println("<Inside SwitchCase : CHP>");
						System.out.println("Number : "+sourceMessage.getdestinationMobileNumber());
												
						if(jb.isRegistered(sourceMessage.getdestinationMobileNumber())) {
							System.out.println("Mobile number is registered. ");
							
							System.out.println(sourceMessage.cpm.getOldPincode());
							sourceMessage.cpm.debug();
							Boolean isPincodeChanged = jb.changePincode(sourceMessage.getdestinationMobileNumber(),sourceMessage.cpm.getOldPincode(),sourceMessage.cpm.getNewPincode());
							
							System.out.println("state of isPincodeChanged : "+isPincodeChanged);
							
							if(isPincodeChanged) {
								replyMessage = "Your New Pincode is "+sourceMessage.cpm.getNewPincode()+".";
								System.out.println("replyMesssage : "+replyMessage);
							}else {
								System.err.println("Error during pincode update, Did you give the correct information.");
							}
						}
						else {
							System.out.println("Mobile Number is not registered for Change Pin code facility.");
						}
					}catch(NullPointerException ne) {
						//
						ne.printStackTrace();
						System.out.println(ne.getMessage());
					}
					return replyMessage;
				case STMT:
					break;
				case TR:
					break;
				
				}
			}
			
		 catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			}
		return replyMessage;
		}
	
	/*Trims the Array Content and removes the spaces */
	public  void trimArray(String[] s) {
		for(int k=0;k<s.length;k++) {
			s[k]= s[k].trim();	
		}
		readAble(s);
	}

	
	/*Generates the readAble output of An array
	 * Data[0] : Zero
	 * Data[1] : One
	 * 
	 * */
	public void readAble(String[] s) {
		for(int k=0;k<s.length;k++) {
			
			//+CMT:  +9779808449651
			System.out.println("Datas["+k+"] : "+s[k]);
		}
	}
	
	private void setOnlyMessage() {
		int p;
		p = response.indexOf("+20\"");
		actualData = response.substring(p+4,response.length());
		actualData = actualData.replace("\n"," ");
		actualData = actualData.replace("\r"," ");
		actualData = actualData.trim();
	}
	
	public String getReturnCode() {
		return this.returnCode;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public String getNumber() {
		return this.destinationMobileNumber;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String getActualData() {
		return this.actualData;
	}
	

   
}
