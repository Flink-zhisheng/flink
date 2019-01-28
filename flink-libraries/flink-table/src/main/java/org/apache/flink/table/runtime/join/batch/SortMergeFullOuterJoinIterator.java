/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.	See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.	You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.runtime.join.batch;

import org.apache.flink.table.codegen.Projection;
import org.apache.flink.table.dataformat.BinaryRow;
import org.apache.flink.table.runtime.sort.RecordComparator;
import org.apache.flink.table.runtime.util.ResettableExternalBuffer;
import org.apache.flink.table.typeutils.BinaryRowSerializer;
import org.apache.flink.util.MutableObjectIterator;

import java.io.Closeable;
import java.io.IOException;

/**
 * Gets two matched rows for full outer join.
 */
public class SortMergeFullOuterJoinIterator implements Closeable {

	private final Projection<BinaryRow, BinaryRow> projection1;
	private final Projection<BinaryRow, BinaryRow> projection2;
	private final RecordComparator keyComparator;
	private final MutableObjectIterator<BinaryRow> iterator1;
	private final MutableObjectIterator<BinaryRow> iterator2;

	private BinaryRow row1;
	private BinaryRow key1;
	private BinaryRow row2;
	private BinaryRow key2;

	private BinaryRow matchKey;

	private ResettableExternalBuffer buffer1;
	private ResettableExternalBuffer buffer2;

	private final int[] nullFilterKeys;
	private final boolean nullSafe;
	private final boolean filterAllNulls;

	public SortMergeFullOuterJoinIterator(
			BinaryRowSerializer serializer1, BinaryRowSerializer serializer2,
			Projection projection1, Projection projection2, RecordComparator keyComparator,
			MutableObjectIterator<BinaryRow> iterator1,
			MutableObjectIterator<BinaryRow> iterator2,
			ResettableExternalBuffer buffer1,
			ResettableExternalBuffer buffer2,
			boolean[] filterNulls) throws IOException {
		this.projection1 = projection1;
		this.projection2 = projection2;
		this.keyComparator = keyComparator;
		this.iterator1 = iterator1;
		this.iterator2 = iterator2;

		this.row1 = serializer1.createInstance();
		this.row2 = serializer2.createInstance();
		this.buffer1 = buffer1;
		this.buffer2 = buffer2;
		this.nullFilterKeys = NullAwareJoinHelper.getNullFilterKeys(filterNulls);
		this.nullSafe = nullFilterKeys.length == 0;
		this.filterAllNulls = nullFilterKeys.length == filterNulls.length;

		nextRow1();
		nextRow2();
	}

	private boolean shouldFilter(BinaryRow key) {
		return NullAwareJoinHelper.shouldFilter(nullSafe, filterAllNulls, nullFilterKeys, key);
	}

	public boolean nextOuterJoin() throws IOException {
		if (key1 != null && (shouldFilter(key1) || key2 == null)) {
			matchKey = null;
			bufferRows1();
			buffer2.reset();
			return true; // outer row1.
		} else if (key2 != null && (shouldFilter(key2) || key1 == null)) {
			matchKey = null;
			buffer1.reset();
			bufferRows2();
			return true; // outer row2.
		} else if (key1 != null && key2 != null) {
			int cmp = keyComparator.compare(key1, key2);
			if (cmp == 0) {
				matchKey = key1;
				bufferRows1();
				bufferRows2(); // match join.
			} else if (cmp > 0) {
				matchKey = null;
				buffer1.reset();
				bufferRows2(); // outer row2.
			} else {
				matchKey = null;
				buffer2.reset();
				bufferRows1(); // outer row1.
			}
			return true;
		} else {
			return false; // bye bye.
		}
	}

	/**
	 * Buffer rows from iterator1 with same key.
	 */
	private void bufferRows1() throws IOException {
		BinaryRow copy = key1.copy();
		buffer1.reset();
		do {
			buffer1.add(row1);
		} while (nextRow1() && keyComparator.compare(key1, copy) == 0);
	}

	/**
	 * Buffer rows from iterator2 with same key.
	 */
	private void bufferRows2() throws IOException {
		BinaryRow copy = key2.copy();
		buffer2.reset();
		do {
			buffer2.add(row2);
		} while (nextRow2() && keyComparator.compare(key2, copy) == 0);
	}

	private boolean nextRow1() throws IOException {
		if ((row1 = iterator1.next(row1)) != null) {
			key1 = projection1.apply(row1);
			return true;
		} else {
			row1 = null;
			key1 = null;
			return false;
		}
	}

	private boolean nextRow2() throws IOException {
		if ((row2 = iterator2.next(row2)) != null) {
			key2 = projection2.apply(row2);
			return true;
		} else {
			row2 = null;
			key2 = null;
			return false;
		}
	}

	public BinaryRow getMatchKey() {
		return matchKey;
	}

	public ResettableExternalBuffer getBuffer1() {
		return buffer1;
	}

	public ResettableExternalBuffer getBuffer2() {
		return buffer2;
	}

	@Override
	public void close() {
		buffer1.close();
		buffer2.close();
	}
}