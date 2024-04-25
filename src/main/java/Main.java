import utils.ConnectConfig;
import utils.DatabaseConnector;

import java.net.InetSocketAddress;
import java.net.URI;
//import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.List;

import com.google.gson.Gson;
import com.google.protobuf.Api;
import com.google.protobuf.ApiOrBuilder;
import com.mysql.cj.util.DnsSrv.SrvRecord;
import com.sun.net.httpserver.*;

import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.BorrowHistories;
import queries.CardList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */
            
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/card", new CardHandler());
            server.createContext("/borrow", new BorrowHandler());
            server.createContext("/book", new BookHandler());
            //server.createContext("/upload", new UploadHandler());
            server.start();
            System.out.println("Server is listening on port 8000");
            // release database connection handler
            if (connector.release()) {
                log.info("Success to release connection.");
            } else {
                log.warning("Failed to release connection.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


       static class CardHandler implements HttpHandler {
        // 关键重写handle方法
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            // 注意判断要用equals方法而不是==啊，java的小坑（
            if (requestMethod.equals("GET")) {
                // 处理GET
                handleGetRequest(exchange);
            } else if (requestMethod.equals("POST")) {
                // 处理POST
                handlePostRequest(exchange);
            } else if (requestMethod.equals("OPTIONS")) {
                // 处理OPTIONS
                handleOptionsRequest(exchange);
            } else {
                // 其他请求返回405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // 响应头，因为是JSON通信
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            // 状态码为200，也就是status ok
            exchange.sendResponseHeaders(200, 0);
            // 获取输出流，java用流对象来进行io操作
            OutputStream outputStream = exchange.getResponseBody();
            // 构建JSON响应数据，这里简化为字符串
            // 连接数据库
            try{
                ConnectConfig conf = new ConnectConfig();
                //log.info("Success to parse connect config. " + conf.toString());
                // connect to database
                DatabaseConnector connector = new DatabaseConnector(conf);
                boolean connStatus = connector.connect();
                if (!connStatus) {
                    log.severe("Failed to connect database.");
                    System.exit(1);
                }
                LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);

                
                ApiResult cardApiResult = library.showCards();
                CardList cardList = ((CardList)cardApiResult.payload);
                String response = "[";
                for(Card card: cardList.getCards()){
                    response += "{\"id\": " + card.getCardId() + ", \"name\": \"" + card.getName() + "\", \"department\": \"" + card.getDepartment() + "\", \"type\": \"" + card.getType() + "\"},";
                }
                response = response.substring(0, response.length() - 1);
                response += "]";
                outputStream.write(response.getBytes());
                outputStream.close();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // 读取POST请求体
            InputStream requestBody = exchange.getRequestBody();
            // 用这个请求体（输入流）构造个buffered reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            // 拼字符串的
            StringBuilder requestBodyBuilder = new StringBuilder();
            // 用来读的
            String line;
            // 没读完，一直读，拼到string builder里
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }

            // 看看读到了啥
            // 实际处理可能会更复杂点
            //System.out.println("Received POST request" + requestBodyBuilder.toString());
            String requestBodyString = requestBodyBuilder.toString();
            Gson gson = new Gson();
            Map<String, Object> requestBodyMap = gson.fromJson(requestBodyString, Map.class);
            String action = (String) requestBodyMap.get("action");

            try{
                ConnectConfig conf = new ConnectConfig();
                //log.info("Success to parse connect config. " + conf.toString());
                // connect to database
                DatabaseConnector connector = new DatabaseConnector(conf);
                boolean connStatus = connector.connect();
                if (!connStatus) {
                    log.severe("Failed to connect database.");
                    System.exit(1);
                }
                LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);

                if("newcard".equals(action)){
                    Card card = new Card();
                    card.setName((String) requestBodyMap.get("name"));
                    card.setDepartment((String) requestBodyMap.get("department"));
                    //System.out.println((String) requestBodyMap.get("type"));
                    String typeString = "Student".equals((String) requestBodyMap.get("type"))?"S":"T";
                    card.setType(Card.CardType.values(typeString));
                    ApiResult result = library.registerCard(card);

                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("modifycard".equals(action)){
                    Card card = new Card();
                    Double id = (double) requestBodyMap.get("id");
                    card.setCardId(id.intValue());
                    card.setName((String) requestBodyMap.get("name"));
                    card.setDepartment((String) requestBodyMap.get("department"));
                    String typeString = "Student".equals((String) requestBodyMap.get("type"))?"S":"T";
                    card.setType(Card.CardType.values(typeString));
                    ApiResult result = library.modifyCardInfo(card);
                    //System.out.println(result.message);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }else if("deletecard".equals(action)){

                    Double id = (double) requestBodyMap.get("id");
                    ApiResult result = library.removeCard(id.intValue());

                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    // exchange.sendResponseHeaders(200, 0);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            

            // 响应头
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            // 响应状态码200
            exchange.sendResponseHeaders(200, 0);

            // 剩下三个和GET一样
            OutputStream outputStream = exchange.getResponseBody();
            
            outputStream.write("Card created successfully".getBytes());
            outputStream.close();
        }

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
            exchange.sendResponseHeaders(204, -1);
        }
    }

    static class BorrowHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            // 注意判断要用equals方法而不是==啊，java的小坑（
            if (requestMethod.equals("GET")) {
                // 处理GET
                handleGetRequest(exchange);
            } else if (requestMethod.equals("POST")) {
                // 处理POST
                //handlePostRequest(exchange);
            } else if (requestMethod.equals("OPTIONS")) {
                // 处理OPTIONS
                handleOptionsRequest(exchange);
            } else {
                // 其他请求返回405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }
        private String time2String(long timestamp){
            if(timestamp == 0){
                return "未归还";
            }
            Instant instant = Instant.ofEpochSecond(timestamp);
            ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = dateTime.format(formatter);
            return formattedDateTime;
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // 响应头，因为是JSON通信
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            // 状态码为200，也就是status ok
            exchange.sendResponseHeaders(200, 0);
            // 获取输出流，java用流对象来进行io操作
            OutputStream outputStream = exchange.getResponseBody();
            URI requeryUri = exchange.getRequestURI();
            String query = requeryUri.getQuery();
            int id;
            id = Integer.parseInt(query.substring(7));

            try{
                //连接数据库
                ConnectConfig conf = new ConnectConfig();
                DatabaseConnector connector = new DatabaseConnector(conf);
                boolean connStatus = connector.connect();
                if (!connStatus) {
                    log.severe("Failed to connect database.");
                    System.exit(1);
                }
                LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);
                //查询借阅历史
                ApiResult borrowApiResult = library.showBorrowHistory(id);
                //System.out.println(borrowApiResult.message);
                BorrowHistories borrowHistories = ((BorrowHistories)borrowApiResult.payload);
                //写入输出流
                String response = "[";
                for(BorrowHistories.Item item: borrowHistories.getItems()){
                    String formattedBorrowTime = time2String(item.getBorrowTime());
                    String formattedReturnTime = time2String(item.getReturnTime());
                    response += "{\"cardID\": " + item.getCardId() + ", \"bookID\": " + item.getBookId() + ", \"borrowTime\":\"" + formattedBorrowTime + "\", \"returnTime\":\"" + formattedReturnTime + "\"},";
                }
                response = response.substring(0, response.length() - 1);
                response += "]";
                outputStream.write(response.getBytes());
                outputStream.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
            exchange.sendResponseHeaders(204, -1);
        }

    }

    static class BookHandler implements HttpHandler {
        // 关键重写handle方法
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            //System.out.println(requestMethod);
            // 注意判断要用equals方法而不是==啊，java的小坑（
            if (requestMethod.equals("GET")) {
                // 处理GET
                handleGetRequest(exchange);
            } else if (requestMethod.equals("POST")) {
                // 处理POST
                handlePostRequest(exchange);
            } else if (requestMethod.equals("OPTIONS")) {
                // 处理OPTIONS
                handleOptionsRequest(exchange);
            } else {
                // 其他请求返回405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }



        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // 响应头，因为是JSON通信
            // System.out.println("Receive GET!");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            // 状态码为200，也就是status ok
            exchange.sendResponseHeaders(200, 0);
            // 获取输出流，java用流对象来进行io操作
            OutputStream outputStream = exchange.getResponseBody();
            // 构建JSON响应数据，这里简化为字符串
            // 连接数据库
            try{
                ConnectConfig conf = new ConnectConfig();
                DatabaseConnector connector = new DatabaseConnector(conf);
                boolean connStatus = connector.connect();
                if (!connStatus) {
                    log.severe("Failed to connect database.");
                    System.exit(1);
                }
                LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);
                BookQueryConditions conditions = new BookQueryConditions();                               
                ApiResult bookApiResult = library.queryBook(conditions);
                BookQueryResults results = ((BookQueryResults)bookApiResult.payload);
                List<Book> books = results.getResults();
                String response = "[";
                for(Book book: books){//传回去的时候字典都是小写的
                    response += "{\"id\": " + book.getBookId() + ", \"category\": \"" + book.getCategory() + "\", \"title\": \"" + book.getTitle() + "\", \"press\": \"" + book.getPress() + "\", \"publishyear\": " + book.getPublishYear() + ", \"author\": \"" + book.getAuthor() + "\", \"price\": " + book.getPrice() + ", \"stock\": " + book.getStock() + "},";
                }
                if(books.size() > 0){
                    response = response.substring(0, response.length() - 1);
                }
                response += "]";
               // System.out.println(response);
                outputStream.write(response.getBytes());
                outputStream.close(); 
                //System.out.println("Send response!");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException{
            // 读取POST请求体
            InputStream requestBody = exchange.getRequestBody();
            // 用这个请求体（输入流）构造个buffered reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            // 拼字符串的
            StringBuilder requestBodyBuilder = new StringBuilder();
            // 用来读的
            String line;
            // 没读完，一直读，拼到string builder里
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
        
            // 看看读到了啥
            // 实际处理可能会更复杂点
            // System.out.println("Received POST request" + requestBodyBuilder.toString());
            String requestBodyString = requestBodyBuilder.toString();
            Gson gson = new Gson();
            Map<String, Object> requestBodyMap = gson.fromJson(requestBodyString, Map.class);
            String action = (String) requestBodyMap.get("action");
        
            try{
                ConnectConfig conf = new ConnectConfig();
                DatabaseConnector connector = new DatabaseConnector(conf);
                boolean connStatus = connector.connect();
                if (!connStatus) {
                    log.severe("Failed to connect database.");
                    System.exit(1);
                }
                LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);
        
                if("newbook".equals(action)){
                    Book book = new Book();
                    book.setCategory((String) requestBodyMap.get("Category"));
                    book.setTitle((String) requestBodyMap.get("Title"));
                    book.setPress((String) requestBodyMap.get("Press"));
                    book.setPublishYear(Integer.parseInt((String) requestBodyMap.get("Publishyear")));
                    book.setAuthor((String) requestBodyMap.get("Author"));
                    book.setPrice(Double.parseDouble((String) requestBodyMap.get("Price")));
                    book.setStock(Integer.parseInt((String) requestBodyMap.get("Number")));
                    
                    ApiResult result = library.storeBook(book);
        
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("modify".equals(action)){
                    Book book = new Book();
                    book.setBookId(Integer.parseInt((String) requestBodyMap.get("id")));
                    book.setCategory((String) requestBodyMap.get("Category"));
                    book.setTitle((String) requestBodyMap.get("Title"));
                    book.setPress((String) requestBodyMap.get("Press"));
                    book.setPublishYear(Integer.parseInt((String) requestBodyMap.get("Publishyear")));
                    book.setAuthor((String) requestBodyMap.get("Author"));
                    book.setPrice(Double.parseDouble((String) requestBodyMap.get("Price")));
                    book.setStock(Integer.parseInt((String) requestBodyMap.get("Number")));
                    ApiResult result = library.modifyBookInfo(book);
                    System.out.println(result.message);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("querybook".equals(action)){
                    BookQueryConditions conditions = new BookQueryConditions();
                    System.out.println("Query book!");
                    System.out.println((String) requestBodyMap.get("Category"));
                    conditions.setCategory((String) requestBodyMap.get("Category"));
                    conditions.setTitle((String) requestBodyMap.get("Title"));
                    conditions.setPress((String) requestBodyMap.get("Press"));
                    conditions.setAuthor((String) requestBodyMap.get("Author"));
                    //conditions.setPublishYear(() requestBodyMap.get("Publishyear"));

                    ApiResult bookApiResult = library.queryBook(conditions);
                    BookQueryResults results = ((BookQueryResults)bookApiResult.payload);
                    List<Book> books = results.getResults();
                    String response = "[";
                    for(Book book: books){
                        response += "{\"id\": " + book.getBookId() + ", \"category\": \"" + book.getCategory() + "\", \"title\": \"" + book.getTitle() + "\", \"press\": \"" + book.getPress() + "\", \"publishyear\": " + book.getPublishYear() + ", \"author\": \"" + book.getAuthor() + "\", \"price\": " + book.getPrice() + ", \"stock\": " + book.getStock() + "},";
                    }
                    if(books.size() > 0){
                        response = response.substring(0, response.length() - 1);
                    }
                    response += "]";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, 0);
                    System.out.println(response);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(response.getBytes());
                    outputStream.close();
                }
                else if("removebook".equals(action)){
                    int id = ((Double) requestBodyMap.get("id")).intValue();
                    ApiResult result = library.removeBook(id);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("modifybook".equals(action)){
                    Book book = new Book();
                    book.setBookId(((Double) requestBodyMap.get("id")).intValue());
                    book.setCategory((String) requestBodyMap.get("Category"));
                    book.setTitle((String) requestBodyMap.get("Title"));
                    book.setPress((String) requestBodyMap.get("Press"));
                    book.setPublishYear(((Double) requestBodyMap.get("Publishyear")).intValue());
                    book.setAuthor((String) requestBodyMap.get("Author"));
                    book.setPrice((Double) requestBodyMap.get("Price"));
                    ApiResult result = library.modifyBookInfo(book);
                    System.out.println(result.message);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("incstock".equals(action)){
                    int id = ((Double) requestBodyMap.get("id")).intValue();
                    int num = ((Double) requestBodyMap.get("deltastock")).intValue();
                    ApiResult result = library.incBookStock(id, num);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("borrowbook".equals(action)){
                    Borrow borrow = new Borrow();
                    borrow.setBookId(((Double) requestBodyMap.get("id")).intValue());
                    borrow.setCardId(((Double) requestBodyMap.get("cardid")).intValue());
                    borrow.setBorrowTime(((Double) requestBodyMap.get("time")).longValue()/1000);
                    ApiResult result = library.borrowBook(borrow);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
                else if("returnbook".equals(action)){
                    Borrow borrow = new Borrow();
                    borrow.setBookId(((Double) requestBodyMap.get("id")).intValue());
                    borrow.setCardId(((Double) requestBodyMap.get("cardid")).intValue());
                    borrow.setReturnTime(((Double) requestBodyMap.get("time")).longValue()/1000);
                    ApiResult result = library.returnBook(borrow);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    if(result.ok == false){
                        exchange.sendResponseHeaders(400, 0);
                    }else{
                        exchange.sendResponseHeaders(200, 0);
                    }
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(result.message.getBytes());
                    outputStream.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
            exchange.sendResponseHeaders(204, -1);
        }

        public static Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length != 2) {
                        continue;
                    }
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params;
        }

    }
}

