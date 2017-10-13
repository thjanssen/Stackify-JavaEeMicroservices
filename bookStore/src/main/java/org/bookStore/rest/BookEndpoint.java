package org.bookStore.rest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.bookStore.model.Book;

/**
 * 
 */
@Stateless
@Path("/books")
public class BookEndpoint {
	
	Logger log = Logger.getLogger(BookEndpoint.class.getSimpleName());
	
	@PersistenceContext(unitName = "bookStore-persistence-unit")
	private EntityManager em;

	@POST
	@Consumes("application/json")
	public Response create(Book entity) {
		em.persist(entity);
		return Response.created(
				UriBuilder.fromResource(BookEndpoint.class)
						.path(String.valueOf(entity.getId())).build()).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id) {
		Book entity = em.find(Book.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Long id) {
		TypedQuery<Book> findByIdQuery = em
				.createQuery(
						"SELECT b FROM Book b WHERE b.id = :entityId",
						Book.class);
		findByIdQuery.setParameter("entityId", id);
		Book entity;
		try {
			entity = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			entity = null;
		}
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(entity).build();
	}

	@GET
	@Produces("application/json")
	public List<Book> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		TypedQuery<Book> findAllQuery = em.createQuery(
				"SELECT b FROM Book b ORDER BY b.title", Book.class);
		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}
		final List<Book> results = findAllQuery.getResultList();

		return results;
	}
	
	@GET
	@Path("/authors")
	@Produces("application/json")
	public List<Book> listBooksWithAuthors(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		TypedQuery<Book> findAllQuery = em.createQuery(
				"SELECT b FROM Book b JOIN FETCH b.author ORDER BY b.title", Book.class);
		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}
		final List<Book> results = findAllQuery.getResultList();

		for (Book b : results) {
			log.info(b.getAuthor().getName());
		}
		
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") Long id, Book entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(entity.getId())) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (em.find(Book.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
			entity = em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}
	
	@Resource
	ManagedExecutorService exec;
	
	@GET
	@Path("/async")
	public void async(@Suspended AsyncResponse response) {
		response.setTimeout(5, TimeUnit.SECONDS);
		
		String firstThread = Thread.currentThread().getName();
		log.info("First thread: "+firstThread);
		
		exec.execute(new Runnable() {
			
			@Override
			public void run() {
				String secondThread = Thread.currentThread().getName();
				log.info("Second thread: "+secondThread);
				
				// do something useful ...
				
				// resume request and return result
				response.resume(Response.ok("Some result ...").build());
			}
		});
	}
}
