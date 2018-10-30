import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// This is an example of Session Bean, stateful, and synchronized.

public class OpBean implements javax.ejb.SessionBean, SessionSynchronization {

    protected int total = 0;        // actual state of the bean
    protected int newtotal = 0;        // value inside Tx, not yet committed.
    protected String clientUser = null;
    protected SessionContext sessionContext = null;

    public void  ejbCreate(String user) {
        total = 0;
        newtotal = total;
        clientUser = user;
    }

    public void ejbActivate() {
        // Nothing to do for this simple example
    }    

    public void ejbPassivate() {
        // Nothing to do for this simple example
    }

    public void ejbRemove() {
        // Nothing to do for this simple example
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    public void afterBegin() {
        newtotal = total;
    }

    public void beforeCompletion() {
        // Nothing to do for this simple example

        // We can access the bean environment everywhere in the bean,
        // for example here!
        try {
            InitialContext ictx = new InitialContext();
            String value = (String) ictx.lookup("java:comp/env/prop1");
            // value should be the one defined in ejb-jar.xml
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public void afterCompletion(boolean committed) {
        if (committed) {
            total = newtotal;
        } else {
            newtotal = total;
        }
    }

    public void buy(int s) {
        newtotal = newtotal + s;
        return;
    }

    public int read() {
        return newtotal;
    }
}