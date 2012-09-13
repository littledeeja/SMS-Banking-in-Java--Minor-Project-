/**
 * 
 */
package serialComm;

/**
 * The <code>ChangePincodeMessage</code> object represents the message for sms query request for changing pincode of the account.
 */
public class ChangePincodeMessage{
	/** 
	 * Old pincode of the account
	 */
	protected String oldPincode = null;
	
	/** 
	 * New pincode of the account, After completion of database update, the pincode will be changed to the value assigned to 
	 * New Pincode.
	 */
	protected String newPincode = null;
	
	/**
	 * Sets the old Pincode
	 * @param oldPin Old Pincode to set
	 */
	public void setOldPincode(String oldPin) {
		oldPincode = oldPin;
	}
	
	/** 
	 * Sets the New Pincode
	 * 
	 * @param newPin New Pincode to set
	 */
	public void setNewPincode(String newPin) {
		newPincode = newPin;
	}
	
	/** 
	 * Returns the New Pincode 
	 * @return returns the new Pincode 
	 */
	
	public String getNewPincode() {
		return newPincode;
	}
	
	/**
	 * Returns the old pincode
	 * @return Returns the old pincode
	 */
	public String getOldPincode() {
		return oldPincode;
	}
	
	public void debug() {
		System.out.println("Old Pincode : "+oldPincode);
		System.out.println("New Pincode : "+newPincode);
	}
}
