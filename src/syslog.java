import java.io.CharArrayWriter; 
import java.io.IOException; 
import java.util.Date; 
import java.util.List; 
 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.xipki.audit.api.AuditEvent; 
import org.xipki.audit.api.AuditEventData; 
import org.xipki.audit.api.AuditLevel; 
import org.xipki.audit.api.AuditService; 
import org.xipki.audit.api.AuditStatus; 
import org.xipki.audit.api.PCIAuditEvent; 
 
import com.cloudbees.syslog.Facility; 
import com.cloudbees.syslog.MessageFormat; 
import com.cloudbees.syslog.Severity; 
import com.cloudbees.syslog.SyslogMessage; 
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender; 
import com.cloudbees.syslog.sender.TcpSyslogMessageSender; 
import com.cloudbees.syslog.sender.UdpSyslogMessageSender; 
 
 
public class SyslogAuditServiceImpl implements AuditService { 
    private static final Logger LOG = LoggerFactory.getLogger(SyslogAuditServiceImpl.class); 
 
    public static final int DFLT_SYSLOG_PORT = 514; 
    public static final String DFLT_SYSLOG_PROTOCOL = "tcp"; 
    public static final String  DFLT_SYSLOG_FACILITY = "user"; 
    public static final String DFLT_SYSLOG_HOST = "localhost"; 
    public static final String DFLT_MESSAGE_FORMAT = "rfc_5424"; 
    protected AbstractSyslogMessageSender syslog = null; 
 
    private String host = DFLT_SYSLOG_HOST; 
    private int port = DFLT_SYSLOG_PORT; 
    private String protocol = DFLT_SYSLOG_PROTOCOL; 
    private String facility = DFLT_SYSLOG_FACILITY; 
    private String messageFormat = DFLT_MESSAGE_FORMAT; 
 
    private int maxMessageLength = 1024; 
 
    private int writeRetries; 
 
    private String localname; 
    private String prefix; 
    private boolean ssl; 
 
    private boolean initialized; 
 
    public SyslogAuditServiceImpl() { 
    } 
 
    @Override 
    public void logEvent( 
            final AuditEvent event) { 
        if (event == null) { 
            return; 
        } 
 
        if (!initialized) { 
            LOG.error("Syslog audit not initialiazed"); 
            return; 
        } 
 
        CharArrayWriter sb = new CharArrayWriter(); 
        if (notEmpty(prefix)) { 
            sb.append(prefix); 
        } 
 
        AuditStatus status = event.getStatus(); 
        if (status == null) { 
            status = AuditStatus.UNDEFINED; 
        } 
 
        sb.append("\tstatus: ").append(status.name()); 
 
        long duration = event.getDuration(); 
        if (duration >= 0) { 
            sb.append("\tduration: ").append(Long.toString(duration)); 
        } 
 
        List<AuditEventData> eventDataArray = event.getEventDatas(); 
        for (AuditEventData m : eventDataArray) { 
            if (duration >= 0 && "duration".equalsIgnoreCase(m.getName())) { 
                continue; 
            } 
            sb.append("\t").append(m.getName()).append(": ").append(m.getValue()); 
        } 
 
        final int n = sb.size(); 
        if (n > maxMessageLength) { 
            LOG.warn("syslog message exceeds the maximal allowed length: {} > {}, ignore it", 
                    n, maxMessageLength); 
            return; 
        } 
 
        SyslogMessage sm = new SyslogMessage(); 
        sm.setFacility(syslog.getDefaultFacility()); 
        if (notEmpty(localname)) { 
            sm.setHostname(localname); 
        } 
        sm.setAppName(event.getApplicationName()); 
        sm.setSeverity(getSeverity(event.getLevel())); 
 
        Date timestamp = event.getTimestamp(); 
        if (timestamp != null) { 
            sm.setTimestamp(timestamp); 
        } 
 
        sm.setMsgId(event.getName()); 
        sm.setMsg(sb); 
 
        try { 
            syslog.sendMessage(sm); 
        } catch (IOException e) { 
            LOG.error("Could not send syslog message: " + e.getMessage()); 
            LOG.debug("Could not send syslog message", e); 
        } 
    } 
 
