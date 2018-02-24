import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDB {
	
	private String dbPath;
	private String dbName;
	private Connection connection;
	
	public SQLiteDB(String dbPath, String dbName) {
		this.dbName = dbName;
		this.dbPath = dbPath;
	}
	
	
	// DB Connection 
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            String file = this.dbPath + this.dbName;
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (ClassNotFoundException | SQLException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        	System.out.println("The connection to the SQLite DB failed");
            this.connection = null;
        }
    }
    
    // Close DB connection
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
        	System.out.println("Closing the SQLite DB connection failed");
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Create Table 
    public void createTable() throws SQLException {
    	if (this.connection == null) {
            this.connect();
        }
    	try {
    		String SQL = "CREATE TABLE IF NOT EXISTS SPELLS (NAME TEXT NOT NULL UNIQUE"
                        + ", LEVEL NUMERIC, COMPONENT TEXT, SPELL_RESISTANCE BOOLEAN);";
    		Statement stt = this.connection.createStatement();
    		stt.executeUpdate(SQL);
    	} catch (SQLException e) {
            	
    		System.out.println("Creating table failed : "+e);
    	}  
    }
    
    // Insert spells
    public void insert(String name, int level, String component, boolean resistance) {
        if (this.connection == null) {
            this.connect();
        }
 
        try {
        	
            String SQL = "INSERT OR REPLACE INTO SPELLS VALUES(?,?,?,?);";
            PreparedStatement ins_stmt = connection.prepareStatement(SQL);
            
            ins_stmt.setString(1, name);
            ins_stmt.setInt(2, level);
            ins_stmt.setString(3, component);
            ins_stmt.setBoolean(4, resistance);
            
 
            ins_stmt.executeUpdate();
        } catch (SQLException e) {
        	
        	System.out.println("Table insert failed : "+e);
            
        }
    }
    
    // Select the right spells
    public void select_Good_Spells() {
    	
        if (this.connection == null) {
            this.connect();
        }
 
        try {
            String SQL="SELECT * FROM SPELLS WHERE (LEVEL<=? AND SPELL_RESISTANCE=? AND COMPONENT LIKE '%V%')";
            PreparedStatement stmt = connection.prepareStatement(SQL);
            stmt.setInt(1, 4);
            stmt.setBoolean(2, false);
            //stmt.setString(3, "V");
            ResultSet rs = stmt.executeQuery();
 
            System.out.println("Spells that can save Pito : ");
            int i=1;
            while (rs.next()) {
            	//System.out.println(i+" : "+rs.getString("COMPONENT"));
                System.out.println(i+" : "+rs.getString("NAME")+", level "+rs.getInt("LEVEL"));
            	i++;
            }
 
            stmt.close();
        } catch (SQLException e) {
        	
        	System.out.println("Selecting good spells failed : "+e);
        	
        }
    }
}
