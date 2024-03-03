/*
 * 
 * This class is for accessing, creating and modifying records in a file
 * 
 * */

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class RandomFile {
	private RandomAccessFile output;
	private RandomAccessFile input;
	
	  // Open file for adding or changing records
    public RandomAccessFile createFile(String fileName) {
        try {
            return new RandomAccessFile(fileName, "rw");
        } catch (IOException ioException) {
            handleFileError("File does not exist!");
            return null;
        }
    }


    // Open file for adding or changing records
    public RandomAccessFile openWriteFile(String fileName) {
        try {
            return new RandomAccessFile(fileName, "rw");
        } catch (IOException ioException) {
            handleFileError("File does not exist!");
            return null;
        }
    }

  
    // Handle file error
    private void handleFileError(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage);
    }

    // Close file for adding or changing records
    public void closeWriteFile() {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioException) {
            handleFileError1("Error closing file!");
        }
    }

    // Handle file error
    private void handleFileError1(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage);
        System.exit(1);
    }

    // Add records to file
	public long addRecords(Employee newEmployee) {
	       try {
	            output.seek(output.length()); // Look for proper position
	            RandomAccessEmployeeRecord record = createRandomAccessRecord(newEmployee);
	            record.write(output); // Write object to file
	            return output.length() - RandomAccessEmployeeRecord.SIZE;
	        } catch (IOException ioException) {
	            handleFileError1("Error writing to file!");
	            return -1;
	        }
	    }

 
    // Create RandomAccessEmployeeRecord from Employee
    private RandomAccessEmployeeRecord createRandomAccessRecord(Employee employee) {
        return new RandomAccessEmployeeRecord(
                employee.getEmployeeId(),
                employee.getPps(),
                employee.getSurname(),
                employee.getFirstName(),
                employee.getGender(),
                employee.getDepartment(),
                employee.getSalary(),
                employee.getFullTime()
        );
    }

    // Change details for existing object
    public void changeRecords(Employee newDetails, long byteToStart) {
        try {
            output.seek(byteToStart); // Look for proper position
            RandomAccessEmployeeRecord record = createRandomAccessRecord(newDetails);
            record.write(output); // Write object to file
        } catch (IOException ioException) {
            handleFileError1("Error writing to file!");
        }
    }

 // Delete existing object
    public void deleteRecords(long byteToStart) {
        try {
            output.seek(byteToStart); // Look for proper position
            RandomAccessEmployeeRecord emptyRecord = createEmptyRandomAccessRecord();
            emptyRecord.write(output); // Replace existing object with empty object
        } catch (IOException ioException) {
            handleFileError1("Error writing to file!");
        }
    }

    // Create an empty RandomAccessEmployeeRecord
    private RandomAccessEmployeeRecord createEmptyRandomAccessRecord() {
        return new RandomAccessEmployeeRecord(); // Create empty object
    }


	// Open file for reading
	public void openReadFile(String fileName) {
		try // open file
		{
			input = new RandomAccessFile(fileName, "r");
		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "File is not suported!");
		} // end catch
	} // end method openFile

	// Close file
	public void closeReadFile() {
		try // close file and exit
		{
			if (input != null)
				input.close();
		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error closing file!");
			System.exit(1);
		} // end catch
	} // end method closeFile

	// Get position of first record in file
	public long getFirst() {
		long byteToStart = 0;

		try {// try to get file
			input.length();
		} // end try
		catch (IOException e) {
		}// end catch
		
		return byteToStart;
	}// end getFirst

	// Get position of last record in file
	public long getLast() {
		long byteToStart = 0;

		try {// try to get position of last record
			byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
		}// end try 
		catch (IOException e) {
		}// end catch

		return byteToStart;
	}// end getFirst

    // Get position of the next record in the file
    public long getNext(long readFrom) {
        long byteToStart = readFrom;

        try {
            input.seek(byteToStart); // Look for the proper position in the file

            // If the next position is the end of the file, go to the start of the file; else, get the next position
            byteToStart = (byteToStart + RandomAccessEmployeeRecord.SIZE == input.length())
                    ? 0
                    : byteToStart + RandomAccessEmployeeRecord.SIZE;
        } catch (IOException e) {
            handleFileError1("Error reading from file!");
        }

        return byteToStart;
    }


    // Get position of the previous record in the file
    public long getPrevious(long readFrom) {
        long byteToStart = readFrom;

        try {
            input.seek(byteToStart); // Look for the proper position in the file

            // If the previous position is the start of the file, go to the end of the file; else, get the previous position
            byteToStart = (byteToStart == 0)
                    ? input.length() - RandomAccessEmployeeRecord.SIZE
                    : byteToStart - RandomAccessEmployeeRecord.SIZE;
        } catch (IOException e) {
            handleFileError1("Error reading from file!");
        }

        return byteToStart;
    }

	// Get object from file in specified position
	public Employee readRecords(long byteToStart) {
		Employee thisEmp = null;
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

        try {
            input.seek(byteToStart); // Look for the proper position in the file
            record.read(input); // Read the record from the file
        } catch (IOException e) {
            handleFileError1("Error reading from file!");
        }
        thisEmp = record;
        return thisEmp;
    }


    // Check if PPS Number already in use
    public boolean isPpsExist(String pps, long currentByteStart) {
        RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();
        boolean ppsExist = false;
        long oldByteStart = currentByteStart;
        long currentByte = 0;

        try {
            // Start from the beginning of the file and loop until the PPS Number is found or the search returns to the start position
            while (currentByte != input.length() && !ppsExist) {
                // If the PPS Number is in the position of the current object, skip the comparison
                if (currentByte != oldByteStart) {
                    input.seek(currentByte); // Look for the proper position in the file
                    record.read(input); // Get the record from the file

                    // If the PPS Number already exists in another record, display a message and stop the search
                    if (record.getPps().trim().equalsIgnoreCase(pps)) {
                        ppsExist = true;
                        handlePpsExistError();
                    }
                }
                currentByte += RandomAccessEmployeeRecord.SIZE;
            }
        } catch (IOException e) {
            handleFileError1("Error reading from file!");
        }

        return ppsExist;
    }

    // Handle PPS exist error
    private void handlePpsExistError() {
        JOptionPane.showMessageDialog(null, "PPS number already exists!");
    }

    // Check if any record contains a valid ID - greater than 0
    public boolean isSomeoneToDisplay() {
        boolean someoneToDisplay = false;
        long currentByte = 0;
        RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

        try {
            // Start from the beginning of the file and loop until a valid ID is found or the search returns to the start position
            while (currentByte != input.length() && !someoneToDisplay) {
                input.seek(currentByte); // Look for the proper position in the file
                record.read(input); // Get the record from the file

                // If a valid ID exists, stop the search
                if (record.getEmployeeId() > 0) {
                    someoneToDisplay = true;
                }

                currentByte += RandomAccessEmployeeRecord.SIZE;
            }
        } catch (IOException e) {
            handleFileError1("Error reading from file!");
        }

        return someoneToDisplay;
    }

}// end class RandomFile
