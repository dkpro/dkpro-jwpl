/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 ******************************************************************************/
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
