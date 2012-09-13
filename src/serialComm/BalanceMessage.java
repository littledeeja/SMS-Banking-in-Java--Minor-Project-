package serialComm;

/**
 * The <code>BalanceMessage</code> object represents the message for balance query request 
 *
 *@see serialComm.ChangePincodeMessage
 */
public class BalanceMessage {

	protected String accountNumber = null;
	protected String accountPassword = null;

	/**
	 * Sets the account number for which the balance query is made.
	 * @param accountNum account number made for balance query
	 */
	public void setAccountNumber(String accountNum) {
		accountNumber = accountNum;
	}

	/**
	 * Sets the account password (pincode), that is received from the sms query request.
	 * @param accountPass account pincode used to make request for balance query in the sms.
	 */
	public void setAccountPassword(String accountPass) {
		accountPassword = accountPass;
	}

	/**
	 * Returns the account number of the Sms Query Making Request
	 * 
	 * @return accountNumber assigned to the Message object.
	 */
	public String getIncomeAccountNumber() {
		return accountNumber;
	}

	/**
	 * Returns the account pincode of the Sms Query Making Request
	 * 
	 * @return accountPincode assigned to the Message object.
	 */
	public String getIncomePincode() {
		return accountPassword;
	}

	public void debug() {
		System.out.println("<Balance debug>");
		System.out.println("Account Number : "+accountNumber);
		System.out.println("Account Password : "+accountPassword);
		
	}
}
