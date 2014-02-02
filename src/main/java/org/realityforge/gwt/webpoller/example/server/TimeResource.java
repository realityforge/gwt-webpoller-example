package org.realityforge.gwt.webpoller.example.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Path("/time")
@Singleton
public class TimeResource
{
  private final ScheduledExecutorService _scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  private final LinkedBlockingQueue<String> _queue = new LinkedBlockingQueue<>();
  private final Collection<AsyncResponse> _waiters = new ConcurrentLinkedQueue<>();

  @PostConstruct
  public void postConstruct()
  {
    _scheduledExecutor.scheduleWithFixedDelay( new TimeGenerator( _queue, _waiters ), 0, 1, TimeUnit.SECONDS );
  }

  @GET
  @Produces("text/plain")
  public String getMessages()
  {
    final String message = _queue.poll();
    return null != message ? message : "";
  }

  @Path("long")
  @GET
  @Produces( "text/plain" )
  public void hangUp( @Suspended AsyncResponse response )
  {
    _waiters.add( response );
  }
}
