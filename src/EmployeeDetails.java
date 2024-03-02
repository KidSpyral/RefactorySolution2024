
/* * 
 * This is a menu driven system that will allow users to define a data structure representing a collection of 
 * records that can be displayed both by means of a dialog that can be scrolled through and by means of a table
 * to give an overall view of the collection contents.
 * 
 * */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

public class EmployeeDetails extends JFrame implements ActionListener, ItemListener, DocumentListener, WindowListener {
	// decimal format for inactive currency text field
	private static final DecimalFormat format = new DecimalFormat("\u20ac ###,###,##0.00");
	// decimal format for active currency text field
	private static final DecimalFormat fieldFormat = new DecimalFormat("0.00");
	// hold object start position in file
	private long currentByteStart = 0;
	private RandomFile application = new RandomFile();
	// display files in File Chooser only with extension .dat
	private FileNameExtensionFilter datfilter = new FileNameExtensionFilter("dat files (*.dat)", "dat");
	// hold file name and path for current file in use
	private File file;
	// holds true or false if any changes are made for text fields
	private boolean change = false;
	// holds true or false if any changes are made for file content
	boolean changesMade = false;
	private JMenuItem open, save, saveAs, create, modify, delete, firstItem, lastItem, nextItem, prevItem, searchById,
			searchBySurname, listAll, closeApp;
	private JButton first, previous, next, last, add, edit, deleteButton, displayAll, searchId, searchSurname,
			saveChange, cancelChange;
	private JComboBox<String> genderCombo, departmentCombo, fullTimeCombo;
	private JTextField idField, ppsField, surnameField, firstNameField, salaryField;
	private static EmployeeDetails frame = new EmployeeDetails();
	// font for labels, text fields and combo boxes
	Font font1 = new Font("SansSerif", Font.BOLD, 16);
	// holds automatically generated file name
	String generatedFileName;
	// holds current Employee object
	Employee currentEmployee;
	JTextField searchByIdField, searchBySurnameField;
	// gender combo box values
	String[] gender = { "", "M", "F" };
	// department combo box values
	String[] department = { "", "Administration", "Production", "Transport", "Management" };
	// full time combo box values
	String[] fullTime = { "", "Yes", "No" };

	// initialize menu bar
	private JMenuBar menuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu, recordMenu, navigateMenu, closeMenu;

		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		recordMenu = new JMenu("Records");
		recordMenu.setMnemonic(KeyEvent.VK_R);
		navigateMenu = new JMenu("Navigate");
		navigateMenu.setMnemonic(KeyEvent.VK_N);
		closeMenu = new JMenu("Exit");
		closeMenu.setMnemonic(KeyEvent.VK_E);

		menuBar.add(fileMenu);
		menuBar.add(recordMenu);
		menuBar.add(navigateMenu);
		menuBar.add(closeMenu);

