/**
 * Copyright (C) 2014 ned.twigg@diffplug.com
 * Copyright (C) 2011 K Venkata Sudhakar <kvenkatasudhakar@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.classyshark.silverghost.translator.apk.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A couple of static methods for creating tables. */
public class Table {
	/** Returns a formatted table string. */
	public static <T> String getTable(Collection<T> objects, List<Column.Data<T>> columns) {
		String[][] data = new String[objects.size()][];
		
		Iterator<T> iter = objects.iterator();
		int i = 0;
		while (i < objects.size()) {
			T object = iter.next();			
			data[i] = new String[columns.size()];
			for (int j = 0; j < columns.size(); ++j) {
				data[i][j] = columns.get(j).getter.apply(object);
			}
			++i;
		}
		
		Column[] rawColumns = columns.stream()
				.map(new Function<Column.Data<T>, Object>() {
					@Override
					public Object apply(Column.Data<T> c) {
						return c.column;
					}
				})
				.collect(Collectors.toList())
				.toArray(new Column[columns.size()]);
		return getTable(rawColumns, data);
	}
	
	/** Returns a formatted table string. */
	public static String getTable(String[] header, String[][] data) {
		Column[] headerCol = Arrays.asList(header).stream()
				.map(new Function<String, Column>() {
					@Override
					public Column apply(String s) {
						return  new Column(s);
					}
				})
				.collect(Collectors.toList())
				.toArray(new Column[header.length]);
		
		return getTable(headerCol, data);
	}
	
	/** Returns a formatted table string. */
	public static String getTable(Column[] headerObjs, String[][] data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Please provide valid data : " + data);
		}
		
		/**
		 * Table String buffer
		 */
		StringBuilder tableBuf = new StringBuilder();
		
		/**
		 * Get maximum number of columns across all rows
		 */
		String[] header = getHeaders(headerObjs);
		int colCount = getMaxColumns(header, data);

		/**
		 * Get max length of data in each column
		 */
		List<Integer> colMaxLenList = getMaxColLengths(colCount, header, data);
		
		/**
		 * Check for the existence of header
		 */
		if (header != null && header.length > 0) {
			/**
			 * 1. Row line
			 */
			tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
			
			/**
			 * 2. Header line
			 */
			tableBuf.append(getRowDataBuf(colCount, colMaxLenList, header, headerObjs, true));
		}
		
		/**
		 * 3. Data Row lines
		 */
		tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
		String[] rowData = null;
		
		//Build row data buffer by iterating through all rows
		for (int i = 0 ; i < data.length ; i++) {
			
			//Build cell data in each row
			rowData = new String [colCount];
			for (int j = 0 ; j < colCount ; j++) {
				
				if (j < data[i].length) {
					rowData[j] = data[i][j];	
				} else {
					rowData[j] = "";
				}
			}
			
			tableBuf.append(getRowDataBuf(colCount, colMaxLenList, rowData, headerObjs, false));
		}
		
