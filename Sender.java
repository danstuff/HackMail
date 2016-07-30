package hackMail;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JTextArea;

public class Sender {
	// sender settings
	String sender_address;
	String sender_username;
	String sender_password;

	// recipient settings
	ArrayList<String> recipients;
	ArrayList<String> companies;
	ArrayList<String> firstnames;

	// email settings
	String mail_title;
	String mail_content;

	// server session
	Session session;

	// output console
	JTextArea console;

	public Sender(JTextArea console) {
		this.console = console;

		sender_address = "";
		sender_username = "";
		sender_password = "";

		mail_title = "";
		mail_content = "";

		// recipient settings
		recipients = new ArrayList<String>();
		companies = new ArrayList<String>();
		firstnames = new ArrayList<String>();
	}

	public void setSender(String address, String password) {
		sender_address = address;
		sender_username = address;
		sender_password = password;
	}

	public void setSender(String address, String username, String password) {
		sender_address = address;
		sender_username = username;
		sender_password = password;
	}

	public void setMailTitle(String title) {
		mail_title = title;
	}

	public void setMailContent(String content) {
		mail_content = content;
	}

	public void createSession(String host, String port, boolean use_ssl) {
		// begin mail server setup
		Properties props = System.getProperties();

		// set host address and port, enable authorization
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.stmp.port", port);

		props.setProperty("mail.smtp.auth", "true");

		// special settings for SSL encryption vs TLS
		if (use_ssl) {
			props.setProperty("mail.smtp.socketFactory.port", port);
			props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		} else {
			props.setProperty("mail.smtp.starttls.enable", "true");
		}

		// create a new session and authenticate it
		session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sender_username, sender_password);
			}
		});
	}

	public void cprint(String output) {
		console.setText(console.getText() + output + "\n");
	}

	public void addRecipient(String address, String company, String firstname) {
		cprint("New recipient " + address + ", " + company + ", " + firstname);

		recipients.add(address);
		companies.add(company);
		firstnames.add(firstname);
	}

	public int recipientCount() {
		return recipients.size();
	}
	
	private class Mailer extends Thread{
		@Override
		public void run() {
			// send all messages
			try {
				// create a message
				MimeMessage message = new MimeMessage(session);
	
				// set sender address
				message.setFrom(new InternetAddress(sender_address));
	
				cprint("Sending started...");
	
				// loop through all recipients and send
				for (int i = 0; i < recipients.size(); i++) {
					// replace &NAME and &COMP with the name and company of the
					// person
					String ptitle = mail_title.replaceAll("&NAME", firstnames.get(i)).replaceAll("&COMP", companies.get(i));
					String pcontent = mail_content.replaceAll("&NAME", firstnames.get(i)).replaceAll("&COMP",
							companies.get(i));
	
					// create the message content
					message.setSubject(ptitle);
					message.setContent(pcontent, "text/html");
	
					// set sender recipient
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients.get(i)));
	
					// send message
					Transport.send(message);
					cprint("Email sent to " + recipients.get(i) + ", " + companies.get(i) + ", " + firstnames.get(i));
				}
	
				cprint("Done!");
	
			} catch (MessagingException mx) {
				mx.printStackTrace();
			}
		}
	}

	public void sendMail(){
		(new Mailer()).start();
	}
	
	public void clearRecipients() {
		recipients.clear();
		companies.clear();
		firstnames.clear();
	}

}
