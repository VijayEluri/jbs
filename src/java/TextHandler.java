/*
 * Copyright 2010 Internet Archive
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;


public class TextHandler implements FieldHandler
{
  public static final int MAX_LENGTH = 100000;

  private String name;
  private String key;
  private int maxLength;

  public TextHandler( String name )
  {
    this( name, name, MAX_LENGTH );
  }

  public TextHandler( String name, int maxLength )
  {
    this( name, name, maxLength );
  }

  public TextHandler( String name, String key )
  {
    this( name, key, MAX_LENGTH );
  }

  public TextHandler( String name, String key, int maxLength )
  {
    this.name = name;
    this.key  = key;
    this.maxLength = maxLength;
  }

  public int getMaxLength( )
  {
    return this.maxLength;
  }

  public void setMaxLength( int maxLength )
  {
    this.maxLength = maxLength;
  }
  
  public void handle( Document doc, DocumentProperties properties )
  {
    // Special handling for content field.
    String text = properties.get( this.key ).trim( );

    if ( text.length() < 1 )
      {
        return ;
      }

    if ( text.length( ) > this.maxLength )
      {
        // Find the last ' ' before the maxLength.
        // FIXME: Maybe we should look for other types of whitespace as well?
        int lastSpace = text.lastIndexOf( ' ', this.maxLength );

        if ( lastSpace > -1 )
          {
            text = text.substring( 0, lastSpace );
          }
        else
          {
            // FIXME: If the text is one giant token and is longer
            //        than the maximum length, do we chop it or not
            //        index it all?  For now, not indexing at all.
            return ;
          }
      }
    
    doc.add( new Field( name, text, Field.Store.NO, Field.Index.ANALYZED ) );
    
    byte[] compressed = CompressionTools.compressString( text );
    
    // Store the shorter of the two.
    if ( compressed.length < text.length() )
      {
        doc.add( new Field( name, compressed, Field.Store.YES ) );    
      }
    else
      {
        doc.add( new Field( name, text, Field.Store.YES, Field.Index.NO ) );
      }
  }

}