		/**
		 * 4. Row line
		 */
		tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
		return tableBuf.toString();
	}
	
	private static String getRowDataBuf(int colCount, List<Integer> colMaxLenList, 
			String[] row, Column[] headerObjs, boolean isHeader) {
		
		StringBuilder rowBuilder = new StringBuilder();
		String formattedData = null;
		Column.Align align;
		
		for (int i = 0 ; i < colCount ; i ++) {
		
			align = isHeader ? Column.Align.HEADER_DEFAULT : Column.Align.DATA_DEFAULT;
			
			if (headerObjs != null && i < headerObjs.length) {
				if (isHeader) {
					align = headerObjs[i].headerAlign;
				} else {
					align = headerObjs[i].dataAlign;
				}
			}
				 
			formattedData = i < row.length ? row[i] : ""; 
			
			//format = "| %" + colFormat.get(i) + "s ";
			formattedData = "| " + 
				getFormattedData(colMaxLenList.get(i), formattedData, align) + " ";
			
			if (i+1 == colCount) {
				formattedData += "|";
			}
			
			rowBuilder.append(formattedData);
		}
		
		return rowBuilder.append("\n").toString();
	}
	
	private static String getFormattedData(int maxLength, String data, Column.Align align) {
		if (data.length() > maxLength) {
			return data;
		}
		
		boolean toggle = true;
		
		while (data.length() < maxLength) {
			if (align == Column.Align.LEFT) {
				data = data + " ";
			} else if (align == Column.Align.RIGHT) {
				data = " " + data;
			} else if (align == Column.Align.CENTER) {
				if (toggle) {
					data = " " + data;
					toggle = false;
				} else {
					data = data + " ";
					toggle = true;
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Each string item rendering requires the border and a space on both sides.
	 * 
	 * 12   3   12      3  12    34 
	 * +-----   +--------  +------+
	 *   abc      venkat     last
	 * 
	 * @param colCount
	 * @param colMaxLenList
	 * @param data
	 * @return
	 */
	private static String getRowLineBuf(int colCount, List<Integer> colMaxLenList, String[][] data) {
		
		StringBuilder rowBuilder = new StringBuilder();
		int colWidth = 0 ;
		
		for (int i = 0 ; i < colCount ; i ++) {
			
			colWidth = colMaxLenList.get(i) + 3;
			
			for (int j = 0; j < colWidth ; j ++) {
				if (j==0) {
					rowBuilder.append("+");
				} else if ((i+1 == colCount && j+1 == colWidth)) {//for last column close the border
					rowBuilder.append("-+");
				} else {
					rowBuilder.append("-");
				}
			}
		}
		
		return rowBuilder.append("\n").toString();
	}
	
	private static int getMaxItemLength(List<String> colData) {
		int maxLength = 0;
		for (int i = 0 ; i < colData.size() ; i ++) {
			maxLength = Math.max(colData.get(i).length(), maxLength);
		}
		return maxLength;
	}

	private static int getMaxColumns(String[] header, String[][] data) {
		int maxColumns = 0;
		for (int i = 0; i < data.length; i++) {
			maxColumns = Math.max(data[i].length, maxColumns);
		}
		maxColumns = Math.max(header.length, maxColumns);
		return maxColumns;
	}
	
	private static List<Integer> getMaxColLengths(int colCount, String[] header, String[][] data) {
		List<Integer> colMaxLenList = new ArrayList<Integer>(colCount);
		List<String> colData = null;
		int maxLength;
		
		for (int i = 0 ; i < colCount ; i ++) {
			colData = new ArrayList<String>();
			
			if (header != null && i < header.length) {
				colData.add(header[i]);
			}
			
			for (int j = 0 ; j < data.length; j ++) {
				if (i < data[j].length) {
					colData.add(data[j][i]);	
				} else {
					colData.add("");
				}
			}
			
			maxLength = getMaxItemLength(colData);
			colMaxLenList.add(maxLength);
		}
		
		return colMaxLenList;
	}
	
	private static String[] getHeaders(Column[] headerObjs) {
		String[] header = new String[0];
		if (headerObjs != null && headerObjs.length > 0) {
			header = new String[headerObjs.length];
			for (int i = 0 ; i < headerObjs.length ; i ++) {
				header[i] = headerObjs[i].header;
			}
		}
		
		return header;
	}

	/** Represents a column's title and alignment. */
    public static class Column {

        /** Represents a horizontal alignment. */
        public enum Align {
            LEFT, CENTER, RIGHT;

            public static final Align HEADER_DEFAULT = LEFT;
            public static final Align DATA_DEFAULT = LEFT;
        }


        public final String header;
        public final Align headerAlign;
        public final Align dataAlign;

        /** A Column with a name. */
        public Column(String headerName) {
            this(headerName, Align.HEADER_DEFAULT, Align.DATA_DEFAULT);
        }

        /** A Column with a name and alignment. */
        public Column(String header, Align headerAlign, Align dataAlign) {
            this.header = header;
            this.headerAlign = headerAlign;
            this.dataAlign = dataAlign;
        }

        /** An object which can extract data with which to populate its column. */
        public <T> Data<T> with(Function<T, String> getter) {
            return new Data<T>(this, getter);
        }

        /** Represents a Data-driven column. */
        public static class Data<T> {
            public final Column column;
            public final Function<T, String> getter;

            private Data(Column column, Function<T, String> getter) {
                this.column = column;
                this.getter = getter;
            }
        }
    }
}
