package serialComm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class JdbcConnect {
	private Connection c; 
	Statement stmt = null;
    ResultSet rs = null;

    /*Connection Function to make database Connection*/
    public Connection dbCon() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbctest?user=root&password= "); 
		return conn;
		
	}
    
    public void dbClose() throws SQLException {
    	stmt.close();
    	rs.close();
    	c.close();
    }
    /*Checks if user is registered in database.
     * If registered returns True otherwise False
     * 
     */
	public boolean isValidUser(String accountNumber,String accountPincode) throws SQLException {
		boolean isRegistered = false;
		c = dbCon();
		
		try {
			
			/*Get the accountnumber and accountpincode and check against the database */
			String st = "SELECT accountnumber,accountpincode FROM customer WHERE accountnumber =  '"+accountNumber +"' AND accountpincode =  '"+accountPincode +"'";
			
			//Create Statement
    	    stmt = c.createStatement();
    	    
    	    //Execute the query
    	    rs = stmt.executeQuery(st);
    	    
    	    if (stmt.execute(st)) {
    	    	
    	    	//Get the resultset
    	        rs = stmt.getResultSet();
    	        
    	        //Iterate throught the resultset and retrieve the data
    	        while(rs.next()) {
    	        	isRegistered = true;			/*User is Registered */
    	        	String res = rs.getString(1);
//    	        	System.out.println(res);
    	        }
    	    }
  
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		return isRegistered;		
	}
	
	   protected boolean isRegistered(String sourceNumber) {
		   Connection c1;
		   JdbcConnect j2 = new JdbcConnect();
		   Statement stmt1;
		   ResultSet rs1;
	    	boolean registered = false;
	    	String hasSms = null;
	    	try {
				c1 = dbCon();
				System.out.println("<Inside isRegistered >");
				System.out.println("sourceNumber : "+sourceNumber);
				String 	st = "SELECT customerrecord.hassms FROM customerinformation Inner Join customerrecord ON customerinformation.id = customerrecord.customerinformationid WHERE customerinformation.phone = '"+sourceNumber.substring(4,sourceNumber.length())+"'";
				System.out.println(st);
				stmt1 = c1.createStatement();
	    	    rs1 = stmt1.executeQuery(st);
	    	    if (stmt1.execute(st)) {
	    	        rs1 = stmt1.getResultSet();
	    	        while(rs1.next()) {
	    	        	hasSms = rs1.getString("hassms");
	    	        }
	    	    }
	    	    
	    	    try {
	    	    	if(hasSms.equals("yes")) {
//		        		System.out.println("Registered With Us");
		        		registered = true;
	    	    	}
		        		else {
		        			registered = false;
		        		}
		        	}catch(NullPointerException ne) {
		        		System.out.println("Null Pointer Reached.");
		        	}
			} catch (SQLException e) {
				System.out.println("Error :: Cannot Connect to Database Server. Please check your username and password.\r\n Is Your Server Running ?");
//				e.printStackTrace();
			}
	    	return registered;
	    }
	   
	   public synchronized boolean getRegistrationDetails(String sourceNumber) {
			boolean state = false;
			JdbcConnect j1 = new JdbcConnect();
			
			try {
				if(j1.isRegistered(sourceNumber)) {
					state = true;
				}
				else {
					state = false;
				}
			} finally {
				//Do nothing
			}
			return state;
		}
	   
	   /**
	     * Returns the Balance of the accountNumber for which request is made. 
	     * @param accountNumber accountNumber for which the balance query request is made.
	     * @return the balance of the accountNumber making sms query request. */
	    
	    protected double getBalance(String accountNumber) throws SQLException {
	    	double balance=0;
	    	c= dbCon();
	    	
	    	try {
	    		String 	st = "SELECT customer.accountnumber, depositor.balance FROM customer Inner Join depositor ON depositor.customerid = customer.id where customer.accountnumber='"+accountNumber+"'";
				stmt = c.createStatement();
	    	    rs = stmt.executeQuery(st);
	    	    if (stmt.execute(st)) {
	    	        rs = stmt.getResultSet();
	    	        while(rs.next()) {
	    	        	balance = rs.getDouble("balance");
	    	        }
	    	    }
	    	    
	    	    
	    	}
	    	finally {
	    		//Do nothing
	    	}
	    	return balance;
	    }
	    
	    /**
		 * Updates the oldPincode with the newPincode supplied by the user.
		 * @param sourceNumber mobileNumber from which the request which made.
		 * @param oldPincode Old Pincode of the account
		 * @param newPincode New Pincode of the account
		 * @return if successfully changed returns true otherwise false.
		 * @throws SQLException 
		 */

		protected synchronized boolean changePincode(String sourceNumber,String oldPincode,String newPincode) throws SQLException {
			System.out.println("Inside ChangePincode");
			boolean isChanged = false;
			PreparedStatement ps = null;
			c = dbCon();
			
			String actualNumber = sourceNumber;
			if(isRegistered(actualNumber)) {
				try {
					System.out.println("Entered the regions for check");
					String tempCheckNum = actualNumber.substring(4,actualNumber.length());
					String 	st = "UPDATE customer,customerinformation,customerrecord SET customer.accountpincode='"+newPincode+"'";
							st += " WHERE customerinformation.id =  customerrecord.id AND customer.id =  customerrecord.customerid ";
							st += " AND customer.accountpincode='"+oldPincode+"' and customerinformation.phone = '"+tempCheckNum+"'";
					System.out.println(st);
					stmt = c.prepareStatement(st);
					int affectedRows = stmt.executeUpdate(st);
					System.out.println("AffectedRows : "+affectedRows);
					if(affectedRows == 1) {
						System.out.println("Changed the pincode Successfully");
						isChanged = true;
					}
				} catch (SQLException e) {
					e.getMessage();
//					e.printStackTrace();
				}
			}
			return isChanged;
		}
		
		public String getNewBalance(String sourceNumber,String accountNumber,String accountPincode) throws SQLException {
			System.out.println("sourceNumber : "+sourceNumber);
			PreparedStatement ps = null;
			String balances=null;
			 c= dbCon();
			 ResultSet rs = null;
			 System.out.println("Checking ...");
			 String st = "select balance from depositor where depositor.id = (SELECT customerrecord.id FROM customer";
					st += " Inner Join customerrecord ON customerrecord.customerid = customer.id ";
					st += " Inner Join customerinformation ON customerrecord.customerinformationid = customerinformation.id WHERE ";
					st += " customerrecord.hassms = 'yes' AND";
					st += " customerinformation.phone =  ? AND";
					st += " customer.accountnumber =  ? AND";
					st += " customer.accountpincode =  ?)";
			ps = c.prepareStatement(st);
			
//			ps.setString(1,"9841778373");
//			ps.setString(2,"c123");
//			ps.setString(3,"5555");
			ps.setString(1,sourceNumber);
			ps.setString(2,accountNumber);
			ps.setString(3,accountPincode);
			rs = ps.executeQuery();
			while(rs.next()) {
				balances = rs.getString("balance");
			}
			return balances;
			
		}
		
		
}