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
package de.tudarmstadt.ukp.wikipedia.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class UnmodifiableArraySet<E>
	implements Set<E>
{
	private Object[] data;

	public UnmodifiableArraySet(E[] aData)
	{
		data = new Object[aData.length];
		System.arraycopy(aData, 0, data, 0, data.length);
	}

	public UnmodifiableArraySet(Set<E> aData)
	{
		data = new Object[aData.size()];
		System.arraycopy(aData.toArray(), 0, data, 0, data.length);
	}

	@Override
	public int size()
	{
		return data != null ? data.length : 0;
	}

	@Override
	public boolean isEmpty()
	{
		return data != null ? data.length > 0 : true;
	}

	@Override
	public boolean contains(Object aO)
	{
		if (data == null) {
			return false;
		}
		for (Object d : data) {
			if (d.equals(aO)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator()
	{
		return (Iterator<E>) Arrays.asList(data).iterator();
	}

	@Override
	public Object[] toArray()
	{
		return data;
	}

	@Override
	public <T> T[] toArray(T[] aA)
	{
		if (aA.length != data.length) {
			throw new IllegalArgumentException("Target array too small");
		}
		System.arraycopy(data, 0, aA, 0, aA.length);
		return aA;
	}

	@Override
	public boolean add(E aE)
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}

	@Override
	public boolean remove(Object aO)
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}

	@Override
	public boolean containsAll(Collection<?> aC)
	{
		for (Object o : aC) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> aC)
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}

	@Override
	public boolean retainAll(Collection<?> aC)
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}

	@Override
	public boolean removeAll(Collection<?> aC)
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Unmodifiable set");
	}
}
