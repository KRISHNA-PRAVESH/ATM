/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package atm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
/**
 *
 * @author krishna
 */
public class ATM {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        // TODO code application logic here
        //connecting to the data base
        Class.forName("com.mysql.cj.jdbc.Driver");
        Scanner sc = new Scanner(System.in);
  
        ResultSet rs;
        PreparedStatement pstmt;
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATM","root","mysql26");
        Statement st = conn.createStatement();

        
        System.out.println("1.Load Cash to ATM");
        System.out.println("2.Show Customer Details");
        System.out.println("3.Show ATM Operations");
        System.out.println("Enter your choice");
        int ch = sc.nextInt();
        //making the balance global so that other utilities can use it
         int balance = 0;
        switch(ch){
            case 1:
                System.out.print("Enter denomination (2000/500/100): ");
                int deno = sc.nextInt();
                System.out.println();
                System.out.print("Enter number: ");
                int number = sc.nextInt();
                System.out.println();
                int val= number*deno;
                //Fetching the previous values of number for the given denomination
                rs = st.executeQuery("select Number,value from atm_cash where Denomination="+deno);
                int prevNumber=0, prevVal=0;
                while(rs.next()){
                   prevNumber = rs.getInt(1);
                   prevVal = rs.getInt(2);
                }
               
                //adding the current number with prev and incrementing the total value for the given denomination
                number = number+prevNumber;
                val = prevVal+val;
                
                //inserting the new values into the table
                pstmt = conn.prepareStatement("update atm_cash set Number=?, value =? where Denomination="+deno);
                pstmt.setInt(1, number);
                pstmt.setInt(2,val);
                pstmt.executeUpdate();
                System.out.println("Updated.");
                break;
            case 2:
               //Customer details
                rs = st.executeQuery("select * from customers");
                System.out.println("Account number | Acc Holder | Pin number | Acc Balance");
                System.out.println("---------------|------------|------------|------------");
                while(rs.next()){;
                System.out.println(rs.getInt(1)+"            | "+rs.getString(2)+"     |  "+rs.getInt(3)+"      |"+rs.getInt(4));
                            
                }
                System.out.println("-----------------------------------------------------");
                
                break;
            case 3:
                //Authentication
                    System.out.println("Enter Account number: ");
                    String accno = sc.next();
                    System.out.println("Enter pin");
                    int pin = sc.nextInt();
                    
                    //fetching original pin number from db
                    pstmt = conn.prepareStatement("select pinNumber from customers where accno=?");
                    pstmt.setString(1, accno);
                    rs = pstmt.executeQuery();
                    int originalPin = 0;
                    while(rs.next()){
                         originalPin = rs.getInt(1);
                      }
                    
                    //checking the entered credentials are correct
                    if(pin!=originalPin){
                        System.out.println("Invalid Username or Pin");
                        break;
                        }
                    System.out.println("1.Check balance");
                    System.out.println("2.Withdraw money");
                    System.out.println("3.Transfer money");
                    System.out.println("4.Check ATM Balance");
                    System.out.println("Enter choice: ");
                    int ch1 = sc.nextInt();
                    switch(ch1){
                    //check customer balance
                    case 1:
                        //fetching balance from db
                        pstmt = conn.prepareStatement("select accbalance from customers where accno=?");
                        pstmt.setString(1, accno);
                        rs = pstmt.executeQuery();
                
                        while(rs.next()){
                            balance = rs.getInt(1);
                        }
                        
                        //formating the balance amount as Indian currency representation
                         NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                         String balanceMoney = formatter.format(balance);
                        
                         System.out.println("Account Balance: "+balanceMoney);
                         break;
                    case 2:
                        //Withdraw money
                        System.out.println("Enter amount: ");
                        int amtNeeded = sc.nextInt();
                        if(amtNeeded>10000 || amtNeeded<100){
                            System.out.println("Uh oh..can't do this withdrawl");
                            break;
                        }
                        //checking atm balance
                        rs = st.executeQuery("select value from atm_cash");
                        int atmbalance =0;
                        while(rs.next()){
                            atmbalance+=rs.getInt(1);
                        }
                        if(atmbalance<amtNeeded){
                            System.out.println("Insufficient funds in the ATM");
                            break;
                        }
                        if(amtNeeded<=5000){
                            
                        }
                        pstmt = conn.prepareStatement("select accbalance from customers where accno=?");
                        pstmt.setString(1, accno);
                        rs = pstmt.executeQuery();
                
                        while(rs.next()){
                            balance = rs.getInt(1);
                        }
                        if(amtNeeded>balance){
                            System.out.println("Insufficient balance");
                            break;
                        }
                        System.out.println("**Processing**");
                        
                        //Pausing program for 3 seconds so that it feels like an actual ATM .while processing xD
                        Thread.sleep(3000);
                        balance = balance-amtNeeded;
//                        System.out.println(balance);
                        
                        //updating new balance
                        pstmt = conn.prepareStatement("update customers set accBalance=? where accno=?");
                        pstmt.setInt(1, balance);
                        pstmt.setString(2,accno);
                        pstmt.execute();
                        System.out.println("Please collect your cash and card");
                        System.out.println("**Thank You**");
                        break;
                    case 3:
                        //Transfer money
                        System.out.println("Enter account number of the receiver: ");
                        String receiver = sc.next();
                        pstmt = conn.prepareStatement("select * from customers where accno=?");
                        pstmt.setString(1, receiver);
                        rs = pstmt.executeQuery();
                        if(!rs.next()){
                            System.out.println("User doesn't exist");
                            break;
                        }
                        System.out.println("Enter amount to be transferred: ");
                        int transferAmt = sc.nextInt();
                        
                        //fetching account balance
                          pstmt = conn.prepareStatement("select accbalance from customers where accno=?");
                        pstmt.setString(1, accno);
                        rs = pstmt.executeQuery();
                   
                        while(rs.next()){
                            balance = rs.getInt(1);
                        }
                        
                        if(transferAmt>balance){
                            System.out.println("Insufficient balance");
                            break;
                        }
                        //updating the balances of both sender and receiver
                        
                        //senders account
                        pstmt = conn.prepareStatement("update customers set accBalance=? where accno=?");
                        pstmt.setInt(1, balance-transferAmt);
                        pstmt.setString(2, accno);
                        pstmt.execute();
                        //receivers account
                          //fetching receiver's account balance
                          pstmt = conn.prepareStatement("select accbalance from customers where accno=?");
                        pstmt.setString(1, receiver);
                        rs = pstmt.executeQuery();
                   
                        while(rs.next()){
                            balance = rs.getInt(1);
                        }
                        pstmt = conn.prepareStatement("update customers set accBalance=? where accno=?");
                        pstmt.setInt(1, balance+transferAmt);
                        pstmt.setString(2, receiver);
                        pstmt.execute();
                        
                        System.out.println("Amount Transfer successfull");
                        break;
                    case 4:
                        System.out.println("Check atm balance"); 
                        break;  
                    default: System.out.println("Invalid choice");
                }
                
                
               
                
                
                
                
                
        }
       
    }
    
}
