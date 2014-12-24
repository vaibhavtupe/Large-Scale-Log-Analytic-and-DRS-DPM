package edu.sjsu.cmpe283.api.pgms;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.types.ObjectId;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;
//import com.google.code.morphia.emul.org.bson.types.ObjectId;

public class newMongoToMySQL 
{
	private static DB db;

	private static Connection conn;
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	private static final String URL = "jdbc:mysql://project2-283.cvz5dtczqgms.us-west-1.rds.amazonaws.com";
	private static final String USER = "root";
    private static final String PASSWORD = "rootroot";

	private static DB getConnection() throws UnknownHostException {
		if (db == null) {
			
			String textUri = "mongodb://vaibhav:tupe@ds053090.mongolab.com:53090/project2";
			MongoClientURI uri = new MongoClientURI(textUri);
			MongoClient m;
			try {
				m = new MongoClient(uri);
				db = m.getDB("project2");
				System.out.println("DATABASE :" + db);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} 
			
		
		return db;
	}

	public static Connection getMysqlConnection() 
	{
		if (conn == null)
		{
			try {
				Class.forName(DRIVER);
				
				conn = DriverManager.getConnection(URL,USER,PASSWORD);
		
				
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}


	public static void insertFromMongodbToMySQL()  {
		
		DBCollection collection;
		try {
			collection = getConnection().getCollection("Logs");
		
			
			String grp = "{$group:{_id:'$VM Name',Unknown:{$avg:'$Unknown'},Cpu_Usage:{$avg:'$Cpu Usage'},"
					+ "Memory_Usage:{$avg:'$Memory Usage'},Net_Usage:{$avg:'$Net Usage'},Disk_Usage:{$avg:'$Disk Usage'}}}";
			DBObject group = (DBObject) JSON.parse(grp);
			
		
			
			
			AggregationOutput output = collection.aggregate(group);
			for (DBObject result : output.results()) {
			   // System.out.println(result);
			    insertIntoMySQL(result);			   
			}
		
			// flushing all the documents in mongodb
			collection.remove(new BasicDBObject());
		} catch (UnknownHostException e) {
		
			e.printStackTrace();
		}
	}
	
	public static void insertIntoMySQL(DBObject object)
	{
		
		 PreparedStatement st;
			try {
			
				
				Calendar calendar = Calendar.getInstance();
				java.util.Date now = calendar.getTime();
				java.sql.Timestamp Timestamp_log = new java.sql.Timestamp(now.getTime());
				
				st = (PreparedStatement) getMysqlConnection().prepareStatement("insert "
						+ " into project283.logs_aggregate1(VM_Name,Cpu_Usage,Unknown,Net_Usage,Disk_Usage,Memory_Usage,Timestamp_log)"
						+ "values(?,?,?,?,?,?,?)");
	
			
			st.setString(1, object.get("_id").toString());
			st.setDouble(2,  (Double) object.get("Cpu_Usage"));
			st.setDouble(3, (Double) object.get("Unknown"));
			st.setDouble(4, (Double)  object.get("Net_Usage"));
			st.setDouble(5, (Double) object.get("Disk_Usage"));
			st.setDouble(6,(Double)  object.get("Memory_Usage"));
			st.setTimestamp(7, Timestamp_log);		
				st.executeUpdate();
				
				System.out.println("record inserted");
			} catch (SQLException e) {
			
				e.printStackTrace();
			}
	}


		

	
	
	static Thread t1 = new Thread()
	{
		public void run(){
			while(true){
			
				insertFromMongodbToMySQL();
				try {
					//t1.sleep(10000);
					
					t1.sleep(60000);
				} catch (InterruptedException e) {
				
					e.printStackTrace();
				}
			
		}
		}
	};
	

	

	public static void getCPUUsage()
	{
		
		 PreparedStatement st;
			try {
				Statement stmt = null;
				//conn = DriverManager.getConnection(DB_URL, USER, PASS)
				stmt = conn.createStatement();
String vmname ="T04-VM02-Ubuntu32";
			      String sql = "SELECT Cpu_Usage from project283.logs_aggregate1 where VM_Name="+"'"+vmname+"'";
			      ResultSet rs = stmt.executeQuery(sql);
				
			      while(rs.next()){
			         
			          System.out.println("CPU Usage: " + rs.getDouble(1));
			         
			       }
				
				System.out.println("record inserted");
			} catch (SQLException e) {
			
				e.printStackTrace();
			}
	}

	private static void archivedata() throws UnknownHostException {
		DBCollection tbl = getConnection().getCollection("Logs");
		Date today = new Date();
		@SuppressWarnings("deprecation")
		String atblname = "archive"+today.getYear()+today.getMonth()+today.getDate();
		DBCollection atbl = getConnection().getCollection(atblname);
		DBCursor cur = tbl.find();
		while (cur.hasNext()) {
			atbl.insert(cur.next());
		}
		tbl.drop();
	}

	public static void main(String[] args) throws UnknownHostException 
	{
		t1.start();
		/*getMysqlConnection();
		getCPUUsage();*/
		
	}
	
	
	
	
}

