package ch.fhnw.thesis;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Connector {

    public static Connection con;
    private Statement statement;
    
    
    public Connector(File dbDirectory, String dbName, String user, String password) throws Exception{
        System.out.println("[DATABASE] Connection to the database");
        if(con==null || con.isClosed()){
            Class.forName("org.h2.Driver");
            con=DriverManager.getConnection("jdbc:h2:"+dbDirectory.toString()+"\\"+dbName, user, password);
            statement = con.createStatement();
        }
        System.out.println("[DATABASE] Connection established");
    }
    
    public void close() throws Exception{
        System.out.println("[DATABASE] Closing connection");
        con.close();
        System.out.println("[DATABASE] Connection closed");
    }
    
    public void insertRow(String tableName, int messageNumberAPM, float longitude, float latitude, float altitude, int timeElapsed, int moduleNumber, int messageNumberController, String[] param) throws Exception
    {
        String insertSQL = "insert into "+tableName+" values("
                + messageNumberAPM+", "
                + longitude+", "
                + latitude+", "
                + altitude+", "
                + "'"+String.format("%02d",((int)(timeElapsed / 1000 / 60 / 60) % 24))+":"+String.format("%02d",((int)(timeElapsed / 1000 / 60) % 60))+":"+String.format("%02d",((int)(timeElapsed / 1000) % 60))+"s "+String.format("%03d",timeElapsed % 1000)+"ms', "
                + timeElapsed+", "
                + "'"+Main.MODULE_NAME[moduleNumber+1]+"', "
                + messageNumberController;
        if(param != null)
            for(int i =0; i<param.length; i++)
            {
                insertSQL+=", '"+param[i]+"'";
            }
        insertSQL += ")";
        statement.execute(insertSQL);
    }

    public void createTable(String tableName, int moduleNumber) throws Exception
    {
        String createTableSQL = "create table "+tableName+"("
                + "APM_MESSAGE_NUMBER int not null, "
                + "LATITUDE float(3.8) not null, "
                + "LONGITUDE float(3.8) not null, "
                + "ALTITUDE float(3.8) not null, "
                + "ELAPSED_TIME varchar(50) not null, "
                + "ELAPSED_TIME_IN_MS int not null, "
                + "MODULE varchar(50) not null, "
                + "MESSAGE_NUMBER_CONTROLLER int not null";
        for(int i =0; i<Main.PARAMETER_NAME[moduleNumber+1].length; i++)
        {
            createTableSQL+=", "+Main.PARAMETER_NAME[moduleNumber+1][i]+" varchar(50) not null";
        }
        createTableSQL += ")";
        statement.execute(createTableSQL);
        System.out.println("[DATABASE] New table created");
    }
    
}
