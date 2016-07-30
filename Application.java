package hackMail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class Application extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	JTextArea header;

	JTextArea text_email_title;
	JTextArea email_title;

	JTextArea text_email_file;
	JButton choose_file_email;

	JTextArea text_recipients;
	JButton choose_file_recipients;

	JButton confirm;

	JTextArea console;

	Sender sender;

	public Application() {
		// layout stuff
		setTitle("HackMail");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// header
		header = new JTextArea("                    HackMail for HackUC 2016", 1, 10);

		header.setEditable(false);
		header.setOpaque(false);
		header.setMargin(new Insets(5, 5, 5, 5));

		// email title
		text_email_title = new JTextArea("Email Title");

		text_email_title.setEditable(false);
		text_email_title.setOpaque(false);
		text_email_title.setMargin(new Insets(5, 5, 5, 5));

		email_title = new JTextArea("HackUC + &COMP = <3");

		email_title.setMargin(new Insets(5, 5, 5, 5));
		email_title.setColumns(18);

		JPanel ptitle = new JPanel(new BorderLayout());
		ptitle.add(text_email_title, BorderLayout.WEST);
		ptitle.add(email_title, BorderLayout.EAST);

		// email file
		text_email_file = new JTextArea("No HTML email file selected");

		text_email_file.setEditable(false);
		text_email_file.setOpaque(false);
		text_email_file.setMargin(new Insets(5, 5, 5, 5));

		choose_file_email = new JButton("...");
		choose_file_email.addActionListener(this);

		JPanel pfile = new JPanel(new BorderLayout());
		pfile.add(text_email_file, BorderLayout.WEST);
		pfile.add(choose_file_email, BorderLayout.EAST);

		// recipients
		text_recipients = new JTextArea("Found 0 Recipients", 1, 20);
		text_recipients.setEditable(false);
		text_recipients.setOpaque(false);
		text_recipients.setMargin(new Insets(5, 5, 5, 5));

		choose_file_recipients = new JButton("...");
		choose_file_recipients.addActionListener(this);

		JPanel precipients = new JPanel(new BorderLayout());
		precipients.add(text_recipients, BorderLayout.WEST);
		precipients.add(choose_file_recipients, BorderLayout.EAST);

		// send button
		confirm = new JButton("Send Emails");
		confirm.addActionListener(this);
		confirm.setPreferredSize(new Dimension(100, 26));

		JPanel psend = new JPanel(new BorderLayout());
		psend.add(confirm, BorderLayout.CENTER);

		// console
		console = new JTextArea();

		console.setEditable(false);
		console.setRows(5);

		JScrollPane pconsole = new JScrollPane(console);
		pconsole.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// put panels in vertical series
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(header);
		panel.add(ptitle);
		panel.add(pfile);
		panel.add(precipients);
		panel.add(psend);
		panel.add(pconsole);

		add(panel);

		pack();
		
		// read the login info from a file
		String address = "";
		String password = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("zoho_login.txt"));		
			String line;
			for(int i = 0; (line = br.readLine()) != null; i++){
				if(i == 0)		address = line;
				else if (i== 1) password = line;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// create a new sender
		sender = new Sender(console);

		sender.setSender(address, address, password);
		sender.createSession("smtp.zoho.com", "465", true);

		openRecipientList(new File("recipients.txt"));
	}

	public void openRecipientList(File file) {
		String line = "";

		try {
			// open the file
			BufferedReader reader = new BufferedReader(new FileReader(file));

			while ((line = reader.readLine()) != null) {
				// read each line and split it up into an array, comma-separated
				ArrayList<String> splitline = new ArrayList<String>(Arrays.asList(line.split(",")));

				// if everything went right, 0=company, 1=full name, 2=email
				String company = splitline.get(0);
				String firstname = splitline.get(1).split(" ")[0];
				String address = splitline.get(2);

				sender.addRecipient(address, company, firstname);
			}

			reader.close();

			text_recipients.setText("Found " + sender.recipientCount() + " Recipients");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String openEmail(String filename) {
		String file = "";
		String line = "";

		try {
			// open the file
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			// cram it all into one string
			while ((line = reader.readLine()) != null) {
				file += line;
			}

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return file;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Application app = new Application();
				app.setLocationRelativeTo(null);
				app.setVisible(true);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == confirm) {
			// make sure there is an actual file to send
			if (text_email_file.getText().equals("No HTML email file selected")) {
				JOptionPane.showMessageDialog(this, "Please select a valid HTML file to send.");
				return;
			}

			// open a final warning box
			if (JOptionPane.showConfirmDialog(
					this, "Are you sure you want to send \"" + email_title.getText() + "\" to "
							+ sender.recipientCount() + " recipients?",
					"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0) {
				
				sender.setMailTitle(email_title.getText());
				sender.sendMail();
			}

		} else if (e.getSource() == choose_file_recipients) {
			// open a file chooser
			JFileChooser fc = new JFileChooser();

			if (fc.showOpenDialog(Application.this) == JFileChooser.APPROVE_OPTION) {
				// clear previous recipients and process new list
				sender.clearRecipients();
				openRecipientList(fc.getSelectedFile());
			}

		} else if (e.getSource() == choose_file_email) {
			// open a file chooser
			JFileChooser fc = new JFileChooser();

			if (fc.showOpenDialog(Application.this) == JFileChooser.APPROVE_OPTION) {
				// set new file contents
				String file_content = openEmail(fc.getSelectedFile().getPath());
				text_email_file.setText(fc.getSelectedFile().getName());

				sender.setMailContent(file_content);
			}
		}
	}
}
