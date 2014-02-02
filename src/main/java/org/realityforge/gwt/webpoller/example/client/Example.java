package org.realityforge.gwt.webpoller.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.WebPoller.RequestFactory;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.gwt.webpoller.client.event.StartEvent;
import org.realityforge.gwt.webpoller.client.event.StopEvent;

public final class Example
  implements EntryPoint, RequestFactory
{
  private HTML _messages;
  private ScrollPanel _scrollPanel;
  private Button _stop;
  private Button _start;
  private CheckBox _longPoll;

  public void onModuleLoad()
  {
    final WebPoller webPoller = WebPoller.newWebPoller( this );
    registerListeners( webPoller );

    _start = new Button( "Start", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        _start.setEnabled( false );
        _longPoll.setEnabled( false );
        webPoller.setLongPoll( _longPoll.getValue() == Boolean.TRUE );
        webPoller.start();
      }
    } );
    _stop = new Button( "Stop", new ClickHandler()
    {
      @Override
      public void onClick( ClickEvent event )
      {
        webPoller.stop();
        _stop.setEnabled( false );
      }
    } );
    _stop.setEnabled( false );

    _longPoll = new CheckBox( "Long Poll" );

    _messages = new HTML();
    _scrollPanel = new ScrollPanel();
    _scrollPanel.setHeight( "250px" );
    _scrollPanel.add( _messages );
    RootPanel.get().add( _scrollPanel );

    {
      final FlowPanel controls = new FlowPanel();
      controls.add( _longPoll );
      controls.add( _start );
      controls.add( _stop );
      RootPanel.get().add( controls );
    }
  }

  @Nonnull
  @Override
  public RequestBuilder getRequestBuilder( @Nonnull final RequestCallback callback )
  {
    final RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, getPollURL() );
    rb.setCallback( new RequestCallback()
    {
      @Override
      public void onResponseReceived( final Request request, final Response response )
      {
        if ( Response.SC_OK == response.getStatusCode() )
        {
          callback.onResponseReceived( request, response );
        }
        else
        {
          callback.onError( request, new Exception( "Bad response code" ) );
        }
      }

      @Override
      public void onError( final Request request, final Throwable exception )
      {
        callback.onError( request, exception );
      }
    } );
    return rb;
  }

  private String getPollURL()
  {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String suffix = _longPoll.getValue() == Boolean.TRUE ? "/long" : "";
    return moduleBaseURL.substring( 0, moduleBaseURL.length() - moduleName.length() - 1 ) + "api/time" + suffix;
  }

  private void registerListeners( final WebPoller webPoller )
  {
    webPoller.addStartHandler( new StartEvent.Handler()
    {
      @Override
      public void onStartEvent( @Nonnull final StartEvent event )
      {
        appendText( "start", "silver" );
        _stop.setEnabled( true );
      }
    } );
    webPoller.addStopHandler( new StopEvent.Handler()
    {
      @Override
      public void onStopEvent( @Nonnull final StopEvent event )
      {
        appendText( "stop", "silver" );
        _start.setEnabled( true );
        _longPoll.setEnabled( true );
        _stop.setEnabled( false );
      }
    } );
    webPoller.addErrorHandler( new ErrorEvent.Handler()
    {
      @Override
      public void onErrorEvent( @Nonnull final ErrorEvent event )
      {
        appendText( "error", "red" );
        _start.setEnabled( false );
        _longPoll.setEnabled( false );
        _stop.setEnabled( false );
      }
    } );
    webPoller.addMessageHandler( new MessageEvent.Handler()
    {
      @Override
      public void onMessageEvent( @Nonnull final MessageEvent event )
      {
        appendText( "message: " + event.getData(), "black" );
      }
    } );
  }

  private void appendText( final String text, final String color )
  {
    final DivElement div = Document.get().createDivElement();
    div.setInnerText( text );
    div.setAttribute( "style", "color:" + color );
    _messages.getElement().appendChild( div );
    _scrollPanel.scrollToBottom();
  }
}
