package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

public class ProductModel {

	private static final String TABLE_NAME = "Prodotto";

	public synchronized void doSave(ProductBean product) throws SQLException {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		String insertSQL = "INSERT INTO Prodotto (nome, descrizione, prezzo, speseSpedizione, emailVenditore, tag, nomeTipologia, model, dataAnnuncio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, current_date())";

		try {
			connection = DriverManagerConnectionPool.getConnection();
			preparedStatement = connection.prepareStatement(insertSQL);
			preparedStatement.setString(1, product.getNome());
			preparedStatement.setString(2, product.getDescrizione());
			preparedStatement.setDouble(3, product.getPrezzo());
			preparedStatement.setDouble(4, product.getSpedizione());
			preparedStatement.setString(5, product.getEmail());
			preparedStatement.setString(6, product.getTag());
			preparedStatement.setString(7, product.getTipologia());
			preparedStatement.setString(8, product.getImmagine());

			preparedStatement.executeUpdate();

			connection.commit();
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} finally {
				if (connection != null)
					DriverManagerConnectionPool.releaseConnection(connection);
			}
		}
	}

	public synchronized ProductBean doRetrieveByKey(int code) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;		

		ProductBean bean = new ProductBean();

		String selectSQL = "SELECT * FROM " + ProductModel.TABLE_NAME + " WHERE codice = ? AND deleted = false";

		try {
			connection = DriverManagerConnectionPool.getConnection();
			preparedStatement = connection.prepareStatement(selectSQL);
			preparedStatement.setInt(1, code);

			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				bean.setCodice(rs.getInt("codice"));
				bean.setNome(rs.getString("nome"));
				bean.setDescrizione(rs.getString("descrizione"));
				bean.setPrezzo(rs.getDouble("prezzo"));
				bean.setSpedizione(rs.getDouble("speseSpedizione"));
				bean.setEmail(rs.getString("emailVenditore"));
				bean.setTag(rs.getString("tag"));
				bean.setTipologia(rs.getString("nomeTipologia"));
				bean.setData(rs.getDate("dataAnnuncio"));
				bean.setImmagine(rs.getString("model"));
			}
			
			

		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} finally {
				if (connection != null) {
					DriverManagerConnectionPool.releaseConnection(connection);
				}
			}
		}
		return bean;
	}

	public synchronized boolean doDelete(int code) throws SQLException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		int result = 0;

		String deleteSQL = "UPDATE " + ProductModel.TABLE_NAME + "SET deleted = false WHERE codice = ?";

		try {
			connection = DriverManagerConnectionPool.getConnection();
			preparedStatement = connection.prepareStatement(deleteSQL);
			preparedStatement.setInt(1, code);

			result = preparedStatement.executeUpdate();
			connection.commit();

		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} finally {
				DriverManagerConnectionPool.releaseConnection(connection);
			}
		}
		return (result != 0);
	}
	
	public synchronized Collection<ProductBean> doRetrieveAll(String where) throws SQLException {
		Connection connection = null;
		Connection connection2 = null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;

		Collection<ProductBean> products = new LinkedList<ProductBean>();

		String selectSQL = "SELECT * FROM " + ProductModel.TABLE_NAME + " WHERE deleted = 'false' AND nomeTipologia = ? ";
		String sql2 = "SELECT AVG(votazione) FROM Recensione WHERE codiceProdotto = ?";
		
		try {
			connection = DriverManagerConnectionPool.getConnection();
			preparedStatement = connection.prepareStatement(selectSQL);
			preparedStatement.setString(1, where);

			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				ProductBean bean = new ProductBean();
				
				int codiceProdotto = rs.getInt("codice");
				bean.setCodice(codiceProdotto);
				bean.setNome(rs.getString("nome"));
				bean.setDescrizione(rs.getString("descrizione"));
				bean.setPrezzo(rs.getDouble("prezzo"));
				bean.setSpedizione(rs.getDouble("speseSpedizione"));
				bean.setEmail(rs.getString("emailVenditore"));
				bean.setTag(rs.getString("tag"));
				bean.setTipologia(rs.getString("nomeTipologia"));
				bean.setData(rs.getDate("dataAnnuncio"));
				bean.setImmagine(rs.getString("model"));
				
				connection2 = DriverManagerConnectionPool.getConnection();
				preparedStatement2 = connection2.prepareStatement(sql2);
				preparedStatement2.setInt(1, codiceProdotto);
				ResultSet rs2 = preparedStatement2.executeQuery();
				if (rs2.next()) {
					bean.setVotazione(rs2.getDouble(1));
				}
				
				products.add(bean);
			}

		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} finally {
				if (connection2 != null) {
					DriverManagerConnectionPool.releaseConnection(connection2);
				}
				if (connection != null) {
					DriverManagerConnectionPool.releaseConnection(connection);
				}
			}
		}
		return products;
	}
	
	
	public synchronized Collection<ProductBean> deleteProduct(int codiceProdotto, Collection<ProductBean> lista) {
		String sql = "UPDATE Prodotto SET deleted = ? WHERE codice = ?";
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = DriverManagerConnectionPool.getConnection();
			ps = con.prepareStatement(sql);
			ps.setBoolean(1, true);
			ps.setInt(2, codiceProdotto);
			
			ps.executeUpdate();
			con.commit();
			
			if (lista.size() > 0) {
				lista.removeIf(a -> a.getCodice() == codiceProdotto);
			}
			
			return lista;
		}
		catch (Exception e) {
			return lista;
		}
		finally {
			if (con != null) {
				DriverManagerConnectionPool.releaseConnection(con);
			}
		}
	}
	
	public synchronized void updateProduct(ProductBean bean) {
		String sql = "UPDATE Prodotto SET nome = ?, descrizione = ?, prezzo = ?, speseSpedizione = ?, tag = ?, nomeTipologia = ? WHERE codice = ?";
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = DriverManagerConnectionPool.getConnection();
			ps = con.prepareStatement(sql);
			
			ps.setString(1, bean.getNome());
			ps.setString(2, bean.getDescrizione());
			ps.setDouble(3, bean.getPrezzo());
			ps.setDouble(4, bean.getSpedizione());
			ps.setString(5, bean.getTag());
			ps.setString(6, bean.getTipologia());
			ps.setInt(7, bean.getCodice());
			
			ps.executeUpdate();
			con.commit();
		}
		catch (Exception e) {
			
		}
		finally {
			if (con != null) {
				DriverManagerConnectionPool.releaseConnection(con);
			}
		}
	}
	
	public synchronized RecensioneBean getRecensione(int codiceProdotto, String email) {
		String sql2 = "SELECT votazione, testo FROM Recensione WHERE codiceProdotto = ? AND emailCliente = ?";
		Connection con = null;
		PreparedStatement ps = null;
		RecensioneBean bean = new RecensioneBean();
		
		try {
			con = DriverManagerConnectionPool.getConnection();
			ps = con.prepareStatement(sql2);
				
			ps.setInt(1, codiceProdotto);
			ps.setString(2, email);
				
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				bean.setVoto(rs.getInt(1));
				bean.setTesto(rs.getString(2));
			}
				return bean;
			}
		catch(Exception e) {
			return null;
		}
		finally {
			if (con != null) {
				DriverManagerConnectionPool.releaseConnection(con);
			}
		}
	}
	
	public synchronized String getRandomCode() {
		String sql2 = "SELECT nome, nomeTipologia FROM Prodotto ORDER BY RAND() LIMIT 1";
		Connection con = null;
		PreparedStatement ps = null;
		String riprova = "Errore: riprova";
		String nome = null;
		String tipologia = null;
		
		try {
			con = DriverManagerConnectionPool.getConnection();
			ps = con.prepareStatement(sql2);
				
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				nome = rs.getString(1);
				tipologia = rs.getString(2);
			}
				String msg = "Dai un'occhiata a " + nome + " in " + tipologia;
				return msg;
			}
		catch(Exception e) {
			return riprova;
		}
		finally {
			if (con != null) {
				DriverManagerConnectionPool.releaseConnection(con);
			}
		}
	}
}
