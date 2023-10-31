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
package org.dkpro.jwpl.revisionmachine.index.indices;

/**
 * This class represents the data used by the ChronoIndex.
 */
public class ChronoIndexData implements Comparable<ChronoIndexData> {

  /**
   * Flag - whether the data should be sorted chronological or in order of the
   * revision counter
   */
  private boolean chronoSort;

  /**
   * Index value (Chronological order position)
   */
  private int index;

  /**
   * Revision counter
   */
  private final int revisionCounter;

  /**
   * Timestamp value
   */
  private final long time;

  /**
   * Creates a new ChronoInfo object.
   *
   * @param time            Timestamp value
   * @param revisionCounter RevisionCounter
   */
  public ChronoIndexData(final long time, final int revisionCounter) {
    this.time = time;
    this.revisionCounter = revisionCounter;
    this.chronoSort = true;
  }

  /**
   * Compares this ChronoInfo to the given info.
   *
   * @return a negative integer, zero, or a positive integer as this object is
   * less than, equal to, or greater than the specified object.
   */
  @Override
  public int compareTo(final ChronoIndexData info) {

    long value;

    if (chronoSort) {
      value = this.time - info.time;
    } else {
      value = this.revisionCounter - info.revisionCounter;
    }

    if (value == 0) {
      return 0;
    } else if (value > 0) {
      return 1;
    } else {
      return -1;
    }
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return (this != (ChronoIndexData) obj) ? false : true;
  }

  /**
   * Returns the index value.
   *
   * @return index value
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Returns the revision counter.
   *
   * @return revision counter
   */
  public int getRevisionCounter() {
    return revisionCounter;
  }

  /**
   * Returns the timestamp value.
   *
   * @return timestamp value
   */
  public long getTime() {
    return time;
  }

  /**
   * Sets the index value.
   *
   * @param index index value
   */
  public void setIndex(final int index) {
    this.index = index;
  }

  /**
   * Sets the sort flag.
   *
   * @param chronoSort TRUE for chronological sorting, FALSE for revision counter
   *                   sorting
   */
  public void setSortFlag(final boolean chronoSort) {
    this.chronoSort = chronoSort;
  }
}
