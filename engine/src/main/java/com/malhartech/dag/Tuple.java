/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.malhartech.dag;

import com.malhartech.bufferserver.Buffer.Data.DataType;

/**
 *
 * Basic object that is streamed<p>
 * <br>
 * Tuples are of the following type<br>
 * Data:<br>
 * Control: begin window, end window, reset window, end stream<br>
 * heartbeat: To be done, not a high priority<br>
 * <br>
 * @author chetan
 */
public class Tuple
{
  protected long windowId;
  private DataType type;
  private StreamContext context;
  private final Object object;

  public Tuple(Object object)
  {
    this.object = object;
  }

  /**
   * @return the context
   */
  public StreamContext getContext()
  {
    return context;
  }

  /**
   * @param context the context to set
   */
  public void setContext(StreamContext context)
  {
    this.context = context;
  }

  public Object getObject()
  {
    return object;
  }

  /**
   * @return the windowId
   */
  public long getWindowId()
  {
    return windowId;
  }

  /**
   * @param windowId the windowId to set
   */
  public void setWindowId(long windowId)
  {
    this.windowId = windowId;
  }

  /**
   * @return the type
   */
  public DataType getType()
  {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(DataType type)
  {
    this.type = type;
  }

  @Override
  public String toString()
  {
    return "type = " + type + " window = " + Long.toHexString(windowId);
  }
}
