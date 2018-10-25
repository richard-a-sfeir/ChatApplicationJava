import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ChatServer {

	JTextArea clientsMessage;
	JTextArea userList;
	ArrayList<PrintWriter> clientOutputStreams;
	JButton sendButton;
	JTextField smsToAll;
	ArrayList<String> uname = new ArrayList<String>();
	ArrayList<Socket> port = new ArrayList<Socket>();

	public class ClientHandler implements Runnable {

		BufferedReader reader;
		Socket sock;

		public ClientHandler(Socket clientSocket) {

			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void run() {
			String message;
			String[] parts = null;
			boolean once = false;

			try {
				while ((message = reader.readLine()) != null) {
					if (!once) {
						port.add(sock);
						parts = message.split("\\:");
						uname.add(parts[0]);
						once = true;
					}
					clientsMessage.append(message + "\n");
					tellEveryone(message);
					showingClients();
				}
				uname.remove(parts[0]);
				port.remove(sock);
				showingClients();
				tellEveryone(parts[0].toUpperCase() + "(" + sock + ")" + "has left the conversation\n");
				clientsMessage.append(parts[0].toUpperCase() + "(" + sock + ")" + "has left the conversation\n");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	public class SendButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clientsMessage.append("SERVER : " + smsToAll.getText() + "\n");
			tellEveryone("SERVER : " + smsToAll.getText());

			smsToAll.setText("");
			smsToAll.requestFocus();

		}

	}

	public void showingClients() {
		String st = "000..,";
		String t = "";
		for (String s : uname) {
			st += s + ",";
			t += s + "\n";
		}

		System.out.println(st);

		Iterator<PrintWriter> it = clientOutputStreams.iterator();
		userList.setText(t);
		while (it.hasNext()) {
			try {
				PrintWriter writer = it.next();
				writer.println(st);
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public void go() {
		clientOutputStreams = new ArrayList<PrintWriter>();
		try (ServerSocket serverSock = new ServerSocket(6666)) {

			while (true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);

				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				System.out.println("got a connection from " + clientSocket);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void tellEveryone(String message) {
		Iterator<PrintWriter> it = clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				PrintWriter writer = it.next();
				writer.println(message);
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void addAction() {
		JFrame window = new JFrame("SERVER");
		JPanel content = new JPanel();
		clientsMessage = new JTextArea(15, 50);
		clientsMessage.setLineWrap(true);
		clientsMessage.setWrapStyleWord(true);
		clientsMessage.setEditable(false);
		JScrollPane qScroller = new JScrollPane(clientsMessage);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		userList = new JTextArea(15, 10);
		userList.setLineWrap(true);
		userList.setWrapStyleWord(true);
		userList.setEditable(false);
		JScrollPane uScroller = new JScrollPane(userList);
		uScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		smsToAll = new JTextField(20);
		sendButton = new JButton("Send To All");
		sendButton.addActionListener(new SendButtonListener());
		content.add(qScroller);
		content.add(uScroller);
		content.add(smsToAll);
		content.add(sendButton);
		window.getContentPane().add(BorderLayout.CENTER, content);
		window.setSize(800, 400);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		clientsMessage.setText("");

	}

	public static void main(String[] args) {
		ChatServer cs = new ChatServer();
		cs.addAction();
		cs.go();

	}

}
