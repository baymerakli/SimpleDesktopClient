package com.ozg.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ozg.client.model.User;
import com.ozg.client.ui.UserTableModel;
import com.ozg.ws.UserService;
import com.ozg.ws.UserServiceImplService;

public class Service {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JButton addUserButton = getAddUserButton();
    private JButton getUsersButton = getGetUsersButton();
    private JTable userTable = getUserTable(300, 150);
    private JFrame frame = new JFrame("Json Objects JTable Demo");
    private JTextField firstName = new JTextField(20);
    private JTextField lastName = new JTextField(20);
	private String successMessage = "User successfully saved."; 
	private String errorMessage = "User couldn't saved, please try again later.";

    public Service() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        JLabel firstNameLabel = new JLabel("First Name:");
        JLabel lastNameLabel = new JLabel("Last Name:");

        jPanel.add(firstNameLabel);
        jPanel.add(firstName);
        jPanel.add(lastNameLabel);
        jPanel.add(lastName);
        jPanel.add(addUserButton);
        jPanel.add(getUsersButton);
        frame.add(jPanel, BorderLayout.PAGE_START);
        frame.add(new JScrollPane(userTable), BorderLayout.PAGE_END);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private JTable getUserTable(final int width, final int height) {
        UserTableModel model = new UserTableModel();
        JTable table = new JTable(model) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(width, height);
            }
        };
        return table;
    }

    private JButton getAddUserButton() {
        JButton button = new JButton("Add User");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		User user = new User();
        		user.setFirstName(firstName.getText());
        		addUser(user);
        		firstName.selectAll();
            }
        });
        return button;
    }
    
	public void addUser(User user) {
		UserServiceImplService userServiceImpl = new UserServiceImplService();
		UserService userServiceInterface = userServiceImpl.getUserServiceImplPort();
		User newUser = new User();
		newUser.setFirstName(firstName.getText());
		newUser.setLastName(lastName.getText());
		String json = "";
		try {
			json = objectMapper.writeValueAsString(newUser);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		boolean result = userServiceInterface.addUser(json);
		if(result){
			JOptionPane.showMessageDialog(null, successMessage);			
		} else {
			JOptionPane.showMessageDialog(null, errorMessage);
		}
		firstName.setText("");
		lastName.setText("");
	}
	
    public List<User> getUsersFromServer() {
		UserServiceImplService userServiceImpl = new UserServiceImplService();
		UserService userServiceInterface = userServiceImpl.getUserServiceImplPort();
		String usersJson =userServiceInterface.getUsers();
		List<User> users = new ArrayList<User>();
		try {
			users = objectMapper.readValue(
					usersJson,
		            objectMapper.getTypeFactory().constructCollectionType(
		                    List.class, User.class));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return users;

	}
    
    private JButton getGetUsersButton() {
        JButton button = new JButton("Get Users");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<User> users = getUsersFromServer();
                if (!users.isEmpty()) {
                	 ((UserTableModel) userTable.getModel()).clear();
                	 for (User user : users) {
                		 ((UserTableModel) userTable.getModel()).addUser(user);
					}
                }
            }
        });
        return button;
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Service();
            }
        });
    }
}