		fileMenu.add(open = new JMenuItem("Open")).addActionListener(this);
		open.setMnemonic(KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		fileMenu.add(save = new JMenuItem("Save")).addActionListener(this);
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		fileMenu.add(saveAs = new JMenuItem("Save As")).addActionListener(this);
		saveAs.setMnemonic(KeyEvent.VK_F2);
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));

		recordMenu.add(create = new JMenuItem("Create new Record")).addActionListener(this);
		create.setMnemonic(KeyEvent.VK_N);
		create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		recordMenu.add(modify = new JMenuItem("Modify Record")).addActionListener(this);
		modify.setMnemonic(KeyEvent.VK_E);
		modify.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		recordMenu.add(delete = new JMenuItem("Delete Record")).addActionListener(this);

		navigateMenu.add(firstItem = new JMenuItem("First"));
		firstItem.addActionListener(this);
		navigateMenu.add(prevItem = new JMenuItem("Previous"));
		prevItem.addActionListener(this);
		navigateMenu.add(nextItem = new JMenuItem("Next"));
		nextItem.addActionListener(this);
		navigateMenu.add(lastItem = new JMenuItem("Last"));
		lastItem.addActionListener(this);
		navigateMenu.addSeparator();
		navigateMenu.add(searchById = new JMenuItem("Search by ID")).addActionListener(this);
		navigateMenu.add(searchBySurname = new JMenuItem("Search by Surname")).addActionListener(this);
		navigateMenu.add(listAll = new JMenuItem("List all Records")).addActionListener(this);

		closeMenu.add(closeApp = new JMenuItem("Close")).addActionListener(this);
		closeApp.setMnemonic(KeyEvent.VK_F4);
		closeApp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK));

		return menuBar;
	}// end menuBar

	// initialize search panel
	private JPanel searchPanel() {
		JPanel searchPanel = new JPanel(new MigLayout());

		searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
		searchPanel.add(new JLabel("Search by ID:"), "growx, pushx");
		searchPanel.add(searchByIdField = new JTextField(20), "width 200:200:200, growx, pushx");
		searchByIdField.addActionListener(this);
		searchByIdField.setDocument(new JTextFieldLimit(20));
		searchPanel.add(searchId = new JButton("Go"),
				"width 35:35:35, height 20:20:20, growx, pushx, wrap");
		searchId.addActionListener(this);
		searchId.setToolTipText("Search Employee By ID");

		searchPanel.add(new JLabel("Search by Surname:"), "growx, pushx");
		searchPanel.add(searchBySurnameField = new JTextField(20), "width 200:200:200, growx, pushx");
		searchBySurnameField.addActionListener(this);
		searchBySurnameField.setDocument(new JTextFieldLimit(20));
		searchPanel.add(
				searchSurname = new JButton("Go"),"width 35:35:35, height 20:20:20, growx, pushx, wrap");
		searchSurname.addActionListener(this);
		searchSurname.setToolTipText("Search Employee By Surname");

		return searchPanel;
	}// end searchPanel

	// initialize navigation panel
	private JPanel navigPanel() {
		JPanel navigPanel = new JPanel();

		navigPanel.setBorder(BorderFactory.createTitledBorder("Navigate"));
		navigPanel.add(first = new JButton(new ImageIcon(
				new ImageIcon("first.png").getImage().getScaledInstance(17, 17, java.awt.Image.SCALE_SMOOTH))));
		first.setPreferredSize(new Dimension(17, 17));
		first.addActionListener(this);
		first.setToolTipText("Display first Record");

		navigPanel.add(previous = new JButton(new ImageIcon(new ImageIcon("prev.png").getImage()
				.getScaledInstance(17, 17, java.awt.Image.SCALE_SMOOTH))));
		previous.setPreferredSize(new Dimension(17, 17));
		previous.addActionListener(this);
		previous.setToolTipText("Display next Record");

		navigPanel.add(next = new JButton(new ImageIcon(
				new ImageIcon("next.png").getImage().getScaledInstance(17, 17, java.awt.Image.SCALE_SMOOTH))));
		next.setPreferredSize(new Dimension(17, 17));
		next.addActionListener(this);
		next.setToolTipText("Display previous Record");

		navigPanel.add(last = new JButton(new ImageIcon(
				new ImageIcon("last.png").getImage().getScaledInstance(17, 17, java.awt.Image.SCALE_SMOOTH))));
		last.setPreferredSize(new Dimension(17, 17));
		last.addActionListener(this);
		last.setToolTipText("Display last Record");

		return navigPanel;
	}// end naviPanel

	private JPanel buttonPanel() {
		JPanel buttonPanel = new JPanel();

		buttonPanel.add(add = new JButton("Add Record"), "growx, pushx");
		add.addActionListener(this);
		add.setToolTipText("Add new Employee Record");
		buttonPanel.add(edit = new JButton("Edit Record"), "growx, pushx");
		edit.addActionListener(this);
		edit.setToolTipText("Edit current Employee");
		buttonPanel.add(deleteButton = new JButton("Delete Record"), "growx, pushx, wrap");
		deleteButton.addActionListener(this);
		deleteButton.setToolTipText("Delete current Employee");
		buttonPanel.add(displayAll = new JButton("List all Records"), "growx, pushx");
		displayAll.addActionListener(this);
		displayAll.setToolTipText("List all Registered Employees");

		return buttonPanel;
	}

	// initialize main/details panel
	private JPanel detailsPanel() {
		JPanel empDetails = new JPanel(new MigLayout());
		JPanel buttonPanel = new JPanel();
		JTextField field;

		empDetails.setBorder(BorderFactory.createTitledBorder("Employee Details"));

		empDetails.add(new JLabel("ID:"), "growx, pushx");
		empDetails.add(idField = new JTextField(20), "growx, pushx, wrap");
		idField.setEditable(false);

		empDetails.add(new JLabel("PPS Number:"), "growx, pushx");
		empDetails.add(ppsField = new JTextField(20), "growx, pushx, wrap");

		empDetails.add(new JLabel("Surname:"), "growx, pushx");
		empDetails.add(surnameField = new JTextField(20), "growx, pushx, wrap");

		empDetails.add(new JLabel("First Name:"), "growx, pushx");
		empDetails.add(firstNameField = new JTextField(20), "growx, pushx, wrap");

		empDetails.add(new JLabel("Gender:"), "growx, pushx");
		empDetails.add(genderCombo = new JComboBox<String>(gender), "growx, pushx, wrap");

		empDetails.add(new JLabel("Department:"), "growx, pushx");
		empDetails.add(departmentCombo = new JComboBox<String>(department), "growx, pushx, wrap");

		empDetails.add(new JLabel("Salary:"), "growx, pushx");
		empDetails.add(salaryField = new JTextField(20), "growx, pushx, wrap");

		empDetails.add(new JLabel("Full Time:"), "growx, pushx");
		empDetails.add(fullTimeCombo = new JComboBox<String>(fullTime), "growx, pushx, wrap");

		buttonPanel.add(saveChange = new JButton("Save"));
		saveChange.addActionListener(this);
		saveChange.setVisible(false);
		saveChange.setToolTipText("Save changes");
		buttonPanel.add(cancelChange = new JButton("Cancel"));
		cancelChange.addActionListener(this);
		cancelChange.setVisible(false);
		cancelChange.setToolTipText("Cancel edit");

		empDetails.add(buttonPanel, "span 2,growx, pushx,wrap");

		// loop through panel components and add listeners and format
		for (int i = 0; i < empDetails.getComponentCount(); i++) {
			empDetails.getComponent(i).setFont(font1);
			if (empDetails.getComponent(i) instanceof JTextField) {
				field = (JTextField) empDetails.getComponent(i);
				field.setEditable(false);
				if (field == ppsField)
					field.setDocument(new JTextFieldLimit(9));
				else
					field.setDocument(new JTextFieldLimit(20));
				field.getDocument().addDocumentListener(this);
			} // end if
			else if (empDetails.getComponent(i) instanceof JComboBox) {
				empDetails.getComponent(i).setBackground(Color.WHITE);
				empDetails.getComponent(i).setEnabled(false);
				((JComboBox<String>) empDetails.getComponent(i)).addItemListener(this);
				((JComboBox<String>) empDetails.getComponent(i)).setRenderer(new DefaultListCellRenderer() {
					// set foregroung to combo boxes
					public void paint(Graphics g) {
						setForeground(new Color(65, 65, 65));
						super.paint(g);
					}// end paint
				});
			} // end else if
		} // end for
		return empDetails;
	}// end detailsPanel

    // Display current Employee details
    public void displayRecords(Employee thisEmployee) {
        if (thisEmployee == null || thisEmployee.getEmployeeId() == 0) {
            return;
        }

        int countGender = findIndex(gender, Character.toString(thisEmployee.getGender()));
        int countDep = findIndex(department, thisEmployee.getDepartment().trim());

        idField.setText(Integer.toString(thisEmployee.getEmployeeId()));
        ppsField.setText(thisEmployee.getPps().trim());
        surnameField.setText(thisEmployee.getSurname().trim());
        firstNameField.setText(thisEmployee.getFirstName());
        genderCombo.setSelectedIndex(countGender);
        departmentCombo.setSelectedIndex(countDep);
        salaryField.setText(format.format(thisEmployee.getSalary()));
        fullTimeCombo.setSelectedIndex(thisEmployee.getFullTime() ? 1 : 2);

        change = false;
    }

    // Helper method to find index in an array
    private int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (value.equalsIgnoreCase(array[i])) {
                return i;
            }
        }
        return -1; // Not found
    }
   


	// display Employee summary dialog
	private void displayEmployeeSummaryDialog() {
		// display Employee summary dialog if these is someone to display
		if (isSomeoneToDisplay())
			new EmployeeSummaryDialog(getAllEmloyees());
	}// end displaySummaryDialog

	// display search by ID dialog
	private void displaySearchByIdDialog() {
		if (isSomeoneToDisplay())
			new SearchByIdDialog(EmployeeDetails.this);
	}// end displaySearchByIdDialog

	// display search by surname dialog
	private void displaySearchBySurnameDialog() {
		if (isSomeoneToDisplay())
			new SearchBySurnameDialog(EmployeeDetails.this);
	}// end displaySearchBySurnameDialog

    // Find byte start in file for the first active record
    private void firstRecord() {
        if (isSomeoneToDisplay()) {
            openFileForReading();
            findFirstRecord();
            closeReadFileIfNeeded();
            if (currentEmployee.getEmployeeId() == 0) {
                nextRecord();
            }
        }
    }

    // Open file for reading
    private void openFileForReading() {
        application.openReadFile(file.getAbsolutePath());
    }

    // Get byte start in file for the first record
    private void findFirstRecord() {
        currentByteStart = application.getFirst();
        currentEmployee = application.readRecords(currentByteStart);
    }

    // Close file for reading
    private void closeReadFileIfNeeded() {
        application.closeReadFile();
    }

    // Find byte start in file for the previous active record
    private void previousRecord() {
        if (isSomeoneToDisplay()) {
            openFileForReading();
            findPreviousRecord();
            closeReadFile();
        }
    }

    // Find byte start in file for the previous record
    private void findPreviousRecord() {
        currentByteStart = application.getPrevious(currentByteStart);
        currentEmployee = application.readRecords(currentByteStart);

        // Loop to previous record until Employee is active - ID is not 0
        while (currentEmployee.getEmployeeId() == 0) {
            currentByteStart = application.getPrevious(currentByteStart);
            currentEmployee = application.readRecords(currentByteStart);
        }
    }

    // Close file for reading
    private void closeReadFile() {
        application.closeReadFile();
    }

    private void nextRecord() {
        if (isSomeoneToDisplay()) {
            openFileForReading();
            findNextRecord();
            closeReadFile();
        }
    }

    // Find byte start in file for the next record
    private void findNextRecord() {
        currentByteStart = application.getNext(currentByteStart);
        currentEmployee = application.readRecords(currentByteStart);

        // Loop to next record until Employee is active - ID is not 0
        while (currentEmployee.getEmployeeId() == 0) {
            currentByteStart = application.getNext(currentByteStart);
            currentEmployee = application.readRecords(currentByteStart);
        }
    }


    // Find byte start in file for the last active record
    private void lastRecord() {
        if (isSomeoneToDisplay()) {
            openFileForReading();
            findLastRecord();
            closeReadFile();
            if (currentEmployee.getEmployeeId() == 0) {
                previousRecord();
            }
        }
    }

    // Find byte start in file for the last record
    private void findLastRecord() {
        currentByteStart = application.getLast();
        currentEmployee = application.readRecords(currentByteStart);
    }


	// search Employee by ID
	public void searchEmployeeById() {
		boolean found = false;

		if (isSomeoneToDisplay()) {
            try {
                int targetId = parseSearchId();

                if (targetId == currentEmployee.getEmployeeId() || searchMatchesCurrentId(targetId)) {
                    found = true;
                    displayRecords(currentEmployee);
                } else {
                    found = searchNextRecords(targetId);
                }

                if (!found) {
                    JOptionPane.showMessageDialog(null, "Employee not found!");
                }
            } catch (NumberFormatException e) {
                handleInvalidIdFormat();
            } finally {
                resetSearchField();
            }
        }
    }

    // Parse the search ID from the input field
    private int parseSearchId() throws NumberFormatException {
        searchByIdField.setBackground(Color.WHITE);
        return Integer.parseInt(searchByIdField.getText().trim());
    }

    // Check if the search ID matches the current Employee's ID
    private boolean searchMatchesCurrentId(int targetId) {
        return searchByIdField.getText().trim().equals(Integer.toString(currentEmployee.getEmployeeId()));
    }

    // Search for the next records until the Employee is found or all Employees have been checked
    private boolean searchNextRecords(int targetId) {
        int firstId = currentEmployee.getEmployeeId();
        nextRecord(); // look for the next record

        while (firstId != currentEmployee.getEmployeeId()) {
            if (targetId == currentEmployee.getEmployeeId()) {
                displayRecords(currentEmployee);
                return true;
            } else {
                nextRecord(); // look for the next record
            }
        }

        return false;
    }

    // Handle invalid ID format
    private void handleInvalidIdFormat() {
        searchByIdField.setBackground(new Color(255, 150, 150));
        JOptionPane.showMessageDialog(null, "Wrong ID format!");
    }

    // Reset the search field
    private void resetSearchField() {
        searchByIdField.setBackground(Color.WHITE);
        searchByIdField.setText("");
    }

	// search Employee by surname
	public void searchEmployeeBySurname() {
		boolean found = false;
		// if any active Employee record search for ID else do nothing
		if (isSomeoneToDisplay()) {
            try {
                String targetSurname = parseSearchSurname();

                if (targetSurname.equalsIgnoreCase(currentEmployee.getSurname().trim())
                        || searchMatchesCurrentSurname(targetSurname)) {
                    found = true;
                    displayRecords(currentEmployee);
                } else {
                    found = searchNextRecordsBySurname(targetSurname);
                }

                if (!found) {
                    JOptionPane.showMessageDialog(null, "Employee not found!");
                }
            } catch (NumberFormatException e) {
                handleInvalidSurnameFormat();
            } finally {
                resetSearchField();
            }
        }
    }

    // Parse the search surname from the input field
    private String parseSearchSurname() throws NumberFormatException {
        searchBySurnameField.setText("");
        return searchBySurnameField.getText().trim();
    }

    // Check if the search surname matches the current Employee's surname
    private boolean searchMatchesCurrentSurname(String targetSurname) {
        return targetSurname.equalsIgnoreCase(currentEmployee.getSurname().trim());
    }

    // Search for the next records until the Employee is found or all Employees have been checked
    private boolean searchNextRecordsBySurname(String targetSurname) {
        String firstSurname = currentEmployee.getSurname().trim();
        nextRecord(); // look for the next record

        while (!firstSurname.equalsIgnoreCase(currentEmployee.getSurname().trim())) {
            if (targetSurname.equalsIgnoreCase(currentEmployee.getSurname().trim())) {
                displayRecords(currentEmployee);
                return true;
            } else {
                nextRecord(); // look for the next record
            }
        }

        return false;
    }

    // Handle invalid surname format
    private void handleInvalidSurnameFormat() {
        JOptionPane.showMessageDialog(null, "Invalid surname format!");
    }

	// get next free ID from Employees in the file
	public int getNextFreeId() {
		int nextFreeId = 0;
		// if file is empty or all records are empty start with ID 1 else look
		// for last active record
		if (file.length() == 0 || !isSomeoneToDisplay())
			nextFreeId++;
		else {
			lastRecord();// look for last active record
			// add 1 to last active records ID to get next ID
			nextFreeId = currentEmployee.getEmployeeId() + 1;
		}
		return nextFreeId;
	}// end getNextFreeId

	// Get values from text fields and create Employee object
    private Employee getChangedDetails() {
        boolean fullTime = ((String) fullTimeCombo.getSelectedItem()).equalsIgnoreCase("Yes");
        int employeeId = Integer.parseInt(idField.getText());
        String pps = ppsField.getText().toUpperCase();
        String surname = surnameField.getText().toUpperCase();
        String firstName = firstNameField.getText().toUpperCase();
        char gender = genderCombo.getSelectedItem().toString().charAt(0);
        String department = departmentCombo.getSelectedItem().toString();
        double salary = Double.parseDouble(salaryField.getText());

        return new Employee(employeeId, pps, surname, firstName, gender, department, salary, fullTime);
    }

    // Add Employee object to file
    public void addRecord(Employee newEmployee) {
        openFileForWriting();
        writeRecordToFile(newEmployee);
        closeWriteFile();
    }

    // Open file for writing
    private void openFileForWriting() {
        application.openWriteFile(file.getAbsolutePath());
    }

    // Write Employee object into the file
    private void writeRecordToFile(Employee newEmployee) {
        currentByteStart = application.addRecords(newEmployee);
    }

    // Close file for writing
    private void closeWriteFile() {
        application.closeWriteFile();
    }

    // Delete (make inactive - empty) record from file
    private void deleteRecord() {
        if (isSomeoneToDisplay()) {
            int userChoice = confirmDeleteRecord();

            if (userChoice == JOptionPane.YES_OPTION) {
                openFileForWriting();
                deleteRecordInFile();
                closeWriteFile();
                moveToNextRecord();
            }
        }
    }

    // Confirm if the user wants to delete the record
    private int confirmDeleteRecord() {
        return JOptionPane.showOptionDialog(frame, "Do you want to delete record?", "Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
    }

    // Delete (make inactive - empty) record in the file at the proper position
    private void deleteRecordInFile() {
        application.deleteRecords(currentByteStart);
    }

    // Move to the next record if any active record is present in the file
    private void moveToNextRecord() {
        if (isSomeoneToDisplay()) {
            nextRecord();
            displayRecords(currentEmployee);
        }
    }

	// create vector of vectors with all Employee details
	private Vector<Object> getAllEmloyees() {
		// vector of Employee objects
		Vector<Object> allEmployee = new Vector<Object>();
		Vector<Object> empDetails;// vector of each employee details
		long byteStart = currentByteStart;
		int firstId;

		firstRecord();// look for first record
		firstId = currentEmployee.getEmployeeId();
		// loop until all Employees are added to vector
		do {
			empDetails = new Vector<Object>();
			empDetails.addElement(new Integer(currentEmployee.getEmployeeId()));
			empDetails.addElement(currentEmployee.getPps());
			empDetails.addElement(currentEmployee.getSurname());
			empDetails.addElement(currentEmployee.getFirstName());
			empDetails.addElement(new Character(currentEmployee.getGender()));
			empDetails.addElement(currentEmployee.getDepartment());
			empDetails.addElement(new Double(currentEmployee.getSalary()));
			empDetails.addElement(new Boolean(currentEmployee.getFullTime()));

			allEmployee.addElement(empDetails);
			nextRecord();// look for next record
		} while (firstId != currentEmployee.getEmployeeId());// end do - while
		currentByteStart = byteStart;

		return allEmployee;
	}// end getAllEmployees

	    // Activate fields for editing
	    private void editDetails() {
	        if (isSomeoneToDisplay()) {
	            removeEuroSignFromSalaryField();
	            resetChangeFlag();
	            enableEditingFields();
	        }
	    }
	
	    // Remove euro sign from salary text field
	    private void removeEuroSignFromSalaryField() {
	        salaryField.setText(fieldFormat.format(currentEmployee.getSalary()));
	    }
	
	    // Reset the change flag
	    private void resetChangeFlag() {
	        change = false;
	    }
	
	    // Enable text fields for editing
	    private void enableEditingFields() {
	        setEnabled(true);
	    }

	// ignore changes and set text field unenabled
	private void cancelChange() {
		setEnabled(false);
		displayRecords(currentEmployee);
	}// end cancelChange

    // Check if any records in the file are active (ID is not 0)
    private boolean isSomeoneToDisplay() {
        openFileForReading();
        boolean someoneToDisplay = application.isSomeoneToDisplay();
        closeReadFile();

        if (!someoneToDisplay) {
            clearTextFieldsAndDisplayMessage();
        }

        return someoneToDisplay;
    }

    // Clear all text fields and display a message if no records are found
    private void clearTextFieldsAndDisplayMessage() {
        currentEmployee = null;
        idField.setText("");
        ppsField.setText("");
        surnameField.setText("");
        firstNameField.setText("");
        salaryField.setText("");
        genderCombo.setSelectedIndex(0);
        departmentCombo.setSelectedIndex(0);
        fullTimeCombo.setSelectedIndex(0);

        JOptionPane.showMessageDialog(null, "No Employees registered!");
    }

    // Check for correct PPS format and if PPS is already in use
    public boolean correctPps(String pps, long currentByte) {
        if (isValidPpsFormat(pps)) {
            openFileForReading();
            boolean ppsExist = application.isPpsExist(pps, currentByte);
            closeReadFile();
            return ppsExist;
        } else {
            return true;
        }
    }

    // Check if PPS has a valid format based on the assignment description
    private boolean isValidPpsFormat(String pps) {
        if (pps.length() == 8 || pps.length() == 9) {
            return (Character.isDigit(pps.charAt(0)) && Character.isDigit(pps.charAt(1))
                    && Character.isDigit(pps.charAt(2)) && Character.isDigit(pps.charAt(3))
                    && Character.isDigit(pps.charAt(4)) && Character.isDigit(pps.charAt(5))
                    && Character.isDigit(pps.charAt(6)) && Character.isLetter(pps.charAt(7))
                    && (pps.length() == 8 || Character.isLetter(pps.charAt(8))));
        } else {
            return false;
        }
    }

	// check if file name has extension .dat
	private boolean checkFileName(File fileName) {
		boolean checkFile = false;
		int length = fileName.toString().length();

		// check if last characters in file name is .dat
		if (fileName.toString().charAt(length - 4) == '.' && fileName.toString().charAt(length - 3) == 'd'
				&& fileName.toString().charAt(length - 2) == 'a' && fileName.toString().charAt(length - 1) == 't')
			checkFile = true;
		return checkFile;
	}// end checkFileName

	// check if any changes text field where made
	private boolean checkForChanges() {
		boolean anyChanges = false;
		// if changes where made, allow user to save there changes
		if (change) {
			saveChanges();// save changes
			anyChanges = true;
		} // end if
			// if no changes made, set text fields as unenabled and display
			// current Employee
		else {
			setEnabled(false);
			displayRecords(currentEmployee);
		} // end else

		return anyChanges;
	}// end checkForChanges

	// check for input in text fields
	private boolean checkInput() {
		boolean valid = true;
		// if any of inputs are in wrong format, colour text field and display
		// message
		if (ppsField.isEditable() && ppsField.getText().trim().isEmpty()) {
			ppsField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (ppsField.isEditable() && correctPps(ppsField.getText().trim(), currentByteStart)) {
			ppsField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (surnameField.isEditable() && surnameField.getText().trim().isEmpty()) {
			surnameField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (firstNameField.isEditable() && firstNameField.getText().trim().isEmpty()) {
			firstNameField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (genderCombo.getSelectedIndex() == 0 && genderCombo.isEnabled()) {
			genderCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (departmentCombo.getSelectedIndex() == 0 && departmentCombo.isEnabled()) {
			departmentCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		try {// try to get values from text field
			Double.parseDouble(salaryField.getText());
			// check if salary is greater than 0
			if (Double.parseDouble(salaryField.getText()) < 0) {
				salaryField.setBackground(new Color(255, 150, 150));
				valid = false;
			} // end if
		} // end try
		catch (NumberFormatException num) {
			if (salaryField.isEditable()) {
				salaryField.setBackground(new Color(255, 150, 150));
				valid = false;
			} // end if
		} // end catch
		if (fullTimeCombo.getSelectedIndex() == 0 && fullTimeCombo.isEnabled()) {
			fullTimeCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
			// display message if any input or format is wrong
		if (!valid)
			JOptionPane.showMessageDialog(null, "Wrong values or format! Please check!");
		// set text field to white colour if text fields are editable
		if (ppsField.isEditable())
			setToWhite();

		return valid;
	}

	// set text field background colour to white
	private void setToWhite() {
		ppsField.setBackground(UIManager.getColor("TextField.background"));
		surnameField.setBackground(UIManager.getColor("TextField.background"));
		firstNameField.setBackground(UIManager.getColor("TextField.background"));
		salaryField.setBackground(UIManager.getColor("TextField.background"));
		genderCombo.setBackground(UIManager.getColor("TextField.background"));
		departmentCombo.setBackground(UIManager.getColor("TextField.background"));
		fullTimeCombo.setBackground(UIManager.getColor("TextField.background"));
	}// end setToWhite

	// enable text fields for editing
	public void setEnabled(boolean booleanValue) {
		boolean search;
		if (booleanValue)
			search = false;
		else
			search = true;
		ppsField.setEditable(booleanValue);
		surnameField.setEditable(booleanValue);
		firstNameField.setEditable(booleanValue);
		genderCombo.setEnabled(booleanValue);
		departmentCombo.setEnabled(booleanValue);
		salaryField.setEditable(booleanValue);
		fullTimeCombo.setEnabled(booleanValue);
		saveChange.setVisible(booleanValue);
		cancelChange.setVisible(booleanValue);
		searchByIdField.setEnabled(search);
		searchBySurnameField.setEnabled(search);
		searchId.setEnabled(search);
		searchSurname.setEnabled(search);
	}// end setEnabled

    // Open a file
    private void openFile() {
        final JFileChooser fc = createFileChooser();
        File newFile = chooseFileToOpen(fc);

        if (shouldSaveOldFile()) {
            int returnVal = askUserToSaveChanges();
            if (returnVal == JOptionPane.YES_OPTION) {
                saveFile();
            }
        }

        if (newFile != null) {
            handleOpenedFile(newFile);
        }
    }

    // Create and configure a JFileChooser
    private JFileChooser createFileChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open");
        fc.setFileFilter(datfilter);
        return fc;
    }

    // Choose a file to open using the given JFileChooser
    private File chooseFileToOpen(JFileChooser fc) {
        int returnVal = fc.showOpenDialog(EmployeeDetails.this);
        return (returnVal == JFileChooser.APPROVE_OPTION) ? fc.getSelectedFile() : null;
    }

    // Check if the old file should be saved
    private boolean shouldSaveOldFile() {
        return file.length() != 0 || change;
    }

    // Ask the user whether to save changes
    private int askUserToSaveChanges() {
        return JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
    }

    // Handle the opened file
    private void handleOpenedFile(File newFile) {
        if (shouldDeleteOldFile()) {
            file.delete();
        }

        file = newFile;
        openAndDisplayRecords();
    }

    // Check if the old file should be deleted
    private boolean shouldDeleteOldFile() {
        return file.getName().equals(generatedFileName);
    }

    // Open the file for reading and display records
    private void openAndDisplayRecords() {
        application.openReadFile(file.getAbsolutePath());
        firstRecord();
        displayRecords(currentEmployee);
        application.closeReadFile();
    }

    // Save the file
    private void saveFile() {
        if (isNewGeneratedFile()) {
            saveFileAs();
        } else {
            handleSaveChanges();
            displayRecords(currentEmployee);
            setEnabled(false);
        }
    }

    // Check if the file is a new generated file
    private boolean isNewGeneratedFile() {
        return file.getName().equals(generatedFileName);
    }

    // Handle saving changes to the file
    private void handleSaveChanges() {
        if (shouldSaveChanges()) {
            openFileForWriting();
            saveChangesToFile();
            closeWriteFile();
        }
    }

    // Check if changes should be saved
    private boolean shouldSaveChanges() {
        return change && !idField.getText().isEmpty();
    }

    // Save changes to the file for the corresponding Employee record
    private void saveChangesToFile() {
        currentEmployee = getChangedDetails();
        application.changeRecords(currentEmployee, currentByteStart);
    }


    // Save changes to the current Employee
    private void saveChanges() {
        int returnVal = askUserToSaveChanges();

        if (returnVal == JOptionPane.YES_OPTION) {
            handleSaveChangesToEmployee();
            displayRecords(currentEmployee);
            setEnabled(false);
        }
    }

    // Handle saving changes to the file
    private void handleSaveChangesToEmployee() {
        openFileForWriting();
        saveChangesToFile();
        closeWriteFile();
        changesMade = false; // State that all changes have been saved
    }


    // Save file as 'save as'
    private void saveFileAs() {
        final JFileChooser fc = createSaveAsFileChooser();
        File newFile = chooseFileToSaveAs(fc);

        if (newFile != null) {
            handleSaveAs(newFile);
        }

        changesMade = false;
    }

    // Create and configure a JFileChooser for 'Save As'
    private JFileChooser createSaveAsFileChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save As");
        fc.setFileFilter(datfilter);
        fc.setApproveButtonText("Save");
        fc.setSelectedFile(new File("new_Employee.dat"));
        return fc;
    }

    // Choose a file to save as using the given JFileChooser
    private File chooseFileToSaveAs(JFileChooser fc) {
        int returnVal = fc.showSaveDialog(EmployeeDetails.this);
        return (returnVal == JFileChooser.APPROVE_OPTION) ? fc.getSelectedFile() : null;
    }

    // Handle saving the file as 'Save As'
    private void handleSaveAs(File newFile) {
        if (!checkFileNameValidity(newFile)) {
            newFile = addDatExtensionAndCreateFile(newFile);
        } else {
            application.createFile(newFile.getAbsolutePath());
        }

        copyOldFileToNewFile(newFile);
        handleGeneratedFileNameDeletion(newFile);
        file = newFile;
    }

    // Check if the file name is valid
    private boolean checkFileNameValidity(File newFile) {
        return newFile.getName().endsWith(".dat");
    }

    // Add '.dat' extension to the file name and create the file
    private File addDatExtensionAndCreateFile(File newFile) {
        File fileWithDatExtension = new File(newFile.getAbsolutePath() + ".dat");
        application.createFile(fileWithDatExtension.getAbsolutePath());
        return fileWithDatExtension;
    }

    // Copy the old file to the new file
    private void copyOldFileToNewFile(File newFile) {
        try {
            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Handle or log the exception
        }
    }

    // Handle the deletion of the old file if its name was generated
    private void handleGeneratedFileNameDeletion(File newFile) {
        if (file.getName().equals(generatedFileName)) {
            file.delete();
        }
    }

    // Allow saving changes to file when exiting the application
    private void exitApp() {
        if (file.length() != 0) {
            handleNonEmptyFileExit();
        } else {
            handleEmptyFileExit();
        }
    }

    // Handle exit when the file is not empty
    private void handleNonEmptyFileExit() {
        if (changesMade) {
            int returnVal = askUserToSaveChangesOnExit();
            handleExitOptions(returnVal);
        } else {
            handleNoChangesExit();
        }
    }

    // Handle exit when the file is empty
    private void handleEmptyFileExit() {
        handleNoChangesExit();
    }

    // Ask the user whether to save changes when exiting
    private int askUserToSaveChangesOnExit() {
        return JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
    }

    // Handle different exit options based on user's choice
    private void handleExitOptions(int returnVal) {
        if (returnVal == JOptionPane.YES_OPTION) {
            saveFile();
        }

        deleteGeneratedFileIfNecessary();
        exitApplication();
    }

    // Handle exit when there are no changes
    private void handleNoChangesExit() {
        deleteGeneratedFileIfNecessary();
        exitApplication();
    }

    // Delete the generated file if its name matches
    private void deleteGeneratedFileIfNecessary() {
        if (file.getName().equals(generatedFileName)) {
            file.delete();
        }
    }

    // Exit the application
    private void exitApplication() {
        System.exit(0);
    }

	// generate 20 character long file name
	private String getFileName() {
		String fileNameChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
		StringBuilder fileName = new StringBuilder();
		Random rnd = new Random();
		// loop until 20 character long file name is generated
		while (fileName.length() < 20) {
			int index = (int) (rnd.nextFloat() * fileNameChars.length());
			fileName.append(fileNameChars.charAt(index));
		}
		String generatedfileName = fileName.toString();
		return generatedfileName;
	}// end getFileName

	// create file with generated file name when application is opened
	private void createRandomFile() {
		generatedFileName = getFileName() + ".dat";
		// assign generated file name to file
		file = new File(generatedFileName);
		// create file
		application.createFile(file.getName());
	}// end createRandomFile

	// action listener for buttons, text field and menu items
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == closeApp) {
			if (checkInput() && !checkForChanges())
				exitApp();
		} else if (e.getSource() == open) {
			if (checkInput() && !checkForChanges())
				openFile();
		} else if (e.getSource() == save) {
			if (checkInput() && !checkForChanges())
				saveFile();
			change = false;
		} else if (e.getSource() == saveAs) {
			if (checkInput() && !checkForChanges())
				saveFileAs();
			change = false;
		} else if (e.getSource() == searchById) {
			if (checkInput() && !checkForChanges())
				displaySearchByIdDialog();
		} else if (e.getSource() == searchBySurname) {
			if (checkInput() && !checkForChanges())
				displaySearchBySurnameDialog();
		} else if (e.getSource() == searchId || e.getSource() == searchByIdField)
			searchEmployeeById();
		else if (e.getSource() == searchSurname || e.getSource() == searchBySurnameField)
			searchEmployeeBySurname();
		else if (e.getSource() == saveChange) {
			if (checkInput() && !checkForChanges())
				;
		} else if (e.getSource() == cancelChange)
			cancelChange();
		else if (e.getSource() == firstItem || e.getSource() == first) {
			if (checkInput() && !checkForChanges()) {
				firstRecord();
				displayRecords(currentEmployee);
			}
		} else if (e.getSource() == prevItem || e.getSource() == previous) {
			if (checkInput() && !checkForChanges()) {
				previousRecord();
				displayRecords(currentEmployee);
			}
		} else if (e.getSource() == nextItem || e.getSource() == next) {
			if (checkInput() && !checkForChanges()) {
				nextRecord();
				displayRecords(currentEmployee);
			}
		} else if (e.getSource() == lastItem || e.getSource() == last) {
			if (checkInput() && !checkForChanges()) {
				lastRecord();
				displayRecords(currentEmployee);
			}
		} else if (e.getSource() == listAll || e.getSource() == displayAll) {
			if (checkInput() && !checkForChanges())
				if (isSomeoneToDisplay())
					displayEmployeeSummaryDialog();
		} else if (e.getSource() == create || e.getSource() == add) {
			if (checkInput() && !checkForChanges())
				new AddRecordDialog(EmployeeDetails.this);
		} else if (e.getSource() == modify || e.getSource() == edit) {
			if (checkInput() && !checkForChanges())
				editDetails();
		} else if (e.getSource() == delete || e.getSource() == deleteButton) {
			if (checkInput() && !checkForChanges())
				deleteRecord();
		} else if (e.getSource() == searchBySurname) {
			if (checkInput() && !checkForChanges())
				new SearchBySurnameDialog(EmployeeDetails.this);
		}
	}// end actionPerformed

	// content pane for main dialog
	private void createContentPane() {
		setTitle("Employee Details");
		createRandomFile();// create random file name
		JPanel dialog = new JPanel(new MigLayout());

		setJMenuBar(menuBar());// add menu bar to frame
		// add search panel to frame
		dialog.add(searchPanel(), "width 400:400:400, growx, pushx");
		// add navigation panel to frame
		dialog.add(navigPanel(), "width 150:150:150, wrap");
		// add button panel to frame
		dialog.add(buttonPanel(), "growx, pushx, span 2,wrap");
		// add details panel to frame
		dialog.add(detailsPanel(), "gap top 30, gap left 150, center");

		JScrollPane scrollPane = new JScrollPane(dialog);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		addWindowListener(this);
	}// end createContentPane

	// create and show main dialog
	private static void createAndShowGUI() {

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.createContentPane();// add content pane to frame
		frame.setSize(760, 600);
		frame.setLocation(250, 200);
		frame.setVisible(true);
	}// end createAndShowGUI

	// main method
	public static void main(String args[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}// end main

	// DocumentListener methods
	public void changedUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	public void insertUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	public void removeUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	// ItemListener method
	public void itemStateChanged(ItemEvent e) {
		change = true;
	}

	// WindowsListener methods
	public void windowClosing(WindowEvent e) {
		// exit application
		exitApp();
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}// end class EmployeeDetails
