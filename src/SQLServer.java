import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Sqlselection 
    {
        public static void main(String[] args)
        {
            try
            {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                String userName = "sa";
                String password = "password";
                String url = "jdbc:microsoft:sqlserver://localhost:1433"+";databaseName=AdventureWorks2008R2";
                Connection con = DriverManager.getConnection(url, userName, password);
                Statement s1 = con.createStatement();
                ResultSet rs = s1.executeQuery("SELECT TOP 1 * FROM HumanResources.Employee");
                String[] result = new String[20];
                if(rs!=null){
                    while (rs.next()){
                        for(int i = 0; i <result.length ;i++)
                        {
                            for(int j = 0; j <result.length;j++)
                            {
                                result[j]=rs.getString(i);
                            System.out.println(result[j]);
                        }
                        }
                    }
                }

                //String result = new result[20];

            } catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	}