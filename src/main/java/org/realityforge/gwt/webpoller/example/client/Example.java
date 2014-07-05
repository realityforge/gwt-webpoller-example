package org.realityforge.gwt.webpoller.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.realityforge.gwt.webpoller.client.AbstractHttpRequestFactory;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.WebPollerListenerAdapter;

public final class Example
  extends AbstractHttpRequestFactory
  implements EntryPoint
{
  private HTML _messages;
  private ScrollPanel _scrollPanel;
  private Button _stop;
  private Button _start;
  private CheckBox _longPoll;
  private CheckBox _longErrorBackOff;

  public void onModuleLoad()
  {
    final WebPoller webPoller = WebPoller.newWebPoller();
    webPoller.setRequestFactory( this );
    registerListeners( webPoller );

    _start = new Button( "Start", new ClickHandler()
    {
      @Override
      public void onClick( final ClickEvent event )
      {
        _start.setEnabled( false );
        _longPoll.setEnabled( false );
        _longErrorBackOff.setEnabled( false );
        webPoller.setLogLevel( Level.INFO );
        webPoller.setInterRequestDuration( _longPoll.getValue() == Boolean.TRUE ? 0 : 2000 );
        webPoller.setInterErrorDuration( _longErrorBackOff.getValue() == Boolean.TRUE ? 5000 : 0 );
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
    _longErrorBackOff = new CheckBox( "Error Backoff" );

    _messages = new HTML();
    _scrollPanel = new ScrollPanel();
    _scrollPanel.setHeight( "250px" );
    _scrollPanel.add( _messages );
    RootPanel.get().add( _scrollPanel );

    {
      final FlowPanel controls = new FlowPanel();
      controls.add( _longPoll );
      controls.add( _longErrorBackOff );
      controls.add( _start );
      controls.add( _stop );
      RootPanel.get().add( controls );
    }
  }

  @Override
  protected RequestBuilder getRequestBuilder()
  {
    return new RequestBuilder( RequestBuilder.GET, getPollURL() );
  }

  private String getPollURL()
  {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String suffix = null != _longPoll && _longPoll.getValue() == Boolean.TRUE ? "/long" : "";
    return moduleBaseURL.substring( 0, moduleBaseURL.length() - moduleName.length() - 1 ) + "api/time" + suffix;
  }

  private void registerListeners( final WebPoller webPoller )
  {
    webPoller.setListener( new WebPollerListenerAdapter()
    {
      @Override
      public void onStart( @Nonnull final WebPoller webPoller )
      {
        appendText( "start", "silver" );
        _stop.setEnabled( true );
      }

      @Override
      public void onStop( @Nonnull final WebPoller webPoller )
      {
        appendText( "stop", "silver" );
        _start.setEnabled( true );
        _longPoll.setEnabled( true );
        _longErrorBackOff.setEnabled( true );
        _stop.setEnabled( false );
      }

      @Override
      public void onMessage( @Nonnull final WebPoller webPoller,
                             @Nonnull final Map<String, String> context,
                             @Nonnull final String data )
      {
        appendText( "message: " + data, "black" );
      }

      @Override
      public void onError( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
      {
        appendText( "error", "red" );
        _longPoll.setEnabled( false );
        _longErrorBackOff.setEnabled( false );
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
