package com.hehaoyisheng.mogaokuregist.action;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.hehaoyisheng.mogaokuregist.entity.Member;
import com.hehaoyisheng.mogaokuregist.utls.Data;
import com.hehaoyisheng.mogaokuregist.utls.Message;
import com.hehaoyisheng.mogaokuregist.utls.RandomIDcard;
import com.hehaoyisheng.mogaokuregist.utls.RandomName;
import com.hehaoyisheng.mogaokuregist.utls.RandomString;

public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JList<String> list;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setTitle("\u83AB\u9AD8\u7A9F\u6CE8\u518Cv1.0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 873, 559);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textField = new JTextField();
		textField.setBounds(38, 40, 470, 21);
		contentPane.add(textField);
		textField.setColumns(10);

		final JRadioButton radioButton = new JRadioButton("\u968F\u673A");
		radioButton.setBounds(38, 433, 121, 23);
		contentPane.add(radioButton);

		JButton button = new JButton("\u6D4F\u89C8");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				jfc.showDialog(new JLabel(), "选择");
				File file = jfc.getSelectedFile();
				if (file == null) {
					return;
				}
				if (file.isDirectory()) {
					System.out.println("文件夹:" + file.getAbsolutePath());
				} else if (file.isFile()) {
					System.out.println("文件:" + file.getAbsolutePath());
				}
				textField.setText(file.getAbsolutePath());
			}
		});
		button.setBounds(518, 39, 93, 23);
		contentPane.add(button);

		JButton button_1 = new JButton("\u5BFC\u51FA");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = textField.getText();
				if (path == null || path.equals("")) {
					JOptionPane.showMessageDialog(null, "错误的文件路径", "导出失败",
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				try {
					FileWriter f = new FileWriter(new File(path));
					for (Member m : Data.list) {
						f.write(m.getUsername() + "	" + m.getPassword() + "	"
								+ m.getName() + "		" + m.getIdcard() + "\r\n");
					}
					f.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, "导出成功", "导出成功",
						JOptionPane.PLAIN_MESSAGE);
			}
		});
		button_1.setBounds(719, 39, 93, 23);
		contentPane.add(button_1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(38, 104, 736, 311);
		contentPane.add(scrollPane);

		list = new JList<String>();
		scrollPane.setViewportView(list);

		JButton button_2 = new JButton("\u5F00\u59CB");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Message.login();
				if (radioButton.isSelected()) {
					final Integer count = Integer.valueOf(textField_1.getText());
					new Thread(new Runnable(){
						public void run() {
							for (int i = 0; i < count; i++) {
								final Member mem = new Member();
								mem.setName(RandomName.getName());
								mem.setPassword(RandomString.getString());
								mem.setUsername(Message.getRandPhone());
								mem.setEmail(RandomString.getString() + "@163.com");
								mem.setIdcard(RandomIDcard.generate());
								Data.list.add(mem);
								new Thread(new Runnable() {

									@Override
									public void run() {
										
										Main m = new Main();
										try {
											m.main(mem);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}).start();
							}
						}
					}).start();
					
				} else {
					new Thread(new Runnable(){
						public void run() {
							for (final Member mem : Data.list) {
								Thread t = new Thread(new Runnable() {

									@Override
									public void run() {
										Main m = new Main();
										try {
											m.main(mem);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										setList();
									}
								});
								t.start();
								while(t.isAlive()){
									
								}
							}
						}
					}).start();	
				}
			}
		});
		button_2.setBounds(651, 488, 93, 23);
		contentPane.add(button_2);

		JButton button_3 = new JButton("\u505C\u6B62");
		button_3.setBounds(754, 488, 93, 23);
		contentPane.add(button_3);

		textField_1 = new JTextField();
		textField_1.setBounds(473, 434, 66, 21);
		contentPane.add(textField_1);
		textField_1.setColumns(10);

		JLabel label = new JLabel("\u6CE8\u518C\u4E2A\u6570");
		label.setBounds(557, 437, 94, 15);
		contentPane.add(label);

		JButton btnNewButton = new JButton("\u5BFC\u5165");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = textField.getText();
				if (path == null || path.equals("")) {
					JOptionPane.showMessageDialog(null, "错误的文件路径", "导入失败",
							JOptionPane.PLAIN_MESSAGE);
					return;
				}
				try {
					BufferedReader f = new BufferedReader(new FileReader(
							new File(path)));
					String sss = null;
					while ((sss = f.readLine()) != null) {
						if (sss.equals("")) {
							continue;
						}
						String[] datas = sss.split(" ");
						Member m = new Member();
						m.setUsername(datas[0]);
						m.setPassword(datas[1]);
						m.setName(datas[2]);
						m.setIdcard(datas[3]);
						m.setEmail(datas[4]);
						Data.list.add(m);
					}
					f.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, "导入成功", "导入成功",
						JOptionPane.PLAIN_MESSAGE);
				setList();
			}
		});
		btnNewButton.setBounds(616, 39, 93, 23);
		contentPane.add(btnNewButton);
		
		JButton button_4 = new JButton("\u91CA\u653E\u624B\u673A\u53F7");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		button_4.setBounds(38, 477, 93, 23);
		contentPane.add(button_4);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setList();
			}
		}).start();
	}

	public void setList() {
		while(true){
			DefaultListModel<String> dlm = new DefaultListModel<String>();
			for (Member mem : Data.list) {
				String s = mem.getUsername() + "                  "
						+ mem.getState();
				dlm.addElement(s);
			}
			list.setModel(dlm);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
