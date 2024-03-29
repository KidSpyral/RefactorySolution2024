/*
 * 
 * This is a class for limiting input in text fields
 * 
 * */

import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
// set text field input limits
class JTextFieldLimit extends PlainDocument {
  private int limit;
  JTextFieldLimit(int limit) {
    super();
    this.limit = limit;
  }// end JTextFieldLimit

  JTextFieldLimit(int limit, boolean upper) {
    super();
    this.limit = limit;
  }// end JTextFieldLimit

  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
	    if (str != null && (getLength() + str.length()) > limit) {
	        JOptionPane.showMessageDialog(null, "For input " + limit + " characters maximum!");
	    } else {
	        super.insertString(offset, str, attr);
	    }
	}

}// end class JTextFieldLimits