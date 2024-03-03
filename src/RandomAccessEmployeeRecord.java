/*
 * 
 * This is a Random Access Employee record definition
 * 
 * */

import java.io.RandomAccessFile;
import java.io.IOException;

public class RandomAccessEmployeeRecord extends Employee
{  
    public static final int SIZE = 175; // Size of each RandomAccessEmployeeRecord object

   // Create empty record
   public RandomAccessEmployeeRecord()
   {
      this(0, "","","",'\0', "", 0.0, false);
   } // end RandomAccessEmployeeRecord

   // Initialize record with details
   public RandomAccessEmployeeRecord( int employeeId, String pps, String surname, String firstName, char gender, 
		   String department, double salary, boolean fullTime)
   {
      super(employeeId, pps, surname, firstName, gender, department, salary, fullTime);
   } // end RandomAccessEmployeeRecord

   // Read a record from specified RandomAccessFile
   public void read( RandomAccessFile file ) throws IOException
   {
	   	setEmployeeId(file.readInt());
		setPps(readName(file));
		setSurname(readName(file));
		setFirstName(readName(file));
		setGender(file.readChar());
		setDepartment(readName(file));
		setSalary(file.readDouble());
		setFullTime(file.readBoolean());
   } // end read

   private String readName(RandomAccessFile file) throws IOException {
	    char[] nameChars = new char[20];

	    for (int count = 0; count < nameChars.length; count++) {
	        nameChars[count] = file.readChar();
	    }

	    return new String(nameChars).replace('\0', ' ').trim();
	}

   // Write a record to specified RandomAccessFile
   public void write( RandomAccessFile file ) throws IOException
   {
      file.writeInt( getEmployeeId() );
      writeName(file, getPps().toUpperCase());
      writeName( file, getSurname().toUpperCase() );
      writeName( file, getFirstName().toUpperCase() );
      file.writeChar(getGender());
      writeName(file,getDepartment());
      file.writeDouble( getSalary() );
      file.writeBoolean(getFullTime());
   } // end write

   private void writeName(RandomAccessFile file, String name) throws IOException {
	    String paddedName = padString(name, 20);
	    file.writeChars(paddedName);
	} // end writeName

	private String padString(String str, int length) {
	    StringBuilder paddedStr = new StringBuilder((str != null) ? str : "");
	    paddedStr.setLength(length);
	    return paddedStr.toString();
	}
} // end class RandomAccessEmployeeRecord