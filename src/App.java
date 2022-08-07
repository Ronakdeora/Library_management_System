import java.awt.event.*;
//import java.rmi.server.UID;
import java.sql.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

//import com.mysql.cj.protocol.Resultset;
import net.proteanit.sql.DbUtils;

public class App {
    public static class ex{
        public static int days=0;
            }
    public static void main(String[] args) throws Exception {
        login();
    }
    
    public static void create() {
        try{
            Connection connection = connect();
            ResultSet rs = connection.getMetaData().getCatalogs();
            while(rs.next())
           {
               String dbname = rs.getString(1);
               if(dbname.equals("library")){
                   Statement stmt =connection.createStatement();
                   stmt.executeUpdate("DROP SCHEMA `library`");
               }
            }
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE SCHEMA `library`");
            stmt.executeUpdate("USE LIBRARY");
            //create tables
            String sql1 = "CREATE TABLE `USERS`(`UID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `USERNAME` VARCHAR(30), `PASSWORD` VARCHAR(30), `ADMIN` BOOLEAN)";
          stmt.executeUpdate(sql1);
          //Insert into users table
          stmt.executeUpdate("INSERT INTO `library`.`users`(USERNAME, PASSWORD, ADMIN) VALUES('admin','admin',TRUE)");
          //Create Books table
          stmt.executeUpdate("CREATE TABLE `library`.`books`(`BID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `BNAME` VARCHAR(50), `GENRE` VARCHAR(20), `PRICE` INT)");
          //Create Issued Table
          stmt.executeUpdate("CREATE TABLE `library`.`issued`(`IID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, `UID` INT, `BID` INT, `ISSUED_DATE` VARCHAR(20), `RETURN_DATE` VARCHAR(20), `PERIOD` INT, `FINE` INT)");
          //Insert into books table
          stmt.executeUpdate("INSERT INTO `library`.`books`(BNAME, GENRE, PRICE) VALUES ('War and Peace', 'Mystery', 200),  ('The Guest Book', 'Fiction', 300), ('The Perfect Murder','Mystery', 150), ('Accidental Presidents', 'Biography', 250), ('The Wicked King','Fiction', 350)");
         rs.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    /*********************login page******************************/
    private static void login() {
        JFrame f = new JFrame("Login");

        JLabel l1,l2;  
        l1=new JLabel("Username");  
        l1.setBounds(30,15, 100,30); 
         
        l2=new JLabel("Password"); 
        l2.setBounds(30,50, 100,30);    
         
        JTextField F_user = new JTextField(); 
        F_user.setBounds(110, 15, 200, 30);
             
        JPasswordField F_pass=new JPasswordField(); 
        F_pass.setBounds(110, 50, 200, 30);
           
        JButton login_but=new JButton("Login");
        login_but.setBounds(130,90,80,25);
        login_but.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent event) {
                String username = F_user.getText();
                String password = F_pass.getText();
                if(username.equals("")) //If username is null
                {
                    JOptionPane.showMessageDialog(null,"Please enter username"); //Display dialog box with the message
                } 
                else if(password.equals("")) //If password is null
                {
                    JOptionPane.showMessageDialog(null,"Please enter password"); //Display dialog box with the message
                }
                else{
                    Connection connection = connect();
                    try{
                        Statement stmt  =connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate("USE LIBRARY");

                        String st = ("SELECT * FROM USERS WHERE USERNAME='" + username +"' AND PASSWORD='"+password+"'");
                        ResultSet rs = stmt.executeQuery(st);
                        //System.out.print("add query");
                        if(rs.next()==false){
                            JOptionPane.showMessageDialog(null,"Wrong User/password!");
                        }else{
                            f.dispose();
                            rs.beforeFirst();//Move the pointer above
                            while(rs.next())
                            {
                                String admin = rs.getString("ADMIN");
                                String uid = rs.getString("UID");
                                if(admin.equals("1")) {
                                    admin_menu();
                                }else{
                                    user_menu(uid);
                                }
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    
                }

            }

        });
        f.add(F_pass);
        f.add(login_but);
        f.add(F_user);
        f.add(l1);
        f.add(l2);

        f.setSize(400,180);
        f.setLayout(null);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
    }
    
    /***************************User_menu******************************** */
    public static void user_menu(String UID) {
        JFrame f = new JFrame("User Functions");
        JButton view_btn = new JButton("View Books");
        view_btn.setBounds(20,20,120,25);
        view_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JFrame f = new JFrame("Books Available");
                
                Connection connection = connect();
                String sql = "SELECT * FROM BOOKS";
                try {
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    JTable book_list = new JTable();
                    book_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollPane = new JScrollPane(book_list);

                    f.add(scrollPane);
                    f.setSize(800 ,400);
                    f.setVisible(true);
                    f.setLocationRelativeTo(null);
                }catch(SQLException e1){
                    JOptionPane.showMessageDialog(null, e1);
                }
            }
        });
        
