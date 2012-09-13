package serialComm;

/**
 * The <code> Message </code> object keeps track of received message. Let's say
 * we get message as follows
 * 
 * BAL a123 12345
 * 
 * Then the message will be stored as follows in instance of Message
 * 
 * operationCode = BAL accountNumber = a123 accountPassword = 12345
 * 
 * operationCode is check against available bankingAction and if it is found to
 * be valid then it is processed, otherwise ignored. destinationNumber is the
 * number to which the request is processed and message is replied back as per
 * the sms query request.
 */

public class Message {
	protected ChangePincodeMessage cpm = new ChangePincodeMessage();
	protected BalanceMessage bm = new BalanceMessage();
	protected BankingAction bankingAction;
	protected String fullMessage;
	protected String destinationMobileNumber;
	/**
	 * Extract the opeartionCode, accountNumber and accountPassword then sets to
	 * the local variable.
	 * 
	 * @param sourceText
	 *            Text from which extraction of operationCode, accountNumber and
	 *            accountPassword is done.
	 */
	public void setMessage(String sourceText) {
		String operationCode;
		fullMessage = sourceText;

		String[] extractedMessage = sourceText.split(" ");
		operationCode = extractedMessage[0];
		if (operationCode.equals("BAL")) {
			bm.setAccountNumber(extractedMessage[1]);
			bm.setAccountPassword(extractedMessage[2]);
			bankingAction = BankingAction.BAL;
		}
		if (operationCode.equals("TR")) {
			bankingAction = BankingAction.TR;
		}
		if (operationCode.equals("CHP")) {
			
			/*Message : CHP 12345 54321 */
			cpm.setOldPincode(extractedMessage[1]);
			cpm.setNewPincode(extractedMessage[2]);
			bankingAction = BankingAction.CHP;
		}
		if (operationCode.equals("STMT")) {
			bankingAction = BankingAction.STMT;
		}

	}

	/**
	 * Sets the destinationNumber to which the message is replied back.
	 * 
	 * @param mobileNumber
	 *        Mobile number to which the message is replied back.
	 */
	public void setDestinationNumber(String mobileNumber) {
		mobileNumber = mobileNumber.trim();
		destinationMobileNumber = mobileNumber;
	}

	/**
	 * Returns the action assigned to the Message object.
	 * 
	 * @return action to which the message is sent
	 */
	public BankingAction getAction() {
		return bankingAction;
	}
	
	public void setDestinationMobileNumber(String mobileNumber) {
		destinationMobileNumber = mobileNumber;
	}

	public String getdestinationMobileNumber( ){
		return destinationMobileNumber;
	}
	
	/** 
	 * Debug the content of Message content
	 * 
	 */
	public void debug() {
		System.out.println("Destination Mobile Number : "+destinationMobileNumber);
		System.out.println("Banking Action :" + bankingAction);
		System.out.println("Full Message : "+fullMessage);
		cpm.debug();
		bm.debug();
	}
}
