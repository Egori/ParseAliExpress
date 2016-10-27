package productDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeaturesDAO
 {
   private final Connection con;
 
   public FeaturesDAO( Connection con )
   {
     this.con = con;
   }
 
   //получение из строки ResultSet
   private Page getPageFromRS(ResultSet rs) throws SQLException
   {
     Page result = new Page();
 
     result.setId( rs.getInt( "id" ) );
     result.setPid( rs.getInt( "pid" ) );
     result.setCaption(rs.getString( "key" ) );
 
     return result;
   }
 
   //получение Page по id
   public Page getFeaturesByID( int id )
   {
     Page result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement( "SELECT * FROM site_subtags WHERE id=?" );
       ps.setInt( 1, id );
 
       ResultSet rs = ps.executeQuery();
       if( rs.next() )
       {
         result = getPageFromRS( rs );
       }
       ps.close();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
 
     return result;
   }
   
   //получение Page по id
   public Page getFeaturesByKey( String key )
   {
     Page result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement( "SELECT * FROM site_subtags WHERE key=?" );
       ps.setString(1, key );
 
       ResultSet rs = ps.executeQuery();
       if( rs.next() )
       {
         result = getPageFromRS( rs );
       }
       ps.close();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
 
     return result;
   }
    
   //создание/обновление страницы в базе
   public void updatePage( Features features )
   {
     try
     {
       PreparedStatement ps;
       //если страница не новая
       if ( features.getId() > 0 )
       {
         ps = con.prepareStatement( "UPDATE site_subtags SET `pid`=?, `key`=?, `value`=? WHERE `id`=?");
         ps.setInt( 8, features.getId() );
       }
       else 
       {
         ps = con.prepareStatement( "INSERT INTO site_subtags (`pid`, `key`, `value`) VALUES (?,?,?)" );
       }
 
       ps.setInt(1, features.getPid());
       ps.setString(2, features.getKey());
       ps.setString(3, features.getValue());
      
       ps.executeUpdate();
       ps.close();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
   }
 
   //удаление
   public void deletePage( int id )
   {
     try
     {
       PreparedStatement ps = con.prepareStatement( "DELETE FROM site_subtags WHERE id=?" );
       ps.setInt( 1, id );
       ps.executeUpdate();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
   }
 }