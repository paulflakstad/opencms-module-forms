package no.npolar.common.forms;

import org.apache.commons.mail.EmailException;
import org.opencms.file.CmsObject;
import org.opencms.mail.CmsMailSettings;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the emails sent as notifications/confirmations whenever a form is 
 * successfully submitted.
 * <p>
 * Mainly a wraps {@link org.opencms.mail.CmsSimpleMail} and adds some
 * functionality.
 * <p>
 * In particular, "macros" are resolved, allowing for submitted form data to be
 * used in the to-address line, subject line, and message body.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 * @see no.npolar.util.SystemMail
 */
public class AutoEmail {
    
    /**
     * Authenticator that uses the standard OpenCms mail settings.
     * <p>
     * Settings are defined in {OPENCMS_WEBAPP}/WEB-INF/config/opencms-system.xml.
     */
    private class AutoMailAuthenticator extends javax.mail.Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost().getUsername(), 
                OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost().getPassword()
            );
        }
    }
    
    /** The recipient address line. */
    private String toAddr = null;
    
    /** The sender address. */
    private String fromAddr = null;
    
    /** The sender name. */
    private String fromName = null;
    
    /** The subject line. */
    private String subject = null;
    
    /** The message body. */
    private String message = null;
    
    /** Logger for this class. */
    private static final Log LOG = LogFactory.getLog(AutoEmail.class);;
    
    /**
     * Creates a new, blank instance.
     */
    public AutoEmail() {}

    /**
     * Gets the recipient address line.
     * <p>
     * Multiple recipients are delimited by semicolon(s).
     * 
     * @return the recipient address line.
     */
    public String getToAddr() {
        return toAddr;
    }

    /**
     * Sets the recipient address line.
     * <p>
     * To add multiple recipients, use the semicolon as delimiter.
     * <p>
     * If the recipient address line includes any macros, 
     * {@link #resolveMacros(java.lang.String, int, org.opencms.file.CmsObject)} 
     * must be invoked afterwards.
     * 
     * @param toAddr the recipient address line.
     */
    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    /**
     * Gets the sender address.
     * 
     * @return the sender address.
     */
    public String getFromAddr() {
        return fromAddr;
    }

    /**
     * Sets the sender address. 
     * 
     * @param fromAddr the sender address.
     */
    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    /**
     * Gets the sender name.
     * 
     * @return the sender name.
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * Sets the sender name.
     * 
     * @param fromName the sender name.
     */
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    /**
     * Gets the subject line.
     * 
     * @return the subject line.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject line.
     * <p>
     * If the subject line includes any macros, 
     * {@link #resolveMacros(java.lang.String, int, org.opencms.file.CmsObject)} 
     * must be invoked afterwards.
     * 
     * @param subject the subject line.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the message body.
     * 
     * @return the message body.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message body.
     * <p>
     * If the message body includes any macros, 
     * {@link #resolveMacros(java.lang.String, int, org.opencms.file.CmsObject)} 
     * must be invoked afterwards.
     * 
     * @param message the message body.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Resolves any macros present in the currently set recipient address line, 
     * subject line and message body.
     * 
     * @param formPath The path to the form to use when resolving the macros.
     * @param id The ID of the single form submission to use when resolving the macros.
     * @param cmso An initialized CmsObject instance.
     * @throws CmsException 
     */
    public void resolveMacros(String formPath, int id, CmsObject cmso) throws CmsException {
        Form f = new Form(formPath, cmso);
        try { toAddr = f.resolveMacros(toAddr, id, cmso); } catch (Exception e) { }
        try { subject = f.resolveMacros(subject, id, cmso); } catch (Exception e) { }
        try { message = f.resolveMacros(message, id, cmso); } catch (Exception e) { }
    }
    
    /**
     * Constructs an email object, ready to send.
     * <p>
     * This is typically the last method one would call, after the necessary 
     * email fields have been set and macros resolved.
     * 
     * @return An email object, ready to send.
     * @throws EmailException If anything goes wrong.
     */    
    public CmsSimpleMail getSimpleEmail() throws EmailException {
        CmsSimpleMail mail = new CmsSimpleMail();
        mail.setCharset("utf-8");
        
        String[] recipients = toAddr.split(";");
        for (int i = 0; i < recipients.length; i++) {
            mail.addTo(recipients[i]);
        }
        mail.setFrom(fromAddr, fromName, "utf-8");
        mail.setSubject(subject);
        mail.setMsg(message);
        
        return mail;
    }
    
    /**
     * Sends the email.
     * <p>
     * Requires SMTP, and that authentication details are defined in opencms-system.xml.
     * <p>
     * ToDo: Add getSimpleEmail().send() as fallback.
     */
    public synchronized void send() {
        CmsMailSettings mailSettings = OpenCms.getSystemInfo().getMailSettings();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailSettings.getDefaultMailHost().getHostname());
        props.put("mail.smtp.port", String.valueOf(mailSettings.getDefaultMailHost().getPort()));

        Session mailSession = Session.getInstance( props, new AutoMailAuthenticator() );

        try {
            Message msg = new MimeMessage(mailSession);

            msg.setFrom(
                    new InternetAddress(mailSettings.getMailFromDefault())
            );
            msg.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toAddr.replace(";", ","))
            );
            msg.setSubject(subject);
            msg.setText(message);

            mailSession.getTransport("smtp").send(msg);
      } catch (Exception e) {
          if (LOG.isErrorEnabled()) {
              LOG.error("Unable to send automatic email.", e);
          }
      }
    }
}