        JButton my_book=new JButton("My Books");//creating instance of JButton  
        my_book.setBounds(150,20,120,25);//x axis, y axis, width, height 
        my_book.addActionListener(new ActionListener() { //Perform action
            public void actionPerformed(ActionEvent e){
                 
                   
                JFrame f = new JFrame("My Books"); //View books issued by user
                //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                int UID_int = Integer.parseInt(UID); //Pass user ID
     
                //.iid,issued.uid,issued.bid,issued.issued_date,issued.return_date,issued,
                Connection connection = connect(); //connect to database
                //retrieve data
                String sql="select distinct issued.*,books.bname,books.genre,books.price from issued,books " + "where ((issued.uid=" + UID_int + ") and (books.bid in (select bid from issued where issued.uid="+UID_int+"))) group by iid";
                String sql1 = "select bid from issued where uid="+UID_int;
                try {
                    Statement stmt = connection.createStatement();
                    //use database
                     stmt.executeUpdate("USE LIBRARY");
                    stmt=connection.createStatement();
                    //store in array
                    ArrayList books_list = new ArrayList();
                    ResultSet rs=stmt.executeQuery(sql);
                    JTable book_list= new JTable(); //store data in table format
                    book_list.setModel(DbUtils.resultSetToTableModel(rs)); 
                    //enable scroll bar
                    JScrollPane scrollPane = new JScrollPane(book_list);
     
                    f.add(scrollPane); //add scroll bar
                    f.setSize(800, 400); //set dimensions of my books frame
                    f.setVisible(true);
                    f.setLocationRelativeTo(null);
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                     JOptionPane.showMessageDialog(null, e1);
                }               
                     
        }
        });
        
