package serialComm;

/**
 *  The <code>BankingAction</code> Enums define the features available for SMS-BANKING.
 *  We define these features as enum, so that they can be used in switch case, comparision is possible.
 *  Further addition of new feature is also very easy, its only matter of increasing enums and adding 
 *  another case in switch statement, there by creating a new Class for a new type of message. Hence 
 *  making the use of object oriented concept.
 *  
 *  @see serialComm.Message#Message()
 */
public enum BankingAction {
	/**
	 * BALANCE REQUEST
	 */
	BAL,
	
	/**
	 * TRANSFER REQUEST
	 */
	TR,
	/**
	 * STATEMENT PRINT REQUEST
	 */
	STMT,
	/**
	 * CHANGE PINCODE REQUEST
	 */
	CHP;
}
