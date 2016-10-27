package productDAO;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PageDAO
 {
   private final Connection con;
 
   public PageDAO( Connection con )
   {
     this.con = con;
   }
 
   //получение из строки ResultSet
   private Page getPageFromRS(ResultSet rs) throws SQLException
   {
     Page result = new Page();
 
     result.setId( rs.getInt( "id" ) );
     result.setPid( rs.getInt( "pid" ) );
     result.setCaption(rs.getString( "caption" ) );
 
     return result;
   }
 
   //получение Page по id
   public Page getPage( int id )
   {
     Page result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement( "SELECT * FROM site_pages WHERE id=?" );
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
   public Page getPageByText( String text )
   {
     Page result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement( "SELECT * FROM site_pages WHERE text=?" );
       ps.setString(1, text );
 
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
   
     public Page getPageByCaption( String caption )
   {
     Page result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement( "SELECT * FROM site_pages WHERE caption=?" );
       ps.setString(1, caption );
 
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
 
   //получение списка всего Page
   public List<Page> getPageList()
   {
     List<Page> result = new ArrayList<Page>();
     try
     {
       ResultSet rs = con.createStatement().executeQuery( "SELECT * FROM site_pages" );
       while( rs.next() )
       {
         result.add( getPageFromRS( rs ) );
       }
       rs.close();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
 
     return result;
   }
 
   //создание/обновление страницы в базе
   public void updatePage( Page page )
   {
     try
     {
       PreparedStatement ps;
       //если страница не новая
       if ( page.getId() > 0 )
       {
         ps = con.prepareStatement( "UPDATE site_pages SET pid=?, url=?, caption=?, text=?, keywords=?, description=?, tags=? WHERE id=?");
         ps.setInt( 8, page.getId() );
       }
       else 
       {
         ps = con.prepareStatement( "INSERT INTO site_pages (pid, url, caption, text, keywords, description, tags, date) VALUES (?,?,?,?,?,?,?,CURDATE())" );
       }
 
       ps.setInt(1, page.getPid());
       ps.setString(2, page.getUrl());
       ps.setString(3, page.getCaption());
       ps.setString(4, page.getText());
       ps.setString(5, page.getKeywords());
       ps.setString(6, page.getDescription());
       ps.setString(7, page.getTags());
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
       PreparedStatement ps = con.prepareStatement( "DELETE FROM site_pages WHERE id=?" );
       ps.setInt( 1, id );
       ps.executeUpdate();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
   }
   
   public String getLastProductCaption(){
       String result = null;
     try
     {
       PreparedStatement ps = con.prepareStatement("SELECT * FROM site_pages WHERE `id`=(SELECT MAX(`id`) FROM site_pages)");
 
       ResultSet rs = ps.executeQuery();
       if( rs.next() )
       {
         result = rs.getString("text");
       }
       ps.close();
     }
     catch( SQLException e )
     { 
       e.printStackTrace(); 
     }
 
     return result;
   }
   
 }