import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ChatClient {

	JTextArea incoming;
	JTextField outgoing;
	JTextArea userList;
	BufferedReader reader;
	PrintWriter writer;
	Socket sock;
	String username;
	String[] ps;

	public static void main(String[] args) {
		ChatClient client = new ChatClient();
		client.initClient();
	}

	public void initClient() {

		JFrame frame = new JFrame("Client");
		JPanel mainPanel = new JPanel();
		incoming = new JTextArea(15, 50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		userList = new JTextArea(15, 10);
		userList.setLineWrap(true);
		userList.setWrapStyleWord(true);
		userList.setEditable(false);
		JScrollPane uScroller = new JScrollPane(userList);
		uScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		outgoing = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		mainPanel.add(qScroller);
		mainPanel.add(uScroller);
		mainPanel.add(outgoing);
		mainPanel.add(sendButton);
		setUpNetworking();

		username = JOptionPane.showInputDialog("Welcome ! Kindly say who you are ?");

		frame.setTitle(username.toUpperCase());

		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	private void setUpNetworking() {

		try {
			sock = new Socket("127.0.0.1", 6666);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			System.out.println("Networking established");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public class SendButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent ev) {
			try {
				writer.println(username + " : " + outgoing.getText());
				writer.flush();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			outgoing.setText("");
			outgoing.requestFocus();
		}
	}

	public class IncomingReader implements Runnable {
		public void run() {
			String message;

			try {
				while ((message = reader.readLine()) != null) {
					if (!messageOrList(message)) {
						incoming.append(message + "\n");
					} else {
						userList.setText("");
						for (int i = 1; i < ps.length; i++) {
							userList.append(ps[i] + "\n");
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public boolean messageOrList(String ms) {
			ps = ms.split("\\,");
			return ps[0].equals("000..");
		}
	}

}
