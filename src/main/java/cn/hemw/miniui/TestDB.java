package cn.hemw.miniui;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

public class TestDB {
	public static String driver;
	public static String url;
	public static String user;
	public static String pwd;
	
	static {
	    InputStream in = TestDB.class.getClassLoader().getResourceAsStream("miniui.properties");
	    if(in == null) {
	        System.err.println("系统配置文件 miniui.properties 不存在");
	        System.exit(1);
	    }
	    
	    try {
	        Properties prop = new Properties();
            prop.load(in);
            driver = prop.getProperty("jdbc.driver");
            url = prop.getProperty("jdbc.url");
            user = prop.getProperty("jdbc.username");
            pwd = prop.getProperty("jdbc.password");
            
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
	}

	////////////////////////////////////////////////////
	public String InsertNode(HashMap<String, Object> n) throws Exception
	{
		
		String sql = "insert into plus_file (id, name, type, size, url, pid, createdate, updatedate, folder, num)"
            + " values(?,?,?,?,?,?,?,?,?,?)";
		
		DBInsert(sql, n);
		return n.get("id").toString();
	}
	
	public void RemoveNode(HashMap<String, Object> n) throws Exception
	{
		String id = n.get("id").toString();
		Connection conn = getConn();		
		Statement stmt = conn.createStatement();
		
        String sql = "delete from plus_file where id = \""+id+"\"";        		
        stmt.executeUpdate(sql);
                
		stmt.close();
		conn.close();
	}
	
	public void UpdateTreeNode(HashMap<String, Object> n) throws Exception
	{
		
		
		String sql =
    		"update plus_file " 
    		+	" set "
    		+	" name = ?,"
    		+	" pid = ?,"
    		+	" num = ?"
    		+" where id = ?";
    
		Connection conn = getConn();		
			
		PreparedStatement stmt = conn.prepareStatement(sql);	
		
		stmt.setString(1, ToString(n.get("name")));
		stmt.setString(2, ToString(n.get("pid")));
		stmt.setInt(3, ToInt(n.get("num")));
		stmt.setString(4, ToString(n.get("id")));
		
		stmt.executeUpdate();		
	    stmt.close();
		conn.close();   
	}
	
    public ArrayList<HashMap<String, Object>> GetDepartments() throws Exception
    {
        String sql = "select *"
					+" from t_department";
        ArrayList<HashMap<String, Object>> data = DBSelect(sql);
        return data;
    }
    public HashMap<String, Object> GetDepartment(String id) throws Exception
    {
    	String sql = "select * from t_department where id = '" + id + "'";
        ArrayList<HashMap<String, Object>> data = DBSelect(sql);
        return data.size() > 0 ? data.get(0) : null;
    }
    public ArrayList<HashMap<String, Object>> GetPositions() throws Exception
    {
    	String sql = "select * from t_position";
        ArrayList<HashMap<String, Object>> data = DBSelect(sql);
        return data;
    }
    public ArrayList<HashMap<String, Object>> GetEducationals() throws Exception
    {
    	String sql = "select * from t_educational";
        ArrayList<HashMap<String, Object>> data = DBSelect(sql);
        return data;
    }
    public ArrayList<HashMap<String, Object>> GetPositionsByDepartmenId(String departmentId) throws Exception
    {        
    	String sql = "select * from t_position where dept_id = '" + departmentId + "'";
        ArrayList<HashMap<String, Object>> dataAll = DBSelect(sql);
        return dataAll;

    }
    public HashMap<String, Object> GetDepartmentEmployees(String departmentId, int index, int size) throws Exception
    {
        String sql = "select * from t_employee where dept_id = '" + departmentId + "'";
        ArrayList<HashMap<String, Object>> dataAll = DBSelect(sql);
        
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int start = index * size, end = start + size;

        for (int i = 0, l = dataAll.size(); i < l; i++)
        {
            HashMap<String, Object> record = dataAll.get(i);
            if (record == null) continue;
            if (start <= i && i < end)
            {
                data.add(record);
            }
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("data", data);
        result.put("total", dataAll.size());
        
        return result;
    }

    public HashMap<String, Object> SearchEmployees(String key, int index, int size, String sortField, String sortOrder) throws Exception
    {
        //System.Threading.Thread.Sleep(300);
    	if(key == null) key = "";
    	
    	String sql = 
 "select a.*, b.name dept_name, c.name position_name, d.name educational_name \n"
+"from t_employee a \n"
+"left join t_department b \n"
+"on a.dept_id = b.id \n"
+"left join t_position c \n"
+"on a.position = c.id \n"
+"left join t_educational d \n"
+"on a.educational = d.id \n"
+"where a.name like '%" + key + "%' \n";

        if (StringUtil.isNullOrEmpty(sortField) == false)
        {
            if ("desc".equals(sortOrder) == false) sortOrder = "asc";
            sql += " order by " + sortField + " " + sortOrder;
        }
        else
        {
            sql += " order by createtime desc";
        }

        ArrayList<HashMap<String, Object>> dataAll = DBSelect(sql);
        
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int start = index * size, end = start + size;

        for (int i = 0, l = dataAll.size(); i < l; i++)
        {
            HashMap<String, Object> record = dataAll.get(i);
            if (record == null) continue;
            if (start <= i && i < end)
            {
                data.add(record);
            }
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.set(2000,10,10,1,1,1);
            record.put("createtime", new Timestamp(calendar.getTimeInMillis()));
        }

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("data", data);
        result.put("total", dataAll.size());

        //minAge, maxAge, avgAge
        ArrayList<HashMap<String, Object>> ages = DBSelect("select min(age) as minAge, max(age) as maxAge, avg(age) as avgAge from t_employee");
        HashMap<String, Object> ageInfo = ages.get(0);
        result.put("minAge", ageInfo.get("minAge"));
        result.put("maxAge", ageInfo.get("maxAge"));
        result.put("avgAge", ageInfo.get("avgAge"));
        



        return result;
    }
    public HashMap<String, Object> GetEmployee(String id) throws Exception
    {
    	String sql = "select * from t_employee where id = '"+id+"'";
        ArrayList<HashMap<String, Object>> data = DBSelect(sql);
        return data.size() > 0 ? data.get(0) : null;
    }
    public String InsertEmployee(HashMap<String, Object> user) throws Exception
    {
    	String id = (user.get("id") == null || user.get("id").toString().equals(""))? UUID.randomUUID().toString() : user.get("id").toString();
        user.put("id", id);

        if (user.get("name") == null) user.put("name", "");
        if (StringUtil.isNullOrEmpty(user.get("gender"))) user.put("gender", 0);
        
    	Connection conn = getConn();	
    	
    	String sql = "insert into t_employee (id, loginname, name, age, married, gender, birthday, country, city, dept_id, position, createtime, salary, educational, school, email, remarks)"
            + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
		PreparedStatement stmt = conn.prepareStatement(sql);
		
		stmt.setString(1, ToString(user.get("id")));
		stmt.setString(2, ToString(user.get("loginname")));
		stmt.setString(3, ToString(user.get("name")));
		stmt.setInt(4, ToInt(user.get("age")));
		stmt.setInt(5, ToInt(user.get("married")));
		stmt.setInt(6, ToInt(user.get("gender")));
		stmt.setTimestamp(7, ToDate(user.get("birthday")));
		stmt.setString(8, ToString(user.get("country")));
		stmt.setString(9, ToString(user.get("city")));
		stmt.setString(10, ToString(user.get("dept_id")));
		stmt.setString(11, ToString(user.get("position")));
		stmt.setTimestamp(12, ToDate(user.get("createtime")));
		stmt.setString(13, ToString(user.get("salary")));
		stmt.setString(14, ToString(user.get("educational")));
		stmt.setString(15, ToString(user.get("school")));
		stmt.setString(16, ToString(user.get("email")));
		stmt.setString(17, ToString(user.get("remarks")));
		
		stmt.executeUpdate();
		
        stmt.close();
		conn.close();
		
        return id;
    }
    public void DeleteEmployee(String userId) throws Exception
    {
      
		Connection conn = getConn();		
		Statement stmt = conn.createStatement();
		
        String sql = "delete from t_employee where id = \""+userId+"\"";        		
        stmt.executeUpdate(sql);
                
		stmt.close();
		conn.close();
    }
    public void UpdateEmployee(HashMap<String, Object> user) throws Exception
    {
        HashMap<String, Object> db_user = GetEmployee(user.get("id").toString());
        
        Iterator<Entry<String, Object>> iter = user.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            Object val = entry.getValue();
            
            db_user.put(key, val);
        }         

        DeleteEmployee(user.get("id").toString());
        InsertEmployee(db_user);
    }
    
    public void UpdateDepartment(HashMap<String, Object> d) throws Exception
    {
        HashMap<String, Object> db_d = GetDepartment(d.get("id").toString());
    	
        Iterator<Entry<String, Object>> iter = d.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            Object val = entry.getValue();
            
            db_d.put(key, val);
        }    	
    	
        String sql =
        		"update t_department " 
        		+	" set "
        		+	" name = ?, "
        		+	" manager = ?, "
        		+	" manager_name = ? "
        		+" where id = ?";
        
		Connection conn = getConn();		
			
		PreparedStatement stmt = conn.prepareStatement(sql);	
		
		stmt.setString(1, ToString(db_d.get("name")));
		stmt.setString(2, ToString(db_d.get("manager")));
		stmt.setString(3, ToString(db_d.get("manager_name")));
		stmt.setString(4, ToString(db_d.get("id")));
		
		stmt.executeUpdate();		
        stmt.close();
		conn.close();      
    }    
    /////////////////////////////////////////////////////////////////
	private Connection getConn() throws Exception{		
		Class.forName(driver).newInstance();
		Connection conn = null;
		if(user == null || user.equals("")){
			conn = java.sql.DriverManager.getConnection(url);
		}else{
			conn = java.sql.DriverManager.getConnection(url, user, pwd);
		}
			
		return conn;
	}	    
	public ArrayList<HashMap<String, Object>> DBSelect(String sql) throws Exception{
    	Connection conn = getConn();		
		Statement stmt = conn.createStatement();
    	
        ResultSet rst = stmt.executeQuery(sql);		
		ArrayList<HashMap<String, Object>> list = ResultSetToList(rst);
		
		rst.close();
		stmt.close();
		conn.close();
		
        return list;
	}
	public void DBDelete(String sql) throws Exception{
		
		Connection conn = getConn();		
		Statement stmt = conn.createStatement();
		
        stmt.executeUpdate(sql);
                
		stmt.close();
		conn.close();
	}
	public void DBInsert(String sql, HashMap<String, Object> node) throws Exception
	{
		Connection conn = getConn();
		PreparedStatement stmt = conn.prepareStatement(sql);
		
		stmt.setString(1, ToString(node.get("id")));
		stmt.setString(2, ToString(node.get("name")));
		stmt.setString(3, ToString(node.get("type")));
		stmt.setString(4, ToString(node.get("size")));
		stmt.setString(5, ToString(node.get("url")));
		stmt.setString(6, ToString(node.get("pid")));
		stmt.setTimestamp(7, ToDate(node.get("createdate")));
		stmt.setTimestamp(8, ToDate(node.get("updatedate")));
		stmt.setInt(9, ToInt(node.get("folder")));
		stmt.setInt(10, ToInt(node.get("num")));
		
		stmt.executeUpdate();
		
        stmt.close();
		conn.close();
	}
	
