package bookStore;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.bookStore.model.Book;
import org.junit.Test;

public class TestEndpoint {

	Logger log = Logger.getLogger(TestEndpoint.class.getSimpleName());
	
	@Test
	public void testAsync() throws InterruptedException, ExecutionException {
		Client client = ClientBuilder.newBuilder().build();
	    WebTarget webTarget = client.target("http://localhost:8080/bookStore/rest/books/async");
	    
	    Invocation.Builder request = webTarget.request();
	    AsyncInvoker asyncInvoker = request.async();
	    Future<Response> futureResp = asyncInvoker.get();
	    
	    log.info("Do something while server process async request ...");
	    
	    Response response = futureResp.get(); //blocks until client responds or times out
	    
	    String responseBody = response.readEntity(String.class);
	    log.info("Received: "+responseBody);
	}
	
	@Test
	public void testListAll() throws InterruptedException, ExecutionException {
		Client client = ClientBuilder.newBuilder().build();
	    WebTarget webTarget = client.target("http://localhost:8080/bookStore/rest/books");
	    
	    Invocation.Builder request = webTarget.request();
	    Response response = request.get();
	}
	
	@Test
	public void testListAllWithAuthors() throws InterruptedException, ExecutionException {
		Client client = ClientBuilder.newBuilder().build();
	    WebTarget webTarget = client.target("http://localhost:8080/bookStore/rest/books/authors");
	    
	    Invocation.Builder request = webTarget.request();
	    Response response = request.get();
	}
}
