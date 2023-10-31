/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.revisionmachine.common.exceptions;

/**
 * DecodingException Describes an exception that occurred while encoding the
 * diff information.
 */
@SuppressWarnings("serial")
public class EncodingException extends Exception {

  /**
   * Creates a new EncodingException.
   *
   * @param description message
   */
  public EncodingException(final String description) {
    super(description);
  }

  /**
   * Creates a new EncodingException.
   *
   * @param e inner exception
   */
  public EncodingException(final Exception e) {
    super(e);
  }

  /**
   * Creates a new EncodingException.
   *
   * @param description message
   * @param e           inner exception
   */
  public EncodingException(final String description, final Exception e) {
    super(description, e);
  }
}