    private static ArrayList<HashMap<String, Object>> ResultSetToList(ResultSet   rs) throws Exception{    	
    	ResultSetMetaData md = rs.getMetaData();
    	int columnCount = md.getColumnCount();
    	ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> rowData;
    	while(rs.next()){
	    	rowData = new HashMap<String, Object>(columnCount);
	    	for(int i = 1; i <= columnCount; i++)   {	 	    		
	    		Object v = rs.getObject(i);	    		
	    		
	    		if(v != null && (v.getClass() == Date.class || v.getClass() == java.sql.Date.class)){
	    			Timestamp ts= rs.getTimestamp(i);
	    			v = new java.util.Date(ts.getTime());
	    			//v = ts;
	    		}else if(v != null && v.getClass() == Clob.class){
	    			v = clob2String((Clob)v);
	    		}
	    		rowData.put(md.getColumnName(i).toLowerCase(),   v);
	    	}
	    	list.add(rowData);	    	
    	}
    	return list;
	} 	
    private static String clob2String(Clob clob) throws Exception {
        return (clob != null ? clob.getSubString(1, (int) clob.length()) : null);
    }  		    
    private int ToInt(Object o){
    	if(o == null) return 0;
    	double d = Double.parseDouble(o.toString());
    	int i = 0;
		i -= d;
		return -i;			
    }    
    private String ToString(Object o){
    	if(o == null) return "";
    	return o.toString();
    }    
    private Timestamp ToDate(Object o){
    	try{
    		if(o.getClass() == String.class){


    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			o = format.parse(o.toString());  
    			return new java.sql.Timestamp(((Date)o).getTime());
    		}
    		return o != null ? new java.sql.Timestamp(((Date)o).getTime()) : null;
		}catch(Exception ex){
			return null;
		}
    }
}