        JButton logout_btn =new JButton("Logout");
        logout_btn.setBounds(80,90,120,25);
        logout_btn.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent event) {
                f.dispose();
                login();
            }});
        
        f.add(my_book);
        f.add(logout_btn); //add my books
        f.add(view_btn); // add view books
        f.setSize(300,200);//400 width and 500 height  
        f.setLayout(null);//using no layout managers  
        f.setVisible(true);//making the frame visible 
        f.setLocationRelativeTo(null);
    }
    /**********************admin menu*************************** */
    public static void admin_menu() {
        JFrame f = new JFrame("Admin menu");

        JButton create_btn = new JButton("create/reset");
        create_btn.setBounds(450,60,120,25);
        create_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                create();
                JOptionPane.showMessageDialog(null,"database created/reset!");
            }
        });
        
        
        JButton view_btn = new JButton("View Books");
        view_btn.setBounds(20,20,120,25);
        view_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event ) {
                JFrame f = new JFrame("Books Available");
                Connection connection = connect();
                String sql = "SELECT * FROM BOOKS";
                try{
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    JTable book_list = new JTable();
                    book_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollPane = new JScrollPane(book_list);
                    f.add(scrollPane);
                    f.setSize(800 , 400);
                    f.setVisible(true);
                    f.setLocationRelativeTo(null);
                }catch(SQLException e1){
                    JOptionPane.showMessageDialog(null,e1);
                }
            }
        });
        
        
        JButton users_btn = new JButton("View Users");
        users_btn.setBounds(150,20,120,25);
        users_btn.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent event){
                JFrame f = new JFrame("Users List");
                Connection connection = connect();
                String sql = "SELECT * FROM USERS";
                try{
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    JTable book_list = new JTable();
                    book_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollpane = new JScrollPane(book_list);

                    f.add((scrollpane));
                    f.setSize(800,400);
                    f.setVisible(true);
                    f.setLocationRelativeTo(null);
                }catch(SQLException e1){
                    JOptionPane.showMessageDialog(null, e1);
                }
            }
        });
    
        
        JButton issued_btn = new JButton("View Issued Books");
        issued_btn.setBounds(280,20,160,25);
        issued_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JFrame f = new JFrame("Users List");

                Connection connection = connect();
                String sql = "SELECT * FROM ISSUED";
                try{
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE LIBRARY");
                    stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    JTable book_list = new JTable();
                    book_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollPane = new JScrollPane(book_list);

                    f.add(scrollPane);
                    f.setSize(800,400);
                    f.setVisible(true);
                    f.setLocationRelativeTo(null);
                }catch(SQLException e1){
                    JOptionPane.showMessageDialog(null,e1);
                }
            }
        });
    
        JButton add_user=new JButton("Add User"); //creating instance of JButton to add users
        add_user.setBounds(20,60,120,25); //set dimensions for button 
        add_user.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                 
                JFrame g = new JFrame("Enter User Details");
                JLabel l1,l2;  
                l1=new JLabel("Username"); 
                l1.setBounds(30,15, 100,30); 
                 
                l2=new JLabel("Password"); 
                l2.setBounds(30,50, 100,30); 
                 
                JTextField F_user = new JTextField();
                F_user.setBounds(110, 15, 200, 30);
                 
                JPasswordField F_pass=new JPasswordField();
                F_pass.setBounds(110, 50, 200, 30);
                
                JRadioButton a1 = new JRadioButton("Admin");
                a1.setBounds(55, 80, 200,30);
                
                JRadioButton a2 = new JRadioButton("User");
                a2.setBounds(130, 80, 200,30);
                
                ButtonGroup bg=new ButtonGroup();    
                bg.add(a1);bg.add(a2);
                
                JButton create_but=new JButton("Create");//creating instance of JButton for Create 
                create_but.setBounds(130,130,80,25);//x axis, y axis, width, height 
                create_but.addActionListener(new ActionListener() {
                     
                    public void actionPerformed(ActionEvent e){
                     
                    String username = F_user.getText();
                    String password = F_pass.getText();
                    Boolean admin = false;
                     
                    if(a1.isSelected()) {
                        admin=true;
                    }
                     
                    Connection connection = connect();
                     
                    try {
                    Statement stmt = connection.createStatement();
                     stmt.executeUpdate("USE LIBRARY");
                     stmt.executeUpdate("INSERT INTO USERS(USERNAME,PASSWORD,ADMIN) VALUES ('"+username+"','"+password+"',"+admin+")");
                     JOptionPane.showMessageDialog(null,"User added!");
                     g.dispose();
                      
                    }
                     
                    catch (SQLException e1) {
                         JOptionPane.showMessageDialog(null, e1);
                    }   
                     
                    }
                     
                });
                                 
                g.add(create_but);
                g.add(a2);
                g.add(a1);
                g.add(l1);
                g.add(l2);
                g.add(F_user);
                g.add(F_pass);
                g.setSize(350,200);//400 width and 500 height  
                g.setLayout(null);//using no layout managers  
                g.setVisible(true);//making the frame visible 
                g.setLocationRelativeTo(null);
             
             
}
});
  
        JButton add_book=new JButton("Add Book"); //creating instance of JButton for adding books
        add_book.setBounds(150,60,120,25); 
        add_book.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                JFrame g = new JFrame("Enter Book Details");
                JLabel l1,l2,l3;  
                l1=new JLabel("Book Name");  //lebel 1 for book name
                l1.setBounds(30,15, 100,30); 
             
                l2=new JLabel("Genre");  //label 2 for genre
                l2.setBounds(30,53, 100,30); 
             
                l3=new JLabel("Price");  //label 2 for price
                l3.setBounds(30,90, 100,30); 
             
                JTextField F_bname = new JTextField();
                F_bname.setBounds(110, 15, 200, 30);
             
                JTextField F_genre=new JTextField();
                F_genre.setBounds(110, 53, 200, 30);
            //set text field for price
                JTextField F_price=new JTextField();
                F_price.setBounds(110, 90, 200, 30);
                     
             
                JButton create_but=new JButton("Submit");//creating instance of JButton to submit details  
                create_but.setBounds(130,130,80,25);//x axis, y axis, width, height 
                create_but.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e){
                    // assign the book name, genre, price
                        String bname = F_bname.getText();
                        String genre = F_genre.getText();
                        String price = F_price.getText();
                    //convert price of integer to int
                        int price_int = Integer.parseInt(price);
                     
                        Connection connection = connect();
                     
                        try {
                        Statement stmt = connection.createStatement();
                        stmt.executeUpdate("USE LIBRARY");
                        stmt.executeUpdate("INSERT INTO BOOKS(BNAME,GENRE,PRICE) VALUES ('"+bname+"','"+genre+"',"+price_int+")");
                        JOptionPane.showMessageDialog(null,"Book added!");
                        g.dispose(); 
                    }
                     catch (SQLException e1) {
                         JOptionPane.showMessageDialog(null, e1);
                    }   
                    }
                     
                });      
                    g.add(l3);
                    g.add(create_but);
                    g.add(l1);
                    g.add(l2);
                    g.add(F_bname);
                    g.add(F_genre);
                    g.add(F_price);
                    g.setSize(350,200);
                    g.setLayout(null);
                    g.setVisible(true); 
                    g.setLocationRelativeTo(null);              
    }
    });

        JButton issue_book=new JButton("Issue Book"); //creating instance of JButton to issue books
        issue_book.setBounds(450,20,120,25); 
        issue_book.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
                JFrame g = new JFrame("Enter Details");
                JLabel l1,l2,l3,l4;  
                l1=new JLabel("Book ID(BID)");  
                l1.setBounds(30,15, 100,30); 
                 
                 
                l2=new JLabel("User ID(UID)");  
                l2.setBounds(30,53, 100,30); 
                 
                l3=new JLabel("Period(days)");  
                l3.setBounds(30,90, 100,30); 
                 
                l4=new JLabel("Issued Date(DD-MM-YYYY)"); 
                l4.setBounds(30,127, 150,30); 
                 
                JTextField F_bid = new JTextField();
                F_bid.setBounds(110, 15, 200, 30);
                 
                 
                JTextField F_uid=new JTextField();
                F_uid.setBounds(110, 53, 200, 30);
                 
                JTextField F_period=new JTextField();
                F_period.setBounds(110, 90, 200, 30);
                 
                JTextField F_issue=new JTextField();
                F_issue.setBounds(180, 130, 130, 30);   
 
                 
                JButton create_but=new JButton("Submit");//creating instance of JButton  
                create_but.setBounds(130,170,80,25);//x axis, y axis, width, height 
                create_but.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e){
                     
                        String uid = F_uid.getText();
                        String bid = F_bid.getText();
                        String period = F_period.getText();
                        String issued_date = F_issue.getText();
     
                        int period_int = Integer.parseInt(period);
                         
                        Connection connection = connect();
                         
                        try {
                        Statement stmt = connection.createStatement();
                         stmt.executeUpdate("USE LIBRARY");
                         stmt.executeUpdate("INSERT INTO ISSUED(UID,BID,ISSUED_DATE,PERIOD) VALUES ('"+uid+"','"+bid+"','"+issued_date+"',"+period_int+")");
                         JOptionPane.showMessageDialog(null,"Book Issued!");
                         g.dispose();
                          
                        }catch (SQLException e1) {
                             JOptionPane.showMessageDialog(null, e1);
                        }   
                         
                        }
                         
                    });
                         
                     
                        g.add(l3);
                        g.add(l4);
                        g.add(create_but);
                        g.add(l1);
                        g.add(l2);
                        g.add(F_uid);
                        g.add(F_bid);
                        g.add(F_period);
                        g.add(F_issue);
                        g.setSize(350,250);//400 width and 500 height  
                        g.setLayout(null);//using no layout managers  
                        g.setVisible(true);//making the frame visible 
                        g.setLocationRelativeTo(null);
                     
                     
        }
        });

        JButton return_book=new JButton("Return Book"); //creating instance of JButton to return books
        return_book.setBounds(280,60,160,25);        
        return_book.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                     
                    JFrame g = new JFrame("Enter Details");
                    //g.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    //set labels 
                    JLabel l1,l2,l3,l4;  
                    l1=new JLabel("Issue ID(IID)");  //Label 1 for Issue ID
                    l1.setBounds(30,15, 100,30); 
                    
                     
                    l4=new JLabel("Return Date(DD-MM-YYYY)");  
                    l4.setBounds(30,50, 150,30); 
                     
                    JTextField F_iid = new JTextField();
                    F_iid.setBounds(110, 15, 200, 30);
                     
                     
                    JTextField F_return=new JTextField();
                    F_return.setBounds(180, 50, 130, 30);
                 
     
                    JButton create_but=new JButton("Return");//creating instance of JButton to mention return date and calculcate fine
                    create_but.setBounds(130,170,80,25);//x axis, y axis, width, height 
                    create_but.addActionListener(new ActionListener() {
                         
                        public void actionPerformed(ActionEvent e){                 
                         
                        String iid = F_iid.getText();
                        String return_date = F_return.getText();
                         
                        Connection connection = connect();
                         
                        try {
                        Statement stmt = connection.createStatement();
                         stmt.executeUpdate("USE LIBRARY");
                         //Intialize date1 with NULL value
                         String date1=null;
                         String date2=return_date; //Intialize date2 with return date
                     
                     //select issue date
                     ResultSet rs = stmt.executeQuery("SELECT ISSUED_DATE FROM ISSUED WHERE IID="+iid);
                     while (rs.next()) {
                         date1 = rs.getString(1);
                         //System.out.print(date1);
                       }
                      
                     try {
                            Date date_1= new SimpleDateFormat("dd-MM-yyyy").parse(date1);
                            Date date_2= new SimpleDateFormat("dd-MM-yyyy").parse(date2);
                            //subtract the dates and store in diff
                            long diff = date_2.getTime() - date_1.getTime();
                            System.out.print(diff+"\\"+date_1);
                            //Convert diff from milliseconds to days
                            ex.days=(int)(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                             
                             
                        } catch (ParseException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                      
                     
                     //update return date
                     stmt.executeUpdate("UPDATE ISSUED SET RETURN_DATE='"+return_date+"' WHERE IID="+iid);
                     g.dispose();
                      
 
                     Connection connection1 = connect();
                     Statement stmt1 = connection1.createStatement();
                     stmt1.executeUpdate("USE LIBRARY");                
                    ResultSet rs1 = stmt1.executeQuery("SELECT PERIOD FROM ISSUED WHERE IID="+iid); //set period
                    String diff=null; 
                    while (rs1.next()) {
                         diff = rs1.getString(1);
                          
                       }
                    int diff_int = Integer.parseInt(diff);
                    if(ex.days>=1) { 
                         
                        //System.out.println(ex.days);
                        int fine = (ex.days-diff_int)*10;
                        stmt1.executeUpdate("UPDATE ISSUED SET FINE="+fine+" WHERE IID="+iid);  
                        String fine_str = ("Fine: Rs. "+fine);
                        JOptionPane.showMessageDialog(null,fine_str);
                         
                    }
 
                     JOptionPane.showMessageDialog(null,"Book Returned!");
                      
                    }
                             
                     
                    catch (SQLException e1) {
                        // TODO Auto-generated catch block
                         JOptionPane.showMessageDialog(null, e1);
                    }   
                     
                    }
                     
                }); 
                    g.add(l4);
                    g.add(create_but);
                    g.add(l1);
                    g.add(F_iid);
                    g.add(F_return);
                    g.setSize(350,250);//400 width and 500 height  
                    g.setLayout(null);//using no layout managers  
                    g.setVisible(true);//making the frame visible 
                    g.setLocationRelativeTo(null);              
    }
    });        

        JButton logout_btn =new JButton("Logout");
        logout_btn.setBounds(220,100,120,25);
        logout_btn.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent event) {
                f.dispose();
                login();
            }});
    f.add(create_btn);
    f.add(return_book);
    f.add(issue_book);
    f.add(add_book);
    f.add(issued_btn);
    f.add(users_btn);
    f.add(view_btn);
    f.add(add_user);
    f.add(logout_btn);
    f.setSize(600,400);//400 width and 500 height  
    f.setLayout(null);//using no layout managers  
    f.setVisible(true);//making the frame visible 
    f.setLocationRelativeTo(null);
     
    }
    /*sql connection */
    protected static Connection connect() {
        //System.out.println("'fuck'");
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","ronak27");
            //System.out.println("hogya");
            return con; 
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    }
