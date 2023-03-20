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
import java.lang.Math;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
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
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
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
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
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
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count number of results.
       while (rs.next()){
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
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
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
      }catch (SQLException e){
         // ignored.
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
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql); break;
                   case 5: updateRoomInfo(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewBookingHistoryofHotel(esql); break;
                   case 8: viewRegularCustomers(esql); break;
                   case 9: placeRoomRepairRequests(esql); break;
                   case 10: viewRoomRepairHistory(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to clean up the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "                     User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
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

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewHotels(Hotel esql) {
      try {
         // Get user input
         System.out.print("Enter latitude: ");
         double latitude = Double.parseDouble(in.readLine());
         System.out.print("Enter longitude: ");
         double longitude = Double.parseDouble(in.readLine());

         // SQL query to select hotels within 30 units of distance
         String query = "SELECT * FROM Hotel H " +
                 "WHERE calculate_distance(" + latitude + ", " + longitude + ", H.latitude, H.longitude) <= 30;";

         // Execute the query and print the results
         int rowCount = esql.executeQueryAndPrintResult(query);
         if (rowCount == 0) {
            System.out.println("Sorry, no hotel found within 30 units from given place.");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewRooms(Hotel esql) {
      try {
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

         // Get user input
         System.out.print("Enter hotel ID: ");
         int hotelID = Integer.parseInt(in.readLine());
         System.out.print("Enter date (YYYY-MM-DD): ");
         String inputDate = in.readLine();

         // Check the date format
         SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
         dateFormat.setLenient(false);
         Date date;
         try {
            date = dateFormat.parse(inputDate);
         } catch (ParseException e) {
            System.err.println("Invalid date format! Please enter as 'YYYY-MM-DD'.");
            return;
         }

         // Define the SQL query to select rooms with their price and availability on the given date
         // Use CASE WHEN to check if the room is available on the given date, it works like an if eles statement
         String query = String.format("SELECT R.roomNumber, R.price, (CASE WHEN B.bookingID IS NULL THEN 'Available' ELSE 'Not Available' END) AS availability FROM Rooms R LEFT JOIN RoomBookings B ON R.hotelID = B.hotelID AND R.roomNumber = B.roomNumber AND B.bookingDate = '%s' WHERE R.hotelID = '%s';", inputDate, hotelID);

         // Execute the SQL query
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.isEmpty()) {
            System.out.println("No room found for the given hotel ID and date.");
            return;
         }
         // Print the results
         System.out.println("Room Number\tPrice\tAvailability");
         for (List<String> row : result) {
            System.out.println(row.get(0) + "\t" + row.get(1) + "\t" + row.get(2));
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void bookRooms(Hotel esql) {
      try {
         // get user input
         System.out.print("Enter hotelID: ");
         String hotelID = in.readLine();
         System.out.print("Enter room number: ");
         String roomNumber = in.readLine();
         System.out.print("Enter booking date (YYYY-MM-DD): ");
         String bookingDate = in.readLine();

         // Check if the room is available on the given date
         String checkAvailabilityQuery = String.format("SELECT * FROM RoomBookings WHERE hotelID = '%s' AND roomNumber = '%s' AND bookingDate = '%s'", hotelID, roomNumber, bookingDate);
         int roomAvailability = esql.executeQuery(checkAvailabilityQuery);

         if (roomAvailability == 0) {
            // Room is available
            // Fetch the room price from the Rooms table
            String roomPriceQuery = String.format("SELECT price FROM Rooms WHERE hotelID = '%s' AND roomNumber = '%s'", hotelID, roomNumber);
            List<List<String>> roomPriceResult = esql.executeQueryAndReturnResult(roomPriceQuery);
            String roomPrice = roomPriceResult.get(0).get(0);

            // Insert the booking into the RoomBookings table
            // Here can use a trigger to update the RoomBookings table
            // TODO: get the customerID from the user
            String insertBookingQuery = String.format("INSERT INTO RoomBookings (customerID, hotelID, roomNumber, bookingDate) VALUES ('CUSTOMER_ID', '%s', '%s', '%s')", /* get customerID */ , hotelID, roomNumber, bookingDate);
            esql.executeUpdate(insertBookingQuery);

            // Display the room price to the customer
            System.out.println("Booking successfully! Room price: $" + roomPrice);
         } else {
            // Room is not available, display a message
            System.out.println("The room is not available on the selected date.");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void updateRoomInfo(Hotel esql) {
      try {
         // TODO: get the managerID from the Users table
         String managerID ;

         System.out.print("Enter hotelID: ");
         String hotelID = in.readLine();
         System.out.print("Enter room number: ");
         String roomNumber = in.readLine();

         // Check if the manager manages the hotel with the given hotelID
         String managerCheckQuery = String.format("SELECT * FROM Hotels WHERE hotelID = '%s' AND managerID = '%s'", hotelID, managerID);
         int managerCheck = esql.executeQuery(managerCheckQuery);

         if (managerCheck > 0) {
            // Manager can update the room information
            // Get current room information
            String currentInfoQuery = String.format("SELECT price, imageURL FROM Rooms WHERE hotelID = '%s' AND roomNumber = '%s'", hotelID, roomNumber);
            List<List<String>> currentInfoResult = esql.executeQueryAndReturnResult(currentInfoQuery);
            String oldPrice = currentInfoResult.get(0).get(0);
            String oldImageURL = currentInfoResult.get(0).get(1);

            // Get the new room information
            System.out.print("Enter new price: ");
            String newPrice = in.readLine();
            System.out.print("Enter new image URL: ");
            String newImageURL = in.readLine();

            // Update room information in the Rooms table
            String updateRoomQuery = String.format("UPDATE Rooms SET price = '%s', imageURL = '%s' WHERE hotelID = '%s' AND roomNumber = '%s'", newPrice, newImageURL, hotelID, roomNumber);
            esql.executeUpdate(updateRoomQuery);

            // Log the update in the RoomUpdatesLog table
            // TODO: can use a trigger to update the RoomUpdatesLog table
            String updateLogQuery = String.format("INSERT INTO RoomUpdatesLog (managerID, hotelID, roomNumber, updatedOn) VALUES ('%s', '%s', '%s', NOW())", managerID, hotelID, roomNumber);
            esql.executeUpdate(updateLogQuery);

            System.out.println("Room information updated successfully!");

            // Show the last 5 recent updates
            String recentUpdatesQuery = String.format("SELECT * FROM RoomUpdatesLog WHERE managerID = '%s' ORDER BY updatedOn DESC LIMIT 5", managerID);
            List<List<String>> recentUpdatesResult = esql.executeQueryAndReturnResult(recentUpdatesQuery);

            System.out.println("******************* Last 5 recent updates: *******************");
            for (List<String> update : recentUpdatesResult) {
               System.out.println(" --> " + update.toString());
            }
         } else {
            // Manager cannot update the room information
            System.out.println("You do not manage the specified hotel and have no access to update the room information.");
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewBookingHistoryofHotel(Hotel esql) {
      try {
         // TODO: get the customerID from the Users table
         String customerID ;

         // Retrieve the last 5 recent bookings of the customer from the RoomBookings table
         // TODO: check this query cause I am not sure if it is correct
         String bookingHistoryQuery = String.format("SELECT RB.hotelID, RB.roomNumber, R.price, RB.bookingDate FROM RoomBookings RB, Rooms R WHERE RB.hotelID = R.hotelID AND RB.roomNumber = R.roomNumber AND customerID = '%s' ORDER BY bookingDate DESC LIMIT 5", customerID);
         List<List<String>> bookingHistoryResult = esql.executeQueryAndReturnResult(bookingHistoryQuery);

         // Display the booking history
         System.out.println("**************** Your last 5 recent bookings: ****************");
         for (List<String> booking : bookingHistoryResult) {
            String hotelID = booking.get(0);
            String roomNumber = booking.get(1);
            String billingInfo = booking.get(2);
            String bookingDate = booking.get(3);

            System.out.println(" --> Hotel ID: " + hotelID + ", Room Number: " + roomNumber + ", Billing information: " + billingInfo + ", Booking Date: " + bookingDate);
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentBookingsfromCustomer(Hotel esql) {
      try {
         // TODO: get the managerID from the Users table
         String managerID;

         // Get the range of dates from the manager
         //TODO: this todo part can be change into a while loop
         System.out.print("Enter the start date (YYYY-MM-DD): ");
         String beginDate = in.readLine();
         System.out.print("Enter the end date (YYYY-MM-DD): ");
         String endDate = in.readLine();

         // check the date input
         SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
         dateFormat.setLenient(false);

         try {
            Date begin = dateFormat.parse(beginDate);
            Date end = dateFormat.parse(endDate);
         } catch (Exception e) {
            System.err.println("Invalid date format.");
            return;
         }
         //TODO

         // Retrieve the booking information from the RoomBookings table within the date range
         String bookingQuery = String.format(
                 "SELECT RB.bookingID, U.name, RB.hotelID, RB.roomNumber, RB.bookingDate " +
                         "FROM RoomBookings RB, Users U, Hotel H " +
                         "WHERE RB.customerID = U.userID AND RB.hotelID = H.hotelID AND H.managerUserID = '%s' AND RB.bookingDate BETWEEN '%s' AND '%s' " +
                         "ORDER BY RB.bookingDate", managerID, beginDate, endDate
         );
         List<List<String>> bookingResult = esql.executeQueryAndReturnResult(bookingQuery);

         // Display the booking information
         System.out.println("**************** Booking information: ****************");
         for (List<String> booking : bookingResult) {
            String bookingID = booking.get(0);
            String customerName = booking.get(1);
            String hotelID = booking.get(2);
            String roomNumber = booking.get(3);
            String bookingDate = booking.get(4);

            System.out.println(" --> Booking ID: " + bookingID + ", Customer Name: " + customerName + ", Hotel ID: " + hotelID + ", Room Number: " + roomNumber + ", Booking Date: " + bookingDate);
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentUpdates(Hotel esql) {}
   public static void viewRegularCustomers(Hotel esql) {}
   public static void placeRoomRepairRequests(Hotel esql) {}
   public static void viewRoomRepairHistory(Hotel esql) {}

}//end Hotel

