/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { // creates a statement object
		Statement stmt = this._connection.createStatement ();// issues the update instruction
		stmt.executeUpdate (sql);// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {//creates a statement object
		Statement stmt = this._connection.createStatement ();//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { //creates a statement object 
		Statement stmt = this._connection.createStatement (); //issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; //iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {//creates a statement object
		Statement stmt = this._connection.createStatement ();//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){// ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	 

	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println(
					"\n\n*******************************************************************************\n" +
					"                 Welcome to your Hospital Database management System!     \n" +
					"***********************************************************************************\n");
       	   System.out.println();
     	  	   System.out.println("--------------------------Welcome to---------------------------");
     	  	   System.out.println("---------------------------------------------------------------");
       	   System.out.println(" 88                                88                    88    ");
       	   System.out.println(" 88                                     88               88    ");
      	   System.out.println(" 88,dba,  ,adba,   ,adba  888888,  88 MM88MMM  ,aPYba,   88    ");
      	   System.out.println(" 88   88 8b    d8 88      88    ad 88   8P     88   88   88    ");
      	   System.out.println(" 88   88 8b    88  ,8888, 88   ad  88   8P    88     88  88    ");
            System.out.println(" 88   88 8b    d8      88 88aad    88   8P P   88    88  88    ");
       	   System.out.println(" 88   88  `YbdP'  aadba,  88       88   8PP     'aPY' 8P 88888 ");
       	   System.out.println("                          88                                   ");
       	   System.out.println("                          88                                   ");
       	   System.out.println("---------------------------------------------------------------");
       	   System.out.println();
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. List all the requests addressed by a given maintenance staff ID");
				System.out.println("10. List all maintenance requests made by a given doctor name");
				System.out.println("11. List the specialized departments for a given hospital name");
				System.out.println("12. Find the appointment details for a given appointment number");
				System.out.println("13. < EXIT");

				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: ListRequestsAddressedbyStaff(esql); break;
					case 10: ListRequestsMadebyDoctor(esql); break;
					case 11: DepartmentsOfHospital(esql); break;
					case 12: DetailsOfAppointment(esql); break;
					case 13: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	static void NoExist() throws Exception { 
		  throw new IllegalArgumentException("ERROR: The id you input is not in our database, please retry");
   }
	
	static void checkid(String str) throws Exception { //any id
	  if (!str.matches("^[0-9]*$")) { 
		  throw new IllegalArgumentException("ERROR: Please enter number for the id\n");
	  }
   }
   static void checkGender(String str) throws Exception { //M, F, Other
	  if (!str.matches("(^M$)|(^F$)|(^Other$)")) { 
		  throw new IllegalArgumentException("ERROR: Please enter M for Male, F for female, Other for other.\n");
	  }
   }
   static void checkdate(String str) throws Exception { //MM/DD/YEAR
	  if (!str.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}")) { 
		  throw new IllegalArgumentException("ERROR: Please enter valid date format for MM/DD/YEAR, all is number only.\n");
	  }
   }

   static void checktime(String str) throws Exception { //00:00-00:00
	  if (!str.matches("[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9]")) { 
		  throw new IllegalArgumentException("ERROR: Please enter valid time slot format for 00:00-00:00, all is number only.\n");
	  }
   }
      static void checkStatus(String str) throws Exception { //AC,AV,WL,PA
	  if (!str.matches("(^AC$)|(^AV$)|(^WL$)|(^PA$)")) { 
		  throw new IllegalArgumentException("ERROR: Please enter corrected status, AV for available, AC for active, WL for waitlist, PA for past.\n");
	  }
   }
   
	public static void AddDoctor(DBproject esql) {//1.Add Doctor: Ask the user for details of a Doctor and add it to the database
		try {
			String query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (";
			System.out.print("\tPlease enter doctorid: ");
			String input1 = in.readLine();
			checkid(input1);
			query += "\'"+ input1 + "\',";
			System.out.print("\tPlease enter doctor name: ");
			String input2 = in.readLine();
			query += "\'"+ input2 + "\',";
			System.out.print("\tPlease enter doctor specialty: ");
			String input3 = in.readLine();
			query += "\'"+ input3 + "\',";
			System.out.print("\tPlease enter doctor departmentid: ");
			String input4 = in.readLine();
			checkid(input1);
			query += "\'" + input4 + "\');";

			esql.executeUpdate(query);
			System.out.print("\tYour entered data has successfully update\n");
			String query2 = "Select * \nFrom Doctor \nWhere doctor_ID = "+ input1 + ";";
			esql.executeQueryAndPrintResult(query2);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void AddPatient(DBproject esql) {//2.Add Patient: Ask the user for details of a Patient and add it to the database
		try {
			String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (";
			System.out.print("\tPlease enter patientid: ");
			String input5 = in.readLine();
			checkid(input5);
			query += "\'"+ input5 + "\',";
			System.out.print("\tPlease enter patient name: ");
			String input6 = in.readLine();
			query += "\'"+ input6 + "\',";
			System.out.print("\tPlease enter patient gender: M for Male, F for female, Other for other ");
			String input7 = in.readLine();
			checkGender(input7);
			query += "\'"+ input7 + "\',";
			System.out.print("\tPlease enter patient age: ");
			String input8 = in.readLine();
			checkid(input8);
			query += "\'"+ input8 + "\',";
			System.out.print("\tPlease enter patient address: ");
			String input9 = in.readLine();
			query += "\'"+ input9 + "\',";
			System.out.print("\tPlease enter patient number_of_appts: ");
			String input10 = in.readLine();
			checkid(input10);
			query += "\'" + input10 + "\');";
			
			esql.executeUpdate(query);
			System.out.print("\tYour entered data has successfully update\n");
			String query2 = "Select * \nFrom Patient \nWhere patient_ID = "+ input5 + ";";
			esql.executeQueryAndPrintResult(query2);	
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void AddAppointment(DBproject esql) {//3.Add Appointment: Ask the user for details of an Appointment and add it to the database.
		try {
			String query = "INSERT INTO Appointment (appnt_ID , adate, time_slot, status) VALUES (";
			System.out.print("\tPlease enter appointment id: ");			
			String input11 = in.readLine();
			checkid(input11);
			query += "\'"+ input11 + "\',";
			System.out.print("\tPlease enter appointment date ex:(MM/DD/YYYY): ");	
			String input12 = in.readLine();
			checkdate(input12);
			query += "\'"+ input12 + "\',";
			System.out.print("\tPlease enter appointment time slot ex:(12:00-14:00): ");
			String input13 = in.readLine();
			checktime(input13);
			query += "\'"+ input13 + "\',";
			System.out.print("\tPlease enter appointment status ex:(AC, AV, PA, WL): ");
			String input14 = in.readLine();
			query += "\'" + input14 + "\');";
			
			esql.executeUpdate(query);
			System.out.print("\t\nYour entered data has successfully update\n\n");
			String query2 = "Select * \nFrom Appointment \nWhere appnt_ID = "+ input11 + ";";
			esql.executeQueryAndPrintResult(query2);	
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4 Make an appointment: Given a patient, a doctor and an appointment of the doctor that s/he wants to take
		try {
		String pid;
		do {
			System.out.print("\tPlease enter patient id for adding or change appiontment : ");
			try {
				pid = in.readLine();
				checkid(pid);
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		String queryp = "select patient_ID \nfrom Patient \nwhere patient_ID = " + pid +" ;";
		int row = esql.executeQueryAndPrintResult(queryp);
		if(row != 0){
			System.out.println("The patient is alreay exist in the databasem, going to next step");			
		}
		else {
			System.out.println("The patient is not in our database, you will need to create a new patient information");
			try {
			String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (";
			System.out.print("\tPlease enter patientid: ");
			String input5 = in.readLine();
			checkid(input5);
			query += "\'"+ input5 + "\',";
			System.out.print("\tPlease enter patient name: ");
			String input6 = in.readLine();
			query += "\'"+ input6 + "\',";
			System.out.print("\tPlease enter patient gender: M for Male, F for female, Other for other ");
			String input7 = in.readLine();
			checkGender(input7);
			query += "\'"+ input7 + "\',";
			System.out.print("\tPlease enter patient age: ");
			String input8 = in.readLine();
			checkid(input8);
			query += "\'"+ input8 + "\',";
			System.out.print("\tPlease enter patient address: ");
			String input9 = in.readLine();
			query += "\'"+ input9 + "\',";
			System.out.print("\tPlease enter patient number_of_appts: ");
			String input10 = in.readLine();
			checkid(input10);
			query += "\'" + input10 + "\');";
			
			esql.executeUpdate(query);
			System.out.print("\tYour entered data has successfully update\n");
			String query2 = "Select * \nFrom Patient \nWhere patient_ID = \'"+ input5 + "\';";
			esql.executeQueryAndPrintResult(query2);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		}
		
		
		
		String did;
		do {
			System.out.print("\tPlease enter doctor id for searching his/her appiontment : ");
			try {
				did = in.readLine();
				checkid(did);
				String dcheck = "select doctor_ID \nfrom Doctor \nwhere doctor_ID = \'" + did +"\' ;";
				int dd = esql.executeQueryAndPrintResult(dcheck);
				if(dd == 0){
					NoExist();
				}
				
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		String aid;
		do {
			System.out.print("\tPlease enter appiontment id: ");
			try {
				aid = in.readLine();
				checkid(aid);
				String acheck = "select appnt_ID \nfrom Appointment \nwhere appnt_ID = \'" + aid +"\' ;";
				int aa = esql.executeQueryAndPrintResult(acheck);
				if(aa == 0){
					NoExist();
				}
				break;
			}catch (Exception e) {
				System.out.println("Invalid input");
				continue;
			}
		}while(true);
		String queryAddCheck = "Select * \nfrom has_appointment \nWhere appt_id = \'" + aid + "\' AND doctor_id = \'" + did + "\';";
		int row1 = esql.executeQueryAndPrintResult(queryAddCheck);
		if(row1 == 0){
		System.out.print("\t\nYou are makeing a new appiontment for selected doctor and appiontment id for this patient\n");
		String queryADD = "INSERT INTO has_appointment(appt_id, doctor_id) VALUES (\'" + aid + "\', \'" + did + "\');"; 
		esql.executeUpdate(queryADD);
		System.out.print("\t\nYou have successfully made a appiontment for this patient\n");
		}
		String query4_1 = "SELECT A.appnt_ID, A.adate, A.time_slot, A.status \nFROM Appointment A\nWHERE A.appnt_ID IN (SELECT appt_id \nFROM has_appointment H \nWHERE H.doctor_id = \'" + did + "\' AND H.appt_id = \'" + aid + "\' );";
		System.out.println("\nList of show you chosen appiontment: \n");
		try{
			esql.executeQueryAndReturnResult(query4_1);
		}catch(SQLException e) {
			System.err.println(e.getMessage());
		}
			
		String query1 = "SELECT A.status \nFROM Appointment A\nWHERE A.appnt_ID IN (SELECT appt_id \nFROM has_appointment H \nWHERE H.doctor_id = \'" + did + "\' AND H.appt_id = \'" + aid + "\' ) AND A.status = \'AV\';";
		int row2 = esql.executeQueryAndPrintResult(query1);
		String query2 = "SELECT A.status \nFROM Appointment A\nWHERE A.appnt_ID IN (SELECT appt_id \nFROM has_appointment H \nWHERE H.doctor_id = \'" + did + "\' AND H.appt_id = \'" + aid + "\' ) AND A.status = \'AC\';";
		int row3 = esql.executeQueryAndPrintResult(query2);
		String query3 = "SELECT A.status \nFROM Appointment A\nWHERE A.appnt_ID IN (SELECT appt_id \nFROM has_appointment H \nWHERE H.doctor_id = \'" + did + "\' AND H.appt_id = \'" + aid + "\' ) AND A.status = \'WL\';";
		int row4 = esql.executeQueryAndPrintResult(query3);
		String query4 = "SELECT A.status \nFROM Appointment A\nWHERE A.appnt_ID IN (SELECT appt_id \nFROM has_appointment H \nWHERE H.doctor_id = \'" + did + "\' AND H.appt_id = \'" + aid + "\' ) AND A.status = \'PA\';";
		int row5 = esql.executeQueryAndPrintResult(query4);
		if(row2 !=0){

				String queryAV = "UPDATE Appointment \nSET status = 'AC' \nWHERE appnt_ID = \'" + aid + "\';";
				String queryAV1 = "UPDATE Patient \nSET number_of_appts = (number_of_appts + 1) \nwhere patient_ID = \'" + pid +"\';";
				String queryAV2 = "Select P.patient_ID, P.name, A.appnt_ID, A.adate, A.time_slot, A.status, D.doctor_id, D.name \nfrom Patient P, Doctor D, Appointment A \nwhere D.doctor_ID = \'" + did +"\' AND A.appnt_ID = \'" + aid + "\' AND P.patient_ID = \'" + pid +"\';"; 
				try{
					esql.executeUpdate(queryAV);
					esql.executeUpdate(queryAV1);
					esql.executeQueryAndPrintResult(queryAV2);
					}catch(SQLException e) {
						System.err.println(e.getMessage());
					}	
				System.out.println("\n\nwe successfully put you in to the appiontment status from AV to AC\n");
				
			
		}
		
		 if(row3 !=0){
				String queryAC = "UPDATE Appointment \nSET status = 'WL' \nWHERE appnt_ID = \'" + aid + "\';";	
				String queryAC1 = "UPDATE Patient \nSET number_of_appts = (number_of_appts + 1) \nwhere patient_ID = \'" + pid +"\';";
				String queryAC2 = "Select P.patient_ID, P.name, A.appnt_ID, A.adate, A.time_slot, A.status, D.doctor_id, D.name \nfrom Patient P, Doctor D, Appointment A \nwhere D.doctor_ID = \'" + did +"\' AND A.appnt_ID = \'" + aid +"\' AND P.patient_ID = \'" + pid +"\';"; 
				try{
					esql.executeUpdate(queryAC);
					esql.executeUpdate(queryAC1);
					esql.executeQueryAndPrintResult(queryAC2);
					}catch(SQLException e) {
						System.err.println(e.getMessage());
					}	
				System.out.println("\n\nwe successfully put you in to the appiontment status from AC to WL\n");
				
			
		}
		
		
		 if(row4 !=0){
			String queryWL = "UPDATE Patient \nSET number_of_appts = (number_of_appts + 1) \nwhere patient_ID = \'" + pid +"\';";
			String queryWL1 = "Select P.patient_ID, P.name, A.appnt_ID, A.adate, A.time_slot, A.status, D.doctor_id, D.name \nfrom Patient P, Doctor D, Appointment A \nwhere D.doctor_ID = \'" + did +"\' AND A.appnt_ID = \'" + aid +"\' AND P.patient_ID = \'" + pid +"\';"; 
				try{
					esql.executeUpdate(queryWL);
					esql.executeQueryAndPrintResult(queryWL1);
					}catch(SQLException e) {
						System.err.println(e.getMessage());
					}	
			System.out.println("\n\nwe have add you to the waitlist of this appiontment\n");
			
			
		}
		
		
		 if(row5!=0){
			System.out.println("\n\nwe are sorry, the appiontment you booked is already past\n");
						
		}

		}catch(Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5 List appointments of a given doctor:
		try {
			String query = "SELECT A.appnt_ID, A.adate, A.time_slot, A.status FROM Appointment A, has_appointment H WHERE A.appnt_ID = H.appt_id AND (A.status = \'AC\' OR A.status = \'AV\') AND H.doctor_id = \'";
			System.out.print("\tPlease enter doctor id: ");
			String input15 = in.readLine();
			query += input15;
			query += "\' AND (A.adate BETWEEN \'"; 
			System.out.print("\tPlease enter first date of date range of the appt (MM/DD/YYYY): ");
			String input16 = in.readLine();
			checkdate(input16);
			query += (input16 + "\' AND \'");
			System.out.print("\tPlease enter second date of date range of the appt (MM/DD/YYYY): ");
			String input17 = in.readLine();
			checkdate(input17);
			query += (input17 + "\');");
						
			int row = esql.executeQueryAndPrintResult(query);
			System.out.println ("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6 List all available appointments of a given department:
		try {
			String query = "SELECT DISTINCT A.appnt_ID, D.name, A.adate, A.time_slot FROM Appointment A, has_appointment H, Doctor D, Department DEPT \nWHERE DEPT.name = \'";
			System.out.print("\tPlease enter department name: ");
			String dname = in.readLine();
			query += dname + "\' ";
			System.out.print("\tPlease enter the specific date: (MM/DD/YEAR):");
			String date = in.readLine();
			checkdate(date);
			query += " AND A.adate = \'" + date + "\' AND A.status = \'AV\' AND A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID;"; 

			System.out.print("\t\nThe list below is all available appiontment for the Department you entered and the date you select\n: ");
			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
			
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7 List total number of different types of appointments per doctor in descending order
		try {
			String query = "SELECT D.doctor_ID, A.status, COUNT(*) AS NAPPNT FROM Appointment A, has_appointment H, Doctor D WHERE A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID GROUP BY D.doctor_ID, A.status ORDER BY NAPPNT DESC";
			
			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8 Find total number of patients per doctor with a given status: Given an appointment status, return the number of patients per doctor with the given status.
		try {
			System.out.print("\tPlease enter appointment status ex:(AC, AV, PA, WL): ");
			String input18 = in.readLine();
			String query = "SELECT H.doctor_id, A.status, COUNT( DISTINCT S.pid ) AS NPATIENT FROM Appointment A, has_appointment H, searches S WHERE S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id GROUP BY H.doctor_id, A.status HAVING A.status = \'";
			checkStatus(input18);
			//"SELECT H.doctor_id, COUNT( DISTINCT S.pid ) AS NPATIENT FROM Appointment A, has_appointment H, searches S WHERE S.aid = A.appnt_ID AND A.appnt_ID = H.appt_id GROUP BY H.doctor_id HAVING A.status = '" + input18 + "' ;" ;
			query += (input18 + "\';");
			

			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListRequestsAddressedbyStaff(DBproject esql) {//9 Given a maintenance staff ID, list all the requests addressed by the staff.
		try {
			String query = "SELECT patient_per_hour, dept_name, time_slot, did FROM request_maintenance WHERE sid = ";
			System.out.print("\tPlease enter staff id: ");
			String input19 = in.readLine();
			query += (input19 + ";");

			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListRequestsMadebyDoctor(DBproject esql) {//10 Given a doctor name, list all maintenance requests made by the doctor.
		try {
			String query = "SELECT R.patient_per_hour, R.dept_name, R.time_slot, R.sid FROM request_maintenance R, Doctor D WHERE R.did = D.doctor_ID AND D.name = \'";
			System.out.print("\tPlease enter doctor name: ");
			String input20 = in.readLine();
			query += (input20 + "\';");

			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void DepartmentsOfHospital(DBproject esql) {//11 Given a hospital name, find the specialized departments in the hospital.
		try {
			String query = "SELECT D.name FROM Hospital H, Department D WHERE H.hospital_ID = D.hid AND H.name = \'";
			System.out.print("\tPlease enter hospital name: ");
			String input21 = in.readLine();
			query += (input21 + "\';");

			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void DetailsOfAppointment(DBproject esql) {//12 Given an appointment number, find the appointment details (time slot, doctor name, department, etc.
		try {
			String query = "SELECT D.name, A.time_slot, Dept.name FROM Appointment A, has_appointment H, Doctor D, Department Dept WHERE A.appnt_ID = H.appt_id AND H.doctor_id = D.doctor_ID AND D.did = Dept.dept_ID AND A.appnt_ID = ";
			System.out.print("\tPlease enter appointment id: ");
			String input22 = in.readLine();
			query += (input22 + ";");

			int row = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + row);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
}