    @Override 
    public void logEvent( 
            final PCIAuditEvent event) { 
        if (event == null) { 
            return; 
        } 
 
        if (!initialized) { 
            LOG.error("Syslog audit not initialiazed"); 
            return; 
        } 
 
        CharArrayWriter msg = event.toCharArrayWriter(prefix); 
        final int n = msg.size(); 
        if (n > maxMessageLength) { 
            LOG.warn("syslog message exceeds the maximal allowed length: {} > {}, ignore it", 
                    n, maxMessageLength); 
            return; 
        } 
 
        SyslogMessage sm = new SyslogMessage(); 
        sm.setFacility(syslog.getDefaultFacility()); 
        if (notEmpty(localname)) { 
            sm.setHostname(localname); 
        } 
 
        sm.setSeverity(getSeverity(event.getLevel())); 
        sm.setMsg(msg); 
 
        try { 
            syslog.sendMessage(sm); 
        } catch (IOException e) { 
            LOG.error("Could not send syslog message: " + e.getMessage()); 
            LOG.debug("Could not send syslog message", e); 
        } 
    } 
 
    public void init() { 
        if (initialized) { 
            return; 
        } 
 
        LOG.info("initializing: {}", SyslogAuditServiceImpl.class); 
 
        try { 
            MessageFormat _messageFormat; 
            if ("rfc3164".equalsIgnoreCase(messageFormat) 
                    || "rfc_3164".equalsIgnoreCase(messageFormat)) { 
                _messageFormat = MessageFormat.RFC_3164; 
            } else if ("rfc5424".equalsIgnoreCase(messageFormat) 
                    || "rfc_5424".equalsIgnoreCase(messageFormat)) { 
                _messageFormat = MessageFormat.RFC_5424; 
            } else { 
                LOG.warn("invalid message format '{}', use the default one '{}'", 
                        messageFormat, DFLT_MESSAGE_FORMAT); 
                _messageFormat = MessageFormat.RFC_5424; 
            } 
 
            if ("udp".equalsIgnoreCase(protocol)) { 
                syslog = new UdpSyslogMessageSender(); 
                ((UdpSyslogMessageSender) syslog).setSyslogServerPort(port); 
            } else if ("tcp".equalsIgnoreCase(protocol)) { 
                syslog = new TcpSyslogMessageSender(); 
                ((TcpSyslogMessageSender) syslog).setSyslogServerPort(port); 
                ((TcpSyslogMessageSender) syslog).setSsl(ssl); 
 
                if (writeRetries > 0) { 
                    ((TcpSyslogMessageSender) syslog).setMaxRetryCount(writeRetries); 
                } 
            } else { 
                LOG.warn("unknown protocol '{}', use the default one 'udp'", this.protocol); 
                syslog = new UdpSyslogMessageSender(); 
                ((UdpSyslogMessageSender) syslog).setSyslogServerPort(port); 
            } 
 
            syslog.setDefaultMessageHostname(host); 
            syslog.setMessageFormat(_messageFormat); 
 
            Facility sysFacility = null; 
            if (notEmpty(facility)) { 
                sysFacility = Facility.fromLabel(facility.toUpperCase()); 
            } 
 
            if (sysFacility == null) { 
                LOG.warn("unknown facility, use the default one '" + DFLT_SYSLOG_FACILITY); 
                sysFacility = Facility.fromLabel(DFLT_SYSLOG_FACILITY.toUpperCase()); 
            } 
 
            if (sysFacility == null) { 
                throw new RuntimeException("should not reach here, sysFacility is null"); 
            } 
 
            syslog.setDefaultFacility(sysFacility); 
 
            // after we're finished set initialized to true 
            this.initialized = true; 
            LOG.info("Initialized: {}", SyslogAuditServiceImpl.class); 
        } catch (Exception e) { 
            LOG.error("error while configuring syslog sender: "  + e.toString()); 
        } 
    } 