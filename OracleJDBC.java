import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginAction extends HttpServlet {
Connection conn;
Statement stmt;
ResultSet rs;
Class.forName("com.mysql.jdbc.Driver");  
Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/sonoo","root","root");  
String query = "SELECT * FROM table1";
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    out.println("Hello");
    String u_name = request.getParameter("uname");
    String u_pass = request.getParameter("upass");
    out.println(u_name);
    out.println(u_pass);
    try{
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE","urja","urja");
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
    }catch(SQLException sex){
        sex.printStackTrace();
    } catch (ClassNotFoundException cnf) {
	 cnf.printStackTrace();
    }
}
}