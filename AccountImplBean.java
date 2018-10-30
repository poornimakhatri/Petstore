import java.rmi.RemoteException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

public class AccountImplBean implements EntityBean {

  // Keep the reference on the EntityContext
  protected EntityContext entityContext;

  // Object state
  public Integer accno;
  public String customer;
  public double balance;

  public Integer ejbCreate(int val_accno, String val_customer, double val_balance) {

    // Init object state
    accno = new Integer(val_accno);
    customer = val_customer;
    balance = val_balance;
    return null;
  }

  public void ejbPostCreate(int val_accno, String val_customer, double val_balance) { 
    // Nothing to be done for this simple example.
  }

  public void ejbActivate() {
    // Nothing to be done for this simple example.
  }

  public void ejbLoad() {
    // Nothing to be done for this simple example, in implicit persistence.
  }

  public void ejbPassivate() {
    // Nothing to be done for this simple example.
  }


  public void ejbRemove() {
    // Nothing to be done for this simple example, in implicit persistence.
  }

  public void ejbStore() {
    // Nothing to be done for this simple example, in implicit persistence.
  }

  public void setEntityContext(EntityContext ctx) {
    // Keep the entity context in object
    entityContext = ctx;
  }

  public void unsetEntityContext() {
    entityContext = null;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double d) {
    balance = balance + d;
  }

  public String  getCustomer() {
    return customer;
  }

  public void setCustomer(String c) {
    customer = c;
  }

  public int getNumber()  {
    return accno.intValue();
  }
} 