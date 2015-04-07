package com.josiah;

import java.sql.*;
import java.sql.Date;
import java.time.DateTimeException;
import java.util.*;

public class Main {

    // JDBC driver name, protocol, used to create a connection to the DB

    private static String protocol = "jdbc:derby:";
    private static String dbName = "beehiveDB1";

    //  Database credentials - for embedded, usually defaults. A client-server DB would need to authenticate connections
    private static final String USER = "temp";
    private static final String PASS = "password";

    public static void main(String[] args) {

        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        PreparedStatement psInsert = null;
        LinkedList<Statement> allStatements = new LinkedList<Statement>();

        try{

            conn = DriverManager.getConnection(protocol + dbName + ";create=true", USER, PASS);
            statement = conn.createStatement();
            allStatements.add(statement);

            System.out.println("Average Weather Database Program");

            //Create a table in the database. Stores today's date, and the min and max temperatures recorded.

            String createTableSQL = "CREATE TABLE temp (collectionDay date, honeyWeight double)";
            String deleteTableSQL = "DROP TABLE temp";
            try {
                statement.executeUpdate(createTableSQL);
                System.out.println("Created temp table");
            } catch (SQLException sqle) {
                //Seems the table already exists. Delete it and recreate it
                if (sqle.getSQLState().startsWith("X0") ) {    //Error code for table already existing start with XO
                    System.out.println("Temp table appears to exist already, delete and recreate");
                    statement.executeUpdate(deleteTableSQL);
                    statement.executeUpdate(createTableSQL);
                } else {
                    //Something else went wrong. If we can't create the table, no point attempting
                    //to run the rest of the code. Throw the exception again to be handled at the end of the program.
                    throw sqle;
                }
            }

            //Add some test data

            String prepStatInsert = "INSERT INTO temp VALUES ( ?, ? )";

            psInsert = conn.prepareStatement(prepStatInsert);
            allStatements.add(psInsert);






            //todo get this working. Exceptions need work.






            Scanner sc = new Scanner(System.in);

            for (int x = 0; x < 4; x++) {
                String tempDate;
                double tempWeight;
                try {
                    System.out.println("What was the date you harvested the honey from hive " + (x+1) + "? (YYYY-MM-DD)");
                    tempDate = sc.nextLine();
                    System.out.println("What was the was the weight(lbs) of the collected honey?");
                    tempWeight = sc.nextDouble();
                    psInsert.setDate(1, Date.valueOf(tempDate));
                    psInsert.setDouble(2, tempWeight);
                } catch (IllegalArgumentException iae){
                    iae.printStackTrace();
                    System.out.println("The date was not in the correct format. Please try again.\nformat: YYYY-MM-DD (2014-04-01)");
                    tempDate = sc.nextLine();
                    psInsert.setDate(1, Date.valueOf(tempDate));
                } catch (InputMismatchException ime) {
                    System.out.println("The weight was not input correctly. Please try again with just numbers.");
                    tempWeight = sc.nextDouble();
                    psInsert.setDouble(2, tempWeight);
                }
            }

            sc.close();





















            //Let's calculate the average minumum and average maximum temperature for all the days.
            //Add up all the maximum temperatures and divide by number of days to get average max temperature.
            //Add up all the minimum temperatures and divide by number of days to get average min temperature.

            double sumMaxTemp = 0, averageMaxTemp;
            double sumMinTemp = 0, averageMinTemp;
            //added a seperate max and min for the total days for easier coding later.
            int totalDaysMin = 0;
            int totalDaysMax = 0;
            String fetchTempsSQL = "SELECT mintemp, maxtemp FROM temp ";
            rs = statement.executeQuery(fetchTempsSQL);
            //here is where I added code to not add the days that are null.
            while (rs.next()) {
                double thisDayMin = rs.getDouble("mintemp");
                //as long as the data is not null, the day is added to total days.
                if (!rs.wasNull()) {
                    sumMinTemp += thisDayMin;
                    totalDaysMin++;
                }
                double thisDayMax = rs.getDouble("maxtemp");
                if (!rs.wasNull()) {
                    sumMaxTemp += thisDayMax;
                    totalDaysMax++; //keep track of how many rows processed.
                }
            }
            //added the new ints so math is correct here
            averageMaxTemp = sumMaxTemp / totalDaysMax;
            averageMinTemp = sumMinTemp / totalDaysMin;

            System.out.println("Average maximum temperature = " + averageMaxTemp + " , average minimum temperature = " + averageMinTemp);


        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            //A finally block runs whether an exception is thrown or not. Close resources and tidy up whether this code worked or not.
            try {
                if (rs != null) {
                    rs.close();  //Close result set
                    System.out.println("ResultSet closed");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }

            //Close all of the statements. Stored a reference to each statement in allStatements so we can loop over all of them and close them all.
            for (Statement s : allStatements) {

                if (s != null) {
                    try {
                        s.close();
                        System.out.println("Statement closed");
                    } catch (SQLException se) {
                        System.out.println("Error closing statement");
                        se.printStackTrace();
                    }
                }
            }

            try {
                if (conn != null) {
                    conn.close();  //Close connection to database
                    System.out.println("Database connection closed");
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        System.out.println("End of program");
    }
}
