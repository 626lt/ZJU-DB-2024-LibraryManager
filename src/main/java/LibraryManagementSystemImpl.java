import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Card.CardType;
import queries.*;
import queries.BorrowHistories.Item;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Api;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();//connect to database
        try{
            //use preparedstatement to avoid injection attack
            String query_sql = "SELECT book_id from book where category = ? and title = ? and press = ? and publish_year = ? and author = ?;";
            PreparedStatement pstmtquery = conn.prepareStatement(query_sql);
            String insert_sql = "Insert into book(category, title, press, publish_year, author, price, stock) values(?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement pstmtinsert = conn.prepareStatement(insert_sql);
            //set query condition,
            pstmtquery.setString(1, book.getCategory());
            pstmtquery.setString(2, book.getTitle());
            pstmtquery.setString(3, book.getPress());
            pstmtquery.setInt(4, book.getPublishYear());
            pstmtquery.setString(5, book.getAuthor());
            //pstmtquery.setDouble(6, book.getPrice());
            //pstmtquery.setInt(7, book.getStock());

            ResultSet rs = pstmtquery.executeQuery();

            if(rs.next()){//the book already exists
                return new ApiResult(false, "Book already exists");
            }
            //perform insert operation
            pstmtinsert.setString(1, book.getCategory());
            pstmtinsert.setString(2, book.getTitle());
            pstmtinsert.setString(3, book.getPress());
            pstmtinsert.setInt(4, book.getPublishYear());
            pstmtinsert.setString(5, book.getAuthor());
            pstmtinsert.setDouble(6, book.getPrice());
            pstmtinsert.setInt(7, book.getStock());
            pstmtinsert.executeUpdate();

            rs = pstmtquery.executeQuery();

            if(rs.next()){
                book.setBookId(rs.getInt("book_id"));
            }
            pstmtinsert.close();
            pstmtquery.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }

        return new ApiResult(true, "Store successfully");
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try{
            String sql = "SELECT stock from book where book_id = ? ;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                int stock = rs.getInt("stock");
                stock += deltaStock;
                if(stock < 0){
                    rollback(conn);
                    return new ApiResult(false, "Stock can't be negative!");
                }
                sql = "UPDATE book SET stock = ? where book_id = ? ;";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, stock);
                pstmt.setInt(2, bookId);
                pstmt.executeUpdate();
                pstmt.close();
                commit(conn);
                return new ApiResult(true, "Stock updated successfully");
            }else{
                rollback(conn);
                return new ApiResult(false, "Book not found");
            }
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try{
            String query_sql = "SELECT book_id from book where category = ? and title = ? and press = ? and publish_year = ? and author = ?;";
            PreparedStatement pstmtquery = conn.prepareStatement(query_sql);
            String insert_sql = "Insert into book(category, title, press, publish_year, author, price, stock) values(?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement pstmtinsert = conn.prepareStatement(insert_sql);
            for(Book book : books){
                pstmtquery.setString(1, book.getCategory());
                pstmtquery.setString(2, book.getTitle());
                pstmtquery.setString(3, book.getPress());
                pstmtquery.setInt(4, book.getPublishYear());
                pstmtquery.setString(5, book.getAuthor());
                //pstmtquery.setDouble(6, book.getPrice());
                //pstmtquery.setInt(7, book.getStock());
                ResultSet rs = pstmtquery.executeQuery();
                if(rs.next()){
                    rollback(conn);
                    return new ApiResult(false, "Some books have already exits");
                }
                pstmtinsert.setString(1, book.getCategory());
                pstmtinsert.setString(2, book.getTitle());
                pstmtinsert.setString(3, book.getPress());
                pstmtinsert.setInt(4, book.getPublishYear());
                pstmtinsert.setString(5, book.getAuthor());
                pstmtinsert.setDouble(6, book.getPrice());
                pstmtinsert.setInt(7, book.getStock());
                pstmtinsert.addBatch();
            }
            pstmtinsert.executeBatch();
            for(Book book: books){
                pstmtquery.setString(1, book.getCategory());
                pstmtquery.setString(2, book.getTitle());
                pstmtquery.setString(3, book.getPress());
                pstmtquery.setInt(4, book.getPublishYear());
                pstmtquery.setString(5, book.getAuthor());
                //pstmtquery.setDouble(6, book.getPrice());
                //pstmtquery.setInt(7, book.getStock());
                ResultSet rs = pstmtquery.executeQuery();
                if(rs.next()){
                    book.setBookId(rs.getInt("book_id"));
                }
            }
            pstmtinsert.close();
            pstmtquery.close();
            commit(conn);
        }
        catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Store successfully");
    }

    @Override
    public ApiResult removeBook(int bookId) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try{
            String sql = "select * from Borrow where book_id = ? and return_time = 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                rollback(conn);
                return new ApiResult(false, "Book is borrowed and not returned");
            }
            sql = "DELETE from book where book_id = ? ;";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            int res = pstmt.executeUpdate();
            if(res == 0){
                rollback(conn);
                return new ApiResult(false, "Book not found");
            }
            //pstmt.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Book removed successfully");
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try{
            String sql = "update book set category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? where book_id = ?;";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, book.getCategory());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getPress());
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getAuthor());
            pstmt.setDouble(6, book.getPrice());
            pstmt.setInt(7, book.getBookId());
            

            pstmt.executeUpdate();
            //pstmt.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "modify information successfully!");
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        List<Book> results = new ArrayList<>();
        try{
            String sql = "select * from book where category like ? and title like ? and press like ? and publish_year >= ? and publish_year <= ? and author like ? and price >= ? and price <= ?;";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, conditions.getCategory() == null ? "%" : "%" + conditions.getCategory() +"%");
            pstmt.setString(2, conditions.getTitle() == null ? "%" : "%" + conditions.getTitle() + "%");
            pstmt.setString(3, conditions.getPress() == null ? "%" : "%" + conditions.getPress() + "%");
            pstmt.setInt(4, conditions.getMinPublishYear() == null ? Integer.MIN_VALUE : conditions.getMinPublishYear());
            pstmt.setInt(5, conditions.getMaxPublishYear() == null ? Integer.MAX_VALUE : conditions.getMaxPublishYear());
            pstmt.setString(6, conditions.getAuthor() == null ? "%" : "%" + conditions.getAuthor() + "%");
            pstmt.setDouble(7, conditions.getMinPrice() == null ? Double.MIN_VALUE : conditions.getMinPrice());
            pstmt.setDouble(8, conditions.getMaxPrice() == null ? Double.MAX_VALUE : conditions.getMaxPrice());

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setCategory(rs.getString("category"));
                book.setTitle(rs.getString("title"));
                book.setPress(rs.getString("press"));
                book.setPublishYear(rs.getInt("publish_year"));
                book.setAuthor(rs.getString("author"));
                book.setPrice(rs.getDouble("price"));
                book.setStock(rs.getInt("stock"));
                results.add(book);
            }

            if(conditions.getSortOrder() ==  SortOrder.ASC){
                results.sort(conditions.getSortBy().getComparator());
            }
            else{
                results.sort(conditions.getSortBy().getComparator().reversed());
            }
            pstmt.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        BookQueryResults bookQueryResults = new BookQueryResults(results);
        return new ApiResult(true, bookQueryResults);
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try {
            String query_sql = "select * from book where book_id = ? for update;";
            PreparedStatement querlstmt = conn.prepareStatement(query_sql);
            querlstmt.setInt(1, borrow.getBookId());
            ResultSet rs = querlstmt.executeQuery();
            // check the stock
            if(rs.next()){
                if(rs.getInt("stock") < 1){
                    rollback(conn);
                    return new ApiResult(false, "The book has no stock!");
                }
            }else{
                rollback(conn);
                return new ApiResult(false, "The book doesn't exist!");
            }
            // check the borrower
            query_sql = "select * from borrow where book_id = ? and card_id = ? and return_time = 0;";
            querlstmt = conn.prepareStatement(query_sql);
            querlstmt.setInt(1, borrow.getBookId());
            querlstmt.setInt(2, borrow.getCardId());
            rs = querlstmt.executeQuery();
            if(rs.next()){
                rollback(conn);
                return new ApiResult(false, "The borrower hasn't return the book!");
            }
            // perform borrow
            String sql = "Update book set stock = stock - 1 where book_id = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getBookId());
            stmt.executeUpdate();
            sql = "Insert into borrow (card_id, book_id, borrow_time) values(?, ?, ?);";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getCardId());
            stmt.setInt(2, borrow.getBookId());
            stmt.setLong(3,borrow.getBorrowTime());
            //stmt.setLong(3,borrow.getReturnTime());
            stmt.executeUpdate();
            stmt.close();
            conn.commit();
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Borrow successfully!");
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        try{
            String sql = "select * from borrow where card_id = ? and book_id = ? and return_time = 0;";
            PreparedStatement qstmt = conn.prepareStatement(sql);
            qstmt.setInt(1, borrow.getCardId());
            qstmt.setInt(2, borrow.getBookId());
            ResultSet rs = qstmt.executeQuery();
            if(!rs.next()){
                rollback(conn);
                return new ApiResult(false, "The borrow doesn't exist!");
            }
            Long borrowTime = rs.getLong("borrow_time");
            if(borrowTime >= borrow.getReturnTime()){
                rollback(conn);
                return new ApiResult(false, "The return time is earlier than the borrow time!");
            }
            sql = "Update borrow set return_time = ? where card_id = ? and book_id = ? and borrow_time = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, borrow.getReturnTime());
            stmt.setInt(2, borrow.getCardId());
            stmt.setInt(3, borrow.getBookId());
            stmt.setLong(4, borrowTime);
            int re = stmt.executeUpdate();
            if( re == 0 ){
                rollback(conn);
                return new ApiResult(false, "There is not the borrow!");
            }
            sql = "Update book set stock = stock + 1 where book_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getBookId());
            stmt.executeUpdate();
            stmt.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "return successfully!");
    }
  
    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        List<BorrowHistories.Item> items = new ArrayList<>();
        try{
            String sql = "select * from borrow natural join book where card_id = ? order by borrow_time DESC, book_id ASC;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cardId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
            {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setCategory(rs.getString("category"));
                book.setTitle(rs.getString("title"));
                book.setPress(rs.getString("press"));
                book.setPublishYear(rs.getInt("publish_year"));
                book.setAuthor(rs.getString("author"));
                book.setPrice(rs.getDouble("price"));
                book.setStock(rs.getInt("stock"));
                Borrow borrow = new Borrow();
                borrow.setBookId(rs.getInt("book_id"));
                borrow.setCardId(rs.getInt("card_id")); 
                borrow.setBorrowTime(rs.getLong("borrow_time"));
                borrow.setReturnTime(rs.getLong("return_time"));
                Item item = new Item(cardId, book, borrow);
                items.add(item);
            }
            pstmt.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, new BorrowHistories(items));
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try{
            String sql = "select card_id from card where name = ? and department = ? and type = ?;";
            PreparedStatement pstmtquery = conn.prepareStatement(sql);
            pstmtquery.setString(1, card.getName());
            pstmtquery.setString(2, card.getDepartment());
            pstmtquery.setString(3, card.getType().getStr());
            ResultSet rs = pstmtquery.executeQuery();
            if(rs.next()){
                rollback(conn);
                return new ApiResult(false, "There exists a card!");
            }
            sql = "insert into card (name, department, type) values (?, ?, ?);";
            PreparedStatement pstmtinsert = conn.prepareStatement(sql);
            pstmtinsert = conn.prepareStatement(sql);
            pstmtinsert.setString(1, card.getName());
            pstmtinsert.setString(2, card.getDepartment());
            pstmtinsert.setString(3, card.getType().getStr());
            pstmtinsert.executeUpdate();
            rs = pstmtquery.executeQuery();
            if(rs.next())
            {
                card.setCardId(rs.getInt("card_id"));
            }
            pstmtinsert.close();
            pstmtquery.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "register successfully!");
    }

    @Override
    public ApiResult modifyCardInfo(Card card){
        Connection conn = connector.getConn();

        try{
            String sql = "Select * from card where card_id = ?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, card.getCardId());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                sql = "Update card set name = ?, department = ?, type = ? where card_id = ?;";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, card.getName());
                stmt.setString(2, card.getDepartment());
                stmt.setString(3, card.getType().getStr());
                stmt.setInt(4, card.getCardId());
                int re = stmt.executeUpdate();
                if(re == 1){
                    stmt.close();
                    commit(conn);
                    return new ApiResult(true, "Modify successfully!");
                }
                else{
                    rollback(conn);
                    return new ApiResult(false, "Modify failed!");
                }
            }else{
                rollback(conn);
                return new ApiResult(false, "There is no such card!");
            }
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        //return new ApiResult(false, "Unimplemented Function");
        Connection conn = connector.getConn();
        
        try{
            // check if exists unreturned book
            String sql = "Select * from borrow where card_id = ? and return_time = 0;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                rollback(conn);
                return new ApiResult(false, "There are unreturned books!");
            }
            // delete the card
            sql = "delete from card where card_id = ?;";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            int re = stmt.executeUpdate();
            if(re == 0){
                rollback(conn);
                return new ApiResult(false, "The card doesn't exist!");
            }
            stmt.close();
            commit(conn);   
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, "Remove successfully!");

    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        List<Card> cards = new ArrayList<>();
        try{
            String sql = "select * from card order by card_id ASC;";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while(rs.next())
            {
                Card temp = new Card();
                temp.setCardId(rs.getInt("card_id"));
                temp.setName(rs.getString("name"));
                temp.setDepartment(rs.getString("department"));
                temp.setType(Card.CardType.values(rs.getString("type")));
                cards.add(temp);
            }
            statement.close();
            commit(conn);
        }catch(Exception e){
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, new CardList(cards));
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}