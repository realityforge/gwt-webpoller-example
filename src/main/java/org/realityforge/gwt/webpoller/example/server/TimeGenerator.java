package org.realityforge.gwt.webpoller.example.server;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import javax.ws.rs.container.AsyncResponse;

final class TimeGenerator
  implements Runnable
{
  private final LinkedBlockingQueue<String> _queue;
  private final Collection<AsyncResponse> _waiters;

  TimeGenerator( final LinkedBlockingQueue<String> queue, final Collection<AsyncResponse> waiters )
  {
    _queue = queue;
    _waiters = waiters;
  }

  @Override
  public void run()
  {
    final String message = "The time is " + new Date();
    _queue.add( message );
    synchronized ( _waiters )
    {
      final Iterator<AsyncResponse> iterator = _waiters.iterator();
      while( iterator.hasNext() )
      {
        final AsyncResponse response = iterator.next();
        iterator.remove();
        if( !response.isCancelled() )
        {
          response.resume( message );
        }
      }
    }
  }
}
