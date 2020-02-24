package informes;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class Frame extends JFrame {

	//Atributos
	/**
	 * Panel por defecto
	 */
	private JPanel contentPane;

	
	/**
	 * panel container CardLayout
	 */
	JPanel cards;
	/**
	 * Paneles contenidos en Card
	 */
	JPanel panelCliente;
	JPanel panelFactura;
	JPanel panelGenerarFacturas;
	
	private JScrollPane scrollPane;
	/**
	 * Tabla que muestra los datos de los clientes
	 */
	private JTable table;
	/**
	 * conexión a la base de datos 
	 */
	private Connection con;
	private Statement stmt;
	private ResultSet rs;
	private JTextField textField;
	/**
	 * tabla que muestra las facturas
	 */
	private JTable table_1;
	/**
	 * añade parámetros a las querys
	 */
	private PreparedStatement preparedStatement;
	/**
	 * aquí se introduce el número de la factura a fenerar
	 */
	private JTextField textField_1;
/**
 * @author Elena de Lope
 * @version 1.0 24/02/2020
 * @param args
 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame frame = new Frame();
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
	public Frame() {
		
		/**
		 * settings del panel por defecto
		 */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/**
		 * menú con opciones de visualizar clientes, facturas y generar factura
		 */
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(10, 0, 151, 32);
		contentPane.add(menuBar);

		JMenu mnMenu = new JMenu("menu");
		menuBar.add(mnMenu);

		/**
		 * opción de menú cliente
		 */
		JMenuItem clientes = new JMenuItem("clientes");
		clientes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visualizarClientes();
			}
		});
		mnMenu.add(clientes);

		/**
		 * opción de menú facturas
		 */
		JMenuItem facturas = new JMenuItem("facturas");
		facturas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visualizarFacturas();
			}
		});
		mnMenu.add(facturas);

		/**
		 * opción de menú generar factura
		 */
		JMenuItem generarFacturas = new JMenuItem("generar facturas");
		generarFacturas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				visualizarGenerarFacturas();
			}
		});
		mnMenu.add(generarFacturas);

		/**
		 * panel contenedor
		 */
		cards = new JPanel();
		cards.setBounds(20, 43, 404, 202);
		contentPane.add(cards);
		cards.setLayout(new CardLayout(0, 0));

		/**
		 * panel contenido en Cards
		 */
		panelCliente = new JPanel();
		cards.add(panelCliente, "clientes");
		panelCliente.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		panelCliente.add(scrollPane, BorderLayout.CENTER);

		/**
		 * tabla donde se visualizan los clientes de la bbdd
		 */
		table = new JTable();
		scrollPane.setViewportView(table);
		try {
			con = Conexion.getConnection();
			if (con == null)
				System.out.println("fail to connect");
			stmt = con.createStatement();

			String sql = "SELECT * FROM CUSTOMER";

			rs = stmt.executeQuery(sql);

			String[] tableColumnsName = { "ID", "FIRSTNAME", "LASTNAME", "STREET", "CITY" };
			DefaultTableModel aModel = (DefaultTableModel) table.getModel();
			aModel.setColumnIdentifiers(tableColumnsName);

			java.sql.ResultSetMetaData rsmd = rs.getMetaData();
			int colNo = rsmd.getColumnCount();
			while (rs.next()) {
				Object[] objects = new Object[colNo];
				for (int i = 0; i < colNo; i++) {
					objects[i] = rs.getObject(i + 1);
				}
				aModel.addRow(objects);
			}
			table.setModel(aModel);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		panelFactura = new JPanel();
		cards.add(panelFactura, "factura");
		panelFactura.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panelFactura.add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel = new JLabel("id cliente");
		panel.add(lblNewLabel);
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		/**
		 * botón que genera la factura en base al número de id de la misma
		 */
		JButton btnNewButton = new JButton("generar");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				limpiarTabla();
				
				try {
					con = Conexion.getConnection();
					if (con == null)
						System.out.println("fail to connect2");
					String sql = "SELECT * FROM INVOICE WHERE CUSTOMERID=?";
					preparedStatement = con.prepareStatement(sql);
					preparedStatement.setString(1, textField.getText().toString());
				
					rs = preparedStatement.executeQuery();

					String[] tableColumnsName = { "ID", "CUSTOMERID", "TOTAL" };
					DefaultTableModel aModel = (DefaultTableModel) table_1.getModel();
					aModel.setColumnIdentifiers(tableColumnsName);

					java.sql.ResultSetMetaData rsmd = rs.getMetaData();
					int colNo = rsmd.getColumnCount();
					while (rs.next()) {
						Object[] objects = new Object[colNo];
						for (int i = 0; i < colNo; i++) {
							objects[i] = rs.getObject(i + 1);
						}
						aModel.addRow(objects);
					}
					table_1.setModel(aModel);

				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
		});
		panel.add(btnNewButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panelFactura.add(scrollPane_1, BorderLayout.CENTER);
		
		/**
		 * tabla donde se visualizan las facturas de la bbdd
		 */
		table_1 = new JTable();
		scrollPane_1.setViewportView(table_1);


		
		panelGenerarFacturas = new JPanel();
		cards.add(panelGenerarFacturas, "generarFactura");
		panelGenerarFacturas.setLayout(null);
		
		JButton btnGenerarFactura = new JButton("generar factura");
		btnGenerarFactura.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/**
				 * File chooser para poner la ruta de dónde se guardan los archivos generados
				 */
				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

				int returnValue = jfc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					System.out.println(selectedFile.getAbsolutePath());
				}


			}
		});
		btnGenerarFactura.setBounds(118, 65, 140, 46);
		panelGenerarFacturas.add(btnGenerarFactura);
		
		JLabel lblNFactura = new JLabel("n\u00BA factura");
		lblNFactura.setBounds(49, 23, 96, 17);
		panelGenerarFacturas.add(lblNFactura);
		
		textField_1 = new JTextField();
		textField_1.setBounds(172, 20, 86, 20);
		panelGenerarFacturas.add(textField_1);
		textField_1.setColumns(10);

	}
	
	/**
	 * método protected sin parámetros 
	 * genera facturas
	 */
	protected void generarFactura() {
        // TODO Auto-generated method stub
        JFileChooser fc = new JFileChooser();

        int seleccion = fc.showSaveDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File fichero = fc.getSelectedFile();
            try {
                con = Conexion.getConnection();
                Map<String, Object> parametros = new HashMap<String, Object>();
                parametros.put("id", Integer.valueOf(textField.getText()));

                JasperPrint print = JasperFillManager.fillReport("informes/proveedores.jasper", parametros, con);
                JasperViewer.viewReport(print);
                String ruta = fichero.toString();
                JasperExportManager.exportReportToPdfFile(print, ruta);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "save fail");
                e.printStackTrace();
            }

        }
    }
	/**
	 * método público para no recordar los valores anteriormente introducidos
	 */
	public void limpiarTabla() {
        DefaultTableModel tb = (DefaultTableModel) table.getModel();
        int a = table.getRowCount() - 1;
        for (int i = a; i >= 0; i--) {
            tb.removeRow(tb.getRowCount() - 1);
        }
    }

	public void itemStateChanged(String evt) {
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, evt);
	}

	public void visualizarClientes() {
		itemStateChanged("clientes");
	}

	public void visualizarFacturas() {
		itemStateChanged("facturas");

	}

	public void visualizarGenerarFacturas() {
		itemStateChanged("Generarfacturas");

	}
}
