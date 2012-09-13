package serialComm;
import javax.comm.*;
import java.io.*;
import java.util.*;

public class SerialSms implements Runnable{
	private static final String PORTNAME = "COM1";
	protected static final int LONGDELAY = 5000;
	protected static final int SHORTDELAY = 1000;
	protected static final int MEDIUMDELAY = 3000; 
//	private static final int TIMEOUTSECS = 20;
	protected static final int BAUD = 115200;
	private static final int SERIAL = 1;
	private static final int MAX_CHARS = 162;
	private static InputStream is;
	private static PrintStream os;
	
	private static final char ctrlZ=(char)26;
	
	private boolean debug = false;
	String response;
	private static CommPortIdentifier cpi;
	private CommPort cp;
	private static SerialPort myPort;
	
	ResponseText responseText;
	/**
	 * @param args
	 */
	String pName;
	Integer pType;
	public void getAllPorts() throws UnsupportedCommOperationException, IOException, InterruptedException {
		
		/*Enumerate all the available ports */
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();
	
		System.err.println("Detectecting Ports ...");
		while(pList.hasMoreElements()) {
			cpi = (CommPortIdentifier)pList.nextElement();
			
			pName = cpi.getName();			// Name of Port	
			pType = cpi.getPortType();		// Type of Port
			if(debug) {
				System.out.println("Port Name : "+pName);
				System.out.println("Port Type : "+pType);
				System.out.println();
			}
			
			if(pName.equals(PORTNAME) && pType == SERIAL) 
				try {
					
					System.out.print("Using Port : " +pName);
					System.out.println();
					cp = cpi.open("Sms Banking System",SHORTDELAY);
					myPort = (SerialPort) cp;
					myPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				}catch (PortInUseException p) {
					System.err.println("Port in Use By Other Programs.");
					System.exit(0);
				}	
		}
	}
	
	public void run() {
		
	}
	
	
	protected synchronized void sendCommand(String s) throws IOException, InterruptedException {

        if (debug) {
            System.out.print(">>> ");
            System.out.print(s);
            System.out.println();
        }
        try {
        	os = new PrintStream(myPort.getOutputStream(),true);
            os.print(s);			//Send Command to Modem
            os.print("\r\n");		//Acts like as if we hit the enter key
              
            
        	is = myPort.getInputStream( );  
        	System.err.println("(Wait) Buffering InputStream ... ");
        	Thread.sleep(LONGDELAY);

        	byte[] readBuffer =  new byte[MAX_CHARS];
        	int numbytes =is.read(readBuffer);
        	
            response 		= new String(readBuffer);
            responseText 	= new ResponseText(response);
            
            // Expect the modem to echo the command.

            if (!expect(s)) {
                System.err.println("WARNING: Modem did not echo command.");
            }

        }catch(IOException e) {
        	System.err.print(e.toString());
        }
        
    }

	protected boolean expect(String exp) throws IOException {
        if (debug) {
            System.out.print("<<< ");
            System.out.print(response);
            System.out.println( );
        }

        return response.indexOf(exp)>= 0;
    }
	
	/* Are we getting the desired output */
	protected Boolean parseMsg(String expectText) {
		if(response.indexOf(expectText)> 0)
			return true;
		else 
			return false;
	}
	
	/* Initiate the input Stream */
	public void initialize() throws IOException, InterruptedException {
			os.flush();
			is = myPort.getInputStream( );  

			byte[] readBuffer =  new byte[ MAX_CHARS ];
           
            int numbytes =is.read(readBuffer);
            response = new String(readBuffer);
            
            /* Filterered Response from Device Output */
            response = lowPassFilter(response);
            
            /* Store the Device Response to ResponseText */
            responseText = new ResponseText(response);            
		
	}
	
	
	/*Discard the unwanted input Streams from Modem Inputs ( Response )*/
	 public String lowPassFilter(String s) {
	    	int c;
	    	StringBuffer fs = new StringBuffer();
	    	for(int i=0;i<s.length();++i) {
	    		c = s.charAt(i);
	    		
	    		if( c==13 || c ==10 || c == '\b' || c==0 ) {
	    			/*Ignore the carriage return [HEX : 13], new line[10], etc.
	    			 * c == 0 removes the square that is produced at output of modem due to the 
	    			 * delay in output
	    			 */
	    		}
	    		else {
	    			
	    			fs.append((char)c);	/*Otherwise build the string, which is cleaned*/ 
	    		}
	    	}
	    	
	    	/*Finally convert it to string */
			String f = fs.toString();
			
			/*Give it back to them who ask for it*/
	    	return f;
	    }

	 
	 /*Send Message Modified */
		protected synchronized void sendReplyMessage(String destinationNumber,String replyMessage) throws InterruptedException, IOException {
			try {
				String toMobileNumber = destinationNumber.trim();	/*Destination Mobile Number */
				
				/*Initialize the modem to send message */
				String setTextMode = new String("AT+CMGF=1");
				
				/*Set the destination number with AT command */
				String setNumber = new String("AT+CMGS=\"" + toMobileNumber + "\"");
				if(debug) {
					System.err.print("Send Message to Number : " + setNumber);
//					System.out.println("<<<"+command);				
				}
				
				/*Convert the mode to textmode to type our message */
				sendCommand(setTextMode);
				
				/*Give sometime to modem for processing */
				Thread.sleep( SHORTDELAY / 2 );
				
				/*Are we getting the desired output*/
				Boolean checkState = parseMsg("OK");
				if(checkState) {
					
					if(debug) System.out.println("OK is Returned");
					
					sendCommand(setNumber);
					checkState = parseMsg(">");
					
					if(debug) System.out.println("Typing ... ");
					
					if(checkState) {
						os.flush();	/* First Clear the buffer, before typing into it */

						os.print(replyMessage+ctrlZ);		/* Actual Composed Message to Send */
						
						
						os.print("\r\n");	/* This is required, I don't know why, */
						is = myPort.getInputStream( );  
			        	if(debug) System.err.println("(Wait) Buffering Text ... ");
			        	Thread.sleep( SHORTDELAY );
			            byte[] readBuffer =  new byte[ MAX_CHARS ];
			           
			            int numbytes =is.read(readBuffer);
			            response = new String(readBuffer);
			            
						System.err.println(response);
					}
					else
					{
						System.out.println("Error Occured");
					}					
				}
			}catch(IOException e) {
				System.out.println("Exception Thrown : ");
				System.err.println(e.toString());
				
			}
			catch(InterruptedException e) {
				System.out.println("Interrupted Exception Thrown : ");
				System.err.println(e.toString());
			}
		}
		
	public static void main(String[] args) throws UnsupportedCommOperationException, IOException, InterruptedException {
	
		SerialSms serial = new SerialSms();
		
		/*Get all available Ports in computer */
		serial.getAllPorts();
		
		/*Send Command to Device For Device Initialization*/
//		System.out.println("Initiating the modem ");
		serial.sendCommand("AT+CNMI=1,2,0,0,0");
//		serial.sendCommand("AT+CMGR=2");
		while(true) {
			serial.initialize();
//			serial.expect("OK");
			is.close( );
			os.close( );
		}
	    
	}
	
	
	
}
